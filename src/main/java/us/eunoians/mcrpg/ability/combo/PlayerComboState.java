package us.eunoians.mcrpg.ability.combo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * Tracks the in-progress combo sequence for a single player.
 * <p>
 * Instances are managed by {@link ComboTracker}. State is mutable and not thread-safe
 * (all access occurs on the main server thread via Bukkit events).
 */
public class PlayerComboState {

    private final List<ComboInput> currentSequence = new ArrayList<>();
    private int timeoutTaskId = -1;

    /**
     * Appends an input to the current combo sequence.
     *
     * @param input The {@link ComboInput} to append.
     */
    public void addInput(@NotNull ComboInput input) {
        currentSequence.add(input);
    }

    /**
     * Returns a snapshot of the current input sequence.
     */
    @NotNull
    public List<ComboInput> getCurrentSequence() {
        return List.copyOf(currentSequence);
    }

    /**
     * Returns how many inputs have been entered so far.
     */
    public int getSequenceLength() {
        return currentSequence.size();
    }

    /**
     * Clears the current input sequence without touching the timeout task.
     * Use {@link ComboTracker#resetState(java.util.UUID)} to also cancel the timeout.
     */
    public void clearSequence() {
        currentSequence.clear();
    }

    /**
     * Checks whether the current sequence exactly completes one of the known patterns.
     *
     * @return An {@link OptionalInt} containing the 1-based slot index if a match is found,
     *         or empty if the sequence is not yet complete or doesn't match any pattern.
     */
    @NotNull
    public OptionalInt getCompletedSlot() {
        for (ComboPattern pattern : ComboPattern.values()) {
            if (pattern.isCompleteMatch(currentSequence)) {
                return OptionalInt.of(pattern.getSlotIndex());
            }
        }
        return OptionalInt.empty();
    }

    /**
     * Checks whether the current sequence is still a valid prefix of at least one pattern.
     */
    public boolean hasAnyValidContinuation() {
        for (ComboPattern pattern : ComboPattern.values()) {
            if (pattern.isValidPrefix(currentSequence)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if no inputs have been entered yet.
     */
    public boolean isEmpty() {
        return currentSequence.isEmpty();
    }

    // --- Timeout task management ---

    /**
     * Returns the Bukkit scheduler task ID for the current timeout, or {@code -1} if none.
     */
    public int getTimeoutTaskId() {
        return timeoutTaskId;
    }

    /**
     * Sets the Bukkit scheduler task ID for the current timeout.
     */
    public void setTimeoutTaskId(int taskId) {
        this.timeoutTaskId = taskId;
    }
}
