package us.eunoians.mcrpg.quest.board.template;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A stage within a {@link TemplatePhaseDefinition}, containing one or more
 * objective definitions with potentially unresolved variable references.
 *
 * @param objectives the objectives in this stage
 */
public record TemplateStageDefinition(
        @NotNull List<TemplateObjectiveDefinition> objectives
) {

    public TemplateStageDefinition {
        objectives = List.copyOf(objectives);
    }
}
