package us.eunoians.mcrpg.quest.chain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestChainStateTest {

    @Test
    @DisplayName("Given ACTIVE state, When isTerminal is called, Then it returns false")
    public void isTerminal_returnsFalse_whenActive() {
        assertFalse(QuestChainState.ACTIVE.isTerminal());
    }

    @Test
    @DisplayName("Given COMPLETED state, When isTerminal is called, Then it returns true")
    public void isTerminal_returnsTrue_whenCompleted() {
        assertTrue(QuestChainState.COMPLETED.isTerminal());
    }

    @Test
    @DisplayName("Given ABANDONED state, When isTerminal is called, Then it returns true")
    public void isTerminal_returnsTrue_whenAbandoned() {
        assertTrue(QuestChainState.ABANDONED.isTerminal());
    }

    @Test
    @DisplayName("Given FAILED state, When isTerminal is called, Then it returns true")
    public void isTerminal_returnsTrue_whenFailed() {
        assertTrue(QuestChainState.FAILED.isTerminal());
    }

    @Test
    @DisplayName("Given EXPIRED state, When isTerminal is called, Then it returns true")
    public void isTerminal_returnsTrue_whenExpired() {
        assertTrue(QuestChainState.EXPIRED.isTerminal());
    }

    @Test
    @DisplayName("Given ACTIVE state, When isRepeatEligible is called, Then it returns false")
    public void isRepeatEligible_returnsFalse_whenActive() {
        assertFalse(QuestChainState.ACTIVE.isRepeatEligible());
    }

    @Test
    @DisplayName("Given COMPLETED state, When isRepeatEligible is called, Then it returns true")
    public void isRepeatEligible_returnsTrue_whenCompleted() {
        assertTrue(QuestChainState.COMPLETED.isRepeatEligible());
    }

    @Test
    @DisplayName("Given FAILED state, When isRepeatEligible is called, Then it returns true")
    public void isRepeatEligible_returnsTrue_whenFailed() {
        assertTrue(QuestChainState.FAILED.isRepeatEligible());
    }

    @Test
    @DisplayName("Given EXPIRED state, When isRepeatEligible is called, Then it returns true")
    public void isRepeatEligible_returnsTrue_whenExpired() {
        assertTrue(QuestChainState.EXPIRED.isRepeatEligible());
    }

    @Test
    @DisplayName("Given ABANDONED state, When isRepeatEligible is called, Then it returns true")
    public void isRepeatEligible_returnsTrue_whenAbandoned() {
        assertTrue(QuestChainState.ABANDONED.isRepeatEligible());
    }
}
