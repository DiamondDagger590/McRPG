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
 * Tests for {@link QuestChainStepAdvanceEvent}.
 */
public class QuestChainStepAdvanceEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildTwoStepDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "two_step_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        QuestChainStep step1 = QuestChainStep.simple(new NamespacedKey("mcrpg", "quest_one"));
        QuestChainStep step2 = QuestChainStep.simple(new NamespacedKey("mcrpg", "quest_two"));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, List.of(step1, step2)).build();
    }

    @DisplayName("Given a step-advance event, When getters are called, Then they return the values passed to the constructor")
    @Test
    void event_getters_returnConstructorValues() {
        QuestChainDefinition definition = buildTwoStepDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep completed = definition.getSteps().get(0);
        QuestChainStep next = definition.getSteps().get(1);

        QuestChainStepAdvanceEvent event = new QuestChainStepAdvanceEvent(definition, player, player.getUniqueId(), completed, next);

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertSame(completed, event.getCompletedStep());
        assertSame(next, event.getNextStep());
    }

    @DisplayName("Given a step-advance event, When getHandlers() is called, Then a HandlerList is returned")
    @Test
    void event_getHandlers_returnsHandlerList() {
        QuestChainDefinition definition = buildTwoStepDefinition();
        PlayerMock player = server.addPlayer();
        QuestChainStep completed = definition.getSteps().get(0);
        QuestChainStep next = definition.getSteps().get(1);

        QuestChainStepAdvanceEvent event = new QuestChainStepAdvanceEvent(definition, player, player.getUniqueId(), completed, next);

        assertEquals(QuestChainStepAdvanceEvent.getHandlerList(), event.getHandlers());
    }
}
