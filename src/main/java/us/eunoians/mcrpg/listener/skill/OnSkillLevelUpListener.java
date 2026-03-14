package us.eunoians.mcrpg.listener.skill;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.database.transaction.FailSafeTransaction;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.event.skill.PostSkillGainExpEvent;
import us.eunoians.mcrpg.event.skill.PostSkillGainLevelEvent;
import us.eunoians.mcrpg.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.source.builtin.AbilityUpgradeQuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * This listener is in charge of handling ability unlocks and ability point distributions
 */
public class OnSkillLevelUpListener implements Listener {

    /** @deprecated Upgrade points are deprecated in favor of quest-based ability upgrades. */
    @Deprecated
    private static final int UPGRADE_POINT_AWARD_THRESHOLD = 1;

    @EventHandler(priority = EventPriority.MONITOR)
    public void handleLevelUp(SkillGainLevelEvent skillGainLevelEvent) {
        SkillHolder skillHolder = skillGainLevelEvent.getSkillHolder();
        UUID uuid = skillHolder.getUUID();
        int levels = skillGainLevelEvent.getLevels();
        Skill skill = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(skillGainLevelEvent.getSkillKey());
        var playerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(uuid);
        if (playerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = playerOptional.get();
            MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
            Audience player = mcRPGPlayer.getAsBukkitPlayer().get();
            McRPGLocalizationManager localizationManager = McRPG.getInstance().registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION);
            Component skillDisplayName = skill.getDisplayName(mcRPGPlayer);
            String serializedSkillName = miniMessage.serialize(skillDisplayName);
            player.sendMessage(localizationManager.getLocalizedMessageAsComponent(mcRPGPlayer, LocalizationKey.SKILL_LEVEL_UP_MESSAGE,
                    Map.of("levels", String.valueOf(levels), "skill", serializedSkillName)));
        }
    }

    @EventHandler
    public void handlePostLevelEvent(PostSkillGainLevelEvent postSkillGainLevelEvent) {
        SkillHolder skillHolder = postSkillGainLevelEvent.getSkillHolder();
        Skill skill = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(postSkillGainLevelEvent.getSkillKey());
        var skillHolderDataOptional = skillHolder.getSkillHolderData(skill);
        if (skillHolderDataOptional.isPresent()) {

            /** @deprecated Upgrade points are deprecated in favor of quest-based upgrades. */
            if (postSkillGainLevelEvent.getBeforeLevel()/UPGRADE_POINT_AWARD_THRESHOLD != postSkillGainLevelEvent.getAfterLevel()/UPGRADE_POINT_AWARD_THRESHOLD) {
                skillHolder.giveUpgradePoints(1);
            }

            var skillHolderData = skillHolderDataOptional.get();
            AbilityRegistry abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);
            for (NamespacedKey abilityKey : abilityRegistry.getAbilitiesBelongingToSkill(skill)) {
                Ability ability = abilityRegistry.getRegisteredAbility(abilityKey);
                if (ability instanceof UnlockableAbility unlockableAbility && unlockableAbility.getUnlockLevel() <= skillHolderData.getCurrentLevel()) {
                    var abilityDataOptional = skillHolder.getAbilityData(abilityKey);
                    if (abilityDataOptional.isPresent()) {
                        AbilityData abilityData = abilityDataOptional.get();
                        var attributeOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE);
                        if (attributeOptional.isPresent()) {
                            AbilityUnlockedAttribute attribute = (AbilityUnlockedAttribute) attributeOptional.get();
                            if (!attribute.getContent()) {
                                AbilityUnlockEvent abilityUnlockEvent = new AbilityUnlockEvent(skillHolder, unlockableAbility);
                                Bukkit.getPluginManager().callEvent(abilityUnlockEvent);
                                abilityData.updateAttribute(attribute, true);

                                Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
                                database.getDatabaseExecutorService().submit(() -> {
                                    try (Connection connection = database.getConnection()) {
                                        new FailSafeTransaction(connection, SkillDAO.savePlayerSkillData(connection, skillHolder)).executeTransaction();
                                    }
                                    catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }
                    }
                }

                if (ability instanceof TierableAbility tierableAbility) {
                    checkAndStartUpgradeQuest(skillHolder, tierableAbility);
                }
            }
        }
    }

    /**
     * Checks if the player is eligible for an upgrade quest for the given tierable ability
     * and auto-starts it if no quest is currently active. Validation against repeat mode
     * and the completion log happens asynchronously.
     */
    private void checkAndStartUpgradeQuest(@NotNull SkillHolder skillHolder,
                                           @NotNull TierableAbility tierableAbility) {
        int currentTier = tierableAbility.getCurrentAbilityTier(skillHolder);
        int nextTier = currentTier + 1;
        if (nextTier > tierableAbility.getMaxTier()) {
            return;
        }

        Optional<AbilityData> abilityDataOpt = skillHolder.getAbilityData(tierableAbility);
        if (abilityDataOpt.isEmpty()) {
            return;
        }
        AbilityData abilityData = abilityDataOpt.get();

        var questAttrOpt = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
        if (questAttrOpt.isPresent() && questAttrOpt.get() instanceof AbilityUpgradeQuestAttribute questAttr
                && questAttr.shouldContentBeSaved()) {
            return;
        }

        if (tierableAbility instanceof SkillAbility skillAbility) {
            int requiredLevel = tierableAbility.getUnlockLevelForTier(nextTier);
            Optional<Integer> currentLevel = skillHolder.getSkillHolderData(skillAbility.getSkillKey())
                    .map(data -> data.getCurrentLevel());
            if (currentLevel.isEmpty() || currentLevel.get() < requiredLevel) {
                return;
            }
        }

        QuestManager questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        Optional<QuestDefinition> defOpt = questManager.resolveUpgradeQuestDefinition(tierableAbility, nextTier);
        if (defOpt.isEmpty()) {
            return;
        }

        QuestDefinition definition = defOpt.get();
        UUID playerUUID = skillHolder.getUUID();

        Database database = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DATABASE).getDatabase();
        database.getDatabaseExecutorService().submit(() -> {
            try (Connection connection = database.getConnection()) {
                if (!questManager.canPlayerStartQuest(connection, playerUUID, definition)) {
                    return;
                }

                Bukkit.getScheduler().runTask(McRPG.getInstance(), () -> {
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player == null || !player.isOnline()) {
                        return;
                    }

                    questManager.startQuest(definition, playerUUID, Map.of("tier", nextTier), new AbilityUpgradeQuestSource()).ifPresent(instance ->
                            abilityData.addAttribute(new AbilityUpgradeQuestAttribute(instance.getQuestUUID())));
                });
            } catch (SQLException e) {
                McRPG.getInstance().getLogger().log(Level.SEVERE,
                        "Failed to check upgrade quest eligibility for player " + playerUUID, e);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void handlePostExperienceGain(PostSkillGainExpEvent skillGainExpEvent) {
        SkillHolder skillHolder = skillGainExpEvent.getSkillHolder();
        Skill skill = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(skillGainExpEvent.getSkillKey());

        // Suppress display at max level — XP accumulates silently in the background
        var skillDataOptional = skillHolder.getSkillHolderData(skill);
        if (skillDataOptional.isPresent() && skillDataOptional.get().getCurrentLevel() >= skill.getMaxLevel()) {
            return;
        }

        McRPGPlayerManager playerManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER);
        var playerOptional = playerManager.getPlayer(skillHolder.getUUID());

        if(Bukkit.getEntity(skillHolder.getUUID()) instanceof Player player && player.isOnline() && playerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = playerOptional.get();
            McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER).manager(McRPGManagerKey.DISPLAY).sendExperienceUpdate(mcRPGPlayer, skill.getSkillKey());
        }
    }
}
