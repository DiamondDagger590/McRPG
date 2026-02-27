package us.eunoians.mcrpg.quest.reward;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Map;
import java.util.OptionalLong;

/**
 * Defines a type of reward that can be granted upon quest, stage, or objective completion.
 * <p>
 * A base (unconfigured) instance is registered in the {@link QuestRewardTypeRegistry}. When a
 * quest definition is parsed, {@link #parseConfig} is called to produce a new configured instance
 * that holds its reward data internally. This configured instance is stored directly in the
 * definition and used at runtime to grant rewards.
 * <p>
 * McRPG ships built-in reward types (experience, commands, etc.) and external plugins
 * can register their own types via the reward type registry.
 * <p>
 * Extends {@link McRPGContent} so that reward types can be distributed via the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system. All implementations
 * must provide {@link #getExpansionKey()} identifying which expansion they belong to.
 */
public interface QuestRewardType extends McRPGContent {

    /**
     * Gets the unique key identifying this reward type.
     *
     * @return the namespaced key for this type
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Parses reward-specific configuration from a YAML section and returns a new configured
     * instance of this type. The returned instance holds the parsed data internally and
     * uses it during {@link #grant}.
     *
     * @param section the BoostedYaml section containing reward-specific data
     * @return a new configured instance of this reward type
     */
    @NotNull
    QuestRewardType parseConfig(@NotNull Section section);

    /**
     * Grants this reward to the specified player, using this instance's internal
     * configuration state.
     *
     * @param player the player to grant the reward to
     */
    void grant(@NotNull Player player);

    /**
     * Serializes this configured instance's internal state to a map that can be
     * stored as JSON and later reconstructed via {@link #fromSerializedConfig}. This
     * is used by the pending reward queue to persist rewards for offline players.
     *
     * @return a serializable map of configuration key-value pairs
     */
    @NotNull
    Map<String, Object> serializeConfig();

    /**
     * Reconstructs a configured instance of this reward type from a previously
     * serialized configuration map (produced by {@link #serializeConfig}). This is
     * used by the pending reward queue to reconstruct rewards for offline players
     * on login without requiring a BoostedYaml {@link Section}.
     *
     * @param config the serialized configuration map
     * @return a configured instance of this reward type
     */
    @NotNull
    QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config);

    /**
     * Returns a new reward instance with any numeric amount scaled by the multiplier.
     * Used by the reward distribution resolver for {@code SPLIT_EVEN} and
     * {@code SPLIT_PROPORTIONAL} pot distribution modes.
     * <p>
     * Implementations with numeric amounts (experience, items, currency) should
     * return a new instance with the scaled value (minimum 1). Implementations
     * without numeric amounts (commands, ability upgrades) return {@code this}
     * unchanged — the resolver logs a warning for non-scalable rewards in split-mode tiers.
     *
     * @param multiplier the scaling factor (e.g., 0.5 for half, 0.1 for one-tenth)
     * @return a scaled copy, or {@code this} if the reward type is not scalable
     */
    @NotNull
    default QuestRewardType withAmountMultiplier(double multiplier) {
        return this;
    }

    /**
     * Returns the numeric amount of this reward, if applicable. Used by the
     * distribution resolver for remainder calculations in split-mode tiers.
     * Reward types without a numeric amount (e.g., ability upgrades) return empty.
     *
     * @return the numeric amount, or empty if not applicable
     */
    @NotNull
    default OptionalLong getNumericAmount() {
        return OptionalLong.empty();
    }
}
