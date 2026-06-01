package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CascadeStartEventTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");

    @Test
    @DisplayName("Given a CascadeStartEvent, when getChainKey is called, then it returns the chain key from construction")
    public void getChainKey_returnsConstructorValue() {
        UUID playerUUID = UUID.randomUUID();
        CascadeStartEvent event = new CascadeStartEvent(CHAIN_KEY, playerUUID);

        assertEquals(CHAIN_KEY, event.getChainKey());
    }

    @Test
    @DisplayName("Given a CascadeStartEvent, when getPlayerUUID is called, then it returns the player UUID from construction")
    public void getPlayerUUID_returnsConstructorValue() {
        UUID playerUUID = UUID.randomUUID();
        CascadeStartEvent event = new CascadeStartEvent(CHAIN_KEY, playerUUID);

        assertEquals(playerUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a CascadeStartEvent, when getHandlers is called, then it returns a non-null handler list")
    public void getHandlers_returnsNonNull() {
        CascadeStartEvent event = new CascadeStartEvent(CHAIN_KEY, UUID.randomUUID());

        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given the CascadeStartEvent class, when getHandlerList is called statically, then it returns a non-null handler list")
    public void getHandlerList_static_returnsNonNull() {
        assertNotNull(CascadeStartEvent.getHandlerList());
    }
}
