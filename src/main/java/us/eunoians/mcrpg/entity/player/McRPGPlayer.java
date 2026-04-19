package us.eunoians.mcrpg.entity.player;

import com.diamonddagger590.mccore.database.table.impl.PlayerSettingDAO;
import com.diamonddagger590.mccore.database.table.impl.PlayerStatisticDAO;
import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
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
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.event.entity.player.PlayerSafeZoneStateChangeEvent;
import us.eunoians.mcrpg.external.common.SafeZonePluginHook;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.stat.PlayerCombatData;

import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
    private final PlayerCombatData playerCombatData;
    private final PlayerComboState comboState = new PlayerComboState();
    private Component actionBarCenterContent;
    private long actionBarCenterContentExpiryTick;
    private boolean standingInSafeZone;

    public McRPGPlayer(@NotNull Player player, @NotNull McRPG mcRPG) {
        super(player.getUniqueId(), mcRPG);
        this.skillHolder = new SkillHolder(mcRPG, getUUID());
        this.questHolder = new QuestHolder(getUUID());
        this.playerExperienceExtras = new PlayerExperienceExtras();
        this.playerCombatData = initCombatData(mcRPG);
        this.standingInSafeZone = false;
    }

    public McRPGPlayer(@NotNull UUID uuid, @NotNull McRPG mcRPG) {
        super(uuid, mcRPG);
        this.skillHolder = new SkillHolder(mcRPG, getUUID());
        this.questHolder = new QuestHolder(getUUID());
        this.playerExperienceExtras = new PlayerExperienceExtras();
        this.playerCombatData = initCombatData(mcRPG);
        this.standingInSafeZone = false;
    }

    /**
     * Builds the initial {@link PlayerCombatData} for this player by looking up
     * the {@link us.eunoians.mcrpg.stat.StatManager} via the manager registry and
     * applying config-driven base stat overrides. Returns an empty container if
     * either the stat manager or the combo config is not registered yet
     * (primarily a defensive path for non-PROD startup profiles).
     *
     * @param mcRPG The McRPG plugin instance.
     * @return A fully initialized {@link PlayerCombatData} for this player.
     */
    @NotNull
    private PlayerCombatData initCombatData(@NotNull McRPG mcRPG) {
        var managerRegistry = mcRPG.registryAccess().registry(RegistryKey.MANAGER);
        if (!managerRegistry.registered(McRPGManagerKey.STAT) || !managerRegistry.registered(McRPGManagerKey.FILE)) {
            return new PlayerCombatData();
        }
        var comboConfig = managerRegistry.manager(McRPGManagerKey.FILE).getFile(FileType.COMBO_CONFIG);
        return managerRegistry.manager(McRPGManagerKey.STAT).createPlayerCombatData(comboConfig);
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
            // Validate they don't have an ongoing upgrade quest
            if (skillHolder.hasActiveUpgradeQuest(tierableAbility.getAbilityKey())) {
                return false;
            }
            if (tierAttributeOptional.isPresent() && tierAttributeOptional.get() instanceof AbilityTierAttribute attribute) {
                int currentTier = attribute.getContent();
                int nextTier = currentTier + 1;
                int upgradeCost = tierableAbility.getUpgradeCostForTier(nextTier);
                // If the next tier is below or at the tier cap
                if (tierableAbility.getMaxTier() >= nextTier) {
                    // If the ability has a skill tied to it
                    if (tierableAbility instanceof SkillAbility skillAbility) {
                        var skillData = skillHolder.getSkillHolderData(skillAbility.getSkillKey());
                        // Check if the current skill level is enough to unlock and ensure player has enough upgrade points
                        return skillData.isPresent() && skillData.get().getCurrentLevel() >= tierableAbility.getUnlockLevelForTier(nextTier) && skillHolder.getUpgradePoints() >= upgradeCost;
                    }
                    // If the ability doesn't have a skill, then check if they have enough upgrade points
                    else {
                        return skillHolder.getUpgradePoints() >= upgradeCost;
                    }
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
     * Gets the {@link PlayerCombatData} for this player, containing all combat stat
     * instances (HP, Mana, etc.).
     *
     * @return The {@link PlayerCombatData} for this player.
     */
    @NotNull
    public PlayerCombatData getPlayerCombatData() {
        return playerCombatData;
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

    // TODO(#216): Move the action bar center content API off McRPGPlayer and
    // into the display manager system. McRPGPlayer should not own HUD-layer state
    // directly — the display manager should hold per-player center content with
    // priority/layering so third-party plugins can compete for the zone cleanly.

    /**
     * Sets the action bar center content with a tick-based expiry. The content is
     * automatically cleared by the HUD renderer once the current server tick reaches
     * or exceeds the expiry tick. Newer writes overwrite older ones.
     *
     * @param content    The component to display in the center zone.
     * @param expiryTick The server tick at which this content expires.
     */
    public void setActionBarCenterContent(@NotNull Component content, long expiryTick) {
        this.actionBarCenterContent = content;
        this.actionBarCenterContentExpiryTick = expiryTick;
    }

    /**
     * Sets the action bar center content that persists until explicitly cleared.
     * Used for ongoing displays like combo progress dots.
     *
     * @param content The component to display in the center zone.
     */
    public void setActionBarCenterContentPersistent(@NotNull Component content) {
        this.actionBarCenterContent = content;
        this.actionBarCenterContentExpiryTick = Long.MAX_VALUE;
    }

    /**
     * Returns the current action bar center content if it has not expired, or empty
     * if no content is set or the entry has expired.
     *
     * @param currentTick The current server tick for expiry checking.
     * @return The center content component, or empty.
     */
    @NotNull
    public Optional<Component> getActionBarCenterContent(long currentTick) {
        if (actionBarCenterContent == null) {
            return Optional.empty();
        }
        if (currentTick >= actionBarCenterContentExpiryTick) {
            clearActionBarCenterContent();
            return Optional.empty();
        }
        return Optional.of(actionBarCenterContent);
    }

    /**
     * Clears the action bar center content immediately.
     */
    public void clearActionBarCenterContent() {
        this.actionBarCenterContent = null;
        this.actionBarCenterContentExpiryTick = 0;
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
