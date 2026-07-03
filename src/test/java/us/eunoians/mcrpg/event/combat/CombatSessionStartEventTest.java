package us.eunoians.mcrpg.event.combat;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.ParticipantType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CombatSessionStartEvent")
class CombatSessionStartEventTest extends McRPGBaseTest {

    @DisplayName("Constructor stores entityUUID")
    @Test
    void constructor_storesEntityUUID() {
        UUID entityUUID = UUID.randomUUID();
        UUID triggerUUID = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");

        CombatSessionStartEvent event = new CombatSessionStartEvent(
                entityUUID, triggerUUID, ParticipantType.MOB, wrapper);

        assertEquals(entityUUID, event.getEntityUUID());
    }

    @DisplayName("Constructor stores triggerParticipantUUID")
    @Test
    void constructor_storesTriggerParticipantUUID() {
        UUID entityUUID = UUID.randomUUID();
        UUID triggerUUID = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");

        CombatSessionStartEvent event = new CombatSessionStartEvent(
                entityUUID, triggerUUID, ParticipantType.MOB, wrapper);

        assertEquals(triggerUUID, event.getTriggerParticipantUUID());
    }

    @DisplayName("Constructor stores triggerParticipantType")
    @Test
    void constructor_storesTriggerParticipantType() {
        UUID entityUUID = UUID.randomUUID();
        UUID triggerUUID = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("PLAYER");

        CombatSessionStartEvent event = new CombatSessionStartEvent(
                entityUUID, triggerUUID, ParticipantType.PLAYER, wrapper);

        assertEquals(ParticipantType.PLAYER, event.getTriggerParticipantType());
    }

    @DisplayName("Constructor stores triggerEntityWrapper")
    @Test
    void constructor_storesTriggerEntityWrapper() {
        UUID entityUUID = UUID.randomUUID();
        UUID triggerUUID = UUID.randomUUID();
        CustomEntityWrapper wrapper = new CustomEntityWrapper("ZOMBIE");

        CombatSessionStartEvent event = new CombatSessionStartEvent(
                entityUUID, triggerUUID, ParticipantType.MOB, wrapper);

        assertEquals(wrapper, event.getTriggerEntityWrapper());
    }

    @DisplayName("Default cancelled state is false")
    @Test
    void defaultCancelledState_isFalse() {
        CombatSessionStartEvent event = new CombatSessionStartEvent(
                UUID.randomUUID(), UUID.randomUUID(), ParticipantType.MOB,
                new CustomEntityWrapper("ZOMBIE"));

        assertFalse(event.isCancelled());
    }

    @DisplayName("setCancelled(true) makes isCancelled() return true")
    @Test
    void setCancelled_makesIsCancelledTrue() {
        CombatSessionStartEvent event = new CombatSessionStartEvent(
                UUID.randomUUID(), UUID.randomUUID(), ParticipantType.MOB,
                new CustomEntityWrapper("ZOMBIE"));

        event.setCancelled(true);

        assertTrue(event.isCancelled());
    }

    @DisplayName("getHandlerList() returns a non-null static HandlerList")
    @Test
    void getHandlerList_returnsNonNull() {
        assertNotNull(CombatSessionStartEvent.getHandlerList());
    }
}
