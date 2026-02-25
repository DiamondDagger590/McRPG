package us.eunoians.mcrpg.gui.board.slot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.gui.board.QuestBoardGui;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScopedTabSlotTest extends McRPGBaseTest {

    @DisplayName("getValidGuiTypes returns QuestBoardGui")
    @Test
    void getValidGuiTypes_returnsQuestBoardGui() {
        ScopedTabSlot slot = new ScopedTabSlot();
        assertEquals(Set.of(QuestBoardGui.class), slot.getValidGuiTypes());
    }
}
