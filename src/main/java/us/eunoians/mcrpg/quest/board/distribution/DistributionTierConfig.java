package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable configuration for one distribution tier, parsed from YAML.
 * Contains the distribution type key, split mode, rewards, rarity gates,
 * and type-specific parameters.
 * <p>
 * Type-specific parameters (e.g., {@code top-player-count} for the
 * {@code mcrpg:top_players} type, or {@code min-contribution-percent}
 * for {@code mcrpg:contribution_threshold}) are stored in a generic
 * {@code typeParameters} map. This allows third-party distribution types
 * to define their own parameters without modifying this class.
 * <p>
 * Built-in convenience accessors ({@link #getTopPlayerCount()},
 * {@link #getMinContributionPercent()}) are provided for the
 * standard McRPG parameters.
 * <p>
 * Rarity gating allows tiers to only apply for quests of a certain rarity
 * or above. {@code min-rarity} uses weight comparison (lower weight = rarer),
 * and {@code required-rarity} requires an exact match.
 */
public final class DistributionTierConfig {

    /**
     * Parameter key used by {@code mcrpg:top_players} to specify how many
     * top contributors qualify.
     */
    public static final String PARAM_TOP_PLAYER_COUNT = "top-player-count";

    /**
     * Parameter key used by {@code mcrpg:contribution_threshold} to specify
     * the minimum contribution percentage required to qualify.
     */
    public static final String PARAM_MIN_CONTRIBUTION_PERCENT = "min-contribution-percent";

    private final String tierKey;
    private final NamespacedKey typeKey;
    private final RewardSplitMode splitMode;
    private final List<DistributionRewardEntry> rewardEntries;
    private final Map<String, Object> typeParameters;
    private final NamespacedKey minRarity;
    private final NamespacedKey requiredRarity;

    /**
     * Constructs a new tier config with {@link DistributionRewardEntry} list.
     *
     * @param tierKey        a human-readable identifier for this tier (used in logging)
     * @param typeKey        the {@link NamespacedKey} of the {@link RewardDistributionType} to use
     * @param splitMode      how rewards are divided among qualifying players
     * @param rewardEntries  the list of reward entries granted to qualifying players
     * @param typeParameters arbitrary key-value parameters consumed by the distribution type
     * @param minRarity      if set, the quest's rarity weight must be {@code <=} this rarity's weight
     * @param requiredRarity if set, the quest's rarity must exactly match this key
     */
    public DistributionTierConfig(@NotNull String tierKey,
                                  @NotNull NamespacedKey typeKey,
                                  @NotNull RewardSplitMode splitMode,
                                  @NotNull List<DistributionRewardEntry> rewardEntries,
                                  @NotNull Map<String, Object> typeParameters,
                                  @Nullable NamespacedKey minRarity,
                                  @Nullable NamespacedKey requiredRarity) {
        this.tierKey = tierKey;
        this.typeKey = typeKey;
        this.splitMode = splitMode;
        this.rewardEntries = List.copyOf(rewardEntries);
        this.typeParameters = Map.copyOf(typeParameters);
        this.minRarity = minRarity;
        this.requiredRarity = requiredRarity;
    }

    /**
     * Backward-compatible constructor accepting raw reward types.
     * Wraps each reward in a {@link DistributionRewardEntry} with default settings.
     */
    public DistributionTierConfig(@NotNull String tierKey,
                                  @NotNull NamespacedKey typeKey,
                                  @NotNull RewardSplitMode splitMode,
                                  @NotNull List<QuestRewardType> rewards,
                                  @NotNull Map<String, Object> typeParameters,
                                  @Nullable NamespacedKey minRarity,
                                  @Nullable NamespacedKey requiredRarity,
                                  @SuppressWarnings("unused") boolean legacyOverload) {
        this(tierKey, typeKey, splitMode,
                rewards.stream().map(DistributionRewardEntry::new).toList(),
                typeParameters, minRarity, requiredRarity);
    }

    /**
     * Gets the human-readable identifier for this distribution tier.
     *
     * @return the tier key string
     */
    @NotNull
    public String getTierKey() {
        return tierKey;
    }

    /**
     * Gets the {@link NamespacedKey} of the {@link RewardDistributionType}
     * that determines which players qualify for this tier's rewards.
     *
     * @return the distribution type key
     */
    @NotNull
    public NamespacedKey getTypeKey() {
        return typeKey;
    }

    /**
     * Gets the {@link RewardSplitMode} controlling how reward amounts are
     * distributed among qualifying players.
     *
     * @return the split mode
     */
    @NotNull
    public RewardSplitMode getSplitMode() {
        return splitMode;
    }

    /**
     * Gets the immutable list of reward entries with per-reward distribution config.
     *
     * @return the reward entry list
     */
    @NotNull
    public List<DistributionRewardEntry> getRewardEntries() {
        return rewardEntries;
    }

    /**
     * Convenience accessor returning the raw reward types without entry metadata.
     * Preserves backward compatibility with callers that don't need pot-behavior info.
     *
     * @return the reward list
     */
    @NotNull
    public List<QuestRewardType> getRewards() {
        return rewardEntries.stream()
                .map(DistributionRewardEntry::reward)
                .toList();
    }

    /**
     * Gets the full map of type-specific parameters. Third-party distribution
     * types can define their own parameter keys and retrieve them from this map.
     *
     * @return an unmodifiable map of parameter key to value
     */
    @NotNull
    public Map<String, Object> getTypeParameters() {
        return typeParameters;
    }

    /**
     * Gets a type-specific parameter by key, cast to the expected type.
     *
     * @param key  the parameter key
     * @param type the expected value class
     * @param <T>  the value type
     * @return the value if present and of the correct type, or empty
     */
    @NotNull
    public <T> Optional<T> getTypeParameter(@NotNull String key, @NotNull Class<T> type) {
        Object value = typeParameters.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    /**
     * Convenience accessor for the {@code top-player-count} parameter used by
     * the {@code mcrpg:top_players} distribution type.
     *
     * @return the top player count, or empty if not configured
     */
    @NotNull
    public Optional<Integer> getTopPlayerCount() {
        return getTypeParameter(PARAM_TOP_PLAYER_COUNT, Integer.class);
    }

    /**
     * Convenience accessor for the {@code min-contribution-percent} parameter
     * used by the {@code mcrpg:contribution_threshold} distribution type.
     *
     * @return the minimum contribution percentage, or empty if not configured
     */
    @NotNull
    public Optional<Double> getMinContributionPercent() {
        return getTypeParameter(PARAM_MIN_CONTRIBUTION_PERCENT, Double.class);
    }

    /**
     * Gets the minimum rarity key for this tier's rarity gate.
     *
     * @return the minimum rarity key, or empty if not configured
     */
    @NotNull
    public Optional<NamespacedKey> getMinRarity() {
        return Optional.ofNullable(minRarity);
    }

    /**
     * Gets the required exact-match rarity key for this tier's rarity gate.
     *
     * @return the required rarity key, or empty if not configured
     */
    @NotNull
    public Optional<NamespacedKey> getRequiredRarity() {
        return Optional.ofNullable(requiredRarity);
    }

    /**
     * Checks whether a quest instance's rarity passes this tier's rarity gate.
     * If no rarity gate is configured, always returns {@code true}.
     * <p>
     * {@code min-rarity} includes the specified rarity and all higher tiers
     * (lower weight = rarer). {@code required-rarity} requires an exact match.
     *
     * @param questRarity    the rarity key of the quest instance, or null for non-board quests
     * @param rarityRegistry used to resolve ordering for min-rarity comparison
     * @return true if the rarity gate is satisfied
     */
    public boolean passesRarityGate(@Nullable NamespacedKey questRarity,
                                    @NotNull QuestRarityRegistry rarityRegistry) {
        boolean hasMinRarity = minRarity != null;
        boolean hasRequiredRarity = requiredRarity != null;

        if (!hasMinRarity && !hasRequiredRarity) {
            return true;
        }

        if (questRarity == null) {
            return false;
        }

        if (hasRequiredRarity) {
            return questRarity.equals(requiredRarity);
        }

        Optional<QuestRarity> questRarityObj = rarityRegistry.get(questRarity);
        Optional<QuestRarity> minRarityObj = rarityRegistry.get(minRarity);
        if (questRarityObj.isEmpty() || minRarityObj.isEmpty()) {
            return false;
        }
        return questRarityObj.get().getWeight() <= minRarityObj.get().getWeight();
    }
}
