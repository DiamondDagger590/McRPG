package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRepeatMode;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link QuestChainStartEvent}.
 */
public class QuestChainStartEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey questKey = new NamespacedKey("mcrpg", "example_quest");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(questKey));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @DisplayName("Given a chain start event, When getters are called, Then they return the values passed to the constructor")
    @Test
    void event_getters_returnConstructorValues() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep firstStep = definition.getSteps().get(0);

        QuestChainStartEvent event = new QuestChainStartEvent(definition, player, firstStep);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertSame(firstStep, event.getFirstStep());
    }

    @DisplayName("Given a chain start event, When getHandlers() is called, Then a HandlerList is returned")
    @Test
    void event_getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep firstStep = definition.getSteps().get(0);

        QuestChainStartEvent event = new QuestChainStartEvent(definition, player, firstStep);

        assertEquals(QuestChainStartEvent.getHandlerList(), event.getHandlers());
    }
}
