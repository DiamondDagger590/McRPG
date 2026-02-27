package us.eunoians.mcrpg.quest.board.template.condition;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Utility for parsing {@link TemplateCondition} instances from YAML sections.
 * Supports both shorthand syntax for built-in conditions and explicit {@code type:}
 * key syntax for any registered condition type.
 */
public final class ConditionParser {

    private static final Logger LOGGER = Logger.getLogger(ConditionParser.class.getName());

    private ConditionParser() {}

    /**
     * Parses a condition from a section containing a {@code condition:} block.
     * Returns null if no condition block is present.
     */
    @Nullable
    public static TemplateCondition parseConditionBlock(@NotNull Section parent) {
        if (!parent.contains("condition")) {
            return null;
        }
        Section condSection = parent.getSection("condition");
        return parseSingle(condSection);
    }

    /**
     * Parses a prerequisite from a section containing a {@code prerequisite:} block.
     * Returns null if no prerequisite block is present.
     */
    @Nullable
    public static TemplateCondition parsePrerequisiteBlock(@NotNull Section parent) {
        if (!parent.contains("prerequisite")) {
            return null;
        }
        Section prereqSection = parent.getSection("prerequisite");
        return parseSingle(prereqSection);
    }

    /**
     * Parses a single condition from a YAML section. Tries shorthand keys first,
     * then falls back to explicit {@code type:} key resolution.
     */
    @NotNull
    public static TemplateCondition parseSingle(@NotNull Section section) {
        // Compound conditions
        if (section.contains("all")) {
            return parseCompound(section, CompoundCondition.LogicMode.ALL);
        }
        if (section.contains("any")) {
            return parseCompound(section, CompoundCondition.LogicMode.ANY);
        }

        // Shorthand: rarity-at-least
        if (section.contains("rarity-at-least")) {
            NamespacedKey rarityKey = parseNamespacedKey(section.getString("rarity-at-least"));
            return new RarityCondition(rarityKey);
        }

        // Shorthand: chance
        if (section.contains("chance")) {
            return new ChanceCondition(section.getDouble("chance"));
        }

        // Shorthand: variable check
        if (section.contains("variable")) {
            Section varSection = section.getSection("variable");
            return parseVariableCondition(varSection);
        }

        // Shorthand: permission
        if (section.contains("permission")) {
            return new PermissionCondition(section.getString("permission"));
        }

        // Shorthand: min-completions (completion prerequisite)
        if (section.contains("min-completions")) {
            return parseCompletionPrerequisite(section);
        }

        // Explicit type: key
        if (section.contains("type")) {
            return parseExplicitType(section);
        }

        throw new IllegalArgumentException("Unrecognized condition format in section: " + section.getRouteAsString());
    }

    @NotNull
    private static TemplateCondition parseCompound(@NotNull Section section, @NotNull CompoundCondition.LogicMode mode) {
        String key = mode == CompoundCondition.LogicMode.ALL ? "all" : "any";
        Section children = section.getSection(key);
        Map<String, TemplateCondition> parsed = new LinkedHashMap<>();
        for (String label : children.getRoutesAsStrings(false)) {
            parsed.put(label, parseSingle(children.getSection(label)));
        }
        return new CompoundCondition(parsed, mode);
    }

    @NotNull
    private static VariableCondition parseVariableCondition(@NotNull Section section) {
        String name = section.getString("name");
        VariableCheck check = parseVariableCheck(section);
        return new VariableCondition(name, check);
    }

    /**
     * Parses a {@link VariableCheck} from a variable condition section.
     */
    @NotNull
    static VariableCheck parseVariableCheck(@NotNull Section section) {
        if (section.contains("contains-any")) {
            return new VariableCheck.ContainsAny(section.getStringList("contains-any"));
        }
        if (section.contains("greater-than")) {
            return new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN,
                    section.getDouble("greater-than"));
        }
        if (section.contains("less-than")) {
            return new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN,
                    section.getDouble("less-than"));
        }
        if (section.contains("at-least")) {
            return new VariableCheck.NumericComparison(ComparisonOperator.GREATER_THAN_OR_EQUAL,
                    section.getDouble("at-least"));
        }
        if (section.contains("at-most")) {
            return new VariableCheck.NumericComparison(ComparisonOperator.LESS_THAN_OR_EQUAL,
                    section.getDouble("at-most"));
        }
        throw new IllegalArgumentException("Variable condition has no recognized check at: " + section.getRouteAsString());
    }

    @NotNull
    private static CompletionPrerequisiteCondition parseCompletionPrerequisite(@NotNull Section section) {
        int count = section.getInt("min-completions");
        NamespacedKey category = section.contains("category")
                ? parseNamespacedKey(section.getString("category"))
                : null;
        NamespacedKey rarity = section.contains("min-rarity")
                ? parseNamespacedKey(section.getString("min-rarity"))
                : null;
        return new CompletionPrerequisiteCondition(count, category, rarity);
    }

    @NotNull
    private static TemplateCondition parseExplicitType(@NotNull Section section) {
        String typeStr = section.getString("type");
        NamespacedKey typeKey = NamespacedKey.fromString(typeStr.toLowerCase());
        if (typeKey == null) {
            throw new IllegalArgumentException("Invalid condition type key: " + typeStr);
        }
        Optional<TemplateCondition> registered = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.TEMPLATE_CONDITION)
                .get(typeKey);
        if (registered.isEmpty()) {
            throw new IllegalArgumentException("Unregistered condition type: " + typeKey);
        }
        return registered.get().fromConfig(section);
    }

    @Nullable
    private static NamespacedKey parseNamespacedKey(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        if (input.contains(":")) {
            return NamespacedKey.fromString(input.toLowerCase());
        }
        return new NamespacedKey(McRPGMethods.getMcRPGNamespace(), input.toLowerCase());
    }
}
