package us.eunoians.mcrpg.quest.objective.type;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

/**
 * Defines a type of quest objective that can be used in quest definitions.
 * <p>
 * Each objective type knows how to:
 * <ul>
 *     <li>Parse its type-specific configuration from YAML, returning a configured copy of itself</li>
 *     <li>Determine whether it can process a given progress context (event)</li>
 *     <li>Calculate how much progress to award for a given event using its internal config state</li>
 * </ul>
 * <p>
 * A base (unconfigured) instance is registered in the {@link QuestObjectiveTypeRegistry}. When a
 * quest definition is parsed, {@link #parseConfig} is called to produce a new configured instance
 * that is stored on the definition. This configured instance is then used at runtime to process
 * progress events.
 * <p>
 * McRPG ships built-in types (block break, mob kill, etc.) and external plugins can register
 * their own types via the objective type registry.
 * <p>
 * Extends {@link McRPGContent} so that objective types can be distributed via the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system. All implementations
 * must provide {@link #getExpansionKey()} identifying which expansion they belong to.
 */
public interface QuestObjectiveType extends McRPGContent {

    /**
     * Gets the unique key identifying this objective type.
     *
     * @return the namespaced key for this type
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Parses type-specific configuration from a YAML section and returns a new configured
     * instance of this type. The returned instance holds the parsed data internally and
     * uses it during {@link #processProgress}.
     *
     * @param section the BoostedYaml section containing type-specific data
     * @return a new configured instance of this objective type
     */
    @NotNull
    QuestObjectiveType parseConfig(@NotNull Section section);

    /**
     * Checks whether this objective type can process the given progress context.
     *
     * @param context the progress context from an event
     * @return {@code true} if this type can handle the given context
     */
    boolean canProcess(@NotNull QuestObjectiveProgressContext context);

    /**
     * Processes the given context and returns the progress delta to apply, using this
     * instance's internal configuration state to determine eligibility.
     *
     * @param instance the objective instance to potentially progress
     * @param context  the progress context from an event
     * @return the amount of progress to add (0 if the context doesn't match)
     */
    long processProgress(@NotNull QuestObjectiveInstance instance,
                         @NotNull QuestObjectiveProgressContext context);

}
