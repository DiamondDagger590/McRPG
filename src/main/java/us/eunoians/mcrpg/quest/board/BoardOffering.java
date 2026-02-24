package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.util.StateMachine;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Represents one offering slot on the quest board.
 * <p>
 * State transitions are enforced by an internal {@link StateMachine} using the
 * transition table defined in {@link #TRANSITIONS}.
 * Valid transitions: {@code VISIBLE→ACCEPTED}, {@code VISIBLE→EXPIRED},
 * {@code ACCEPTED→COMPLETED}, {@code ACCEPTED→ABANDONED}, {@code ACCEPTED→EXPIRED}.
 */
public class BoardOffering {

    public enum State {
        VISIBLE, ACCEPTED, COMPLETED, EXPIRED, ABANDONED
    }

    /** Transition table fed into the {@link StateMachine} to enforce valid state changes. */
    private static final Map<State, Set<State>> TRANSITIONS = Map.of(
            State.VISIBLE, Set.of(State.ACCEPTED, State.EXPIRED),
            State.ACCEPTED, Set.of(State.COMPLETED, State.ABANDONED, State.EXPIRED),
            State.COMPLETED, Set.of(),
            State.EXPIRED, Set.of(),
            State.ABANDONED, Set.of()
    );

    private final UUID offeringId;
    private final UUID rotationId;
    private final NamespacedKey categoryKey;
    private final int slotIndex;
    private final NamespacedKey questDefinitionKey;
    private final NamespacedKey rarityKey;
    private final String scopeTargetId;
    private final StateMachine<State> stateMachine;
    private final Duration completionTime;
    private Long acceptedAt;
    private UUID questInstanceUUID;

    /**
     * Creates a new offering in the {@code VISIBLE} state.
     *
     * @param offeringId         unique identifier for this offering
     * @param rotationId         the rotation this offering belongs to
     * @param categoryKey        the category that generated this offering
     * @param slotIndex          positional index on the board
     * @param questDefinitionKey the quest definition this offering represents
     * @param rarityKey          the rolled rarity tier
     * @param scopeTargetId      the scope target for scoped categories, or {@code null} for shared
     * @param completionTime     time limit after acceptance
     */
    public BoardOffering(@NotNull UUID offeringId,
                         @NotNull UUID rotationId,
                         @NotNull NamespacedKey categoryKey,
                         int slotIndex,
                         @NotNull NamespacedKey questDefinitionKey,
                         @NotNull NamespacedKey rarityKey,
                         @Nullable String scopeTargetId,
                         @NotNull Duration completionTime) {
        this(offeringId, rotationId, categoryKey, slotIndex, questDefinitionKey,
                rarityKey, scopeTargetId, completionTime, State.VISIBLE, null, null);
    }

    /**
     * Reconstruction constructor for loading from the database.
     *
     * @param offeringId         unique identifier for this offering
     * @param rotationId         the rotation this offering belongs to
     * @param categoryKey        the category that generated this offering
     * @param slotIndex          positional index on the board
     * @param questDefinitionKey the quest definition this offering represents
     * @param rarityKey          the rolled rarity tier
     * @param scopeTargetId      the scope target, or {@code null} for shared
     * @param completionTime     time limit after acceptance
     * @param initialState       the persisted state
     * @param acceptedAt         epoch millis when accepted, or {@code null} if not yet accepted
     * @param questInstanceUUID  the quest instance created on acceptance, or {@code null}
     */
    public BoardOffering(@NotNull UUID offeringId,
                         @NotNull UUID rotationId,
                         @NotNull NamespacedKey categoryKey,
                         int slotIndex,
                         @NotNull NamespacedKey questDefinitionKey,
                         @NotNull NamespacedKey rarityKey,
                         @Nullable String scopeTargetId,
                         @NotNull Duration completionTime,
                         @NotNull State initialState,
                         @Nullable Long acceptedAt,
                         @Nullable UUID questInstanceUUID) {
        this.offeringId = offeringId;
        this.rotationId = rotationId;
        this.categoryKey = categoryKey;
        this.slotIndex = slotIndex;
        this.questDefinitionKey = questDefinitionKey;
        this.rarityKey = rarityKey;
        this.scopeTargetId = scopeTargetId;
        this.completionTime = completionTime;
        this.stateMachine = new StateMachine<>(initialState, TRANSITIONS);
        this.acceptedAt = acceptedAt;
        this.questInstanceUUID = questInstanceUUID;
    }

    /**
     * Accepts this offering, transitioning to {@link State#ACCEPTED} and recording
     * the acceptance timestamp and quest instance that was created.
     *
     * @param acceptedAt        epoch millis when the offering was accepted
     * @param questInstanceUUID the UUID of the quest instance created for this acceptance
     * @throws IllegalStateException if the offering cannot transition to ACCEPTED
     */
    public void accept(long acceptedAt, @NotNull UUID questInstanceUUID) {
        stateMachine.transitionTo(State.ACCEPTED);
        this.acceptedAt = acceptedAt;
        this.questInstanceUUID = questInstanceUUID;
    }

    /**
     * Transitions this offering to the given state, validating against the
     * allowed transition table.
     *
     * @param newState the target state
     * @throws IllegalStateException if the transition is not valid
     */
    public void transitionTo(@NotNull State newState) {
        stateMachine.transitionTo(newState);
    }

    /**
     * Checks whether transitioning to the given state is valid from the current state.
     *
     * @param newState the target state to check
     * @return {@code true} if the transition is allowed
     */
    public boolean canTransitionTo(@NotNull State newState) {
        return stateMachine.canTransitionTo(newState);
    }

    /**
     * Returns the current state of this offering.
     *
     * @return the current state
     */
    @NotNull
    public State getState() {
        return stateMachine.getCurrentState();
    }

    /**
     * Returns the unique identifier for this offering.
     *
     * @return the offering UUID
     */
    @NotNull
    public UUID getOfferingId() {
        return offeringId;
    }

    /**
     * Returns the rotation this offering belongs to.
     *
     * @return the rotation UUID
     */
    @NotNull
    public UUID getRotationId() {
        return rotationId;
    }

    /**
     * Returns the category that generated this offering.
     *
     * @return the category key
     */
    @NotNull
    public NamespacedKey getCategoryKey() {
        return categoryKey;
    }

    /**
     * Returns the positional index of this offering on the board.
     *
     * @return the zero-based slot index
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    /**
     * Returns the quest definition this offering represents.
     *
     * @return the quest definition key
     */
    @NotNull
    public NamespacedKey getQuestDefinitionKey() {
        return questDefinitionKey;
    }

    /**
     * Returns the rolled rarity tier for this offering.
     *
     * @return the rarity key
     */
    @NotNull
    public NamespacedKey getRarityKey() {
        return rarityKey;
    }

    /**
     * Returns the scope target identifier for scoped categories (e.g. a land UUID).
     * Empty for shared offerings.
     *
     * @return the scope target, or empty if this is a shared offering
     */
    @NotNull
    public Optional<String> getScopeTargetId() {
        return Optional.ofNullable(scopeTargetId);
    }

    /**
     * Returns the time limit players have to complete this offering after acceptance.
     *
     * @return the completion time window
     */
    @NotNull
    public Duration getCompletionTime() {
        return completionTime;
    }

    /**
     * Returns the epoch millis when this offering was accepted. Only populated
     * after {@link #accept(long, UUID)} is called.
     *
     * @return the acceptance timestamp, or empty if not yet accepted
     */
    @NotNull
    public Optional<Long> getAcceptedAt() {
        return Optional.ofNullable(acceptedAt);
    }

    /**
     * Returns the quest instance UUID created when this offering was accepted.
     * Only populated after {@link #accept(long, UUID)} is called.
     *
     * @return the quest instance UUID, or empty if not yet accepted
     */
    @NotNull
    public Optional<UUID> getQuestInstanceUUID() {
        return Optional.ofNullable(questInstanceUUID);
    }
}
