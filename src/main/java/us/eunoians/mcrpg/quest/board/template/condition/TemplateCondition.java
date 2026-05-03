package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Map;

/**
 * A condition that can be attached to any template element (phase, stage, or objective).
 * Evaluated during {@code QuestTemplateEngine.generate()} to determine whether the element
 * is included in the generated {@code QuestDefinition}.
 * <p>
 * The interface is extensible — third-party plugins can register custom condition types via
 * {@link TemplateConditionRegistry} and the content expansion system, following the same
 * pattern as {@code RewardDistributionType}, {@code QuestObjectiveType}, and {@code QuestRewardType}.
 * <p>
 * Each condition type is identified by a {@link NamespacedKey} and must provide a
 * {@link #fromConfig(Section, ConditionParser)} factory method that parses condition-specific parameters.
 * <p>
 * <b>Prototype registration pattern:</b> One instance per condition type is registered in the
 * {@link TemplateConditionRegistry} as a prototype. This instance is <em>never evaluated</em> —
 * its sole purpose is to serve as a factory delegate: the {@link TemplateConditionRegistry} looks
 * it up by key and calls {@link #fromConfig} to produce a new, properly configured instance.
 * Implementations should expose a public no-arg constructor (with placeholder field values) for
 * registry registration, and a separate public or package-private constructor that accepts the
 * configuration parameters needed for evaluation. See {@code BlockBreakObjectiveType} for the
 * canonical example of this two-constructor pattern.
 */
public interface TemplateCondition extends McRPGContent {

    /**
     * The unique key identifying this condition type (e.g., {@code mcrpg:rarity_gate}).
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Evaluates this condition against the current generation context.
     *
     * @param context the template generation context (rarity, random, resolved variables)
     * @return true if the condition is met and the element should be included
     */
    boolean evaluate(@NotNull ConditionContext context);

    /**
     * Creates a configured instance of this condition type from the given YAML section.
     * Called during template parsing to construct condition instances from config.
     * <p>
     * The {@code parser} argument is provided so that compound or recursive conditions
     * (e.g. {@link CompoundCondition}) can delegate sub-condition parsing back through
     * the same parser instance rather than calling static helpers directly.
     *
     * @param section the YAML section containing condition-specific parameters
     * @param parser  the active condition parser (for recursive/nested parsing)
     * @return a configured condition instance
     */
    @NotNull
    TemplateCondition fromConfig(@NotNull Section section, @NotNull ConditionParser parser);

    /**
     * Serializes this configured condition's state to a map that can be stored as JSON
     * and later reconstructed via {@link #fromConfig(Section)} using the
     * {@code GeneratedQuestDefinitionCodec} bridge. The keys in the returned map
     * must match what {@link #fromConfig} expects to read from its {@link Section}.
     * <p>
     * Implementations must not include a {@code "type"} key — the serializer injects
     * the type discriminator automatically when needed (e.g., inside a
     * {@link CompoundCondition}).
     * <p>
     * The default returns an empty map, which is suitable for stateless conditions
     * with no configuration parameters. Third-party implementations with configuration
     * state must override this method.
     *
     * @return a serializable map of configuration key-value pairs
     */
    @NotNull
    default Map<String, Object> serializeConfig() {
        return Map.of();
    }
}
