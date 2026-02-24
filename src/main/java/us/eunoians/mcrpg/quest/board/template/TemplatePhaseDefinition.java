package us.eunoians.mcrpg.quest.board.template;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;

import java.util.List;

/**
 * Lightweight intermediate structure holding a template's phase definition
 * before variable substitution. Differs from {@link us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition}
 * because it may contain unresolved variable references.
 *
 * @param completionMode how stages within this phase must be completed
 * @param stages         the stages in this phase
 */
public record TemplatePhaseDefinition(
        @NotNull PhaseCompletionMode completionMode,
        @NotNull List<TemplateStageDefinition> stages
) {

    public TemplatePhaseDefinition {
        stages = List.copyOf(stages);
    }
}
