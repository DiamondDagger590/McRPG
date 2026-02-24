package us.eunoians.mcrpg.quest.board.template.variable;

import org.jetbrains.annotations.NotNull;

/**
 * A variable declared in a quest template that is resolved at generation time.
 * Variables come in two flavors: {@link VariableType#POOL POOL} (selects values
 * from weighted pools) and {@link VariableType#RANGE RANGE} (picks a number
 * within a min/max range scaled by difficulty).
 */
public interface TemplateVariable {

    /** The two flavors of template variable. */
    enum VariableType { POOL, RANGE }

    /**
     * Returns the type discriminator for this variable.
     *
     * @return {@link VariableType#POOL} or {@link VariableType#RANGE}
     */
    @NotNull
    VariableType getType();

    /**
     * Returns the variable name as declared in the template YAML. This name is
     * used to reference the variable in expressions (e.g. {@code {my_variable}}).
     *
     * @return the variable name
     */
    @NotNull
    String getName();
}
