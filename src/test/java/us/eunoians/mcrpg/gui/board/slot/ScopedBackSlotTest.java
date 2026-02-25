package us.eunoians.mcrpg.gui.board.slot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScopedBackSlotTest extends McRPGBaseTest {

    @DisplayName("getValidGuiTypes returns QuestBoardGui")
    @Test
    void getValidGuiTypes_returnsQuestBoardGui() {
        ScopedBackSlot slot = new ScopedBackSlot();
        assertEquals(Set.of(QuestBoardGui.class), slot.getValidGuiTypes());
    }
}
