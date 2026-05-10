package us.eunoians.mcrpg.entity.player;

import com.diamonddagger590.mccore.database.table.impl.PlayerSettingDAO;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.display.impl.PlayerDisplay;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.combo.PlayerComboState;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.database.table.LoadoutAbilityDAO;
import us.eunoians.mcrpg.database.table.LoadoutDisplayDAO;
import us.eunoians.mcrpg.database.table.LoadoutInfoDAO;
import us.eunoians.mcrpg.database.table.PlayerExperienceExtrasDAO;
import us.eunoians.mcrpg.database.table.PlayerLoadoutSelectionDAO;
import us.eunoians.mcrpg.database.table.PlayerLoginTimeDAO;
import us.eunoians.mcrpg.database.table.PlayerStatDAO;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.event.entity.player.PlayerSafeZoneStateChangeEvent;
import us.eunoians.mcrpg.external.common.SafeZonePluginHook;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.stat.PlayerStat;
import us.eunoians.mcrpg.stat.PlayerStatRegistry;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.stat.instance.PlayerStatData;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * The main "player" object for any player who will be playing McRPG.
 * <p>
 * This is also the main access point to a player's skill data through
 * {@link #asSkillHolder()}
 */
public class McRPGPlayer extends CorePlayer {

    private final SkillHolder skillHolder;
    private final QuestHolder questHolder;
    private final PlayerExperienceExtras playerExperienceExtras;
    private final PlayerStatData playerStatData;
    private final PlayerComboState comboState = new PlayerComboState();
    private final Map<Class<? extends PlayerDisplay>, PlayerDisplay> displays = new HashMap<>();
    private boolean standingInSafeZone;

    public McRPGPlayer(@NotNull Player player, @NotNull McRPG mcRPG) {
        super(player.getUniqueId(), mcRPG);
        this.skillHolder = new SkillHolder(mcRPG, getUUID());
        this.questHolder = new QuestHolder(getUUID());
        this.playerExperienceExtras = new PlayerExperienceExtras();
        this.playerStatData = new PlayerStatData();
        this.standingInSafeZone = false;
    }

    public McRPGPlayer(@NotNull UUID uuid, @NotNull McRPG mcRPG) {
        super(uuid, mcRPG);
        this.skillHolder = new SkillHolder(mcRPG, getUUID());
        this.questHolder = new QuestHolder(getUUID());
        this.playerExperienceExtras = new PlayerExperienceExtras();
        this.playerStatData = new PlayerStatData();
        this.standingInSafeZone = false;
    }

    @Override
    public boolean useMutex() {
        return false;
    }

    /**
     * Gets the {@link McRPG} instance that created this player.
     *
     * @return The {@link McRPG} instance that created this player.
     */
    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @NotNull
    @Override
    public Set<McRPGSetting> getPlayerSettings() {
        return super.getPlayerSettings().stream().filter(setting -> setting instanceof McRPGSetting).map(setting -> (McRPGSetting) setting).collect(Collectors.toSet());
    }

    /**
     * Gets the {@link SkillHolder} representation of this player, allowing access to McRPG
     * skill functionality.
     *
     * @return The {@link SkillHolder} representation of this player.
     */
    @NotNull
    public SkillHolder asSkillHolder() {
        return skillHolder;
    }

    /**
     * Gets the {@link QuestHolder} representation of this player, allowing access
     * to McRPG quest functionality
     *
     * @return The {@link QuestHolder} representation of this player.
     */
    @NotNull
    public QuestHolder asQuestHolder() {
        return questHolder;
    }

    /**
     * Checks to see if this player can start an upgrade quest for the provided {@link TierableAbility}.
     *
     * @param tierableAbility The {@link TierableAbility} to check.
     * @return {@code true} if this player can start an upgrade quest for the provided {@link TierableAbility}
     */
    public boolean canPlayerStartUpgradeQuest(@NotNull TierableAbility tierableAbility) {
        var abilityDataOptional = skillHolder.getAbilityData(tierableAbility);
        if (abilityDataOptional.isPresent()) {
            var tierAttributeOptional = abilityDataOptional.get().getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY);
            if (skillHolder.hasActiveUpgradeQuest(tierableAbility.getAbilityKey())) {
                return false;
            }
            if (tierAttributeOptional.isPresent() && tierAttributeOptional.get() instanceof AbilityTierAttribute attribute) {
                int currentTier = attribute.getContent();
                int nextTier = currentTier + 1;
                if (tierableAbility.getMaxTier() >= nextTier) {
                    if (tierableAbility instanceof SkillAbility skillAbility) {
                        var skillData = skillHolder.getSkillHolderData(skillAbility.getSkillKey());
                        return skillData.isPresent() && skillData.get().getCurrentLevel() >= tierableAbility.getUnlockLevelForTier(nextTier);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Starts an upgrade quest for the provided {@link TierableAbility}. Looks up the
     * quest definition for the next tier and delegates to
     * {@link us.eunoians.mcrpg.quest.QuestManager#startQuest}. On success, the
     * {@link us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute} is set
     * on the player's ability data.
     *
     * @param tierableAbility the tierable ability to start an upgrade quest for
     */
    public void startUpgradeQuest(@NotNull TierableAbility tierableAbility) {
        int nextTier = tierableAbility.getCurrentAbilityTier(skillHolder) + 1;
        QuestManager questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        questManager.resolveUpgradeQuestDefinition(tierableAbility, nextTier).ifPresent(definition ->
                questManager.startQuest(definition, getUUID(), Map.of("tier", nextTier), new AbilityUpgradeQuestSource()).ifPresent(instance ->
                        skillHolder.getAbilityData(tierableAbility).ifPresent(abilityData ->
                                abilityData.addAttribute(new AbilityUpgradeQuestAttribute(instance.getQuestUUID())))));
    }

    /**
     * Gets the {@link PlayerExperienceExtras} for this player.
     *
     * @return The {@link PlayerExperienceExtras} for this player.
     */
    @NotNull
    public PlayerExperienceExtras getExperienceExtras() {
        return playerExperienceExtras;
    }

    /**
     * Gets the {@link PlayerStatData} for this player, containing all player stat
     * instances (HP, Mana, etc.).
     *
     * @return The {@link PlayerStatData} for this player.
     */
    @NotNull
    public PlayerStatData getPlayerStatData() {
        return playerStatData;
    }

    /**
     * Gets the mutable combo input state for this player. Managed by
     * {@link us.eunoians.mcrpg.ability.combo.ComboManager}.
     *
     * @return The combo state for this player.
     */
    @NotNull
    public PlayerComboState getComboState() {
        return comboState;
    }

    /**
     * Gets the {@link PlayerDisplay} of the provided type associated with this player,
     * if one has been registered.
     * <p>
     * Displays are keyed by the base class passed in ({@code ExperienceDisplay.class},
     * {@code ActionBarHudDisplay.class}, etc.) so concrete subclasses are interchangeable
     * lookups.
     *
     * @param type The base {@link PlayerDisplay} class to look up.
     * @param <T>  The display type.
     * @return An {@link Optional} containing the registered display, or empty if none.
     */
    @NotNull
    public <T extends PlayerDisplay> Optional<T> getDisplay(@NotNull Class<T> type) {
        return Optional.ofNullable(displays.get(type)).map(type::cast);
    }

    /**
     * Checks whether this player currently has a registered {@link PlayerDisplay} of
     * the provided type.
     *
     * @param type The base {@link PlayerDisplay} class to check.
     * @return {@code true} if a display of the given type is registered.
     */
    public boolean hasDisplay(@NotNull Class<? extends PlayerDisplay> type) {
        return displays.containsKey(type);
    }

    /**
     * Registers a {@link PlayerDisplay} for this player, cleaning up any previously
     * registered display of the same type.
     *
     * @param type    The base {@link PlayerDisplay} class to register under.
     * @param display The display instance to register.
     * @param <T>     The display type.
     */
    public <T extends PlayerDisplay> void setDisplay(@NotNull Class<T> type, @NotNull T display) {
        PlayerDisplay previous = displays.put(type, display);
        if (previous != null && previous != display) {
            previous.cleanDisplay();
        }
    }

    /**
     * Removes the registered {@link PlayerDisplay} for this player, invoking
     * {@link PlayerDisplay#cleanDisplay()} if present.
     *
     * @param type The base {@link PlayerDisplay} class to remove.
     */
    public void removeDisplay(@NotNull Class<? extends PlayerDisplay> type) {
        PlayerDisplay previous = displays.remove(type);
        if (previous != null) {
            previous.cleanDisplay();
        }
    }

    /**
     * Copies every {@link PlayerDisplay} currently registered on this player
     * into the caller-provided {@code sink}.
     * <p>
     * Using a caller-owned buffer lets hot-path iterators (e.g. the HUD tick
     * loop) reuse a single collection across players and avoid allocating a
     * fresh snapshot list every tick. Once populated, the caller can iterate
     * {@code sink} safely even if a display mutates the player's display map
     * during iteration.
     *
     * @param sink The destination collection. Expected to be cleared by the
     *             caller prior to this call if only the current snapshot is
     *             wanted; this method only appends.
     */
    public void snapshotDisplaysInto(@NotNull Collection<? super PlayerDisplay> sink) {
        if (displays.isEmpty()) {
            return;
        }
        sink.addAll(displays.values());
    }

    /**
     * Cleans up and removes every {@link PlayerDisplay} registered for this player.
     */
    public void clearAllDisplays() {
        if (displays.isEmpty()) {
            return;
        }
        for (PlayerDisplay display : displays.values()) {
            display.cleanDisplay();
        }
        displays.clear();
    }

    /**
     * Checks to see if the player is currently standing in a safe zone or not.
     * <p>
     * This value is updated periodically while the player is online. If you want to get
     * a live-updated version, then call {@link #isStandingInSafeZone(boolean)}.
     * <p>
     * If the player is offline,
     * then check {@link us.eunoians.mcrpg.database.table.PlayerLoginTimeDAO#didPlayerLogoutInSafeZone(Connection, UUID)}.
     *
     * @return {@code true} if the player is currently standing in a safe zone or not.
     */
    public boolean isStandingInSafeZone() {
        return standingInSafeZone;
    }

    /**
     * Checks to see if the player is currently standing in a safe zone or not. If force
     * update is set to true, then it will call {@link #refreshSafeZoneState()} before
     * returning the state.
     * <p>
     * If the player is offline,
     * then check {@link us.eunoians.mcrpg.database.table.PlayerLoginTimeDAO#didPlayerLogoutInSafeZone(Connection, UUID)}.
     *
     * @param forceUpdate If the cached state should be updated or not.
     * @return {@code true} if the player is currently standing in a safe zone or not.
     */
    public boolean isStandingInSafeZone(boolean forceUpdate) {
        return forceUpdate ? refreshSafeZoneState() : isStandingInSafeZone();
    }

    /**
     * Forcibly refreshes the player's safe zone state if they are online.
     *
     * @return The updated safe zone state.
     */
    public boolean refreshSafeZoneState() {
        var playerOptional = this.getAsBukkitPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            List<SafeZonePluginHook> safeZonePluginHooks = RegistryAccess.registryAccess()
                    .registry(RegistryKey.PLUGIN_HOOK).pluginHooks(SafeZonePluginHook.class);
            boolean safeZoneAllowed = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE)
                    .getFile(FileType.MAIN_CONFIG).getBoolean(MainConfigFile.SAFE_ZONE_ALLOW_ACCUMULATION);
            boolean isPlayerInSafeZone = safeZonePluginHooks.stream()
                    .map(safeZonePluginHook -> safeZonePluginHook.isPlayerInSafeZone(player))
                    .reduce((a, b) -> a || b)
                    .orElse(false) && safeZoneAllowed;
            boolean wasPlayerInSafeZone = isStandingInSafeZone();
            if (isPlayerInSafeZone != wasPlayerInSafeZone) {
                this.standingInSafeZone = isPlayerInSafeZone;
                PlayerSafeZoneStateChangeEvent playerSafeZoneStateChangeEvent = new PlayerSafeZoneStateChangeEvent(this,
                        standingInSafeZone ? PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType.ENTERED : PlayerSafeZoneStateChangeEvent.SafeZoneStateChangeType.LEFT);
                Bukkit.getPluginManager().callEvent(playerSafeZoneStateChangeEvent);
            }
        }
        return this.standingInSafeZone;
    }

    /**
     * Saves all player data using the provided {@link Connection}.
     *
     * @param connection The {@link Connection} to use to save data to.
     */
    public void savePlayer(@NotNull Connection connection) {
        BatchTransaction batchTransaction = new BatchTransaction(connection);
        FailSafeTransaction failsafeTransaction = new FailSafeTransaction(connection);
        failsafeTransaction.addAll(SkillDAO.saveAllSkillHolderInformation(connection, skillHolder));
        failsafeTransaction.addAll(LoadoutInfoDAO.saveAllLoadoutInfo(connection, skillHolder));
        failsafeTransaction.addAll(LoadoutAbilityDAO.saveAllLoadouts(connection, skillHolder));
        failsafeTransaction.addAll(LoadoutDisplayDAO.saveAllLoadoutDisplays(connection, skillHolder));
        failsafeTransaction.addAll(PlayerExperienceExtrasDAO.savePlayerExperienceExtras(connection, getUUID(), playerExperienceExtras));
        batchTransaction.addAll(PlayerSettingDAO.savePlayerSettings(connection, getUUID(), getPlayerSettings()));
        batchTransaction.addAll(PlayerLoadoutSelectionDAO.setActiveLoadout(connection, getUUID(), skillHolder.getCurrentLoadoutSlot()));
        failsafeTransaction.executeTransaction();
        batchTransaction.executeTransaction();

        // Save statistics separately — snapshot dirty entries first so that keys dirtied by the
        // main thread between getModifiedEntries() and markClean() are not silently dropped.
        var modifiedEntries = getStatisticData().getModifiedEntries();
        if (!modifiedEntries.isEmpty()) {
            FailSafeTransaction statisticTransaction = new FailSafeTransaction(connection);
            statisticTransaction.addAll(PlayerStatisticDAO.savePlayerStatistics(connection, getUUID(), modifiedEntries));
            statisticTransaction.executeTransaction();
            getStatisticData().markClean(modifiedEntries.keySet());
        }

        // Save resource pool stats (mana, etc.) — only persist pools that have a regen component
        // since flat stats don't have a meaningful current/persisted value.
        PlayerStatRegistry statRegistry = getPlugin().registryAccess().registry(McRPGRegistryKey.PLAYER_STAT);
        Map<org.bukkit.NamespacedKey, Double> statsToSave = new LinkedHashMap<>();
        for (PlayerStat stat : statRegistry.allStats()) {
            if (stat.isResourcePool()) {
                playerStatData.getInstance(stat.getKey())
                        .ifPresent(instance -> statsToSave.put(stat.getKey(), instance.getCurrent()));
            }
        }
        if (!statsToSave.isEmpty()) {
            try {
                FailSafeTransaction statTransaction = new FailSafeTransaction(connection);
                statTransaction.addAll(PlayerStatDAO.saveStats(connection, getUUID(), statsToSave));
                statTransaction.executeTransaction();
            } catch (SQLException e) {
                getPlugin().getLogger().log(Level.SEVERE, "Failed to save player stats for " + getUUID(), e);
            }
        }
    }

    /**
     * Saves the player data relating to logout times using the provided {@link Connection}.
     *
     * @param connection The {@link Connection} to use to save data to.
     */
    public void savePlayerLogoutTime(@NotNull Connection connection) {
        FailSafeTransaction lastLogoutTransaction = new FailSafeTransaction(connection);
        Instant logoutTime = McRPG.getInstance().getTimeProvider().now();
        lastLogoutTransaction.addAll(PlayerLoginTimeDAO.saveLastLogoutTime(connection, this.getUUID(), logoutTime));
        lastLogoutTransaction.addAll(PlayerLoginTimeDAO.saveLastSeenTime(connection, this.getUUID(), logoutTime));
        lastLogoutTransaction.addAll(PlayerLoginTimeDAO.saveLoggedOutInSafeZone(connection, this.getUUID(), this.isStandingInSafeZone(true)));
        lastLogoutTransaction.executeTransaction();
    }
}
