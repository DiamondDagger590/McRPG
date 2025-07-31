package us.eunoians.mcrpg.builder.item.ability;

import com.diamonddagger590.mccore.pair.ImmutablePair;
import com.diamonddagger590.mccore.pair.Pair;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.Methods;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A helper class that provides the functionality to return lore
 * that should be appended to ability items conditionally.
 */
public final class AbilityLoreAppender {

    /**
     * Returns a {@link Pair} containing information needed to append to ability item lore.
     * <p>
     * The left side of the pair contains a {@link List} representing the lore that should be appended.
     * The right side of the pair contains a {@link Map} of placeholders needed to support the lore. The key
     * of this map is the placeholder and the value is the value to replace the placeholder with.
     *
     * @param mcRPGPlayer The player to use as context when generating the lore.
     * @param ability     The ability to use as context when generating the lore.
     * @return A {@link Pair} containing information needed to append to ability item lore.
     */
    @NotNull
    public static Pair<List<String>, Map<String, String>> getAppendLore(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability) {
        SkillHolder skillHolder = mcRPGPlayer.asSkillHolder();
        McRPGLocalizationManager localizationManager = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        SkillRegistry skillRegistry = mcRPGPlayer.getPlugin().registryAccess().registry(McRPGRegistryKey.SKILL);
        Optional<AbilityData> abilityDataOptional = skillHolder.getAbilityData(ability);
        List<String> lore = new ArrayList<>();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("ability", ability.getName(mcRPGPlayer));

        if (abilityDataOptional.isPresent()) {
            AbilityData abilityData = abilityDataOptional.get();
            if (ability instanceof TierableAbility tierableAbility) {
                // Check if it's unlocked
                if (abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE)
                        .map(value -> value instanceof AbilityUnlockedAttribute attribute && attribute.getContent()).orElse(true)) {
                    var abilityQuestOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
                    // If there is an active quest
                    if (abilityQuestOptional.isPresent() && abilityQuestOptional.get() instanceof AbilityUpgradeQuestAttribute questAttribute && questAttribute.shouldContentBeSaved()) {
                        QuestManager questManager = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
                        var questOptional = questManager.getActiveQuest(questAttribute.getContent());
                        if (questOptional.isPresent()) {
                            lore.add("");
                            lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.QUEST_PROGRESS_LORE));
                            placeholders.put("quest-progress", Methods.getProgressBarAsString(questOptional.get().getQuestProgress(), 20));
                        } else {
                            throw new IllegalArgumentException("The ability quest for ability " + ability.getName() + " was not found.");
                        }
                    } else {
                        abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY).ifPresent(abilityAttribute -> {
                            if (abilityAttribute instanceof AbilityTierAttribute abilityTierAttribute) {
                                int tier = abilityTierAttribute.getContent();
                                int nextTier = tier + 1;
                                int upgradeCost = tierableAbility.getUpgradeCostForTier(nextTier);
                                // If the ability isn't the max tier
                                if (tierableAbility.getMaxTier() > tier) {
                                    // If the ability has a skill it belongs to
                                    if (tierableAbility instanceof SkillAbility skillAbility) {
                                        var skillDataOptional = skillHolder.getSkillHolderData(skillAbility.getSkillKey());
                                        if (skillDataOptional.isPresent()) {
                                            Skill skill = skillRegistry.getRegisteredSkill(skillAbility.getSkillKey());
                                            int currentLevel = skillDataOptional.get().getCurrentLevel();
                                            // If the current skill level is above the unlock level
                                            if (currentLevel >= tierableAbility.getUnlockLevelForTier(nextTier)) {
                                                // If they have enough upgrade points, tell them they can click
                                                if (skillHolder.getUpgradePoints() >= upgradeCost) {
                                                    lore.add("");
                                                    lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.CLICK_TO_START_UPGRADE_QUEST_LORE));
                                                    placeholders.put("next-tier-ability-points", Integer.toString(upgradeCost));
                                                }
                                                // If they don't have enough, tell them how many they need
                                                else {
                                                    lore.add("");
                                                    lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.NOT_ENOUGH_ABILITY_POINTS_TO_START_QUEST_LORE));
                                                    placeholders.put("next-tier-ability-points", Integer.toString(upgradeCost));
                                                }
                                                lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.ABILITY_POINT_COUNT_LORE));
                                                placeholders.put(AbilityItemPlaceholderKeys.ABILITY_POINT_COUNT.getKey(), Integer.toString(skillHolder.getUpgradePoints()));
                                                placeholders.put(AbilityItemPlaceholderKeys.SKILL.getKey(), skill.getName(mcRPGPlayer));

                                            }
                                            // Otherwise, tell the player the level they need to reach
                                            else {
                                                lore.add("");
                                                lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.UPGRADE_LOCKED_BEHIND_LEVELUP_LORE));
                                                placeholders.put("next-tier-level", Integer.toString(tierableAbility.getUnlockLevelForTier(nextTier)));
                                                placeholders.put(AbilityItemPlaceholderKeys.SKILL.getKey(), skill.getName(mcRPGPlayer));

                                            }
                                        }
                                    }
                                    // If the ability doesn't have a skill, we only care about upgrade cost
                                    else {
                                        // If they have enough upgrade points, tell them they can click
                                        if (skillHolder.getUpgradePoints() >= upgradeCost) {
                                            lore.add("");
                                            lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.CLICK_TO_START_UPGRADE_QUEST_LORE));
                                            placeholders.put("next-tier-ability-points", Integer.toString(upgradeCost));
                                        }
                                        // If they don't have enough, tell them how many they need
                                        else {
                                            lore.add("");
                                            lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.NOT_ENOUGH_ABILITY_POINTS_TO_START_QUEST_LORE));
                                            placeholders.put("next-tier-ability-points", Integer.toString(upgradeCost));
                                        }
                                        lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.ABILITY_POINT_COUNT_LORE));
                                        placeholders.put(AbilityItemPlaceholderKeys.ABILITY_POINT_COUNT.getKey(), Integer.toString(skillHolder.getUpgradePoints()));
                                    }
                                }
                            }
                        });
                    }
                } else {
                    lore.add("");
                    lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.ABILITY_LOCKED_LORE));
                }
            }
            if (ability.getExpansionKey().isPresent()) {
                var expansionOptional = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.CONTENT_EXPANSION)
                        .getContentExpansion(ability.getExpansionKey().get());
                if (expansionOptional.isPresent()) {
                    lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.EXPANSION_PACK_LORE));
                    placeholders.put(AbilityItemPlaceholderKeys.EXPANSION_PACK.getKey(), expansionOptional.get().getExpansionName(mcRPGPlayer));
                }
            }
        }
        return ImmutablePair.of(lore, placeholders);
    }
}
