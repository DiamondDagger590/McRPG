package us.eunoians.mcrpg.display.hud.content;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the tick-based expiry contract for {@link TimedCenterContent} — the
 * priority resolver relies on {@code render()} returning empty at/after
 * {@code expiryTick} so transient slots (XP gain, safe-zone flash,
 * not-enough-mana) free themselves without explicit callers.
 */
public class TimedCenterContentTest {

    @Test
    @DisplayName("Given the current tick is before the expiry tick, when render is called, then it returns the stored component")
    void render_returnsStoredComponent_whenBeforeExpiryTick() {
        Component body = Component.text("+12 XP");
        TimedCenterContent content = new TimedCenterContent(body, 100L);

        Optional<Component> rendered = content.render(99L);

        assertTrue(rendered.isPresent());
        assertSame(body, rendered.get());
    }

    @Test
    @DisplayName("Given the current tick equals the expiry tick, when render is called, then it returns empty so the slot self-evicts")
    void render_returnsEmpty_whenCurrentTickEqualsExpiry() {
        TimedCenterContent content = new TimedCenterContent(Component.text("+12 XP"), 100L);

        assertFalse(content.render(100L).isPresent());
    }

    @Test
    @DisplayName("Given the current tick is past the expiry tick, when render is called, then it still returns empty (no resurrection on overshoot)")
    void render_returnsEmpty_whenCurrentTickPastExpiry() {
        TimedCenterContent content = new TimedCenterContent(Component.text("+12 XP"), 100L);

        assertFalse(content.render(1_000L).isPresent());
    }
}
