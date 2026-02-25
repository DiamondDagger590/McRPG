package us.eunoians.mcrpg.gui.board;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BoardGuiModeTest extends McRPGBaseTest {

    @DisplayName("SHARED_AND_PERSONAL enum value exists")
    @Test
    void sharedAndPersonal_exists() {
        assertNotNull(BoardGuiMode.SHARED_AND_PERSONAL);
    }

    @DisplayName("SCOPED enum value exists")
    @Test
    void scoped_exists() {
        assertNotNull(BoardGuiMode.SCOPED);
    }

    @DisplayName("enum contains exactly two values")
    @Test
    void enumValues_containsTwo() {
        assertEquals(2, BoardGuiMode.values().length);
    }
}
