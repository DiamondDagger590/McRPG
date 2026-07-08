package us.eunoians.mcrpg.event.quest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.source.QuestSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PreQuestStartEventTest extends McRPGBaseTest {

    @DisplayName("PreQuestStartEvent is not cancelled by default")
    @Test
    public void event_notCancelledByDefault() {
        QuestDefinition definition = mock(QuestDefinition.class);
        PlayerMock player = server.addPlayer();
        QuestSource source = mock(QuestSource.class);
        PreQuestStartEvent event = new PreQuestStartEvent(definition, player, source);
        assertFalse(event.isCancelled());
    }

    @DisplayName("PreQuestStartEvent can be cancelled")
    @Test
    public void event_canBeCancelled() {
        QuestDefinition definition = mock(QuestDefinition.class);
        PlayerMock player = server.addPlayer();
        QuestSource source = mock(QuestSource.class);
        PreQuestStartEvent event = new PreQuestStartEvent(definition, player, source);
        event.setCancelled(true);
        assertTrue(event.isCancelled());
    }

    @DisplayName("getDefinition returns the quest definition")
    @Test
    public void getDefinition_returnsDefinition() {
        QuestDefinition definition = mock(QuestDefinition.class);
        PlayerMock player = server.addPlayer();
        QuestSource source = mock(QuestSource.class);
        PreQuestStartEvent event = new PreQuestStartEvent(definition, player, source);
        assertSame(definition, event.getDefinition());
    }

    @DisplayName("getPlayer returns the player")
    @Test
    public void getPlayer_returnsPlayer() {
        QuestDefinition definition = mock(QuestDefinition.class);
        PlayerMock player = server.addPlayer();
        QuestSource source = mock(QuestSource.class);
        PreQuestStartEvent event = new PreQuestStartEvent(definition, player, source);
        assertSame(player, event.getPlayer());
    }

    @DisplayName("getSource returns the quest source")
    @Test
    public void getSource_returnsSource() {
        QuestDefinition definition = mock(QuestDefinition.class);
        PlayerMock player = server.addPlayer();
        QuestSource source = mock(QuestSource.class);
        PreQuestStartEvent event = new PreQuestStartEvent(definition, player, source);
        assertSame(source, event.getSource());
    }

    @DisplayName("getHandlerList returns non-null")
    @Test
    public void handlerList_isNotNull() {
        assertNotNull(PreQuestStartEvent.getHandlerList());
    }
}
