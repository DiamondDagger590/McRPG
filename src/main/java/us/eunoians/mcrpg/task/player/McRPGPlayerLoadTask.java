package us.eunoians.mcrpg.task.player;

import com.diamonddagger590.mccore.database.transaction.BatchTransaction;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.player.PlayerLoadTask;
import net.kyori.adventure.audience.Audience;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.LoadoutAbilityDAO;
import us.eunoians.mcrpg.database.table.LoadoutDisplayDAO;
import us.eunoians.mcrpg.database.table.PlayerExperienceExtrasDAO;
import us.eunoians.mcrpg.database.table.PlayerLoginTimeDAO;
import us.eunoians.mcrpg.database.table.PlayerSettingDAO;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.database.table.SkillDataSnapshot;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A {@link PlayerLoadTask} that loads McRPG player data
 */
//TODO javadoc
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
        Instant loginTime = Instant.now();
        SkillRegistry skillRegistry = getPlugin().registryAccess().registry(McRPGRegistryKey.SKILL);
        AbilityRegistry abilityRegistry = getPlugin().registryAccess().registry(McRPGRegistryKey.ABILITY);
        AbilityAttributeRegistry abilityAttributeRegistry = getPlugin().registryAccess().registry(McRPGRegistryKey.ABILITY_ATTRIBUTE);
        SkillHolder skillHolder = getCorePlayer().asSkillHolder();
        UUID uuid = getCorePlayer().getUUID();

        // TODO move this into the skill holder
        try (Connection connection = getPlugin().getDatabase().getConnection()) {

            // TODO Do these player manipulations on main thread oop
            for (NamespacedKey skillKey : skillRegistry.getRegisteredSkillKeys()) {
                Skill skill = skillRegistry.getRegisteredSkill(skillKey);
                getPlugin().getLogger().log(Level.INFO, "Loading data for skill: " + skillKey.getKey());
                SkillDataSnapshot skillDataSnapshot = SkillDAO.getAllPlayerSkillInformation(connection, getCorePlayer().getUUID(), skillKey);
                getPlugin().getLogger().log(Level.INFO, "Data loaded for skill: " + skillKey.getKey() + " Skill level: " + skillDataSnapshot.getCurrentLevel() + " Skill exp: " + skillDataSnapshot.getCurrentExp());
                skillHolder.addSkillHolderData(skill, skillDataSnapshot.getCurrentLevel(), skillDataSnapshot.getCurrentExp());

                for (NamespacedKey abilityKey : abilityRegistry.getAbilitiesBelongingToSkill(skillKey)) {
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

            // Loadouts
            int loadoutAmount = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_AMOUNT);
            for (int x = 1; x <= loadoutAmount; x++) {
                Loadout loadout = LoadoutAbilityDAO.getLoadout(connection, uuid, x);
                var displayOptional = LoadoutDisplayDAO.getLoadoutDisplay(connection, uuid, x);
                displayOptional.ifPresent(loadout::setLoadoutDisplay);
                skillHolder.setLoadout(loadout);
            }

            // Player settings
            PlayerSettingDAO.getPlayerSettings(connection, uuid).forEach(playerSetting -> getCorePlayer().setPlayerSetting(playerSetting));
            // Experience extras
            getCorePlayer().getExperienceExtras().copyExtras(PlayerExperienceExtrasDAO.getPlayerExperienceExtras(connection, uuid));
            awardRestedExperience(connection);
            updatePlayerLoginTimes(connection, loginTime);
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
            Audience audience = getPlugin().getAdventure().player(player.get());
            audience.sendMessage(getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessageAsComponent(getCorePlayer(), LocalizationKey.LOGIN_UNABLE_TO_LOAD_DATA));
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
     */
    private void awardRestedExperience(@NotNull Connection connection) {
        var logoutTimeOptional = PlayerLoginTimeDAO.getLastLogoutTime(connection, getCorePlayer().getUUID());
        boolean safeZoneLogout = PlayerLoginTimeDAO.didPlayerLogoutInSafeZone(connection, getCorePlayer().getUUID());
        if (logoutTimeOptional.isPresent()) {
            Instant logoutTime = logoutTimeOptional.get();
            Instant now = Instant.now();
            double difference = Duration.between(now, logoutTime).abs().toSeconds();
            RestedExperienceManager restedExperienceManager = getPlugin().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.RESTED_EXPERIENCE);
            // Award rested experience
            restedExperienceManager.awardRestedExperience(getCorePlayer(), (int) difference, safeZoneLogout);
        }
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
        BatchTransaction loginInfoTransaction = new BatchTransaction(connection);
        if (!hasPlayerLoggedInBefore) {
            loginInfoTransaction.addAll(PlayerLoginTimeDAO.saveFirstLoginTime(connection, uuid, loginTime));
        }
        loginInfoTransaction.addAll(PlayerLoginTimeDAO.saveLastLoginTime(connection, uuid, loginTime));
        loginInfoTransaction.addAll(PlayerLoginTimeDAO.saveLastSeenTime(connection, uuid, loginTime));
        // Reset now that they've logged in
        loginInfoTransaction.addAll(PlayerLoginTimeDAO.saveLoggedOutInSafeZone(connection, uuid, false));
        loginInfoTransaction.executeTransaction();
    }
}
