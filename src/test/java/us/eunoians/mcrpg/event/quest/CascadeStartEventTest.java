package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class CascadeStartEventTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");

    @Test
    @DisplayName("Given a CascadeStartEvent, when getChainKey is called, then it returns the chain key from construction")
    public void getChainKey_returnsConstructorValue() {
        UUID playerUUID = UUID.randomUUID();
        CascadeStartEvent event = new CascadeStartEvent(CHAIN_KEY, playerUUID, null);

        assertEquals(CHAIN_KEY, event.getChainKey());
    }

    @Test
    @DisplayName("Given a CascadeStartEvent, when getPlayerUUID is called, then it returns the player UUID from construction")
    public void getPlayerUUID_returnsConstructorValue() {
        UUID playerUUID = UUID.randomUUID();
        CascadeStartEvent event = new CascadeStartEvent(CHAIN_KEY, playerUUID, null);

        assertEquals(playerUUID, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a CascadeStartEvent with a player, when getPlayer is called, then it returns the player")
    public void getPlayer_returnsPlayer() {
        Player player = mock(Player.class);
        CascadeStartEvent event = new CascadeStartEvent(CHAIN_KEY, UUID.randomUUID(), player);

        assertEquals(player, event.getPlayer());
    }

    @Test
    @DisplayName("Given a CascadeStartEvent with null player, when getPlayer is called, then it returns null")
    public void getPlayer_returnsNull_whenNoPlayer() {
        CascadeStartEvent event = new CascadeStartEvent(CHAIN_KEY, UUID.randomUUID(), null);

        assertNull(event.getPlayer());
    }

    @Test
    @DisplayName("Given a CascadeStartEvent, when getHandlers is called, then it returns a non-null handler list")
    public void getHandlers_returnsNonNull() {
        CascadeStartEvent event = new CascadeStartEvent(CHAIN_KEY, UUID.randomUUID(), null);

        assertNotNull(event.getHandlers());
    }

    @Test
    @DisplayName("Given the CascadeStartEvent class, when getHandlerList is called statically, then it returns a non-null handler list")
    public void getHandlerList_static_returnsNonNull() {
        assertNotNull(CascadeStartEvent.getHandlerList());
    }
}
