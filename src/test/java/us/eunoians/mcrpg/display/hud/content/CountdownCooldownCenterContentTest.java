package us.eunoians.mcrpg.display.hud.content;

import com.diamonddagger590.mccore.util.TimeProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Drives {@link CountdownCooldownCenterContent} against the spy-backed
 * {@link TimeProvider} wired up by the McRPG bootstrap, stepping the virtual
 * clock via Mockito to verify:
 * <ul>
 *   <li>the rendered countdown tracks the remaining time;</li>
 *   <li>the slot self-evicts (empty {@link Optional}) once the cooldown ends,
 *       so the HUD's priority resolver can reveal lower-priority content;</li>
 *   <li>the displayed seconds never drop below 1 while the slot is still
 *       alive — preventing a distracting "0s" flash on the final frame.</li>
 * </ul>
 */
public class CountdownCooldownCenterContentTest extends McRPGBaseTest {

    private TimeProvider timeProvider;

    @BeforeEach
    void setUp() {
        timeProvider = McRPG.getInstance().getTimeProvider();
    }

    @AfterEach
    void tearDown() {
        // The bootstrap-shared TimeProvider is a Mockito spy; clear any
        // `when(...)` stubs so the next test class starts from the real clock
        // instead of inheriting stale time returns.
        reset(timeProvider);
    }

    private String plain(Component component) {
        StringBuilder sb = new StringBuilder();
        ComponentFlattener.basic().flatten(component, sb::append);
        return sb.toString();
    }

    @Test
    @DisplayName("Given the cooldown is still active, when render is called, then the countdown component contains the remaining seconds")
    void render_returnsComponentWithRemainingSeconds_whenCooldownStillActive() {
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(1_000L));
        CountdownCooldownCenterContent content = new CountdownCooldownCenterContent("Bleed", 4_000L, timeProvider);

        Optional<Component> rendered = content.render(0L);

        assertTrue(rendered.isPresent());
        assertTrue(plain(rendered.get()).contains("3s"));
    }

    @Test
    @DisplayName("Given the clock advances between frames, when render is called repeatedly, then the countdown steps down each second")
    void render_stepsCountdown_whenClockAdvances() {
        CountdownCooldownCenterContent content = new CountdownCooldownCenterContent("Bleed", 3_000L, timeProvider);

        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(0L));
        assertTrue(plain(content.render(0L).orElseThrow()).contains("3s"));

        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(1_000L));
        assertTrue(plain(content.render(0L).orElseThrow()).contains("2s"));

        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(2_000L));
        assertTrue(plain(content.render(0L).orElseThrow()).contains("1s"));
    }

    @Test
    @DisplayName("Given less than one second remains on the cooldown, when render is called, then the displayed countdown floors at 1s")
    void render_floorsToOneSecond_whenPartialSecondRemains() {
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(2_500L));
        CountdownCooldownCenterContent content = new CountdownCooldownCenterContent("Bleed", 3_000L, timeProvider);

        assertTrue(plain(content.render(0L).orElseThrow()).contains("1s"));
    }

    @Test
    @DisplayName("Given the clock is exactly at expiry, when render is called, then it returns empty so the slot self-evicts")
    void render_returnsEmpty_whenClockAtExpiry() {
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(3_000L));
        CountdownCooldownCenterContent content = new CountdownCooldownCenterContent("Bleed", 3_000L, timeProvider);

        assertFalse(content.render(0L).isPresent());
    }

    @Test
    @DisplayName("Given the clock is past expiry, when render is called, then it returns empty")
    void render_returnsEmpty_whenClockPastExpiry() {
        when(timeProvider.now()).thenReturn(Instant.ofEpochMilli(5_000L));
        CountdownCooldownCenterContent content = new CountdownCooldownCenterContent("Bleed", 3_000L, timeProvider);

        assertFalse(content.render(0L).isPresent());
    }

    @Test
    @DisplayName("Given the content was constructed with an ability name and expiry, when the getters are called, then they return those values")
    void getters_returnConstructorValues() {
        CountdownCooldownCenterContent content = new CountdownCooldownCenterContent("Bleed", 3_000L, timeProvider);

        assertEquals("Bleed", content.getAbilityName());
        assertEquals(3_000L, content.getExpiryEpochMillis());
    }
}
