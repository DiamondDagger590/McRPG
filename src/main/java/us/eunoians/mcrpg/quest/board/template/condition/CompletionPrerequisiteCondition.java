package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Evaluates to {@code true} if the player has completed at least {@code minCompletions} quests,
 * optionally filtered by board category and/or minimum rarity.
 */
public final class CompletionPrerequisiteCondition implements TemplateCondition {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "completion_prerequisite");

    private final int minCompletions;
    @Nullable
    private final NamespacedKey categoryKey;
    @Nullable
    private final NamespacedKey minRarity;

    public CompletionPrerequisiteCondition(int minCompletions,
                                           @Nullable NamespacedKey categoryKey,
                                           @Nullable NamespacedKey minRarity) {
        if (minCompletions < 1) {
            throw new IllegalArgumentException("minCompletions must be >= 1, got: " + minCompletions);
        }
        this.minCompletions = minCompletions;
        this.categoryKey = categoryKey;
        this.minRarity = minRarity;
    }

    @Override
    public boolean evaluate(@NotNull ConditionContext context) {
        if (context.playerUUID() == null || context.completionHistory() == null) {
            return false;
        }
        int completed = context.completionHistory().countCompletedQuests(
                context.playerUUID(), categoryKey, minRarity);
        return completed >= minCompletions;
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
        int count = section.getInt("min-completions");
        NamespacedKey category = section.contains("category")
                ? parseKey(section.getString("category"))
                : null;
        NamespacedKey rarity = section.contains("min-rarity")
                ? parseKey(section.getString("min-rarity"))
                : null;
        return new CompletionPrerequisiteCondition(count, category, rarity);
    }

    @Nullable
    private static NamespacedKey parseKey(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        if (input.contains(":")) {
            return NamespacedKey.fromString(input.toLowerCase());
        }
        return new NamespacedKey(McRPGMethods.getMcRPGNamespace(), input.toLowerCase());
    }

    public int getMinCompletions() {
        return minCompletions;
    }

    @NotNull
    public Optional<NamespacedKey> getCategoryKey() {
        return Optional.ofNullable(categoryKey);
    }

    @NotNull
    public Optional<NamespacedKey> getMinRarity() {
        return Optional.ofNullable(minRarity);
    }
}
