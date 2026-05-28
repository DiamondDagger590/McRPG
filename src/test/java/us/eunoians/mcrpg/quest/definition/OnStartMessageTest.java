package us.eunoians.mcrpg.quest.definition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OnStartMessageTest {

    @Test
    @DisplayName("Given a locale key, when fromLocaleKey is called, then localeKey returns the key")
    public void fromLocaleKey_setsLocaleKey() {
        OnStartMessage msg = OnStartMessage.fromLocaleKey("tutorial.quest.started");
        assertTrue(msg.localeKey().isPresent());
        assertEquals("tutorial.quest.started", msg.localeKey().get());
    }

    @Test
    @DisplayName("Given a locale key, when fromLocaleKey is called, then inlineMessages returns an empty list")
    public void fromLocaleKey_setsEmptyInlineMessages() {
        OnStartMessage msg = OnStartMessage.fromLocaleKey("tutorial.quest.started");
        assertTrue(msg.inlineMessages().isEmpty());
    }

    @Test
    @DisplayName("Given inline messages, when fromInline is called, then inlineMessages returns all messages in order")
    public void fromInline_setsInlineMessages() {
        List<String> messages = List.of("<primary>Quest started!", "<body>Good luck.");
        OnStartMessage msg = OnStartMessage.fromInline(messages);
        assertEquals(messages, msg.inlineMessages());
    }

    @Test
    @DisplayName("Given inline messages, when fromInline is called, then localeKey returns empty")
    public void fromInline_setsEmptyLocaleKey() {
        OnStartMessage msg = OnStartMessage.fromInline(List.of("<primary>Hi"));
        assertTrue(msg.localeKey().isEmpty());
    }

    @Test
    @DisplayName("Given inline messages, when fromInline is called, then the returned list is defensively copied")
    public void fromInline_makesDefensiveCopy() {
        List<String> original = new java.util.ArrayList<>(List.of("<primary>Hello"));
        OnStartMessage msg = OnStartMessage.fromInline(original);
        original.add("<body>mutated");
        assertEquals(1, msg.inlineMessages().size(), "inline messages list should not reflect post-construction mutations");
    }
}
