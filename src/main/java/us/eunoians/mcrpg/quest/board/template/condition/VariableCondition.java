package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Evaluates to {@code true} if a resolved template variable satisfies the specified check.
 * Enables multi-stage templates where later stages depend on earlier variable rolls.
 */
public final class VariableCondition implements TemplateCondition {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "variable_check");

    private final String variableName;
    private final VariableCheck check;

    public VariableCondition(@NotNull String variableName, @NotNull VariableCheck check) {
        this.variableName = variableName;
        this.check = check;
    }

    @Override
    public boolean evaluate(@NotNull ConditionContext context) {
        if (context.resolvedVariables() == null) {
            return true;
        }
        Object value = context.resolvedVariables().resolvedValues().get(variableName);
        if (value == null) {
            return false;
        }
        return check.test(value);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    @NotNull
    @Override
    public TemplateCondition fromConfig(@NotNull Section section) {
        String name = section.getString("name");
        VariableCheck parsedCheck = ConditionParser.parseVariableCheck(section);
        return new VariableCondition(name, parsedCheck);
    }

    @NotNull
    public String getVariableName() {
        return variableName;
    }

    @NotNull
    public VariableCheck getCheck() {
        return check;
    }
}
