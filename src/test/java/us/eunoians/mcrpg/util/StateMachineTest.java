package us.eunoians.mcrpg.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StateMachineTest {

    private enum TestState {
        A, B, C, D
    }

    @Test
    @DisplayName("Valid transition succeeds and state changes")
    void validTransitionSucceedsAndStateChanges() {
        var transitions = Map.of(
                TestState.A, Set.of(TestState.B),
                TestState.B, Set.of(TestState.C)
        );
        var sm = new StateMachine<>(TestState.A, transitions);

        sm.transitionTo(TestState.B);
        assertEquals(TestState.B, sm.getCurrentState());

        sm.transitionTo(TestState.C);
        assertEquals(TestState.C, sm.getCurrentState());
    }

    @Test
    @DisplayName("Invalid transition throws IllegalStateException with message containing state names")
    void invalidTransitionThrowsExceptionWithStateNames() {
        var transitions = Map.of(TestState.A, Set.of(TestState.B));
        var sm = new StateMachine<>(TestState.A, transitions);

        var ex = assertThrows(IllegalStateException.class, () -> sm.transitionTo(TestState.C));
        assertTrue(ex.getMessage().contains("A"));
        assertTrue(ex.getMessage().contains("C"));
    }

    @Test
    @DisplayName("canTransitionTo returns true for valid, false for invalid")
    void canTransitionToReturnsCorrectly() {
        var transitions = Map.of(TestState.A, Set.of(TestState.B));
        var sm = new StateMachine<>(TestState.A, transitions);

        assertTrue(sm.canTransitionTo(TestState.B));
        assertFalse(sm.canTransitionTo(TestState.C));
    }

    @Test
    @DisplayName("Terminal state rejects all transitions")
    void terminalStateRejectsAllTransitions() {
        Map<TestState, Set<TestState>> transitions = Map.of(
                TestState.A, Set.of(TestState.B),
                TestState.B, Set.of()
        );
        var sm = new StateMachine<>(TestState.A, transitions);
        sm.transitionTo(TestState.B);

        assertFalse(sm.canTransitionTo(TestState.A));
        assertFalse(sm.canTransitionTo(TestState.C));
        assertThrows(IllegalStateException.class, () -> sm.transitionTo(TestState.A));
    }

    @Test
    @DisplayName("Initial state is set correctly")
    void initialStateIsSetCorrectly() {
        var transitions = Map.of(TestState.A, Set.of(TestState.B));
        var sm = new StateMachine<>(TestState.A, transitions);
        assertEquals(TestState.A, sm.getCurrentState());
    }

    @Test
    @DisplayName("Transition from state not in transitions map rejects all")
    void stateNotInTransitionsMapRejectsAll() {
        var transitions = Map.of(TestState.A, Set.of(TestState.B));
        var sm = new StateMachine<>(TestState.A, transitions);
        sm.transitionTo(TestState.B);

        assertFalse(sm.canTransitionTo(TestState.A));
        assertFalse(sm.canTransitionTo(TestState.C));
        assertFalse(sm.canTransitionTo(TestState.D));
        assertThrows(IllegalStateException.class, () -> sm.transitionTo(TestState.C));
    }
}
