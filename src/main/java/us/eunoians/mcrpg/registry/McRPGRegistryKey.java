package us.eunoians.mcrpg.registry;

import com.diamonddagger590.mccore.registry.Registry;
import com.diamonddagger590.mccore.registry.RegistryKey;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.combat.state.CombatStateTypeRegistry;
import us.eunoians.mcrpg.quest.board.category.BoardSlotCategoryRegistry;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionTypeRegistry;
import us.eunoians.mcrpg.quest.board.refresh.RefreshTypeRegistry;
import us.eunoians.mcrpg.quest.board.scope.ScopedBoardAdapterRegistry;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProviderRegistry;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.quest.source.QuestSourceRegistry;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.combat.condition.CombatConditionRegistry;
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
    /**
     * Retrieves the {@link CombatConditionRegistry}, holding every registered
     * {@link us.eunoians.mcrpg.combat.condition.CombatCondition}.
     * <p>
     * Safe operations: {@link CombatConditionRegistry#register(us.eunoians.mcrpg.combat.condition.CombatCondition)},
     * {@link CombatConditionRegistry#unregister(org.bukkit.NamespacedKey)},
     * {@link CombatConditionRegistry#get(org.bukkit.NamespacedKey)}, {@link CombatConditionRegistry#getAll()}.
     * <p>
     * Registering a condition here only adds it to the registry — it does not begin evaluating it.
     * Third-party expansions that register conditions via a
     * {@link us.eunoians.mcrpg.expansion.content.CombatConditionContentPack} have the periodic
     * evaluation task started automatically by the content processor. Code that registers a condition
     * directly at runtime must also call
     * {@link us.eunoians.mcrpg.combat.CombatTrackerManager#startConditionTask(us.eunoians.mcrpg.combat.condition.CombatCondition)}
     * (and {@link us.eunoians.mcrpg.combat.CombatTrackerManager#stopConditionTask(org.bukkit.NamespacedKey)}
     * before unregistering) so the condition is actually polled.
     */
    RegistryKey<CombatConditionRegistry> COMBAT_CONDITION = create(CombatConditionRegistry.class);
    /**
     * Retrieves the {@link CombatStateTypeRegistry}, holding every registered
     * {@link us.eunoians.mcrpg.combat.state.CombatStateType}.
     * <p>
     * Safe operations: {@link CombatStateTypeRegistry#register(us.eunoians.mcrpg.combat.state.CombatStateType)},
     * {@link CombatStateTypeRegistry#unregister(org.bukkit.NamespacedKey)},
     * {@link CombatStateTypeRegistry#get(org.bukkit.NamespacedKey)}, {@link CombatStateTypeRegistry#getAll()}.
     * <p>
     * Registration is what makes a type's persistence and end-of-session resolution take effect;
     * session-scoped reads and writes work on unregistered types too. Note that
     * {@link us.eunoians.mcrpg.event.combat.CombatStateChangeEvent} is not fired when persistent
     * state is re-attached at session start or cleared at session end — see that event's Javadoc.
     */
    RegistryKey<CombatStateTypeRegistry> COMBAT_STATE_TYPE = create(CombatStateTypeRegistry.class);
}
