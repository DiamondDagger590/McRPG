package us.eunoians.mcrpg.ability.combo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerComboStateTest {

    private PlayerComboState state;

    @BeforeEach
    void setUp() {
        state = new PlayerComboState();
    }

    @Nested
    @DisplayName("Initial state")
    class InitialState {

        @DisplayName("isEmpty returns true on fresh state")
        @Test
        void isEmpty_returnsTrue() {
            assertTrue(state.isEmpty());
        }

        @DisplayName("getSequenceLength returns zero on fresh state")
        @Test
        void getSequenceLength_returnsZero() {
            assertEquals(0, state.getSequenceLength());
        }

        @DisplayName("getCurrentSequence returns empty list on fresh state")
        @Test
        void getCurrentSequence_returnsEmptyList() {
            assertTrue(state.getCurrentSequence().isEmpty());
        }

        @DisplayName("getCompletedSlot returns empty on fresh state")
        @Test
        void getCompletedSlot_returnsEmpty() {
            assertTrue(state.getCompletedSlot().isEmpty());
        }

        @DisplayName("hasAnyValidContinuation returns false on empty sequence")
        @Test
        void hasAnyValidContinuation_returnsFalse() {
            assertFalse(state.hasAnyValidContinuation());
        }

        @DisplayName("getTimeoutTaskId returns -1 on fresh state")
        @Test
        void getTimeoutTaskId_returnsNegativeOne() {
            assertEquals(-1, state.getTimeoutTaskId());
        }
    }

    @Nested
    @DisplayName("addInput")
    class AddInput {

        @DisplayName("single input increments sequence length")
        @Test
        void addInput_incrementsLength() {
            state.addInput(ComboInput.RIGHT);
            assertEquals(1, state.getSequenceLength());
        }

        @DisplayName("multiple inputs accumulate in order")
        @Test
        void addInput_multipleInputs_accumulateInOrder() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.LEFT);
            state.addInput(ComboInput.RIGHT);
            assertEquals(List.of(ComboInput.RIGHT, ComboInput.LEFT, ComboInput.RIGHT), state.getCurrentSequence());
        }

        @DisplayName("isEmpty returns false after adding input")
        @Test
        void addInput_isEmptyReturnsFalse() {
            state.addInput(ComboInput.RIGHT);
            assertFalse(state.isEmpty());
        }
    }

    @Nested
    @DisplayName("getCurrentSequence")
    class GetCurrentSequence {

        @DisplayName("returns defensive copy")
        @Test
        void getCurrentSequence_returnsDefensiveCopy() {
            state.addInput(ComboInput.RIGHT);
            List<ComboInput> snapshot = state.getCurrentSequence();
            state.addInput(ComboInput.LEFT);
            assertEquals(1, snapshot.size());
            assertEquals(2, state.getSequenceLength());
        }
    }

    @Nested
    @DisplayName("clearSequence")
    class ClearSequence {

        @DisplayName("resets sequence to empty")
        @Test
        void clearSequence_resetsToEmpty() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.RIGHT);
            state.clearSequence();
            assertTrue(state.isEmpty());
            assertEquals(0, state.getSequenceLength());
        }

        @DisplayName("does not reset timeout task ID")
        @Test
        void clearSequence_doesNotResetTimeoutTaskId() {
            state.setTimeoutTaskId(42);
            state.addInput(ComboInput.RIGHT);
            state.clearSequence();
            assertEquals(42, state.getTimeoutTaskId());
        }
    }

    @Nested
    @DisplayName("getCompletedSlot")
    class GetCompletedSlot {

        @DisplayName("RRR completes slot 1")
        @Test
        void getCompletedSlot_rrr_completesSlot1() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.RIGHT);
            OptionalInt result = state.getCompletedSlot();
            assertTrue(result.isPresent());
            assertEquals(1, result.getAsInt());
        }

        @DisplayName("RRL completes slot 2")
        @Test
        void getCompletedSlot_rrl_completesSlot2() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.LEFT);
            OptionalInt result = state.getCompletedSlot();
            assertTrue(result.isPresent());
            assertEquals(2, result.getAsInt());
        }

        @DisplayName("RLR completes slot 3")
        @Test
        void getCompletedSlot_rlr_completesSlot3() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.LEFT);
            state.addInput(ComboInput.RIGHT);
            OptionalInt result = state.getCompletedSlot();
            assertTrue(result.isPresent());
            assertEquals(3, result.getAsInt());
        }

        @DisplayName("partial sequence returns empty")
        @Test
        void getCompletedSlot_partialSequence_returnsEmpty() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.RIGHT);
            assertTrue(state.getCompletedSlot().isEmpty());
        }

        @DisplayName("invalid sequence returns empty")
        @Test
        void getCompletedSlot_invalidSequence_returnsEmpty() {
            state.addInput(ComboInput.LEFT);
            state.addInput(ComboInput.LEFT);
            state.addInput(ComboInput.LEFT);
            assertTrue(state.getCompletedSlot().isEmpty());
        }

        @DisplayName("RLL returns empty (no matching pattern)")
        @Test
        void getCompletedSlot_rll_returnsEmpty() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.LEFT);
            state.addInput(ComboInput.LEFT);
            assertTrue(state.getCompletedSlot().isEmpty());
        }
    }

    @Nested
    @DisplayName("hasAnyValidContinuation")
    class HasAnyValidContinuation {

        @DisplayName("R is a valid prefix")
        @Test
        void hasAnyValidContinuation_r_returnsTrue() {
            state.addInput(ComboInput.RIGHT);
            assertTrue(state.hasAnyValidContinuation());
        }

        @DisplayName("RR is a valid prefix")
        @Test
        void hasAnyValidContinuation_rr_returnsTrue() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.RIGHT);
            assertTrue(state.hasAnyValidContinuation());
        }

        @DisplayName("RL is a valid prefix")
        @Test
        void hasAnyValidContinuation_rl_returnsTrue() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.LEFT);
            assertTrue(state.hasAnyValidContinuation());
        }

        @DisplayName("L is not a valid prefix for any pattern")
        @Test
        void hasAnyValidContinuation_l_returnsFalse() {
            state.addInput(ComboInput.LEFT);
            assertFalse(state.hasAnyValidContinuation());
        }

        @DisplayName("complete sequence RRR is still a valid prefix")
        @Test
        void hasAnyValidContinuation_rrr_returnsTrue() {
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.RIGHT);
            state.addInput(ComboInput.RIGHT);
            assertTrue(state.hasAnyValidContinuation());
        }
    }

    @Nested
    @DisplayName("Timeout task ID")
    class TimeoutTaskId {

        @DisplayName("setTimeoutTaskId stores value")
        @Test
        void setTimeoutTaskId_storesValue() {
            state.setTimeoutTaskId(99);
            assertEquals(99, state.getTimeoutTaskId());
        }

        @DisplayName("setTimeoutTaskId can overwrite previous value")
        @Test
        void setTimeoutTaskId_overwritesPrevious() {
            state.setTimeoutTaskId(10);
            state.setTimeoutTaskId(20);
            assertEquals(20, state.getTimeoutTaskId());
        }

        @DisplayName("setTimeoutTaskId accepts zero")
        @Test
        void setTimeoutTaskId_acceptsZero() {
            state.setTimeoutTaskId(0);
            assertEquals(0, state.getTimeoutTaskId());
        }
    }
}
