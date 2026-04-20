package us.eunoians.mcrpg.display.hud.content;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the "never self-evicts" contract for {@link IndefiniteCenterContent}:
 * combo dots must keep rendering across arbitrary tick values until the owning
 * caller clears the slot, so no expiry check may leak into the renderer.
 */
public class IndefiniteCenterContentTest {

    @Test
    @DisplayName("Given any tick value, when render is called, then it returns the stored component without self-evicting")
    void render_returnsStoredComponent_whenCalledAtAnyTick() {
        Component body = Component.text("⬤ ⬤ ○");
        IndefiniteCenterContent content = new IndefiniteCenterContent(body);

        assertTrue(content.render(0L).isPresent());
        assertTrue(content.render(Long.MAX_VALUE).isPresent());
        assertSame(body, content.render(1_000L).orElseThrow());
    }
}
