package us.eunoians.mcrpg.event.display;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.display.hud.ActionBarCenterContent;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("Display events")
class DisplayEventCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("ActionBarSlotSetEvent")
    class ActionBarSlotSetEventTests {

        @Test
        @DisplayName("constructor stores player, priority, previous content, and new content")
        void constructor_storesAllFields(McRPGPlayer player) {
            ActionBarCenterContent previous = mock(ActionBarCenterContent.class);
            ActionBarCenterContent newContent = mock(ActionBarCenterContent.class);

            var event = new ActionBarSlotSetEvent(player, 40, previous, newContent);

            assertSame(player, event.getMcRPGPlayer());
            assertEquals(40, event.getPriority());
            assertSame(previous, event.getPreviousContent());
            assertSame(newContent, event.getNewContent());
        }

        @Test
        @DisplayName("previousContent can be null when slot was empty")
        void previousContent_canBeNull(McRPGPlayer player) {
            ActionBarCenterContent newContent = mock(ActionBarCenterContent.class);

            var event = new ActionBarSlotSetEvent(player, 10, null, newContent);

            assertNull(event.getPreviousContent());
        }

        @Test
        @DisplayName("setNewContent replaces the content to be written")
        void setNewContent_replacesContent(McRPGPlayer player) {
            ActionBarCenterContent original = mock(ActionBarCenterContent.class);
            ActionBarCenterContent replacement = mock(ActionBarCenterContent.class);

            var event = new ActionBarSlotSetEvent(player, 10, null, original);
            event.setNewContent(replacement);

            assertSame(replacement, event.getNewContent());
        }

        @Test
        @DisplayName("setNewContent can be called multiple times")
        void setNewContent_canBeCalledMultipleTimes(McRPGPlayer player) {
            ActionBarCenterContent first = mock(ActionBarCenterContent.class);
            ActionBarCenterContent second = mock(ActionBarCenterContent.class);
            ActionBarCenterContent third = mock(ActionBarCenterContent.class);

            var event = new ActionBarSlotSetEvent(player, 10, null, first);
            event.setNewContent(second);
            event.setNewContent(third);

            assertSame(third, event.getNewContent());
        }

        @Test
        @DisplayName("isCancelled defaults to false")
        void isCancelled_defaultsFalse(McRPGPlayer player) {
            var event = new ActionBarSlotSetEvent(player, 10, null, mock(ActionBarCenterContent.class));

            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled(true) makes isCancelled return true")
        void setCancelled_true(McRPGPlayer player) {
            var event = new ActionBarSlotSetEvent(player, 10, null, mock(ActionBarCenterContent.class));

            event.setCancelled(true);

            assertTrue(event.isCancelled());
        }

        @Test
        @DisplayName("setCancelled can revert to false")
        void setCancelled_canRevert(McRPGPlayer player) {
            var event = new ActionBarSlotSetEvent(player, 10, null, mock(ActionBarCenterContent.class));

            event.setCancelled(true);
            event.setCancelled(false);

            assertFalse(event.isCancelled());
        }

        @Test
        @DisplayName("priority preserves zero value")
        void priority_preservesZero(McRPGPlayer player) {
            var event = new ActionBarSlotSetEvent(player, 0, null, mock(ActionBarCenterContent.class));

            assertEquals(0, event.getPriority());
        }

        @Test
        @DisplayName("priority preserves negative value")
        void priority_preservesNegative(McRPGPlayer player) {
            var event = new ActionBarSlotSetEvent(player, -5, null, mock(ActionBarCenterContent.class));

            assertEquals(-5, event.getPriority());
        }

        @Test
        @DisplayName("getHandlers returns non-null")
        void getHandlers_returnsNonNull(McRPGPlayer player) {
            var event = new ActionBarSlotSetEvent(player, 10, null, mock(ActionBarCenterContent.class));

            assertNotNull(event.getHandlers());
        }

        @Test
        @DisplayName("static getHandlerList returns non-null")
        void getHandlerList_returnsNonNull() {
            assertNotNull(ActionBarSlotSetEvent.getHandlerList());
        }

        @Test
        @DisplayName("instance and static handler lists are the same object")
        void handlerLists_areSameObject(McRPGPlayer player) {
            var event = new ActionBarSlotSetEvent(player, 10, null, mock(ActionBarCenterContent.class));

            assertSame(ActionBarSlotSetEvent.getHandlerList(), event.getHandlers());
        }
    }

    @Nested
    @DisplayName("ActionBarSlotClearEvent")
    class ActionBarSlotClearEventTests {

        @Test
        @DisplayName("constructor stores player, priority, and content")
        void constructor_storesAllFields(McRPGPlayer player) {
            ActionBarCenterContent content = mock(ActionBarCenterContent.class);

            var event = new ActionBarSlotClearEvent(player, 40, content);

            assertSame(player, event.getMcRPGPlayer());
            assertEquals(40, event.getPriority());
            assertSame(content, event.getContent());
        }

        @Test
        @DisplayName("priority preserves zero value")
        void priority_preservesZero(McRPGPlayer player) {
            var event = new ActionBarSlotClearEvent(player, 0, mock(ActionBarCenterContent.class));

            assertEquals(0, event.getPriority());
        }

        @Test
        @DisplayName("priority preserves high value")
        void priority_preservesHighValue(McRPGPlayer player) {
            var event = new ActionBarSlotClearEvent(player, 99, mock(ActionBarCenterContent.class));

            assertEquals(99, event.getPriority());
        }

        @Test
        @DisplayName("getHandlers returns non-null")
        void getHandlers_returnsNonNull(McRPGPlayer player) {
            var event = new ActionBarSlotClearEvent(player, 10, mock(ActionBarCenterContent.class));

            assertNotNull(event.getHandlers());
        }

        @Test
        @DisplayName("static getHandlerList returns non-null")
        void getHandlerList_returnsNonNull() {
            assertNotNull(ActionBarSlotClearEvent.getHandlerList());
        }

        @Test
        @DisplayName("instance and static handler lists are the same object")
        void handlerLists_areSameObject(McRPGPlayer player) {
            var event = new ActionBarSlotClearEvent(player, 10, mock(ActionBarCenterContent.class));

            assertSame(ActionBarSlotClearEvent.getHandlerList(), event.getHandlers());
        }

        @Test
        @DisplayName("event is not cancellable")
        void event_isNotCancellable(McRPGPlayer player) {
            var event = new ActionBarSlotClearEvent(player, 10, mock(ActionBarCenterContent.class));

            assertFalse(event instanceof org.bukkit.event.Cancellable);
        }
    }
}
