package us.eunoians.mcrpg.builder.item.ability;

import com.diamonddagger590.mccore.pair.ImmutablePair;
import com.diamonddagger590.mccore.pair.Pair;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUpgradeQuestAttribute;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;
import us.eunoians.mcrpg.ability.unlock.builtin.AllOfUnlockConditionType;
import us.eunoians.mcrpg.ability.unlock.builtin.AnyOfUnlockConditionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
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
                if (abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE)
                        .map(value -> value instanceof AbilityUnlockedAttribute attribute && attribute.getContent()).orElse(true)) {
                    var abilityQuestOptional = abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_QUEST_ATTRIBUTE);
                    QuestInstance activeUpgradeQuest = null;
                    if (abilityQuestOptional.isPresent() && abilityQuestOptional.get() instanceof AbilityUpgradeQuestAttribute questAttribute && questAttribute.shouldContentBeSaved()) {
                        QuestManager questManager = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
                        activeUpgradeQuest = questManager.getActiveQuestsForPlayer(mcRPGPlayer.getUUID()).stream()
                                .filter(q -> q.getQuestUUID().equals(questAttribute.getContent()))
                                .findFirst()
                                .orElse(null);
                    }
                    if (activeUpgradeQuest != null) {
                        lore.add("");
                        lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.QUEST_PROGRESS_LORE));
                        double overallProgress = activeUpgradeQuest.getOverallProgress();
                        placeholders.put("upgrade-quest-progress", activeUpgradeQuest.getOverallProgressBar(20));
                        placeholders.put("quest-percent", localizationManager.getDisplayDecimalFormatter()
                                .formatDisplayDecimal(mcRPGPlayer, overallProgress * 100, 0, 1));
                    } else {
                        abilityData.getAbilityAttribute(AbilityAttributeRegistry.ABILITY_TIER_ATTRIBUTE_KEY).ifPresent(abilityAttribute -> {
                            if (abilityAttribute instanceof AbilityTierAttribute abilityTierAttribute) {
                                int tier = abilityTierAttribute.getContent();
                                int nextTier = tier + 1;
                                if (tierableAbility.getMaxTier() > tier && tierableAbility instanceof SkillAbility skillAbility) {
                                    var skillDataOptional = skillHolder.getSkillHolderData(skillAbility.getSkillKey());
                                    if (skillDataOptional.isPresent()) {
                                        Skill skill = skillRegistry.getRegisteredSkill(skillAbility.getSkillKey());
                                        int currentLevel = skillDataOptional.get().getCurrentLevel();
                                        if (currentLevel < tierableAbility.getUnlockLevelForTier(nextTier)) {
                                            lore.add("");
                                            lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.UPGRADE_LOCKED_BEHIND_LEVELUP_LORE));
                                            placeholders.put("next-tier-level", Integer.toString(tierableAbility.getUnlockLevelForTier(nextTier)));
                                            placeholders.put(AbilityItemPlaceholderKeys.SKILL.getKey(), skill.getColoredName(mcRPGPlayer));
                                        }
                                    }
                                }
                            }
                        });
                    }
                } else {
                    lore.add("");
                    lore.addAll(renderUnlockConditionLore(mcRPGPlayer, tierableAbility));
                    if (tierableAbility instanceof SkillAbility skillAbility) {
                        Skill skill = skillRegistry.getRegisteredSkill(skillAbility.getSkillKey());
                        placeholders.put(AbilityItemPlaceholderKeys.SKILL.getKey(), skill.getColoredName(mcRPGPlayer));
                    }
                }
            }
            var expansionKeyOptional = ability.getExpansionKey();
            if (expansionKeyOptional.isPresent() && !expansionKeyOptional.get().equals(McRPGExpansion.EXPANSION_KEY)) {
                var expansionOptional = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.CONTENT_EXPANSION)
                        .getContentExpansion(expansionKeyOptional.get());
                if (expansionOptional.isPresent()) {
                    lore.addAll(localizationManager.getLocalizedMessages(mcRPGPlayer, LocalizationKey.EXPANSION_PACK_LORE));
                    placeholders.put(AbilityItemPlaceholderKeys.EXPANSION_PACK.getKey(), expansionOptional.get().getExpansionName(mcRPGPlayer));
                }
            }
        }
        return ImmutablePair.of(lore, placeholders);
    }

    /**
     * Renders the unlock-condition lore for a locked ability as a list of MiniMessage
     * strings, one per lore line. Rendering rules ({@code unlock_condition_system} LLD §7.6):
     * <ul>
     *   <li>A single non-composite condition renders as one line, no OR header.</li>
     *   <li>A single composite condition renders its own header + indented bullets.</li>
     *   <li>Multiple top-level conditions render the list header followed by bulleted entries.</li>
     * </ul>
     */
    @NotNull
    private static List<String> renderUnlockConditionLore(@NotNull McRPGPlayer mcRPGPlayer,
                                                          @NotNull UnlockableAbility unlockable) {
        McRPGLocalizationManager localization = mcRPGPlayer.getPlugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        List<UnlockConditionType> conditions = unlockable.getUnlockConditions();
        List<String> lines = new ArrayList<>();
        if (conditions.isEmpty()) {
            return lines;
        }
        if (conditions.size() == 1) {
            Component description = conditions.get(0).getDisplayDescription(mcRPGPlayer);
            appendComponentLines(lines, description, miniMessage);
            return lines;
        }
        lines.add(miniMessage.serialize(localization.getLocalizedMessageAsComponent(
                mcRPGPlayer, LocalizationKey.UNLOCK_CONDITION_LIST_HEADER)));
        Component bullet = localization.getLocalizedMessageAsComponent(
                mcRPGPlayer, LocalizationKey.UNLOCK_CONDITION_BULLET);
        for (UnlockConditionType condition : conditions) {
            if (condition instanceof AllOfUnlockConditionType || condition instanceof AnyOfUnlockConditionType) {
                appendComponentLines(lines, bullet.append(condition.getDisplayDescription(mcRPGPlayer)), miniMessage);
            } else {
                lines.add(miniMessage.serialize(bullet.append(condition.getDisplayLabel(mcRPGPlayer))));
            }
        }
        return lines;
    }

    /**
     * Serializes {@code component} to MiniMessage, splits on newlines, and appends each line
     * to {@code lines}. Composite descriptions are multi-line (header + children) and
     * shouldn't collapse to a single GUI lore item.
     */
    private static void appendComponentLines(@NotNull List<String> lines,
                                             @NotNull Component component,
                                             @NotNull MiniMessage miniMessage) {
        String serialized = miniMessage.serialize(component);
        for (String segment : serialized.split("\n", -1)) {
            lines.add(segment);
        }
    }
}
