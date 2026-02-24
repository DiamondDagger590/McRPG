package us.eunoians.mcrpg.quest.objective.type;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A configurable mock implementation of {@link QuestObjectiveType} for use in tests.
 * Allows callers to customise {@link #canProcess} and {@link #processProgress} behaviour.
 */
public class MockQuestObjectiveType implements QuestObjectiveType {

    private final NamespacedKey key;
    private final NamespacedKey expansionKey;
    private Predicate<QuestObjectiveProgressContext> canProcessPredicate;
    private BiFunction<QuestObjectiveInstance, QuestObjectiveProgressContext, Long> progressFunction;

    public MockQuestObjectiveType(@NotNull NamespacedKey key, @NotNull NamespacedKey expansionKey) {
        this.key = key;
        this.expansionKey = expansionKey;
        this.canProcessPredicate = ctx -> true;
        this.progressFunction = (inst, ctx) -> 1L;
    }

    /**
     * Sets the predicate used by {@link #canProcess}.
     *
     * @param predicate returns {@code true} when this type should handle the context
     * @return this instance for chaining
     */
    @NotNull
    public MockQuestObjectiveType withCanProcess(@NotNull Predicate<QuestObjectiveProgressContext> predicate) {
        this.canProcessPredicate = predicate;
        return this;
    }

    /**
     * Sets the function used by {@link #processProgress}.
     *
     * @param function computes the progress delta for a given instance and context
     * @return this instance for chaining
     */
    @NotNull
    public MockQuestObjectiveType withProgressFunction(
            @NotNull BiFunction<QuestObjectiveInstance, QuestObjectiveProgressContext, Long> function) {
        this.progressFunction = function;
        return this;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    @Override
    public QuestObjectiveType parseConfig(@NotNull Section section) {
        return this;
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return canProcessPredicate.test(context);
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        return progressFunction.apply(instance, context);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(expansionKey);
    }
}
