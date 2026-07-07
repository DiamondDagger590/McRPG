package us.eunoians.mcrpg.event.board;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.BoardOffering;
import us.eunoians.mcrpg.quest.board.BoardRotation;
import us.eunoians.mcrpg.quest.board.QuestBoard;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BoardEventCoverageTest extends McRPGBaseTest {

    private static final NamespacedKey BOARD_KEY = new NamespacedKey("mcrpg", "default_board");

    private QuestBoard mockBoard;
    private BoardRotation mockRotation;

    @BeforeEach
    void setUp() {
        mockBoard = mock(QuestBoard.class);
        when(mockBoard.getBoardKey()).thenReturn(BOARD_KEY);
        mockRotation = mock(BoardRotation.class);
        when(mockRotation.getRotationId()).thenReturn(UUID.randomUUID());
    }

    @Nested
    @DisplayName("BoardOfferingAcceptEvent")
    class OfferingAcceptEvent {

        private Player mockPlayer;
        private BoardOffering mockOffering;
        private BoardOfferingAcceptEvent event;

        @BeforeEach
        void setUp() {
            mockPlayer = mock(Player.class);
            mockOffering = mock(BoardOffering.class);
            event = new BoardOfferingAcceptEvent(mockBoard, mockPlayer, mockOffering);
        }

        @Test
        @DisplayName("getBoard returns constructor board")
        void getBoard_returnsConstructorBoard() {
            assertSame(mockBoard, event.getBoard());
        }

        @Test
        @DisplayName("getBoardKey delegates to board")
        void getBoardKey_delegatesToBoard() {
            assertEquals(BOARD_KEY, event.getBoardKey());
        }

        @Test
        @DisplayName("getPlayer returns constructor player")
        void getPlayer_returnsConstructorPlayer() {
            assertSame(mockPlayer, event.getPlayer());
        }

        @Test
        @DisplayName("getOffering returns constructor offering")
        void getOffering_returnsConstructorOffering() {
            assertSame(mockOffering, event.getOffering());
        }

        @Test
        @DisplayName("isCancelled defaults to false")
        void isCancelled_defaultsFalse() {
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled true makes isCancelled return true")
        void setCancelled_true() {
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled is revertible")
        void setCancelled_revertible() {
            event.setCancelled(true);
            event.setCancelled(false);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("getHandlers returns non-null")
        void getHandlers_returnsNonNull() {
            assertNotNull(event.getHandlers());
        }

        @Test
        @DisplayName("getHandlerList returns same instance as getHandlers")
        void getHandlerList_matchesGetHandlers() {
            assertSame(BoardEvent.getHandlerList(), event.getHandlers());
        }
    }

    @Nested
    @DisplayName("BoardOfferingExpireEvent")
    class OfferingExpireEvent {

        private BoardOfferingExpireEvent event;

        @BeforeEach
        void setUp() {
            event = new BoardOfferingExpireEvent(mockBoard, mockRotation);
        }

        @Test
        @DisplayName("getBoard returns constructor board")
        void getBoard_returnsConstructorBoard() {
            assertSame(mockBoard, event.getBoard());
        }

        @Test
        @DisplayName("getBoardKey delegates to board")
        void getBoardKey_delegatesToBoard() {
            assertEquals(BOARD_KEY, event.getBoardKey());
        }

        @Test
        @DisplayName("getExpiredRotation returns constructor rotation")
        void getExpiredRotation_returnsConstructorRotation() {
            assertSame(mockRotation, event.getExpiredRotation());
        }

        @Test
        @DisplayName("getExpiredRotationId delegates to rotation")
        void getExpiredRotationId_delegatesToRotation() {
            assertEquals(mockRotation.getRotationId(), event.getExpiredRotationId());
        }

        @Test
        @DisplayName("getHandlers returns non-null")
        void getHandlers_returnsNonNull() {
            assertNotNull(event.getHandlers());
        }
    }

    @Nested
    @DisplayName("BoardOfferingGenerateEvent")
    class OfferingGenerateEvent {

        private BoardOffering mockOffering;

        @BeforeEach
        void setUp() {
            mockOffering = mock(BoardOffering.class);
        }

        @Test
        @DisplayName("getBoard returns constructor board")
        void getBoard_returnsConstructorBoard() {
            var event = new BoardOfferingGenerateEvent(mockBoard, mockRotation, List.of(mockOffering));
            assertSame(mockBoard, event.getBoard());
        }

        @Test
        @DisplayName("getRotation returns constructor rotation")
        void getRotation_returnsConstructorRotation() {
            var event = new BoardOfferingGenerateEvent(mockBoard, mockRotation, List.of());
            assertSame(mockRotation, event.getRotation());
        }

        @Test
        @DisplayName("getOfferings returns mutable copy")
        void getOfferings_returnsMutableCopy() {
            var original = new ArrayList<>(List.of(mockOffering));
            var event = new BoardOfferingGenerateEvent(mockBoard, mockRotation, original);
            List<BoardOffering> returned = event.getOfferings();
            assertNotSame(original, returned);
            assertEquals(1, returned.size());
        }

        @Test
        @DisplayName("getOfferings list is mutable")
        void getOfferings_isMutable() {
            var event = new BoardOfferingGenerateEvent(mockBoard, mockRotation, List.of(mockOffering));
            List<BoardOffering> offerings = event.getOfferings();
            BoardOffering anotherOffering = mock(BoardOffering.class);
            offerings.add(anotherOffering);
            assertEquals(2, event.getOfferings().size());
        }

        @Test
        @DisplayName("modifications to original list do not affect event")
        void originalList_doesNotAffectEvent() {
            var original = new ArrayList<>(List.of(mockOffering));
            var event = new BoardOfferingGenerateEvent(mockBoard, mockRotation, original);
            original.clear();
            assertEquals(1, event.getOfferings().size());
        }

        @Test
        @DisplayName("empty offerings list accepted")
        void emptyOfferingsList_accepted() {
            var event = new BoardOfferingGenerateEvent(mockBoard, mockRotation, List.of());
            assertTrue(event.getOfferings().isEmpty());
        }
    }

    @Nested
    @DisplayName("BoardRotationEvent")
    class RotationEvent {

        private BoardOffering mockOffering;

        @BeforeEach
        void setUp() {
            mockOffering = mock(BoardOffering.class);
        }

        @Test
        @DisplayName("getBoard returns constructor board")
        void getBoard_returnsConstructorBoard() {
            var event = new BoardRotationEvent(mockBoard, mockRotation, List.of());
            assertSame(mockBoard, event.getBoard());
        }

        @Test
        @DisplayName("getRotation returns constructor rotation")
        void getRotation_returnsConstructorRotation() {
            var event = new BoardRotationEvent(mockBoard, mockRotation, List.of());
            assertSame(mockRotation, event.getRotation());
        }

        @Test
        @DisplayName("getOfferings returns unmodifiable list")
        void getOfferings_returnsUnmodifiableList() {
            var event = new BoardRotationEvent(mockBoard, mockRotation, List.of(mockOffering));
            assertThrows(UnsupportedOperationException.class, () -> event.getOfferings().add(mock(BoardOffering.class)));
        }

        @Test
        @DisplayName("getOfferings preserves original elements")
        void getOfferings_preservesOriginalElements() {
            var event = new BoardRotationEvent(mockBoard, mockRotation, List.of(mockOffering));
            assertEquals(1, event.getOfferings().size());
            assertSame(mockOffering, event.getOfferings().get(0));
        }

        @Test
        @DisplayName("modifications to original list do not affect event")
        void originalList_doesNotAffectEvent() {
            var original = new ArrayList<>(List.of(mockOffering));
            var event = new BoardRotationEvent(mockBoard, mockRotation, original);
            original.clear();
            assertEquals(1, event.getOfferings().size());
        }
    }

    @Nested
    @DisplayName("PersonalOfferingGenerateEvent")
    class PersonalOfferingGenerate {

        private UUID playerUUID;
        private BoardOffering mockOffering;

        @BeforeEach
        void setUp() {
            playerUUID = UUID.randomUUID();
            mockOffering = mock(BoardOffering.class);
        }

        @Test
        @DisplayName("getBoard returns constructor board")
        void getBoard_returnsConstructorBoard() {
            var event = new PersonalOfferingGenerateEvent(mockBoard, playerUUID, mockRotation, List.of());
            assertSame(mockBoard, event.getBoard());
        }

        @Test
        @DisplayName("getPlayerUUID returns constructor UUID")
        void getPlayerUUID_returnsConstructorUUID() {
            var event = new PersonalOfferingGenerateEvent(mockBoard, playerUUID, mockRotation, List.of());
            assertEquals(playerUUID, event.getPlayerUUID());
        }

        @Test
        @DisplayName("getRotation returns constructor rotation")
        void getRotation_returnsConstructorRotation() {
            var event = new PersonalOfferingGenerateEvent(mockBoard, playerUUID, mockRotation, List.of());
            assertSame(mockRotation, event.getRotation());
        }

        @Test
        @DisplayName("getOfferings returns mutable copy")
        void getOfferings_returnsMutableCopy() {
            var original = new ArrayList<>(List.of(mockOffering));
            var event = new PersonalOfferingGenerateEvent(mockBoard, playerUUID, mockRotation, original);
            List<BoardOffering> returned = event.getOfferings();
            assertNotSame(original, returned);
        }

        @Test
        @DisplayName("getOfferings list is mutable")
        void getOfferings_isMutable() {
            var event = new PersonalOfferingGenerateEvent(mockBoard, playerUUID, mockRotation, List.of(mockOffering));
            event.getOfferings().add(mock(BoardOffering.class));
            assertEquals(2, event.getOfferings().size());
        }

        @Test
        @DisplayName("modifications to original list do not affect event")
        void originalList_doesNotAffectEvent() {
            var original = new ArrayList<>(List.of(mockOffering));
            var event = new PersonalOfferingGenerateEvent(mockBoard, playerUUID, mockRotation, original);
            original.clear();
            assertEquals(1, event.getOfferings().size());
        }
    }

    @Nested
    @DisplayName("TemplateQuestGenerateEvent")
    class TemplateQuestGenerate {

        private QuestTemplate mockTemplate;
        private NamespacedKey rarityKey;
        private QuestDefinition mockDefinition;
        private TemplateQuestGenerateEvent event;

        @BeforeEach
        void setUp() {
            mockTemplate = mock(QuestTemplate.class);
            rarityKey = new NamespacedKey("mcrpg", "common");
            mockDefinition = mock(QuestDefinition.class);
            event = new TemplateQuestGenerateEvent(mockTemplate, rarityKey, mockDefinition);
        }

        @Test
        @DisplayName("getTemplate returns constructor template")
        void getTemplate_returnsConstructorTemplate() {
            assertSame(mockTemplate, event.getTemplate());
        }

        @Test
        @DisplayName("getRarityKey returns constructor rarity key")
        void getRarityKey_returnsConstructorRarityKey() {
            assertEquals(rarityKey, event.getRarityKey());
        }

        @Test
        @DisplayName("getGeneratedDefinition returns constructor definition")
        void getGeneratedDefinition_returnsConstructorDefinition() {
            assertSame(mockDefinition, event.getGeneratedDefinition());
        }

        @Test
        @DisplayName("isCancelled defaults to false")
        void isCancelled_defaultsFalse() {
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled true makes isCancelled return true")
        void setCancelled_true() {
            event.setCancelled(true);
            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled is revertible")
        void setCancelled_revertible() {
            event.setCancelled(true);
            event.setCancelled(false);
            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("getHandlers returns non-null")
        void getHandlers_returnsNonNull() {
            assertNotNull(event.getHandlers());
        }

        @Test
        @DisplayName("getHandlerList returns same instance as getHandlers")
        void getHandlerList_matchesGetHandlers() {
            assertSame(TemplateQuestGenerateEvent.getHandlerList(), event.getHandlers());
        }

        @Test
        @DisplayName("handler list is separate from BoardEvent handler list")
        void handlerList_separateFromBoardEvent() {
            assertNotSame(BoardEvent.getHandlerList(), TemplateQuestGenerateEvent.getHandlerList());
        }
    }
}
