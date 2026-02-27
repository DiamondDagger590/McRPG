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
 * @param typeKey                    the objective type key (e.g., {@code mcrpg:block_break})
 * @param requiredProgressExpression expression string for required progress, may reference variables
 * @param config                     raw config map, may contain variable references as string values
 * @param condition                  optional condition evaluated during generation to include/exclude this objective
 * @param weight                     weight for weighted random selection (default 1)
 */
public record TemplateObjectiveDefinition(
        @NotNull NamespacedKey typeKey,
        @NotNull String requiredProgressExpression,
        @NotNull Map<String, Object> config,
        @Nullable TemplateCondition condition,
        int weight
) {

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
        this(typeKey, requiredProgressExpression, config, null, 1);
    }

    @NotNull
    public Optional<TemplateCondition> getCondition() {
        return Optional.ofNullable(condition);
    }

    public int getWeight() {
        return weight;
    }
}
