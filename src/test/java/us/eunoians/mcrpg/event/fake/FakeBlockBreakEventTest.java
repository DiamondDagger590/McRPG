package us.eunoians.mcrpg.event.fake;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class FakeBlockBreakEventTest extends McRPGBaseTest {

    private FakeBlockBreakEvent event;

    @BeforeEach
    void setUp() {
        Block mockBlock = mock(Block.class);
        Player mockPlayer = mock(Player.class);
        event = new FakeBlockBreakEvent(mockBlock, mockPlayer);
    }

    @Test
    @DisplayName("hasPassedChecks defaults to true")
    void hasPassedChecks_defaultsTrue() {
        assertTrue(event.hasPassedChecks());
    }

    @Test
    @DisplayName("setPassedChecks false makes hasPassedChecks return false")
    void setPassedChecks_false() {
        event.setPassedChecks(false);
        assertFalse(event.hasPassedChecks());
    }

    @Test
    @DisplayName("setPassedChecks is revertible")
    void setPassedChecks_revertible() {
        event.setPassedChecks(false);
        event.setPassedChecks(true);
        assertTrue(event.hasPassedChecks());
    }

    @Test
    @DisplayName("event extends BlockBreakEvent")
    void extendsBlockBreakEvent() {
        assertTrue(event instanceof org.bukkit.event.block.BlockBreakEvent);
    }
}
