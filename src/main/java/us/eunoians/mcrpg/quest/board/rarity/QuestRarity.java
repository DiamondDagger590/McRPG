package us.eunoians.mcrpg.quest.board.rarity;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Optional;

/**
 * Immutable data class representing a single rarity tier for board quests.
 * <p>
 * Loaded from {@code board.yml} config (auto-namespaced under {@code mcrpg:}) or
 * registered programmatically by third-party plugins via content packs.
 */
public final class QuestRarity implements McRPGContent {

    private final NamespacedKey key;
    private final int weight;
    private final double difficultyMultiplier;
    private final double rewardMultiplier;
    private final NamespacedKey expansionKey;
    private final Integer customModelData;
    private final boolean glint;

    public QuestRarity(@NotNull NamespacedKey key,
                       int weight,
                       double difficultyMultiplier,
                       double rewardMultiplier,
                       @NotNull NamespacedKey expansionKey) {
        this(key, weight, difficultyMultiplier, rewardMultiplier, expansionKey, null, false);
    }

    public QuestRarity(@NotNull NamespacedKey key,
                       int weight,
                       double difficultyMultiplier,
                       double rewardMultiplier,
                       @NotNull NamespacedKey expansionKey,
                       @Nullable Integer customModelData,
                       boolean glint) {
        this.key = key;
        this.weight = weight;
        this.difficultyMultiplier = difficultyMultiplier;
        this.rewardMultiplier = rewardMultiplier;
        this.expansionKey = expansionKey;
        this.customModelData = customModelData;
        this.glint = glint;
    }

    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    public int getWeight() {
        return weight;
    }

    public double getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    public double getRewardMultiplier() {
        return rewardMultiplier;
    }

    /**
     * Returns the optional custom model data value for resource pack integration.
     *
     * @return the custom model data, or empty if not configured
     */
    @NotNull
    public Optional<Integer> getCustomModelData() {
        return Optional.ofNullable(customModelData);
    }

    /**
     * Returns whether this rarity should display an enchantment glint on offering items.
     *
     * @return true if glint is enabled
     */
    public boolean hasGlint() {
        return glint;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(expansionKey);
    }
}
