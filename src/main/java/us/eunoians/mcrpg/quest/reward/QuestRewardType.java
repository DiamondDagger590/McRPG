package us.eunoians.mcrpg.quest.reward;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;

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
     * Indicates whether this reward type carries a numeric amount that the distribution
     * resolver can scale via {@link #withAmountMultiplier} / {@link #withExactAmount}.
     * <p>
     * The default is {@code false}. Reward types with numeric amounts (experience, items,
     * currency) must override this to return {@code true}. The resolver uses this explicit
     * signal — rather than probing object identity — to decide whether a {@code SCALE}
     * pot-behavior tier can split the reward.
     *
     * @return {@code true} if the reward can be scaled to a specific amount
     */
    default boolean isScalable() {
        return false;
    }

    /**
     * Returns a new reward instance with its numeric amount set to exactly {@code amount}.
     * Used by the distribution resolver to grant computed remainder shares without the
     * rounding drift that a multiplier round-trip would introduce.
     * <p>
     * The default returns {@code this} unchanged. Scalable reward types (those returning
     * {@code true} from {@link #isScalable()}) must override this to build a copy carrying
     * the exact amount.
     *
     * @param amount the exact numeric amount the returned reward should grant
     * @return a copy carrying the exact amount, or {@code this} if the type is not scalable
     */
    @NotNull
    default QuestRewardType withExactAmount(long amount) {
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

    /**
     * Returns a human-readable description of this configured reward for GUI display.
     * Implementations should include all relevant details (e.g., skill name for experience,
     * display label for commands). Used as a fallback when no localization entry exists
     * or when no player context is available.
     *
     * @return a display-ready description string
     */
    @NotNull
    default String describeForDisplay() {
        String typeName = getKey().getKey().replace('_', ' ');
        return getNumericAmount()
                .stream()
                .mapToObj(amount -> amount + " " + typeName)
                .findFirst()
                .orElse(typeName);
    }

    /**
     * Returns a localized, human-readable description of this configured reward for GUI display,
     * resolved through the player's locale chain. Implementations should attempt locale resolution
     * via the auto-derived route and fall back to {@link #describeForDisplay()} if no translation
     * is found.
     * <p>
     * The default implementation delegates to the no-arg {@link #describeForDisplay()}.
     *
     * @param player the player whose locale chain determines the language
     * @return a localized display-ready description string
     */
    @NotNull
    default String describeForDisplay(@NotNull McRPGPlayer player) {
        return describeForDisplay();
    }

    /**
     * Returns a new configured instance of this reward type with the given localization route set.
     * The route is auto-derived by the quest/template config loader and follows the pattern:
     * <ul>
     *     <li>Hand-crafted quest-level reward: {@code quests.<ns>.<questKey>.rewards.<label>}</li>
     *     <li>Objective-level reward: {@code quests.<ns>.<questKey>.objectives.<objKey>.rewards.<label>}</li>
     *     <li>Template reward: {@code templates.<ns>.<templateKey>.rewards.<label>}</li>
     * </ul>
     * <p>
     * The default implementation returns {@code this} unchanged. Reward types that support
     * localized display labels (e.g. {@code mcrpg:command}) should override this to store the
     * route and attempt resolution in {@link #describeForDisplay(McRPGPlayer)}.
     *
     * @param route the auto-derived localization route for this reward's display label
     * @return a new instance with the route set, or {@code this} if the type does not use it
     */
    @NotNull
    default QuestRewardType withLocalizationRoute(@NotNull Route route) {
        return this;
    }

    /**
     * Returns a new configured instance of this reward type with the given inline display label set.
     * The label is sourced from the {@code display.rewards.<rewardLabel>} entry in the quest or
     * template YAML's {@code display:} block — the same block that holds quest name, description,
     * and objective labels. This is the inline fallback used when no locale file entry exists.
     * <p>
     * The default implementation returns {@code this} unchanged. Reward types that support
     * inline display labels (e.g. {@code mcrpg:command}) should override this to store the
     * label and return it in {@link #describeForDisplay(McRPGPlayer)} when the locale lookup fails.
     *
     * @param label the inline display label from the quest's {@code display.rewards} block
     * @return a new instance with the label set, or {@code this} if the type does not use it
     */
    @NotNull
    default QuestRewardType withInlineDisplayLabel(@NotNull String label) {
        return this;
    }

    /**
     * Prepends the configured default reward color (from {@link LocalizationKey#QUEST_REWARD_DEFAULT_COLOR})
     * to the given display label. The color value is resolved through the player's locale chain so
     * server owners can customise it in {@code en_quest.yml} under
     * {@code quest-reward-types.default-color}.
     * <p>
     * If the configured color is blank or the locale entry is missing, the label is returned unchanged
     * — server owners opt out by setting {@code default-color: ""}.
     * <p>
     * Any explicit MiniMessage color tag already present in {@code label} overrides the prepended
     * default because of MiniMessage stream ordering — the later tag wins for everything after it.
     *
     * @param localization the localization manager, already resolved by the caller
     * @param player       the player whose locale chain is used to look up the color value
     * @param label        the display label to potentially prefix
     * @return the label with the default color prepended, or the label unchanged if no color is set
     */
    @NotNull
    default String prependDefaultColor(@NotNull McRPGLocalizationManager localization,
                                       @NotNull McRPGPlayer player,
                                       @NotNull String label) {
        try {
            String color = localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_DEFAULT_COLOR);
            return color.isBlank() ? label : color + label;
        } catch (Exception ignored) {
            return label;
        }
    }
}
