package us.eunoians.mcrpg.quest.board.template.condition;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.board.template.ResolvedVariableContext;

import java.util.Random;
import java.util.UUID;

/**
 * Unified context passed to every {@link TemplateCondition#evaluate} call. Different
 * evaluation sites populate different fields — conditions handle missing fields
 * gracefully by returning a context-appropriate default.
 *
 * @param rolledRarity      the rarity rolled during template generation (null if not in generation context)
 * @param rarityRegistry    rarity registry for weight comparisons (null if not available)
 * @param random            seeded random for deterministic chance evaluation (null if not in generation context)
 * @param resolvedVariables resolved template variable values (null if not in generation context)
 * @param playerUUID        the player UUID for player-dependent conditions (null for shared generation)
 * @param completionHistory query interface for player completion data (null if not available)
 */
public record ConditionContext(
        @Nullable NamespacedKey rolledRarity,
        @Nullable QuestRarityRegistry rarityRegistry,
        @Nullable Random random,
        @Nullable ResolvedVariableContext resolvedVariables,
        @Nullable UUID playerUUID,
        @Nullable QuestCompletionHistory completionHistory
) {

    /**
     * Context for template generation (shared offerings). Has rarity, random, and
     * resolved variables. No player data — player-dependent conditions return true
     * (include by default) so they don't inadvertently filter shared content.
     */
    @NotNull
    public static ConditionContext forTemplateGeneration(
            @NotNull NamespacedKey rarity, @NotNull QuestRarityRegistry registry,
            @NotNull Random random, @NotNull ResolvedVariableContext vars) {
        return new ConditionContext(rarity, registry, random, vars, null, null);
    }

    /**
     * Context for personal offering generation. Has everything template generation
     * has, plus the player UUID and their completion history for prerequisite
     * and player-dependent conditions.
     */
    @NotNull
    public static ConditionContext forPersonalGeneration(
            @NotNull NamespacedKey rarity, @NotNull QuestRarityRegistry registry,
            @NotNull Random random, @NotNull ResolvedVariableContext vars,
            @NotNull UUID playerUUID, @NotNull QuestCompletionHistory history) {
        return new ConditionContext(rarity, registry, random, vars, playerUUID, history);
    }

    /**
     * Context for prerequisite evaluation (template/category eligibility).
     * Has player UUID and completion history. No generation-specific fields.
     */
    @NotNull
    public static ConditionContext forPrerequisiteCheck(
            @NotNull UUID playerUUID, @NotNull QuestCompletionHistory history) {
        return new ConditionContext(null, null, null, null, playerUUID, history);
    }

    /**
     * Context for reward grant-time evaluation (fallback reward checks).
     * Has the player UUID and optionally the quest's rarity key.
     */
    @NotNull
    public static ConditionContext forRewardGrant(
            @NotNull UUID playerUUID, @Nullable NamespacedKey rarity,
            @Nullable QuestRarityRegistry registry) {
        return new ConditionContext(rarity, registry, null, null, playerUUID, null);
    }
}
