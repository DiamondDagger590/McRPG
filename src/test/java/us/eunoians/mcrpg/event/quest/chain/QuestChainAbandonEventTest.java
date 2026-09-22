package us.eunoians.mcrpg.event.quest.chain;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class QuestChainAbandonEventTest extends McRPGBaseTest {

    private static QuestChainDefinition buildDefinition() {
        NamespacedKey chainKey = new NamespacedKey("mcrpg", "test_chain");
        NamespacedKey sourceKey = new NamespacedKey("mcrpg", "manual");
        NamespacedKey triggerKey = new NamespacedKey("mcrpg", "manual");
        List<QuestChainStep> steps = List.of(QuestChainStep.simple(new NamespacedKey("mcrpg", "quest_one")));
        return new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps).build();
    }

    @Test
    @DisplayName("Getters return values passed to the constructor")
    void getters_returnConstructorValues() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainAbandonEvent event = new QuestChainAbandonEvent(definition, player, player.getUniqueId());

        assertSame(definition, event.getChainDefinition());
        assertSame(player, event.getPlayer());
        assertEquals(player.getUniqueId(), event.getPlayerUUID());
    }

    @Test
    @DisplayName("Null player is returned when constructed with offline player")
    void nullPlayer_returnsNull() {
        QuestChainDefinition definition = buildDefinition();
        UUID offlineUUID = UUID.randomUUID();

        QuestChainAbandonEvent event = new QuestChainAbandonEvent(definition, null, offlineUUID);

        assertNull(event.getPlayer());
        assertEquals(offlineUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("getHandlers and getHandlerList return the same HandlerList")
    void handlers_returnStaticHandlerList() {
        QuestChainDefinition definition = buildDefinition();
        PlayerMock player = server.addPlayer();

        QuestChainAbandonEvent event = new QuestChainAbandonEvent(definition, player, player.getUniqueId());

        assertSame(QuestChainAbandonEvent.getHandlerList(), event.getHandlers());
    }
}
