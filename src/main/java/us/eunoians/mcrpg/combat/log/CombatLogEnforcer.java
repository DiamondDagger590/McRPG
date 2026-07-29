package us.eunoians.mcrpg.combat.log;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.common.ReloadableBoolean;
import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatParticipant;
import us.eunoians.mcrpg.combat.CombatSession;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.database.table.CombatLogDAO;
import us.eunoians.mcrpg.event.combat.CombatLogPunishmentEvent;
import us.eunoians.mcrpg.event.combat.PlayerCombatLogEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Evaluates whether a player's logout qualifies as a combat log and applies the
 * configured punishments. Called from {@link us.eunoians.mcrpg.listener.entity.player.PlayerLeaveListener}
 * before the session is ended, so the session is still alive and queryable.
 * <p>
 * All configuration reads are cached via {@link ReloadableContent} / {@link ReloadableBoolean}
 * and refreshed automatically when {@code /mcrpg admin reload} calls
 * {@link com.diamonddagger590.mccore.configuration.ReloadableContentManager#reloadAllContent()}.
 */
public class CombatLogEnforcer {

    private static final String PUNISHMENT_ROUTE_PREFIX = "combat-log.punishment.";

    private final McRPG mcRPG;
    private final ReloadableContent<CombatLogMode> mode;

    /**
     * Constructs a new {@link CombatLogEnforcer}. Initializes the reloadable combat log
     * mode and walks the {@link CombatLogPunishmentTypeRegistry} to initialize each
     * registered type's enabled state from the combat configuration. Each type's
     * {@link ReloadableBoolean} is tracked with the {@link com.diamonddagger590.mccore.configuration.ReloadableContentManager}
     * for automatic refresh on {@code /mcrpg admin reload}.
     *
     * @param mcRPG The plugin instance for config access, localization, and database access.
     */
    public CombatLogEnforcer(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;

        YamlDocument config = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.COMBAT_CONFIG);

        this.mode = new ReloadableContent<>(config, CombatConfigFile.COMBAT_LOG_MODE,
                (yaml, route) -> {
                    String modeString = yaml.getString(route, "DISABLED");
                    try {
                        return CombatLogMode.valueOf(modeString.toUpperCase());
                    }
                    catch (IllegalArgumentException e) {
                        mcRPG.getLogger().warning("Unknown combat-log mode '" + modeString
                                + "' in combat_configuration.yml, defaulting to DISABLED");
                        return CombatLogMode.DISABLED;
                    }
                });

        Set<ReloadableContent<?>> reloadables = new HashSet<>();
        reloadables.add(mode);

        CombatLogPunishmentTypeRegistry punishmentRegistry = mcRPG.registryAccess()
                .registry(McRPGRegistryKey.COMBAT_LOG_PUNISHMENT_TYPE);
        for (CombatLogPunishmentType type : punishmentRegistry.getRegisteredPunishmentTypes()) {
            Route enabledRoute = Route.fromString(PUNISHMENT_ROUTE_PREFIX + type.getConfigKey());
            type.initializeEnabledState(config, enabledRoute);
            ReloadableBoolean reloadable = type.getEnabledReloadable();
            if (reloadable != null) {
                reloadables.add(reloadable);
            }
        }

        mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(ManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(reloadables);
    }

    /**
     * Gets the shared {@link ReloadableContent} for the combat log mode. Exposed so
     * that {@link us.eunoians.mcrpg.listener.combat.OnCombatExitMessageListener}
     * can read the same cached mode without duplicating the parse logic.
     *
     * @return The reloadable combat log mode.
     */
    @NotNull
    public ReloadableContent<CombatLogMode> getMode() {
        return mode;
    }

    /**
     * Evaluates whether the player's logout with the given active session constitutes
     * a combat log, and if so, fires the detection and punishment events, applies
     * surviving punishments, and records an audit trail entry.
     * <p>
     * Must be called on the main thread while the session is still active (before
     * {@code endSession}).
     *
     * @param player  The player who is logging out.
     * @param session The player's active combat session.
     */
    public void evaluateAndEnforce(@NotNull Player player, @NotNull CombatSession session) {
        CombatLogMode currentMode = mode.getContent();
        CombatType combatType = session.getCombatType();

        if (!currentMode.shouldPunish(combatType)) {
            return;
        }

        Collection<CombatParticipant> participants = session.getParticipants();

        PlayerCombatLogEvent logEvent = new PlayerCombatLogEvent(player, session, combatType, participants);
        Bukkit.getPluginManager().callEvent(logEvent);
        if (!logEvent.shouldApplyPunishment()) {
            return;
        }

        Map<CombatLogPunishmentType, Boolean> punishmentMap = buildPunishmentMap();
        CombatLogPunishmentEvent punishmentEvent =
                new CombatLogPunishmentEvent(player, session, combatType, punishmentMap);
        Bukkit.getPluginManager().callEvent(punishmentEvent);

        if (!punishmentEvent.hasAnyPunishment()) {
            return;
        }

        List<CombatLogPunishmentType> appliedPunishments = punishmentEvent.getEnabledPunishments();
        applyPunishments(player, session, punishmentEvent);
        recordAuditEntry(player, session, combatType, participants, appliedPunishments);
    }

    /**
     * Builds the initial punishment map by querying each registered
     * {@link CombatLogPunishmentType} for its enabled state.
     *
     * @return A map of punishment types to their current enabled state.
     */
    @NotNull
    private Map<CombatLogPunishmentType, Boolean> buildPunishmentMap() {
        CombatLogPunishmentTypeRegistry punishmentRegistry = mcRPG.registryAccess()
                .registry(McRPGRegistryKey.COMBAT_LOG_PUNISHMENT_TYPE);
        Map<CombatLogPunishmentType, Boolean> map = new LinkedHashMap<>();
        for (CombatLogPunishmentType type : punishmentRegistry.getRegisteredPunishmentTypes()) {
            map.put(type, type.isEnabled());
        }
        return map;
    }

    /**
     * Resolves mutual exclusions and applies surviving punishments. For each
     * enabled type (in insertion order), any types in its {@code getExcludes()}
     * set are disabled. Then each remaining enabled type's {@code apply()} is called.
     *
     * @param player          The player being punished.
     * @param session         The player's active combat session.
     * @param punishmentEvent The punishment event with the final punishment map.
     */
    private void applyPunishments(@NotNull Player player, @NotNull CombatSession session,
                                  @NotNull CombatLogPunishmentEvent punishmentEvent) {
        List<CombatLogPunishmentType> enabled = punishmentEvent.getEnabledPunishments();

        Set<NamespacedKey> excluded = new HashSet<>();
        for (CombatLogPunishmentType type : enabled) {
            excluded.addAll(type.getExcludes());
        }

        for (CombatLogPunishmentType type : enabled) {
            if (!excluded.contains(type.getKey())) {
                type.apply(player, session, mcRPG);
            }
        }
    }

    /**
     * Records the combat log incident to the audit trail asynchronously.
     *
     * @param player             The player who combat logged.
     * @param session            The player's active combat session.
     * @param combatType         The derived combat type at logout.
     * @param participants       The participant roster at logout.
     * @param appliedPunishments The punishments that were applied.
     */
    private void recordAuditEntry(@NotNull Player player, @NotNull CombatSession session,
                                  @NotNull CombatType combatType,
                                  @NotNull Collection<CombatParticipant> participants,
                                  @NotNull List<CombatLogPunishmentType> appliedPunishments) {
        Location loc = player.getLocation();
        List<UUID> participantUUIDs = participants.stream()
                .map(CombatParticipant::getUUID)
                .collect(Collectors.toList());

        CombatLogEntry entry = new CombatLogEntry(
                0,
                player.getUniqueId(),
                Instant.now(),
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                combatType,
                participantUUIDs,
                appliedPunishments
        );

        var database = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection conn = database.getConnection()) {
                BatchTransaction transaction = new BatchTransaction(conn,
                        CombatLogDAO.insertCombatLog(conn, entry));
                transaction.executeTransaction();
            }
            catch (Exception e) {
                mcRPG.getLogger().log(Level.WARNING,
                        "Failed to record combat log entry for " + player.getName(), e);
            }
        });
    }
}
