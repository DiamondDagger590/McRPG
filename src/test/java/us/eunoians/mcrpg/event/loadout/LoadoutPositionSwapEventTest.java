package us.eunoians.mcrpg.event.loadout;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LoadoutPositionSwapEventTest extends McRPGBaseTest {

    @Test
    @DisplayName("Given a LoadoutPositionSwapEvent, when getPlayerUUID is called, then the constructor value is returned")
    public void getPlayerUUID_returnsConstructorValue() {
        UUID uuid = UUID.randomUUID();
        var event = new LoadoutPositionSwapEvent(uuid, 1, 3);

        assertEquals(uuid, event.getPlayerUUID());
    }

    @Test
    @DisplayName("Given a LoadoutPositionSwapEvent, when getFromComboSlot is called, then the constructor value is returned")
    public void getFromComboSlot_returnsConstructorValue() {
        UUID uuid = UUID.randomUUID();
        var event = new LoadoutPositionSwapEvent(uuid, 2, 3);

        assertEquals(2, event.getFromComboSlot());
    }

    @Test
    @DisplayName("Given a LoadoutPositionSwapEvent, when getToComboSlot is called, then the constructor value is returned")
    public void getToComboSlot_returnsConstructorValue() {
        UUID uuid = UUID.randomUUID();
        var event = new LoadoutPositionSwapEvent(uuid, 1, 3);

        assertEquals(3, event.getToComboSlot());
    }

    @Test
    @DisplayName("Given a LoadoutPositionSwapEvent, when getHandlerList is called, then a non-null handler list is returned")
    public void getHandlerList_returnsNonNull() {
        assertNotNull(LoadoutPositionSwapEvent.getHandlerList());
    }

    @Test
    @DisplayName("Given a LoadoutPositionSwapEvent instance, when getHandlers is called, then a non-null handler list is returned")
    public void getHandlers_returnsNonNull() {
        var event = new LoadoutPositionSwapEvent(UUID.randomUUID(), 1, 2);
        assertNotNull(event.getHandlers());
    }
}
