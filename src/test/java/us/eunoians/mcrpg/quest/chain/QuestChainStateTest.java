package us.eunoians.mcrpg.quest.chain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestChainStateTest {

    @DisplayName("Given ACTIVE state, When isTerminal is called, Then it returns false")
    @Test
    public void isTerminal_returnsFalse_whenActive() {
        assertFalse(QuestChainState.ACTIVE.isTerminal());
    }

    @DisplayName("Given COMPLETED state, When isTerminal is called, Then it returns true")
    @Test
    public void isTerminal_returnsTrue_whenCompleted() {
        assertTrue(QuestChainState.COMPLETED.isTerminal());
    }

    @DisplayName("Given ABANDONED state, When isTerminal is called, Then it returns true")
    @Test
    public void isTerminal_returnsTrue_whenAbandoned() {
        assertTrue(QuestChainState.ABANDONED.isTerminal());
    }

    @DisplayName("Given FAILED state, When isTerminal is called, Then it returns true")
    @Test
    public void isTerminal_returnsTrue_whenFailed() {
        assertTrue(QuestChainState.FAILED.isTerminal());
    }

    @DisplayName("Given EXPIRED state, When isTerminal is called, Then it returns true")
    @Test
    public void isTerminal_returnsTrue_whenExpired() {
        assertTrue(QuestChainState.EXPIRED.isTerminal());
    }

    @DisplayName("Given ACTIVE state, When isRepeatEligible is called, Then it returns false")
    @Test
    public void isRepeatEligible_returnsFalse_whenActive() {
        assertFalse(QuestChainState.ACTIVE.isRepeatEligible());
    }

    @DisplayName("Given COMPLETED state, When isRepeatEligible is called, Then it returns true")
    @Test
    public void isRepeatEligible_returnsTrue_whenCompleted() {
        assertTrue(QuestChainState.COMPLETED.isRepeatEligible());
    }

    @DisplayName("Given FAILED state, When isRepeatEligible is called, Then it returns true")
    @Test
    public void isRepeatEligible_returnsTrue_whenFailed() {
        assertTrue(QuestChainState.FAILED.isRepeatEligible());
    }

    @DisplayName("Given EXPIRED state, When isRepeatEligible is called, Then it returns true")
    @Test
    public void isRepeatEligible_returnsTrue_whenExpired() {
        assertTrue(QuestChainState.EXPIRED.isRepeatEligible());
    }

    @DisplayName("Given ABANDONED state, When isRepeatEligible is called, Then it returns true")
    @Test
    public void isRepeatEligible_returnsTrue_whenAbandoned() {
        assertTrue(QuestChainState.ABANDONED.isRepeatEligible());
    }
}
