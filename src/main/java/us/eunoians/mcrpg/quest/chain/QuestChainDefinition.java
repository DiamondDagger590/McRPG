package us.eunoians.mcrpg.quest.chain;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.quest.chain.availability.AvailabilityConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable blueprint for a quest chain. Loaded from YAML and shared across all
 * runtime instances. Individual quest definitions referenced by steps remain
 * unaware that they belong to a chain.
 * <p>
 * The {@code stepIndex} map enables O(1) quest-to-step resolution during chain
 * advancement. It is built at construction time from the ordered step list.
 * <p>
 * Constructed exclusively via {@link Builder}.
 */
public final class QuestChainDefinition implements McRPGContent {

    private final NamespacedKey chainKey;
    private final String displayName;
    private final NamespacedKey sourceKey;
    private final NamespacedKey autoStartTriggerKey;
    private final List<QuestChainStep> steps;
    private final QuestChainRepeatMode repeatMode;
    private final Duration repeatCooldown;
    private final int maxCompletions;
    private final String onQuestExpireDefault;
    private final AvailabilityConfig availabilityConfig;
    private final Map<NamespacedKey, Integer> stepIndex;

    private QuestChainDefinition(@NotNull NamespacedKey chainKey,
                                 @NotNull String displayName,
                                 @NotNull NamespacedKey sourceKey,
                                 @NotNull NamespacedKey autoStartTriggerKey,
                                 @NotNull List<QuestChainStep> steps,
                                 @NotNull QuestChainRepeatMode repeatMode,
                                 @Nullable Duration repeatCooldown,
                                 int maxCompletions,
                                 @NotNull String onQuestExpireDefault,
                                 @Nullable AvailabilityConfig availabilityConfig,
                                 @NotNull Map<NamespacedKey, Integer> stepIndex) {
        this.chainKey = chainKey;
        this.displayName = displayName;
        this.sourceKey = sourceKey;
        this.autoStartTriggerKey = autoStartTriggerKey;
        this.steps = List.copyOf(steps);
        this.repeatMode = repeatMode;
        this.repeatCooldown = repeatCooldown;
        this.maxCompletions = maxCompletions;
        this.onQuestExpireDefault = onQuestExpireDefault;
        this.availabilityConfig = availabilityConfig;
        this.stepIndex = Map.copyOf(stepIndex);
    }

    /**
     * Gets the unique key identifying this chain.
     *
     * @return the chain key
     */
    @NotNull
    public NamespacedKey getChainKey() {
        return chainKey;
    }

    /**
     * Returns the player-facing display name for this chain. Used in GUIs and messages.
     * Falls back to the chain key's value portion (e.g., {@code "tutorial_chain"} from
     * {@code mcrpg:tutorial_chain}) if no explicit display name is configured.
     *
     * @return the display name
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the quest source key used for quests started by this chain.
     *
     * @return the source key
     */
    @NotNull
    public NamespacedKey getSourceKey() {
        return sourceKey;
    }

    /**
     * Gets the auto-start trigger key that determines when this chain is evaluated
     * for automatic starts.
     *
     * @return the auto-start trigger key
     */
    @NotNull
    public NamespacedKey getAutoStartTriggerKey() {
        return autoStartTriggerKey;
    }

    /**
     * Gets the ordered list of steps in this chain.
     *
     * @return the immutable step list
     */
    @NotNull
    public List<QuestChainStep> getSteps() {
        return steps;
    }

    /**
     * Gets the repeat mode controlling whether the chain can be completed more than once.
     *
     * @return the repeat mode
     */
    @NotNull
    public QuestChainRepeatMode getRepeatMode() {
        return repeatMode;
    }

    /**
     * Gets the cooldown duration between chain repetitions. Only applicable for
     * {@link QuestChainRepeatMode#COOLDOWN} and {@link QuestChainRepeatMode#COOLDOWN_LIMITED} modes.
     *
     * @return the cooldown, or empty if no cooldown is configured
     */
    @NotNull
    public Optional<Duration> getRepeatCooldown() {
        return Optional.ofNullable(repeatCooldown);
    }

    /**
     * Gets the maximum number of times this chain can be completed. Only applicable for
     * {@link QuestChainRepeatMode#LIMITED} and {@link QuestChainRepeatMode#COOLDOWN_LIMITED} modes.
     * Returns -1 when unlimited.
     *
     * @return the max completions, or -1 for unlimited
     */
    public int getMaxCompletions() {
        return maxCompletions;
    }

    /**
     * Gets the default expiration behavior applied to steps that do not override it.
     *
     * @return the default on-quest-expire behavior string
     */
    @NotNull
    public String getOnQuestExpireDefault() {
        return onQuestExpireDefault;
    }

    /**
     * Returns the availability window configuration for this chain, if any.
     * When present and no window is currently active, {@link QuestChainManager#tryStartChain}
     * will refuse to start new instances.
     *
     * @return the availability config, or empty if the chain has no time restrictions
     */
    @NotNull
    public Optional<AvailabilityConfig> getAvailabilityConfig() {
        return Optional.ofNullable(availabilityConfig);
    }

    /**
     * Returns the 0-based index of the step referencing the given quest key,
     * or -1 if not found.
     *
     * @param questKey the quest definition key
     * @return the step index, or -1
     */
    public int getStepIndex(@NotNull NamespacedKey questKey) {
        return stepIndex.getOrDefault(questKey, -1);
    }

    /**
     * Returns the step that references the given quest key, or empty if no step references it.
     *
     * @param questKey the quest definition key to search for
     * @return the step referencing this quest key, or empty
     */
    @NotNull
    public Optional<QuestChainStep> getStep(@NotNull NamespacedKey questKey) {
        int index = getStepIndex(questKey);
        if (index < 0) {
            return Optional.empty();
        }
        return Optional.of(steps.get(index));
    }

    /**
     * Returns the step after the one referencing the given quest key,
     * or empty if this is the last step.
     *
     * @param questKey the quest definition key of the current step
     * @return the next step, or empty
     */
    @NotNull
    public Optional<QuestChainStep> getNextStep(@NotNull NamespacedKey questKey) {
        int index = getStepIndex(questKey);
        if (index < 0 || index >= steps.size() - 1) {
            return Optional.empty();
        }
        return Optional.of(steps.get(index + 1));
    }

    @Override
    @NotNull
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.empty();
    }

    /**
     * Builder for {@link QuestChainDefinition}. Required fields are parameters of the
     * builder constructor; optional fields use fluent setters with sensible defaults.
     */
    public static final class Builder {

        private final NamespacedKey chainKey;
        private final NamespacedKey sourceKey;
        private final NamespacedKey autoStartTriggerKey;
        private final List<QuestChainStep> steps;

        private String displayName;
        private QuestChainRepeatMode repeatMode = QuestChainRepeatMode.ONCE;
        private Duration repeatCooldown;
        private int maxCompletions = -1;
        private String onQuestExpireDefault = "fail-chain";
        private AvailabilityConfig availabilityConfig;

        /**
         * @param chainKey            unique key for this chain
         * @param sourceKey           the quest source key for chain-managed quests
         * @param autoStartTriggerKey the auto-start trigger key
         * @param steps               ordered step list (must contain at least one step)
         */
        public Builder(@NotNull NamespacedKey chainKey,
                       @NotNull NamespacedKey sourceKey,
                       @NotNull NamespacedKey autoStartTriggerKey,
                       @NotNull List<QuestChainStep> steps) {
            this.chainKey = chainKey;
            this.sourceKey = sourceKey;
            this.autoStartTriggerKey = autoStartTriggerKey;
            this.steps = new ArrayList<>(steps);
        }

        /**
         * Sets the player-facing display name. If not set, defaults to the chain key's
         * value portion (e.g., {@code "tutorial_chain"} from {@code mcrpg:tutorial_chain}).
         *
         * @param displayName the display name, or null to use the key's value portion
         * @return this builder
         */
        @NotNull
        public Builder displayName(@Nullable String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Sets the repeat mode for this chain.
         *
         * @param mode the repeat mode
         * @return this builder
         */
        @NotNull
        public Builder repeatMode(@NotNull QuestChainRepeatMode mode) {
            this.repeatMode = mode;
            return this;
        }

        /**
         * Sets the cooldown duration between chain repetitions.
         *
         * @param cooldown the cooldown duration, or null for no cooldown
         * @return this builder
         */
        @NotNull
        public Builder repeatCooldown(@Nullable Duration cooldown) {
            this.repeatCooldown = cooldown;
            return this;
        }

        /**
         * Sets the maximum number of times the chain can be completed.
         *
         * @param max the maximum completions (-1 for unlimited)
         * @return this builder
         */
        @NotNull
        public Builder maxCompletions(int max) {
            this.maxCompletions = max;
            return this;
        }

        /**
         * Sets the default on-quest-expire behavior applied to steps that do not override it.
         *
         * @param behavior the expire behavior string (e.g., {@code "fail-chain"})
         * @return this builder
         */
        @NotNull
        public Builder onQuestExpireDefault(@NotNull String behavior) {
            this.onQuestExpireDefault = behavior;
            return this;
        }

        /**
         * Sets the availability window configuration for this chain.
         *
         * @param config the availability config, or null for no time restrictions
         * @return this builder
         */
        @NotNull
        public Builder availabilityConfig(@Nullable AvailabilityConfig config) {
            this.availabilityConfig = config;
            return this;
        }

        /**
         * Builds the chain definition. Validates that steps is non-empty and contains no
         * duplicate quest keys. Builds the stepIndex map. If {@code displayName} was not set,
         * defaults to the chain key's value portion.
         *
         * @return the immutable chain definition
         * @throws IllegalArgumentException if steps is empty
         * @throws IllegalStateException    if duplicate quest keys exist in steps
         */
        @NotNull
        public QuestChainDefinition build() {
            if (steps.isEmpty()) {
                throw new IllegalArgumentException("Quest chain '" + chainKey + "' must have at least one step");
            }

            Map<NamespacedKey, Integer> index = new HashMap<>();
            Set<NamespacedKey> seen = new HashSet<>();
            for (int i = 0; i < steps.size(); i++) {
                NamespacedKey questKey = steps.get(i).questKey();
                if (!seen.add(questKey)) {
                    throw new IllegalStateException(
                            "Duplicate quest key '" + questKey + "' in chain '" + chainKey + "'");
                }
                index.put(questKey, i);
            }

            String resolvedDisplayName = (displayName != null && !displayName.isEmpty())
                    ? displayName
                    : chainKey.getKey();

            return new QuestChainDefinition(
                    chainKey, resolvedDisplayName, sourceKey, autoStartTriggerKey,
                    steps, repeatMode, repeatCooldown, maxCompletions,
                    onQuestExpireDefault, availabilityConfig, index);
        }
    }
}
