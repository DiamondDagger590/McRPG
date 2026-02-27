package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Combines multiple conditions with {@code ALL} (AND) or {@code ANY} (OR) logic.
 * Child conditions are stored as a named map keyed by arbitrary human-readable labels.
 */
public final class CompoundCondition implements TemplateCondition {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "compound");

    private final Map<String, TemplateCondition> conditions;
    private final LogicMode mode;

    public enum LogicMode { ALL, ANY }

    public CompoundCondition(@NotNull Map<String, TemplateCondition> conditions, @NotNull LogicMode mode) {
        this.conditions = Map.copyOf(conditions);
        this.mode = mode;
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("CompoundCondition requires at least one child condition");
        }
    }

    @Override
    public boolean evaluate(@NotNull ConditionContext context) {
        return switch (mode) {
            case ALL -> conditions.values().stream().allMatch(c -> c.evaluate(context));
            case ANY -> conditions.values().stream().anyMatch(c -> c.evaluate(context));
        };
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
        LogicMode parsedMode = section.contains("all") ? LogicMode.ALL : LogicMode.ANY;
        String key = parsedMode == LogicMode.ALL ? "all" : "any";
        Section children = section.getSection(key);
        Map<String, TemplateCondition> parsed = new LinkedHashMap<>();
        for (String label : children.getRoutesAsStrings(false)) {
            parsed.put(label, ConditionParser.parseSingle(children.getSection(label)));
        }
        return new CompoundCondition(parsed, parsedMode);
    }

    @NotNull
    public Map<String, TemplateCondition> getConditions() {
        return conditions;
    }

    @NotNull
    public LogicMode getMode() {
        return mode;
    }
}
