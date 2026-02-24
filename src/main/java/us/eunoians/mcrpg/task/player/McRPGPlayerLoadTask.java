package us.eunoians.mcrpg.task.player;

import com.diamonddagger590.mccore.database.table.impl.PlayerSettingDAO;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.pair.ImmutablePair;
import com.diamonddagger590.mccore.pair.Pair;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.task.core.CoreTask;
import com.diamonddagger590.mccore.task.player.PlayerLoadTask;
import net.kyori.adventure.audience.Audience;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.LoadoutAbilityDAO;
import us.eunoians.mcrpg.database.table.LoadoutDisplayDAO;
import us.eunoians.mcrpg.database.table.PlayerExperienceExtrasDAO;
import us.eunoians.mcrpg.database.table.PlayerLoadoutSelectionDAO;
import us.eunoians.mcrpg.database.table.PlayerLoginTimeDAO;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.database.table.SkillDataSnapshot;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.PlayerExperienceExtras;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.loadout.LoadoutDisplay;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceAccumulationType;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A {@link PlayerLoadTask} that loads McRPG player data
 */
public final class McRPGPlayerLoadTask extends PlayerLoadTask {

    public McRPGPlayerLoadTask(@NotNull McRPG plugin, @NotNull McRPGPlayer mcRPGPlayer) {
        super(plugin, mcRPGPlayer);
    }

    @Override
    public McRPGPlayer getCorePlayer() {
        return (McRPGPlayer) super.getCorePlayer();
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @VisibleForTesting
    @Override
    protected boolean loadPlayer() { //TODO completable future?
        Instant loginTime = getPlugin().getTimeProvider().now();

        // TODO move this into the skill holder
        try (Connection connection = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase().getConnection()) {
            List<UpdatePlayerDataSyncFunction> updatePlayerDataSyncFunctions = new ArrayList<>();
            updatePlayerDataSyncFunctions.add(loadPlayerSkills(connection));
            updatePlayerDataSyncFunctions.add(loadPlayerLoadouts(connection));
            updatePlayerDataSyncFunctions.add(loadPlayerSettings(connection));
            updatePlayerDataSyncFunctions.add(loadPlayerExperienceExtras(connection));
            updatePlayerDataSyncFunctions.add(awardRestedExperience(connection));
            updatePlayerLoginTimes(connection, loginTime);
            // Jump to main thread to save the data
            new CoreTask(getPlugin()) {
                @Override
                public void run() {
                    updatePlayerDataSyncFunctions.forEach(UpdatePlayerDataSyncFunction::updateData);
                }
            }.runTask();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @VisibleForTesting
    @Override
    protected void onPlayerLoadSuccessfully() {
        getPlugin().getLogger().log(Level.INFO, "Player data has been loaded for player: " + getCorePlayer().getUUID());

        //Begin tracking player
        getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).addPlayer(getCorePlayer());
        getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackAbilityHolder(getCorePlayer().asSkillHolder());
        getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).trackQuestHolder(getCorePlayer().asQuestHolder());

        // Fire event
        super.onPlayerLoadSuccessfully();
    }

    @VisibleForTesting
    @Override
    protected void onPlayerLoadFail() {
        getPlugin().getLogger().log(Level.SEVERE, "There was an issue loading in the McRPG player data for player with UUID: " + getCorePlayer().getUUID());

        Optional<Player> player = getCorePlayer().getAsBukkitPlayer();

        if (player.isPresent() && player.get().isOnline()) {
            Audience audience = player.get();
            audience.sendMessage(getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedMessageAsComponent(getCorePlayer(), LocalizationKey.LOGIN_UNABLE_TO_LOAD_DATA));
        }
    }

    @Override
    protected void onDelayComplete() {
    }

    @Override
    protected void onIntervalStart() {
    }

    @Override
    protected void onIntervalPause() {
    }

    @Override
    protected void onIntervalResume() {
    }

    @Override
    public void onTaskExpire() {
    }

    @Override
    protected void onCancel() {
    }

    /**
     * Awards players rested experience based on their time offline
     *
     * @param connection The {@link Connection} to use when checking player login information.
     * @return The {@link UpdatePlayerDataSyncFunction} to run on the main thread to load the player's data.
     */
    @NotNull
    private UpdatePlayerDataSyncFunction awardRestedExperience(@NotNull Connection connection) {
        var logoutTimeOptional = PlayerLoginTimeDAO.getLastLogoutTime(connection, getCorePlayer().getUUID());
        RestedExperienceAccumulationType accumulationType = PlayerLoginTimeDAO.didPlayerLogoutInSafeZone(connection, getCorePlayer().getUUID())
                ? RestedExperienceAccumulationType.OFFLINE_SAFE_ZONE : RestedExperienceAccumulationType.OFFLINE;
        return () -> {
            if (logoutTimeOptional.isPresent()) {
                Instant logoutTime = logoutTimeOptional.get();
                Instant now = McRPG.getInstance().getTimeProvider().now();
                double waitTimeBeforeAccumulation = getPlugin().registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG)
                        .getDouble(MainConfigFile.RESTED_EXPERIENCE_OFFLINE_WAIT_PERIOD_BEFORE_ACCUMULATION, 0.0d);
                int adjustedOfflineTime = calculateAdjustedOfflineTime(logoutTime, now, waitTimeBeforeAccumulation);
                RestedExperienceManager restedExperienceManager = getPlugin().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.RESTED_EXPERIENCE);
                // Award rested experience
                restedExperienceManager.awardRestedExperience(getCorePlayer(), adjustedOfflineTime, accumulationType, true);
            }
        };
    }

    /**
     * Calculates the adjusted offline time for rested experience accumulation,
     * accounting for the configured wait period before accumulation starts.
     *
     * @param logoutTime                  The {@link Instant} when the player logged out.
     * @param now                         The current {@link Instant}.
     * @param waitTimeBeforeAccumulation  The wait period in seconds before accumulation starts.
     * @return The adjusted offline time in seconds, never negative.
     */
    @VisibleForTesting
    static int calculateAdjustedOfflineTime(@NotNull Instant logoutTime, @NotNull Instant now, double waitTimeBeforeAccumulation) {
        double totalOfflineSeconds = Duration.between(now, logoutTime).abs().toSeconds();
        return (int) Math.max(0, totalOfflineSeconds - waitTimeBeforeAccumulation);
    }

    /**
     * Loads player skills and abilities from database.
     *
     * @param connection The {@link Connection} to use when loading a player's skill and ability data.
     * @return The {@link UpdatePlayerDataSyncFunction} to run on the main thread to load the player's data.
     */
    @NotNull
    private UpdatePlayerDataSyncFunction loadPlayerSkills(@NotNull Connection connection) {
        SkillRegistry skillRegistry = getPlugin().registryAccess().registry(McRPGRegistryKey.SKILL);
        AbilityRegistry abilityRegistry = getPlugin().registryAccess().registry(McRPGRegistryKey.ABILITY);
        AbilityAttributeRegistry abilityAttributeRegistry = getPlugin().registryAccess().registry(McRPGRegistryKey.ABILITY_ATTRIBUTE);
        SkillHolder skillHolder = getCorePlayer().asSkillHolder();
        Map<Skill, SkillDataSnapshot> skillDataSnapshots = new HashMap<>();
        for (NamespacedKey skillKey : skillRegistry.getRegisteredSkillKeys()) {
            Skill skill = skillRegistry.getRegisteredSkill(skillKey);
            SkillDataSnapshot skillDataSnapshot = SkillDAO.getAllPlayerSkillInformation(connection, getCorePlayer().getUUID(), skillKey);
            skillDataSnapshots.put(skill, skillDataSnapshot);
        }
        return () -> {
            // Load skill/ability data
            for (Map.Entry<Skill, SkillDataSnapshot> entry : skillDataSnapshots.entrySet()) {
                Skill skill = entry.getKey();
                SkillDataSnapshot skillDataSnapshot = entry.getValue();
                skillHolder.addSkillHolderData(skill, skillDataSnapshot.getTotalExperience());

                for (NamespacedKey abilityKey : abilityRegistry.getAbilitiesBelongingToSkill(skill)) {
                    Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
                    skillHolder.addAvailableAbility(abilityKey);
                    AbilityData abilityData = new AbilityData(abilityKey, skillDataSnapshot.getAbilityAttributes(abilityKey).values());
                    for (NamespacedKey attributeKey : ability.getApplicableAttributes()) {
                        if (!abilityData.hasAttribute(attributeKey)) {
                            abilityAttributeRegistry.getAttribute(attributeKey).ifPresent(abilityData::addAttribute);
                        }
                    }
                    skillHolder.addAbilityData(abilityData);
                }
                getPlugin().getLogger().log(Level.INFO, "Player abilities are now: "
                        + getCorePlayer().asSkillHolder().getAvailableAbilities().stream()
                        .map(NamespacedKey::getKey).reduce((s, s2) -> s + " " + s2).get());
                getPlugin().getLogger().log(Level.INFO, "Player skills are now: "
                        + getCorePlayer().asSkillHolder().getSkills().stream()
                        .map(NamespacedKey::getKey).reduce((s, s2) -> s + " " + s2).get());
            }
        };
    }

    /**
     * Loads player loadouts from database.
     *
     * @param connection The {@link Connection} to use when loading a player's loadouts.
     * @return The {@link UpdatePlayerDataSyncFunction} to run on the main thread to load the player's data.
     */
    @NotNull
    private UpdatePlayerDataSyncFunction loadPlayerLoadouts(@NotNull Connection connection) {
        int loadoutAmount = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_AMOUNT);
        SkillHolder skillHolder = getCorePlayer().asSkillHolder();
        UUID uuid = getCorePlayer().getUUID();
        skillHolder.setCurrentLoadoutSlot(PlayerLoadoutSelectionDAO.getActiveLoadout(connection, uuid));
        List<Pair<Loadout, Optional<LoadoutDisplay>>> loadouts = new ArrayList<>();
        for (int x = 1; x <= loadoutAmount; x++) {
            Loadout loadout = LoadoutAbilityDAO.getLoadout(connection, uuid, x);
            var displayOptional = LoadoutDisplayDAO.getLoadoutDisplay(connection, uuid, x);
            loadouts.add(ImmutablePair.of(loadout, displayOptional));
        }
        return () -> {
            for (Pair<Loadout, Optional<LoadoutDisplay>> loadoutData : loadouts) {
                Loadout loadout = loadoutData.getLeft();
                loadoutData.getRight().ifPresent(loadout::setLoadoutDisplay);
                skillHolder.setLoadout(loadout);
            }
        };
    }

    /**
     * Loads {@link PlayerExperienceExtras} from database.
     *
     * @param connection The {@link Connection} to use when loading a player's experience extras.
     * @return The {@link UpdatePlayerDataSyncFunction} to run on the main thread to load the player's data.
     */
    @NotNull
    private UpdatePlayerDataSyncFunction loadPlayerExperienceExtras(@NotNull Connection connection) {
        UUID uuid = getCorePlayer().getUUID();
        PlayerExperienceExtras experienceExtras = PlayerExperienceExtrasDAO.getPlayerExperienceExtras(connection, uuid);
        return () -> {
            getCorePlayer().getExperienceExtras().copyExtras(experienceExtras);
        };
    }

    /**
     * Loads player settings from database.
     *
     * @param connection The {@link Connection} to use when loading a player's settings.
     * @return The {@link UpdatePlayerDataSyncFunction} to run on the main thread to load the player's data.
     */
    @NotNull
    private UpdatePlayerDataSyncFunction loadPlayerSettings(@NotNull Connection connection) {
        UUID uuid = getCorePlayer().getUUID();
        Set<PlayerSetting> playerSettings = PlayerSettingDAO.getPlayerSettings(connection, uuid);
        return () -> {
            playerSettings.forEach(playerSetting -> getCorePlayer().setPlayerSetting(playerSetting));
        };
    }

    /**
     * Updates the player's log in times.
     *
     * @param connection The {@link Connection} to use when saving player login information.
     * @param loginTime  The {@link Instant} that the player logged in.
     */
    private void updatePlayerLoginTimes(@NotNull Connection connection, @NotNull Instant loginTime) {
        UUID uuid = getCorePlayer().getUUID();
        // Check if the player has logged in before
        boolean hasPlayerLoggedInBefore = PlayerLoginTimeDAO.hasPlayerLoggedInBefore(connection, uuid);
        FailSafeTransaction loginInfoTransaction = new FailSafeTransaction(connection);
        if (!hasPlayerLoggedInBefore) {
            FailSafeTransaction firstLoginTimeTransaction = new FailSafeTransaction(connection);
            firstLoginTimeTransaction.addAll(PlayerLoginTimeDAO.saveFirstLoginTime(connection, uuid, loginTime));
            firstLoginTimeTransaction.executeTransaction();
        }
        loginInfoTransaction.addAll(PlayerLoginTimeDAO.saveLastLoginTime(connection, uuid, loginTime));
        loginInfoTransaction.addAll(PlayerLoginTimeDAO.saveLastSeenTime(connection, uuid, loginTime));
        // Reset now that they've logged in
        loginInfoTransaction.addAll(PlayerLoginTimeDAO.saveLoggedOutInSafeZone(connection, uuid, false));
        loginInfoTransaction.executeTransaction();
    }

    @FunctionalInterface
    private interface UpdatePlayerDataSyncFunction {
        void updateData();
    }
}
