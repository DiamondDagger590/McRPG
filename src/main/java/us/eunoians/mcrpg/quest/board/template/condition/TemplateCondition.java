package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

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
 * {@link #fromConfig(Section)} factory method that parses condition-specific parameters.
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
     *
     * @param section the YAML section containing condition-specific parameters
     * @return a configured condition instance
     */
    @NotNull
    TemplateCondition fromConfig(@NotNull Section section);
}
