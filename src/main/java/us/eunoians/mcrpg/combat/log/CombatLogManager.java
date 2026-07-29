package us.eunoians.mcrpg.combat.log;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import dev.dejvokep.boostedyaml.YamlDocument;
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
 * configured punishments. The session must still be alive and queryable when
 * this is invoked — it is called before the session is ended.
 * <p>
 * Registered as a {@link Manager} in the {@link com.diamonddagger590.mccore.registry.manager.ManagerRegistry}
 * under {@link McRPGManagerKey#COMBAT_LOG} so that listeners and other collaborators
 * can retrieve it from the registry rather than requiring constructor injection.
 */
public class CombatLogManager extends Manager<McRPG> {

    private final ReloadableContent<CombatLogMode> mode;

    /**
     * Constructs a new {@link CombatLogManager}. Initializes the reloadable combat log
     * mode from the combat configuration and tracks it with the
     * {@link com.diamonddagger590.mccore.configuration.ReloadableContentManager} for
     * automatic refresh on {@code /mcrpg admin reload}.
     *
     * @param mcRPG The plugin instance for config access, localization, and database access.
     */
    public CombatLogManager(@NotNull McRPG mcRPG) {
        super(mcRPG);

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

        mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(ManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(mode);
    }

    /**
     * Gets the shared {@link ReloadableContent} for the combat log mode.
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
     * The {@link PlayerCombatLogEvent} is always fired regardless of the current
     * {@link CombatLogMode}, so third-party listeners can observe every
     * logout-during-combat. Punishment is gated on both the mode policy and the
     * event's {@code shouldApplyPunishment()} flag.
     * <p>
     * Must be called on the main thread while the session is still active (before
     * {@code endSession}).
     *
     * @param player  The player who is logging out.
     * @param session The player's active combat session.
     * @throws IllegalStateException if called from any thread other than the main server thread.
     */
    public void evaluateAndEnforce(@NotNull Player player, @NotNull CombatSession session) {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("evaluateAndEnforce must be called from the main server thread, "
                    + "but was called from thread: " + Thread.currentThread().getName());
        }

        CombatLogMode currentMode = mode.getContent();
        CombatType combatType = session.getCombatType();
        Collection<CombatParticipant> participants = session.getParticipants();

        PlayerCombatLogEvent logEvent = new PlayerCombatLogEvent(player, session, combatType, participants);
        Bukkit.getPluginManager().callEvent(logEvent);

        if (!currentMode.shouldPunish(combatType) || !logEvent.shouldApplyPunishment()) {
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
        CombatLogPunishmentTypeRegistry punishmentRegistry = plugin().registryAccess()
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
     * Each type is isolated — a failure in one type does not prevent subsequent types
     * from running.
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
                try {
                    type.apply(player, session, plugin());
                }
                catch (Exception e) {
                    plugin().getLogger().log(Level.WARNING,
                            "[CombatLogManager] Punishment type " + type.getKey()
                                    + " threw during apply for " + player.getName(), e);
                }
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
                plugin().getTimeProvider().now(),
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                combatType,
                participantUUIDs,
                appliedPunishments
        );

        var database = plugin().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection conn = database.getConnection()) {
                BatchTransaction transaction = new BatchTransaction(conn,
                        CombatLogDAO.insertCombatLog(conn, entry));
                transaction.executeTransaction();
            }
            catch (Exception e) {
                plugin().getLogger().log(Level.WARNING,
                        "Failed to record combat log entry for " + player.getName(), e);
            }
        });
    }
}
