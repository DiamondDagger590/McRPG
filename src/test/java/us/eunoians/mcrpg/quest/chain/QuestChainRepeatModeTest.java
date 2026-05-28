package us.eunoians.mcrpg.quest.chain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestChainRepeatModeTest {

    @DisplayName("Given 'once', When fromString is called, Then it returns ONCE")
    @Test
    public void fromString_returnsOnce_whenOnce() {
        assertEquals(Optional.of(QuestChainRepeatMode.ONCE), QuestChainRepeatMode.fromString("once"));
    }

    @DisplayName("Given 'cooldown-limited', When fromString is called, Then it returns COOLDOWN_LIMITED")
    @Test
    public void fromString_returnsCooldownLimited_whenHyphenated() {
        assertEquals(Optional.of(QuestChainRepeatMode.COOLDOWN_LIMITED), QuestChainRepeatMode.fromString("cooldown-limited"));
    }

    @DisplayName("Given 'ONCE', When fromString is called, Then it returns ONCE (case insensitive)")
    @Test
    public void fromString_returnsOnce_whenUpperCase() {
        assertEquals(Optional.of(QuestChainRepeatMode.ONCE), QuestChainRepeatMode.fromString("ONCE"));
    }

    @DisplayName("Given 'unlimited', When fromString is called, Then it returns UNLIMITED")
    @Test
    public void fromString_returnsUnlimited_whenUnlimited() {
        assertEquals(Optional.of(QuestChainRepeatMode.UNLIMITED), QuestChainRepeatMode.fromString("unlimited"));
    }

    @DisplayName("Given 'limited', When fromString is called, Then it returns LIMITED")
    @Test
    public void fromString_returnsLimited_whenLimited() {
        assertEquals(Optional.of(QuestChainRepeatMode.LIMITED), QuestChainRepeatMode.fromString("limited"));
    }

    @DisplayName("Given 'cooldown', When fromString is called, Then it returns COOLDOWN")
    @Test
    public void fromString_returnsCooldown_whenCooldown() {
        assertEquals(Optional.of(QuestChainRepeatMode.COOLDOWN), QuestChainRepeatMode.fromString("cooldown"));
    }

    @DisplayName("Given 'invalid', When fromString is called, Then it returns empty")
    @Test
    public void fromString_returnsEmpty_whenInvalid() {
        assertTrue(QuestChainRepeatMode.fromString("invalid").isEmpty());
    }

    @DisplayName("Given 'COOLDOWN_LIMITED', When fromString is called, Then it returns COOLDOWN_LIMITED")
    @Test
    public void fromString_returnsCooldownLimited_whenUnderscored() {
        assertEquals(Optional.of(QuestChainRepeatMode.COOLDOWN_LIMITED), QuestChainRepeatMode.fromString("COOLDOWN_LIMITED"));
    }
}
