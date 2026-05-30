package us.eunoians.mcrpg.quest.chain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestChainRepeatModeTest {

    @Test
    @DisplayName("Given 'once', When fromString is called, Then it returns ONCE")
    public void fromString_returnsOnce_whenOnce() {
        assertEquals(Optional.of(QuestChainRepeatMode.ONCE), QuestChainRepeatMode.fromString("once"));
    }

    @Test
    @DisplayName("Given 'cooldown-limited', When fromString is called, Then it returns COOLDOWN_LIMITED")
    public void fromString_returnsCooldownLimited_whenHyphenated() {
        assertEquals(Optional.of(QuestChainRepeatMode.COOLDOWN_LIMITED), QuestChainRepeatMode.fromString("cooldown-limited"));
    }

    @Test
    @DisplayName("Given 'ONCE', When fromString is called, Then it returns ONCE (case insensitive)")
    public void fromString_returnsOnce_whenUpperCase() {
        assertEquals(Optional.of(QuestChainRepeatMode.ONCE), QuestChainRepeatMode.fromString("ONCE"));
    }

    @Test
    @DisplayName("Given 'unlimited', When fromString is called, Then it returns UNLIMITED")
    public void fromString_returnsUnlimited_whenUnlimited() {
        assertEquals(Optional.of(QuestChainRepeatMode.UNLIMITED), QuestChainRepeatMode.fromString("unlimited"));
    }

    @Test
    @DisplayName("Given 'limited', When fromString is called, Then it returns LIMITED")
    public void fromString_returnsLimited_whenLimited() {
        assertEquals(Optional.of(QuestChainRepeatMode.LIMITED), QuestChainRepeatMode.fromString("limited"));
    }

    @Test
    @DisplayName("Given 'cooldown', When fromString is called, Then it returns COOLDOWN")
    public void fromString_returnsCooldown_whenCooldown() {
        assertEquals(Optional.of(QuestChainRepeatMode.COOLDOWN), QuestChainRepeatMode.fromString("cooldown"));
    }

    @Test
    @DisplayName("Given 'invalid', When fromString is called, Then it returns empty")
    public void fromString_returnsEmpty_whenInvalid() {
        assertTrue(QuestChainRepeatMode.fromString("invalid").isEmpty());
    }

    @Test
    @DisplayName("Given 'COOLDOWN_LIMITED', When fromString is called, Then it returns COOLDOWN_LIMITED")
    public void fromString_returnsCooldownLimited_whenUnderscored() {
        assertEquals(Optional.of(QuestChainRepeatMode.COOLDOWN_LIMITED), QuestChainRepeatMode.fromString("COOLDOWN_LIMITED"));
    }
}
