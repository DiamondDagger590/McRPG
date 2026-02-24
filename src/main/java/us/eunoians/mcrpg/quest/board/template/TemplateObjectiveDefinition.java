package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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
 */
public record TemplateObjectiveDefinition(
        @NotNull NamespacedKey typeKey,
        @NotNull String requiredProgressExpression,
        @NotNull Map<String, Object> config
) {

    public TemplateObjectiveDefinition {
        config = Map.copyOf(config);
    }
}
