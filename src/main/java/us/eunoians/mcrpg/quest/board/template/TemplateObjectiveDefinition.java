package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;

import java.util.Map;
import java.util.Optional;

/**
 * An objective definition within a {@link TemplateStageDefinition}.
 * <p>
 * The {@code config} map holds raw configuration that may contain variable
 * references. For example, {@code blocks: target_blocks} means "substitute
 * the resolved value of the {@code target_blocks} pool variable here".
 * The {@code requiredProgressExpression} may also reference template variables
 * (e.g., {@code "block_count"}).
 *
 * @param label                      the YAML section key used to identify this objective in the template
 *                                   (e.g., {@code "kill_mobs"}); preserved so the template engine can
 *                                   remap {@code inlineDisplay} objective entries after key generation
 * @param typeKey                    the objective type key (e.g., {@code mcrpg:block_break})
 * @param requiredProgressExpression expression string for required progress, may reference variables
 * @param config                     raw config map, may contain variable references as string values
 * @param condition                  optional condition evaluated during generation to include/exclude this objective
 * @param weight                     weight for weighted random selection (default 1)
 */
public record TemplateObjectiveDefinition(
        @NotNull String label,
        @NotNull NamespacedKey typeKey,
        @NotNull String requiredProgressExpression,
        @NotNull Map<String, Object> config,
        @Nullable TemplateCondition condition,
        int weight
) {

    /** Canonical constructor — makes {@code config} immutable and clamps {@code weight} to at least 1. */
    public TemplateObjectiveDefinition {
        config = Map.copyOf(config);
        if (weight < 1) {
            weight = 1;
        }
    }

    /**
     * Backward-compatible constructor without condition and weight.
     */
    public TemplateObjectiveDefinition(@NotNull NamespacedKey typeKey,
                                       @NotNull String requiredProgressExpression,
                                       @NotNull Map<String, Object> config) {
        this("", typeKey, requiredProgressExpression, config, null, 1);
    }

    /**
     * Returns the optional generation-time condition that must evaluate to {@code true}
     * for this objective to be included in the generated quest.
     *
     * @return the condition, or empty if this objective is unconditional
     */
    @NotNull
    public Optional<TemplateCondition> getCondition() {
        return Optional.ofNullable(condition);
    }

    /**
     * Returns the relative selection weight used by {@link WeightedObjectiveSelector}.
     * Always {@code >= 1}.
     *
     * @return the selection weight
     */
    public int getWeight() {
        return weight;
    }
}
