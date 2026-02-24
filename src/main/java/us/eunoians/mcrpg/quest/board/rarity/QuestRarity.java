package us.eunoians.mcrpg.quest.board.rarity;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Optional;

/**
 * Immutable data class representing a single rarity tier for board quests.
 * <p>
 * Loaded from {@code board.yml} config (auto-namespaced under {@code mcrpg:}) or
 * registered programmatically by third-party plugins via content packs.
 * <p>
 * In Phase 1, rarity affects appearance frequency for hand-crafted quests.
 * Difficulty/reward multipliers are stored but only have mechanical effect on
 * template-generated quests (Phase 2+).
 */
public final class QuestRarity implements McRPGContent {

    private final NamespacedKey key;
    private final int weight;
    private final double difficultyMultiplier;
    private final double rewardMultiplier;
    private final NamespacedKey expansionKey;

    public QuestRarity(@NotNull NamespacedKey key,
                       int weight,
                       double difficultyMultiplier,
                       double rewardMultiplier,
                       @NotNull NamespacedKey expansionKey) {
        this.key = key;
        this.weight = weight;
        this.difficultyMultiplier = difficultyMultiplier;
        this.rewardMultiplier = rewardMultiplier;
        this.expansionKey = expansionKey;
    }

    /**
     * Returns the unique identifier for this rarity tier.
     *
     * @return the namespaced key
     */
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Returns the weight used for weighted random selection. Higher values
     * increase the probability of this rarity being rolled.
     *
     * @return the selection weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Returns the difficulty scaling factor applied to template-generated quests.
     * Cosmetic in Phase 1 (hand-crafted quests define their own difficulty).
     *
     * @return the difficulty multiplier
     */
    public double getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    /**
     * Returns the reward scaling factor applied to template-generated quests.
     * Cosmetic in Phase 1 (hand-crafted quests define their own rewards).
     *
     * @return the reward multiplier
     */
    public double getRewardMultiplier() {
        return rewardMultiplier;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(expansionKey);
    }
}
