package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link QuestChainCompleteEvent}.
 */
public class QuestChainCompleteEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(new NamespacedKey("mcrpg", "quest_one")));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @DisplayName("Given a chain-complete event, When getters are called, Then they return the values passed to the constructor")
    @Test
    void event_getters_returnConstructorValues() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        int completionNumber = 3;

        QuestChainCompleteEvent event = new QuestChainCompleteEvent(definition, player, player.getUniqueId(), completionNumber);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(completionNumber, event.getCompletionNumber());
    }

    @DisplayName("Given a chain-complete event with completionNumber 1, When getCompletionNumber() is called, Then 1 is returned")
    @Test
    void event_firstCompletion_completionNumberIsOne() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainCompleteEvent event = new QuestChainCompleteEvent(definition, player, player.getUniqueId(), 1);

        assertEquals(1, event.getCompletionNumber());
    }

    @DisplayName("Given a chain-complete event, When getHandlers() is called, Then a HandlerList is returned")
    @Test
    void event_getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainCompleteEvent event = new QuestChainCompleteEvent(definition, player, player.getUniqueId(), 1);

        assertEquals(QuestChainCompleteEvent.getHandlerList(), event.getHandlers());
    }
}
