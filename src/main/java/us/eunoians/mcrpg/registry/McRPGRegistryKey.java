package us.eunoians.mcrpg.registry;

import com.diamonddagger590.mccore.registry.Registry;
import com.diamonddagger590.mccore.registry.RegistryKey;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategoryRegistry;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionTypeRegistry;
import us.eunoians.mcrpg.quest.board.refresh.RefreshTypeRegistry;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.quest.chain.trigger.ChainAutoStartTriggerRegistry;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProviderRegistry;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.source.QuestSourceRegistry;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.stat.PlayerStatRegistry;

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
    RegistryKey<ScopedBoardAdapterRegistry> SCOPED_BOARD_ADAPTER = create(ScopedBoardAdapterRegistry.class);
    RegistryKey<TemplateConditionRegistry> TEMPLATE_CONDITION = create(TemplateConditionRegistry.class);
    /**
     * Retrieves the {@link QuestChainRegistry} containing all registered
     * {@link us.eunoians.mcrpg.quest.chain.QuestChainDefinition} blueprints.
     * <p>
     * Chain definitions are loaded from YAML via {@code QuestChainConfigLoader} and registered
     * during startup. Third-party chains can be registered via
     * {@link us.eunoians.mcrpg.expansion.content.QuestChainContentPack} in a
     * {@link us.eunoians.mcrpg.expansion.ContentExpansion}.
     */
    RegistryKey<QuestChainRegistry> QUEST_CHAIN = create(QuestChainRegistry.class);

    /**
     * Retrieves the {@link ChainAutoStartTriggerRegistry} containing all registered
     * {@link us.eunoians.mcrpg.quest.chain.trigger.ChainAutoStartTrigger} types.
     * <p>
     * Built-in triggers: {@code mcrpg:manual}, {@code mcrpg:first_join}, {@code mcrpg:login}.
     * Third-party triggers can be registered via
     * {@link us.eunoians.mcrpg.expansion.content.ChainAutoStartTriggerContentPack} in a
     * {@link us.eunoians.mcrpg.expansion.ContentExpansion}.
     */
    RegistryKey<ChainAutoStartTriggerRegistry> CHAIN_AUTO_START_TRIGGER = create(ChainAutoStartTriggerRegistry.class);

    /**
     * Retrieves the {@link PlayerStatRegistry} containing all registered
     * {@link us.eunoians.mcrpg.stat.PlayerStat} definitions.
     * <p>
     * Safe operations: {@link PlayerStatRegistry#register(us.eunoians.mcrpg.stat.PlayerStat)},
     * {@link PlayerStatRegistry#getStat(org.bukkit.NamespacedKey)}, {@link PlayerStatRegistry#allStats()}.
     * <p>
     * Stats must be registered during expansion processing (via
     * {@link us.eunoians.mcrpg.expansion.content.PlayerStatContentPack}), before any
     * {@link us.eunoians.mcrpg.stat.instance.PlayerStatData} instances are created (i.e.,
     * before any player joins). Registering stats after a player has already joined will not
     * retroactively create a {@link us.eunoians.mcrpg.stat.instance.PlayerStatInstance} for that player.
     */
    RegistryKey<PlayerStatRegistry> PLAYER_STAT = create(PlayerStatRegistry.class);
}
