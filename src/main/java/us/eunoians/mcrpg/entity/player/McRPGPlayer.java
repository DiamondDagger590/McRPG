package us.eunoians.mcrpg.entity.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.impl.TierableAbility;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.quest.Quest;
import us.eunoians.mcrpg.quest.QuestManager;

import java.util.UUID;

/**
 * The main "player" object for any player who will be playing McRPG.
 * <p>
 * This is also the main access point to a player's skill data through
 * {@link #asSkillHolder()}
 */
public class McRPGPlayer extends CorePlayer {

    private final SkillHolder skillHolder;
    private final QuestHolder questHolder;

    public McRPGPlayer(@NotNull Player player) {
        super(player.getUniqueId());
        skillHolder = new SkillHolder(getUUID());
        questHolder = new QuestHolder(getUUID());
    }

    public McRPGPlayer(@NotNull UUID uuid) {
        super(uuid);
        skillHolder = new SkillHolder(getUUID());
        questHolder = new QuestHolder(getUUID());
    }

    @Override
    public boolean useMutex() {
        return false;
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

    public boolean canPlayerStartUpgradeQuest(@NotNull TierableAbility tierableAbility) {
        var abilityDataOptional = skillHolder.getAbilityData(tierableAbility);
        if (abilityDataOptional.isPresent()) {
            var tierAttributeOptional = abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY);
            var questAttributeOptional = abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE);
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
                    if (tierableAbility.getSkill().isPresent()) {
                        var skillData = skillHolder.getSkillHolderData(tierableAbility.getSkill().get());
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

    public void startUpgradeQuest(@NotNull TierableAbility tierableAbility) {
        var abilityDataOptional = skillHolder.getAbilityData(tierableAbility);

        if (abilityDataOptional.isEmpty() || abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.ABILITY_QUEST_ATTRIBUTE).isEmpty()
                || abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY).isEmpty()) {
            throw new IllegalArgumentException("Expected ability quest data for ability " + tierableAbility.getDisplayName());
        }
        int tier = (int) abilityDataOptional.get().getAbilityAttribute(AbilityAttributeManager.ABILITY_TIER_ATTRIBUTE_KEY).get().getContent() + 1;
        Quest quest = tierableAbility.getUpgradeQuestForTier(tier);
        abilityDataOptional.get().addAttribute(new AbilityUpgradeQuestAttribute(quest.getUUID()));
        QuestManager questManager = McRPG.getInstance().getQuestManager();
        skillHolder.setUpgradePoints(skillHolder.getUpgradePoints() - tierableAbility.getUpgradeCostForTier(tier));
        questManager.addActiveQuest(quest);
        questManager.addHolderToQuest(questHolder, quest);
        quest.startQuest();
    }
}
