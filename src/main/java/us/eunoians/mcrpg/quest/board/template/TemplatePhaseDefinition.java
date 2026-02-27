package us.eunoians.mcrpg.quest.board.template;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;

import java.util.List;
import java.util.Optional;

/**
 * Lightweight intermediate structure holding a template's phase definition
 * before variable substitution. Differs from {@link us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition}
 * because it may contain unresolved variable references.
 *
 * @param completionMode how stages within this phase must be completed
 * @param stages         the stages in this phase
 * @param condition      optional condition evaluated during generation to include/exclude this phase
 */
public record TemplatePhaseDefinition(
        @NotNull PhaseCompletionMode completionMode,
        @NotNull List<TemplateStageDefinition> stages,
        @Nullable TemplateCondition condition
) {

    public TemplatePhaseDefinition {
        stages = List.copyOf(stages);
    }

    /**
     * Backward-compatible constructor without condition.
     */
    public TemplatePhaseDefinition(@NotNull PhaseCompletionMode completionMode,
                                   @NotNull List<TemplateStageDefinition> stages) {
        this(completionMode, stages, null);
    }

    @NotNull
    public Optional<TemplateCondition> getCondition() {
        return Optional.ofNullable(condition);
    }

    /**
     * Returns a copy of this phase with the given stages list.
     */
    @NotNull
    public TemplatePhaseDefinition withStages(@NotNull List<TemplateStageDefinition> newStages) {
        return new TemplatePhaseDefinition(completionMode, newStages, condition);
    }
}
