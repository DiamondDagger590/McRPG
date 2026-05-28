package us.eunoians.mcrpg.quest.definition;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.quest.board.BoardMetadata;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.template.condition.QuestRewardEntry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * An immutable definition (frame) for a complete quest.
 * <p>
 * A quest definition describes the full structure of a quest: its phases (ordered groups of stages),
 * scope type, optional expiration, completion rewards, and on-start messages. Definitions are loaded
 * from YAML config or registered programmatically via the developer API.
 * <p>
 * At runtime, a definition is used to create {@link us.eunoians.mcrpg.quest.impl.QuestInstance QuestInstance}
 * objects that track mutable progress state. The definition itself is immutable and shared across
 * all instances.
 * <p>
 * Use {@link Builder} to construct instances. The canonical constructor is private.
 * <p>
 * Implements {@link McRPGContent} so that quest definitions can be distributed via the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system.
 */
public class QuestDefinition implements McRPGContent {

    private final NamespacedKey questKey;
    private final NamespacedKey scopeType;
    private final Duration expiration;
    private final List<QuestPhaseDefinition> phases;
    private final List<QuestRewardEntry> rewardEntries;
    private final List<OnStartMessage> onStartMessages;
    private final QuestRepeatMode repeatMode;
    private final Duration repeatCooldown;
    private final int repeatLimit;
    private final NamespacedKey expansionKey;
    private final Map<NamespacedKey, QuestDefinitionMetadata> metadata;
    private final RewardDistributionConfig rewardDistribution;
    private final Map<String, String> inlineDisplay;
    private final Map<NamespacedKey, QuestObjectiveDefinition> objectiveIndex;

    private QuestDefinition(@NotNull NamespacedKey questKey,
                            @NotNull NamespacedKey scopeType,
                            @Nullable Duration expiration,
                            @NotNull List<QuestPhaseDefinition> phases,
                            @NotNull List<QuestRewardEntry> rewardEntries,
                            @NotNull List<OnStartMessage> onStartMessages,
                            @NotNull QuestRepeatMode repeatMode,
                            @Nullable Duration repeatCooldown,
                            int repeatLimit,
                            @Nullable NamespacedKey expansionKey,
                            @Nullable Map<NamespacedKey, QuestDefinitionMetadata> metadata,
                            @Nullable RewardDistributionConfig rewardDistribution,
                            @Nullable Map<String, String> inlineDisplay,
                            @NotNull Map<NamespacedKey, QuestObjectiveDefinition> objectiveIndex) {
        if (phases.isEmpty()) {
            throw new IllegalArgumentException("A quest must have at least one phase");
        }
        this.questKey = questKey;
        this.scopeType = scopeType;
        this.expiration = expiration;
        this.phases = List.copyOf(phases);
        this.rewardEntries = List.copyOf(rewardEntries);
        this.onStartMessages = List.copyOf(onStartMessages);
        this.repeatMode = repeatMode;
        this.repeatCooldown = repeatCooldown;
        this.repeatLimit = repeatLimit;
        this.expansionKey = expansionKey;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Collections.emptyMap();
        this.rewardDistribution = rewardDistribution;
        this.inlineDisplay = inlineDisplay != null ? Map.copyOf(inlineDisplay) : Collections.emptyMap();
        this.objectiveIndex = objectiveIndex;
    }

    /**
     * Gets the unique key identifying this quest.
     *
     * @return the quest's namespaced key
     */
    @NotNull
    public NamespacedKey getQuestKey() {
        return questKey;
    }

    /**
     * Gets the {@link Route} used to look up this quest's display name in the localization system.
     * The route is derived from the quest's {@link NamespacedKey} following the pattern
     * {@code quests.{namespace}.{key}.display-name}.
     *
     * @return the localization route for the display name
     */
    @NotNull
    public Route getDisplayNameRoute() {
        return Route.fromString("quests." + questKey.getNamespace() + "." + questKey.getKey() + ".display-name");
    }

    /**
     * Gets the {@link Route} used to look up this quest's description in the localization system.
     * The route follows the pattern {@code quests.{namespace}.{key}.description}.
     *
     * @return the localization route for the description
     */
    @NotNull
    public Route getDescriptionRoute() {
        return Route.fromString("quests." + questKey.getNamespace() + "." + questKey.getKey() + ".description");
    }

    /**
     * Gets the localized description for this quest, resolved through the player's locale chain.
     * Falls back to the inline {@code display.description} value from the quest YAML if the
     * localization entry is absent. Returns empty if neither source provides a description,
     * since not all quests require a description.
     *
     * @param player the player whose locale chain determines the language
     * @return an {@link Optional} containing the description, or empty if none is configured
     */
    @NotNull
    public Optional<String> getDescription(@NotNull McRPGPlayer player) {
        try {
            return Optional.of(RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedMessage(player, getDescriptionRoute()));
        } catch (Exception e) {
            String inline = inlineDisplay.get("description");
            if (inline != null && !inline.isEmpty()) {
                return Optional.of(inline);
            }
            return Optional.empty();
        }
    }

    /**
     * Gets the localized display name for this quest, resolved through the player's locale chain.
     * Falls back to a formatted version of the quest key if no localization entry exists.
     *
     * @param player the player whose locale chain determines the language
     * @return the localized display name, or a key-derived fallback
     */
    @NotNull
    public String getDisplayName(@NotNull McRPGPlayer player) {
        try {
            return RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedMessage(player, getDisplayNameRoute());
        } catch (Exception e) {
            String inline = inlineDisplay.get("name");
            if (inline != null && !inline.isEmpty()) {
                return inline;
            }
            return formatFallbackDisplayName();
        }
    }

    /**
     * Produces a readable fallback name from this quest's key, stripping generated prefixes
     * and UUID suffixes (e.g. {@code gen_template_choose_path_1f97a1b5} becomes {@code Choose Path}).
     * Used when neither the locale file nor the inline display name yields a result.
     *
     * @return a human-readable title-cased string derived from the quest key
     */
    @NotNull
    private String formatFallbackDisplayName() {
        String rawKey = questKey.getKey();
        String cleaned = rawKey;
        if (cleaned.startsWith("gen_template_")) {
            cleaned = cleaned.substring("gen_template_".length());
        } else if (cleaned.startsWith("gen_")) {
            cleaned = cleaned.substring("gen_".length());
        }
        cleaned = cleaned.replaceAll("_[0-9a-f]{8,}$", "");
        String[] parts = cleaned.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.isEmpty() ? rawKey.replace('_', ' ') : sb.toString();
    }

    /**
     * Gets the {@link NamespacedKey} identifying which {@link us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider}
     * should be used to create scopes for instances of this quest.
     *
     * @return the scope type key
     */
    @NotNull
    public NamespacedKey getScopeType() {
        return scopeType;
    }

    /**
     * Gets the optional expiration duration for instances of this quest.
     * If empty, quest instances do not expire.
     *
     * @return an {@link Optional} containing the expiration duration, or empty if the quest does not expire
     */
    @NotNull
    public Optional<Duration> getExpiration() {
        return Optional.ofNullable(expiration);
    }

    /**
     * Gets the ordered list of phase definitions. Phases are sequential;
     * a quest progresses through them in order.
     *
     * @return an immutable list of phases
     */
    @NotNull
    public List<QuestPhaseDefinition> getPhases() {
        return phases;
    }

    /**
     * Gets the reward entries granted upon completing the entire quest.
     * Entries may carry optional {@link us.eunoians.mcrpg.quest.board.distribution.RewardFallback} conditions for
     * per-player reward substitution at grant time.
     *
     * @return an immutable list of quest-level reward entries
     */
    @NotNull
    public List<QuestRewardEntry> getRewardEntries() {
        return rewardEntries;
    }

    /**
     * Convenience accessor returning the raw reward types without entry metadata.
     * Preserves backward compatibility with callers that don't need fallback info.
     *
     * @return an immutable list of configured quest-level reward types
     */
    @NotNull
    public List<QuestRewardType> getRewards() {
        return rewardEntries.stream()
                .map(QuestRewardEntry::reward)
                .toList();
    }

    /**
     * Gets the messages sent to players when this quest starts.
     * These are informational only — not tangible rewards — so that future chain cascade
     * auto-complete logic can skip them without risking dropped rewards.
     *
     * @return an immutable list of on-start messages (empty if none configured)
     */
    @NotNull
    public List<OnStartMessage> getOnStartMessages() {
        return onStartMessages;
    }

    /**
     * Gets the total number of phases in this quest.
     *
     * @return the phase count
     */
    public int getPhaseCount() {
        return phases.size();
    }

    /**
     * Checks whether this quest has a phase at the given index.
     *
     * @param phaseIndex the zero-based phase index
     * @return {@code true} if the index is within bounds
     */
    public boolean hasPhase(int phaseIndex) {
        return phaseIndex >= 0 && phaseIndex < phases.size();
    }

    /**
     * Gets the {@link QuestPhaseDefinition} at the given index, if it exists.
     *
     * @param phaseIndex the zero-based phase index
     * @return an {@link Optional} containing the phase definition, or empty if the index is out of bounds
     */
    @NotNull
    public Optional<QuestPhaseDefinition> getPhase(int phaseIndex) {
        if (!hasPhase(phaseIndex)) {
            return Optional.empty();
        }
        return Optional.of(phases.get(phaseIndex));
    }

    /**
     * Looks up a {@link QuestStageDefinition} by its key across all phases.
     *
     * @param stageKey the stage's namespaced key
     * @return an {@link Optional} containing the stage definition, or empty if not found
     */
    @NotNull
    public Optional<QuestStageDefinition> findStageDefinition(@NotNull NamespacedKey stageKey) {
        return phases.stream()
                .flatMap(phase -> phase.getStages().stream())
                .filter(stage -> stage.getStageKey().equals(stageKey))
                .findFirst();
    }

    /**
     * Gets the repeat mode for this quest definition.
     *
     * @return the repeat mode
     */
    @NotNull
    public QuestRepeatMode getRepeatMode() {
        return repeatMode;
    }

    /**
     * Gets the cooldown duration between completions. Used by {@link QuestRepeatMode#COOLDOWN}
     * and {@link QuestRepeatMode#COOLDOWN_LIMITED}.
     *
     * @return an {@link Optional} containing the cooldown duration, or empty if not set
     */
    @NotNull
    public Optional<Duration> getRepeatCooldown() {
        return Optional.ofNullable(repeatCooldown);
    }

    /**
     * Gets the maximum number of completions per player. Used by {@link QuestRepeatMode#LIMITED}
     * and {@link QuestRepeatMode#COOLDOWN_LIMITED}.
     *
     * @return an {@link OptionalInt} containing the limit, or empty if not set
     */
    @NotNull
    public OptionalInt getRepeatLimit() {
        return repeatLimit > 0 ? OptionalInt.of(repeatLimit) : OptionalInt.empty();
    }

    /**
     * Looks up a {@link QuestObjectiveDefinition} by its key across all phases and stages.
     *
     * @param objectiveKey the objective's namespaced key
     * @return an {@link Optional} containing the objective definition, or empty if not found
     */
    @NotNull
    public Optional<QuestObjectiveDefinition> findObjectiveDefinition(@NotNull NamespacedKey objectiveKey) {
        return Optional.ofNullable(objectiveIndex.get(objectiveKey));
    }

    /**
     * Gets a typed metadata attachment by its key.
     *
     * @param key  the metadata key
     * @param type the expected type
     * @param <T>  the metadata type
     * @return the metadata if present and of the correct type, or empty
     */
    @NotNull
    public <T extends QuestDefinitionMetadata> Optional<T> getMetadata(@NotNull NamespacedKey key,
                                                                        @NotNull Class<T> type) {
        QuestDefinitionMetadata meta = metadata.get(key);
        if (type.isInstance(meta)) {
            return Optional.of(type.cast(meta));
        }
        return Optional.empty();
    }

    /**
     * Convenience accessor for {@link BoardMetadata}.
     *
     * @return the board metadata if present
     */
    @NotNull
    public Optional<BoardMetadata> getBoardMetadata() {
        return getMetadata(BoardMetadata.METADATA_KEY, BoardMetadata.class);
    }

    /**
     * Checks whether this definition has board metadata attached.
     *
     * @return {@code true} if board metadata is present
     */
    public boolean hasBoardMetadata() {
        return metadata.containsKey(BoardMetadata.METADATA_KEY);
    }

    /**
     * Gets all metadata attachments.
     *
     * @return an unmodifiable map of all metadata
     */
    @NotNull
    public Map<NamespacedKey, QuestDefinitionMetadata> getAllMetadata() {
        return metadata;
    }

    /**
     * Gets the optional reward distribution configuration for quest-level completion rewards.
     *
     * @return an {@link Optional} containing the distribution config, or empty if standard (non-distributed) rewards apply
     */
    @NotNull
    public Optional<RewardDistributionConfig> getRewardDistribution() {
        return Optional.ofNullable(rewardDistribution);
    }

    /**
     * Gets the inline display strings from the quest/template YAML file.
     * These serve as fallbacks when the localization system has no entry.
     *
     * @return an unmodifiable map of display key to display string
     */
    @NotNull
    public Map<String, String> getInlineDisplay() {
        return inlineDisplay;
    }

    /**
     * Gets an inline display value by key, if present.
     *
     * @param key the display key (e.g. "name", "description", or an objective key)
     * @return the display value, or empty
     */
    @NotNull
    public Optional<String> getInlineDisplayValue(@NotNull String key) {
        return Optional.ofNullable(inlineDisplay.get(key));
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.ofNullable(expansionKey);
    }

    /**
     * Builder for {@link QuestDefinition}. Required fields (quest key, scope type, and phases) are
     * provided via the constructor; all other fields have sensible defaults and may be set via
     * fluent setters before calling {@link #build()}.
     */
    public static final class Builder {

        private final NamespacedKey questKey;
        private final NamespacedKey scopeType;
        private final List<QuestPhaseDefinition> phases;

        private Duration expiration;
        private List<QuestRewardEntry> rewardEntries = List.of();
        private List<OnStartMessage> onStartMessages = List.of();
        private QuestRepeatMode repeatMode = QuestRepeatMode.ONCE;
        private Duration repeatCooldown;
        private int repeatLimit = -1;
        private NamespacedKey expansionKey;
        private Map<NamespacedKey, QuestDefinitionMetadata> metadata;
        private RewardDistributionConfig rewardDistribution;
        private Map<String, String> inlineDisplay;

        /**
         * Creates a new builder with the three required fields.
         *
         * @param questKey  the unique key identifying this quest
         * @param scopeType the scope provider key
         * @param phases    the ordered phase list (must contain at least one)
         */
        public Builder(@NotNull NamespacedKey questKey,
                       @NotNull NamespacedKey scopeType,
                       @NotNull List<QuestPhaseDefinition> phases) {
            this.questKey = questKey;
            this.scopeType = scopeType;
            this.phases = phases;
        }

        /**
         * Sets the quest-level completion rewards from raw reward types (auto-wrapped into entries).
         *
         * @param rewards the completion reward types
         * @return this builder
         */
        @NotNull
        public Builder rewards(@NotNull List<QuestRewardType> rewards) {
            this.rewardEntries = rewards.stream().map(QuestRewardEntry::new).toList();
            return this;
        }

        /**
         * Sets the quest-level completion reward entries (with optional fallbacks).
         *
         * @param entries the completion reward entries
         * @return this builder
         */
        @NotNull
        public Builder rewardEntries(@NotNull List<QuestRewardEntry> entries) {
            this.rewardEntries = entries;
            return this;
        }

        /**
         * Sets messages sent to players when the quest starts, before objectives begin.
         * These are informational messages only — not tangible rewards — so that future chain
         * cascade auto-complete logic can cleanly skip them without risking dropped rewards.
         *
         * @param messages the on-start messages
         * @return this builder
         */
        @NotNull
        public Builder onStartMessages(@NotNull List<OnStartMessage> messages) {
            this.onStartMessages = messages;
            return this;
        }

        /**
         * Sets the expiration duration for quest instances.
         *
         * @param expiration the duration, or {@code null} for no expiration
         * @return this builder
         */
        @NotNull
        public Builder expiration(@Nullable Duration expiration) {
            this.expiration = expiration;
            return this;
        }

        /**
         * Sets how this quest may be repeated.
         *
         * @param mode the repeat mode
         * @return this builder
         */
        @NotNull
        public Builder repeatMode(@NotNull QuestRepeatMode mode) {
            this.repeatMode = mode;
            return this;
        }

        /**
         * Sets the cooldown between completions.
         *
         * @param cooldown the cooldown, or {@code null} for none
         * @return this builder
         */
        @NotNull
        public Builder repeatCooldown(@Nullable Duration cooldown) {
            this.repeatCooldown = cooldown;
            return this;
        }

        /**
         * Sets the maximum number of completions per player ({@code -1} for unlimited).
         *
         * @param limit the completion limit
         * @return this builder
         */
        @NotNull
        public Builder repeatLimit(int limit) {
            this.repeatLimit = limit;
            return this;
        }

        /**
         * Sets the expansion key for this definition.
         *
         * @param key the expansion key, or {@code null} for config-loaded definitions
         * @return this builder
         */
        @NotNull
        public Builder expansionKey(@Nullable NamespacedKey key) {
            this.expansionKey = key;
            return this;
        }

        /**
         * Sets extensible metadata attachments.
         *
         * @param metadata the metadata map, or {@code null} for none
         * @return this builder
         */
        @NotNull
        public Builder metadata(@Nullable Map<NamespacedKey, QuestDefinitionMetadata> metadata) {
            this.metadata = metadata;
            return this;
        }

        /**
         * Sets the reward distribution configuration.
         *
         * @param config the distribution config, or {@code null} for standard rewards
         * @return this builder
         */
        @NotNull
        public Builder rewardDistribution(@Nullable RewardDistributionConfig config) {
            this.rewardDistribution = config;
            return this;
        }

        /**
         * Sets inline display string fallbacks from the quest YAML's {@code display:} block.
         *
         * @param display the inline display map, or {@code null} for none
         * @return this builder
         */
        @NotNull
        public Builder inlineDisplay(@Nullable Map<String, String> display) {
            this.inlineDisplay = display;
            return this;
        }

        /**
         * Builds and returns a new immutable {@link QuestDefinition}.
         *
         * @return the quest definition
         * @throws IllegalArgumentException if phases is empty
         */
        @NotNull
        public QuestDefinition build() {
            Map<NamespacedKey, QuestObjectiveDefinition> index = new HashMap<>();
            for (QuestPhaseDefinition phase : phases) {
                for (QuestStageDefinition stage : phase.getStages()) {
                    for (QuestObjectiveDefinition obj : stage.getObjectives()) {
                        NamespacedKey objectiveKey = obj.getObjectiveKey();
                        if (index.containsKey(objectiveKey)) {
                            throw new IllegalStateException(
                                    "Duplicate objective key '" + objectiveKey + "' in quest definition '" + questKey + "'");
                        }
                        index.put(objectiveKey, obj);
                    }
                }
            }
            return new QuestDefinition(questKey, scopeType, expiration, phases,
                    rewardEntries, onStartMessages, repeatMode, repeatCooldown,
                    repeatLimit, expansionKey, metadata, rewardDistribution, inlineDisplay,
                    Map.copyOf(index));
        }
    }
}
