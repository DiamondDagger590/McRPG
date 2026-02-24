package us.eunoians.mcrpg.quest.definition;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import us.eunoians.mcrpg.quest.board.BoardMetadata;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * An immutable definition (frame) for a complete quest.
 * <p>
 * A quest definition describes the full structure of a quest: its phases (ordered groups of stages),
 * scope type, optional expiration, and completion rewards. Definitions are loaded from YAML config
 * or registered programmatically via the developer API.
 * <p>
 * At runtime, a definition is used to create {@link us.eunoians.mcrpg.quest.impl.QuestInstance QuestInstance}
 * objects that track mutable progress state. The definition itself is immutable and shared across
 * all instances.
 * <p>
 * Implements {@link McRPGContent} so that quest definitions can be distributed via the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system.
 */
public class QuestDefinition implements McRPGContent {

    private final NamespacedKey questKey;
    private final NamespacedKey scopeType;
    private final Duration expiration;
    private final List<QuestPhaseDefinition> phases;
    private final List<QuestRewardType> rewards;
    private final QuestRepeatMode repeatMode;
    private final Duration repeatCooldown;
    private final int repeatLimit;
    private final NamespacedKey expansionKey;
    private final Map<NamespacedKey, QuestDefinitionMetadata> metadata;

    /**
     * Creates a new quest definition.
     *
     * @param questKey       the unique key identifying this quest
     * @param scopeType      the key identifying the scope provider for instances of this quest
     * @param expiration     the expiration duration, or {@code null} if instances do not expire
     * @param phases         the ordered list of phase definitions (must contain at least one)
     * @param rewards        the quest-level rewards granted on completion
     * @param repeatMode     how this quest may be repeated (defaults to {@link QuestRepeatMode#ONCE})
     * @param repeatCooldown the cooldown between completions (only used with {@link QuestRepeatMode#COOLDOWN}), or {@code null}
     * @param repeatLimit    the maximum number of completions per player (only used with {@link QuestRepeatMode#LIMITED}), or {@code -1} for no limit
     * @param expansionKey   the key of the {@link us.eunoians.mcrpg.expansion.ContentExpansion} that provides this definition, or {@code null} for config-loaded definitions
     * @throws IllegalArgumentException if {@code phases} is empty
     */
    public QuestDefinition(@NotNull NamespacedKey questKey,
                           @NotNull NamespacedKey scopeType,
                           @Nullable Duration expiration,
                           @NotNull List<QuestPhaseDefinition> phases,
                           @NotNull List<QuestRewardType> rewards,
                           @NotNull QuestRepeatMode repeatMode,
                           @Nullable Duration repeatCooldown,
                           int repeatLimit,
                           @Nullable NamespacedKey expansionKey) {
        this(questKey, scopeType, expiration, phases, rewards, repeatMode, repeatCooldown, repeatLimit, expansionKey, null);
    }

    /**
     * Creates a new quest definition with optional metadata.
     *
     * @param questKey       the unique key identifying this quest
     * @param scopeType      the key identifying the scope provider for instances of this quest
     * @param expiration     the expiration duration, or {@code null} if instances do not expire
     * @param phases         the ordered list of phase definitions (must contain at least one)
     * @param rewards        the quest-level rewards granted on completion
     * @param repeatMode     how this quest may be repeated (defaults to {@link QuestRepeatMode#ONCE})
     * @param repeatCooldown the cooldown between completions (only used with {@link QuestRepeatMode#COOLDOWN}), or {@code null}
     * @param repeatLimit    the maximum number of completions per player (only used with {@link QuestRepeatMode#LIMITED}), or {@code -1} for no limit
     * @param expansionKey   the key of the {@link us.eunoians.mcrpg.expansion.ContentExpansion} that provides this definition, or {@code null} for config-loaded definitions
     * @param metadata       extensible metadata map, or {@code null} for none
     * @throws IllegalArgumentException if {@code phases} is empty
     */
    public QuestDefinition(@NotNull NamespacedKey questKey,
                           @NotNull NamespacedKey scopeType,
                           @Nullable Duration expiration,
                           @NotNull List<QuestPhaseDefinition> phases,
                           @NotNull List<QuestRewardType> rewards,
                           @NotNull QuestRepeatMode repeatMode,
                           @Nullable Duration repeatCooldown,
                           int repeatLimit,
                           @Nullable NamespacedKey expansionKey,
                           @Nullable Map<NamespacedKey, QuestDefinitionMetadata> metadata) {
        if (phases.isEmpty()) {
            throw new IllegalArgumentException("A quest must have at least one phase");
        }
        this.questKey = questKey;
        this.scopeType = scopeType;
        this.expiration = expiration;
        this.phases = List.copyOf(phases);
        this.rewards = List.copyOf(rewards);
        this.repeatMode = repeatMode;
        this.repeatCooldown = repeatCooldown;
        this.repeatLimit = repeatLimit;
        this.expansionKey = expansionKey;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Collections.emptyMap();
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
     * Gets the localized display name for this quest, resolved through the player's locale chain.
     *
     * @param player the player whose locale chain determines the language
     * @return the localized display name
     */
    @NotNull
    public String getDisplayName(@NotNull McRPGPlayer player) {
        return RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessage(player, getDisplayNameRoute());
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
     * Gets the rewards granted upon completing the entire quest.
     *
     * @return an immutable list of configured quest-level reward types
     */
    @NotNull
    public List<QuestRewardType> getRewards() {
        return rewards;
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
     * Gets the cooldown duration between completions. Only meaningful when the repeat
     * mode is {@link QuestRepeatMode#COOLDOWN}.
     *
     * @return an {@link Optional} containing the cooldown duration, or empty if not set
     */
    @NotNull
    public Optional<Duration> getRepeatCooldown() {
        return Optional.ofNullable(repeatCooldown);
    }

    /**
     * Gets the maximum number of completions per player. Only meaningful when the repeat
     * mode is {@link QuestRepeatMode#LIMITED}.
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
        return phases.stream()
                .flatMap(phase -> phase.getStages().stream())
                .flatMap(stage -> stage.getObjectives().stream())
                .filter(objective -> objective.getObjectiveKey().equals(objectiveKey))
                .findFirst();
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

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.ofNullable(expansionKey);
    }
}
