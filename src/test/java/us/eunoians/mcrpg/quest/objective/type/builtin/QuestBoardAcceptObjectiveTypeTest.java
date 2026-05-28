package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.board.BoardOfferingAcceptEvent;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.QuestBoard;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QuestBoardAcceptObjectiveTypeTest extends McRPGBaseTest {

    private QuestBoardAcceptObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new QuestBoardAcceptObjectiveType();
    }

    @Test
    @DisplayName("Given the type, when getKey is called, then it returns quest_board_accept")
    public void getKey_returnsExpectedKey() {
        assertEquals(QuestBoardAcceptObjectiveType.KEY, type.getKey());
    }

    @Test
    @DisplayName("Given the type, when getExpansionKey is called, then it returns McRPG expansion key")
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @Test
    @DisplayName("Given QuestBoardAcceptQuestContext, when canProcess is called, then it returns true")
    public void canProcess_correctContextType_returnsTrue() {
        assertTrue(type.canProcess(mock(QuestBoardAcceptQuestContext.class)));
    }

    @Test
    @DisplayName("Given wrong context, when canProcess is called, then it returns false")
    public void canProcess_wrongContextType_returnsFalse() {
        assertFalse(type.canProcess(mock(BlockBreakQuestContext.class)));
    }

    @Test
    @DisplayName("Given board accept event with no filter, when processProgress is called, then it returns 1")
    public void processProgress_noBoardFilter_returnsOne() {
        BoardOfferingAcceptEvent event = mockBoardAcceptEvent(new NamespacedKey("mcrpg", "default"));
        assertEquals(1, type.processProgress(mock(QuestObjectiveInstance.class), new QuestBoardAcceptQuestContext(event)));
    }

    @Test
    @DisplayName("Given matching board filter, when processProgress is called, then it returns 1")
    public void processProgress_matchingBoardFilter_returnsOne() {
        Section section = mock(Section.class);
        when(section.contains("board")).thenReturn(true);
        when(section.getString("board")).thenReturn("mcrpg:default");
        QuestBoardAcceptObjectiveType configured = type.parseConfig(section);

        BoardOfferingAcceptEvent event = mockBoardAcceptEvent(new NamespacedKey("mcrpg", "default"));
        assertEquals(1, configured.processProgress(mock(QuestObjectiveInstance.class), new QuestBoardAcceptQuestContext(event)));
    }

    @Test
    @DisplayName("Given board filter mismatch, when processProgress is called, then it returns 0")
    public void processProgress_boardFilterMismatch_returnsZero() {
        Section section = mock(Section.class);
        when(section.contains("board")).thenReturn(true);
        when(section.getString("board")).thenReturn("mcrpg:default");
        QuestBoardAcceptObjectiveType configured = type.parseConfig(section);

        BoardOfferingAcceptEvent event = mockBoardAcceptEvent(new NamespacedKey("mcrpg", "other"));
        assertEquals(0, configured.processProgress(mock(QuestObjectiveInstance.class), new QuestBoardAcceptQuestContext(event)));
    }

    @NotNull
    private BoardOfferingAcceptEvent mockBoardAcceptEvent(@NotNull NamespacedKey boardKey) {
        QuestBoard board = mock(QuestBoard.class);
        when(board.getBoardKey()).thenReturn(boardKey);
        Player player = mock(Player.class);
        BoardOfferingAcceptEvent event = mock(BoardOfferingAcceptEvent.class);
        when(event.getBoard()).thenReturn(board);
        when(event.getPlayer()).thenReturn(player);
        return event;
    }
}
