package us.eunoians.mcrpg.entity.player;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.display.impl.PlayerDisplay;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the generic per-player {@link PlayerDisplay} container surface on
 * {@link McRPGPlayer}. These tests lock in the invariants the
 * {@link us.eunoians.mcrpg.display.DisplayManager} relies on (type-keyed lookup,
 * cleanup-on-replace, snapshot reuse) so refactors to the display API don't
 * silently change semantics for every downstream display caller.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class McRPGPlayerDisplayContainerTest extends McRPGBaseTest {

    /**
     * Minimal {@link PlayerDisplay} that records whether
     * {@link #cleanDisplay()} has been invoked. Used to assert
     * set-replacement and remove-driven cleanup on the player's display
     * container.
     */
    private static final class RecordingDisplay extends PlayerDisplay {

        boolean cleaned;

        RecordingDisplay(McRPGPlayer player) {
            super(player);
        }

        @Override
        public void cleanDisplay() {
            cleaned = true;
        }
    }

    @Test
    @DisplayName("Given a player with no registered displays, when getDisplay is called, then an empty Optional is returned")
    void getDisplay_returnsEmpty_whenNoDisplayRegistered(McRPGPlayer mcRPGPlayer) {
        Optional<RecordingDisplay> display = mcRPGPlayer.getDisplay(RecordingDisplay.class);

        assertFalse(display.isPresent());
        assertFalse(mcRPGPlayer.hasDisplay(RecordingDisplay.class));
    }

    @Test
    @DisplayName("Given a display registered under a type, when the same type is queried, then the registered display is returned")
    void getDisplay_returnsRegisteredInstance_whenDisplayRegisteredUnderType(McRPGPlayer mcRPGPlayer) {
        RecordingDisplay display = new RecordingDisplay(mcRPGPlayer);
        mcRPGPlayer.setDisplay(RecordingDisplay.class, display);

        Optional<RecordingDisplay> resolved = mcRPGPlayer.getDisplay(RecordingDisplay.class);

        assertTrue(resolved.isPresent());
        assertSame(display, resolved.get());
        assertTrue(mcRPGPlayer.hasDisplay(RecordingDisplay.class));
    }

    @Test
    @DisplayName("Given an existing display at a type, when setDisplay registers a new instance, then the previous instance is cleaned")
    void setDisplay_cleansPreviousInstance_whenReplacingSameType(McRPGPlayer mcRPGPlayer) {
        RecordingDisplay first = new RecordingDisplay(mcRPGPlayer);
        RecordingDisplay second = new RecordingDisplay(mcRPGPlayer);
        mcRPGPlayer.setDisplay(RecordingDisplay.class, first);

        mcRPGPlayer.setDisplay(RecordingDisplay.class, second);

        assertTrue(first.cleaned, "Replaced display should have cleanDisplay invoked");
        assertFalse(second.cleaned, "New display should not be cleaned on set");
        assertSame(second, mcRPGPlayer.getDisplay(RecordingDisplay.class).orElseThrow());
    }

    @Test
    @DisplayName("Given the same display instance is set twice, when setDisplay is called with the same reference, then cleanDisplay is not invoked on it")
    void setDisplay_doesNotClean_whenSameInstanceReRegistered(McRPGPlayer mcRPGPlayer) {
        RecordingDisplay display = new RecordingDisplay(mcRPGPlayer);
        mcRPGPlayer.setDisplay(RecordingDisplay.class, display);

        mcRPGPlayer.setDisplay(RecordingDisplay.class, display);

        assertFalse(display.cleaned,
                "Re-registering the same reference should not trigger cleanup");
    }

    @Test
    @DisplayName("Given a registered display, when removeDisplay is called, then the display is cleaned and cleared from the container")
    void removeDisplay_cleansAndClears_whenDisplayWasRegistered(McRPGPlayer mcRPGPlayer) {
        RecordingDisplay display = new RecordingDisplay(mcRPGPlayer);
        mcRPGPlayer.setDisplay(RecordingDisplay.class, display);

        mcRPGPlayer.removeDisplay(RecordingDisplay.class);

        assertTrue(display.cleaned);
        assertFalse(mcRPGPlayer.hasDisplay(RecordingDisplay.class));
    }

    @Test
    @DisplayName("Given no registered displays, when snapshotDisplaysInto is called, then the sink is left untouched")
    void snapshotDisplaysInto_leavesSinkEmpty_whenContainerEmpty(McRPGPlayer mcRPGPlayer) {
        List<PlayerDisplay> sink = new ArrayList<>();

        mcRPGPlayer.snapshotDisplaysInto(sink);

        assertTrue(sink.isEmpty(),
                "Empty container must not touch the caller-owned snapshot buffer");
    }

    @Test
    @DisplayName("Given registered displays, when snapshotDisplaysInto populates a caller-owned buffer, then every registered display is appended")
    void snapshotDisplaysInto_appendsAllRegisteredDisplays_whenContainerPopulated(McRPGPlayer mcRPGPlayer) {
        RecordingDisplay display = new RecordingDisplay(mcRPGPlayer);
        mcRPGPlayer.setDisplay(RecordingDisplay.class, display);

        List<PlayerDisplay> sink = new ArrayList<>();
        mcRPGPlayer.snapshotDisplaysInto(sink);

        assertEquals(1, sink.size());
        assertSame(display, sink.get(0));
    }

    @Test
    @DisplayName("Given multiple registered displays, when clearAllDisplays is invoked, then every display is cleaned and the container is emptied")
    void clearAllDisplays_cleansEveryDisplayAndEmptiesContainer_whenContainerPopulated(McRPGPlayer mcRPGPlayer) {
        RecordingDisplay display = new RecordingDisplay(mcRPGPlayer);
        mcRPGPlayer.setDisplay(RecordingDisplay.class, display);

        mcRPGPlayer.clearAllDisplays();

        assertTrue(display.cleaned);
        assertFalse(mcRPGPlayer.hasDisplay(RecordingDisplay.class));
    }
}
