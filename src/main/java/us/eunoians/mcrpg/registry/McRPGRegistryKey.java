package us.eunoians.mcrpg.registry;

import com.diamonddagger590.mccore.registry.Registry;
import com.diamonddagger590.mccore.registry.RegistryKey;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategoryRegistry;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionTypeRegistry;
import us.eunoians.mcrpg.quest.board.refresh.RefreshTypeRegistry;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProviderRegistry;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.source.QuestSourceRegistry;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;

import static com.diamonddagger590.mccore.registry.RegistryKeyImpl.create;

/**
 * A soft enum of different {@link RegistryKey}s supported by McRPG.
 * <p>
 * To use these, you will need access to the {@link com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry}
 * via {@link com.diamonddagger590.mccore.registry.RegistryAccess#registry(RegistryKey)} and pass in whatever key
 * you want to get the {@link Registry} for.
 */
public interface McRPGRegistryKey extends RegistryKey<Registry<?>> {

    RegistryKey<AbilityRegistry> ABILITY = create(AbilityRegistry.class);
    RegistryKey<SkillRegistry> SKILL = create(SkillRegistry.class);
    RegistryKey<AbilityAttributeRegistry> ABILITY_ATTRIBUTE = create(AbilityAttributeRegistry.class);
    RegistryKey<ExperienceModifierRegistry> EXPERIENCE_MODIFIER = create(ExperienceModifierRegistry.class);
    RegistryKey<QuestDefinitionRegistry> QUEST_DEFINITION = create(QuestDefinitionRegistry.class);
    RegistryKey<QuestScopeProviderRegistry> QUEST_SCOPE_PROVIDER = create(QuestScopeProviderRegistry.class);
    RegistryKey<QuestObjectiveTypeRegistry> QUEST_OBJECTIVE_TYPE = create(QuestObjectiveTypeRegistry.class);
    RegistryKey<QuestRewardTypeRegistry> QUEST_REWARD_TYPE = create(QuestRewardTypeRegistry.class);
    RegistryKey<QuestSourceRegistry> QUEST_SOURCE = create(QuestSourceRegistry.class);
    RegistryKey<QuestRarityRegistry> QUEST_RARITY = create(QuestRarityRegistry.class);
    RegistryKey<BoardSlotCategoryRegistry> BOARD_SLOT_CATEGORY = create(BoardSlotCategoryRegistry.class);
    RegistryKey<RefreshTypeRegistry> REFRESH_TYPE = create(RefreshTypeRegistry.class);
    RegistryKey<QuestTemplateRegistry> QUEST_TEMPLATE = create(QuestTemplateRegistry.class);
    RegistryKey<RewardDistributionTypeRegistry> REWARD_DISTRIBUTION_TYPE = create(RewardDistributionTypeRegistry.class);
}
