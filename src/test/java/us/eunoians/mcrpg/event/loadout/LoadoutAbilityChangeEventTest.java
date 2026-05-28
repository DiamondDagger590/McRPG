package us.eunoians.mcrpg.event.loadout;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LoadoutAbilityChangeEventTest extends McRPGBaseTest {

    @DisplayName("EQUIP reason: newAbility present, previousAbility empty")
    @Test
    public void equip_hasNewAbility_noPreviousAbility() {
        UUID uuid = UUID.randomUUID();
        NamespacedKey newKey = new NamespacedKey("mcrpg", "bleed");
        var event = new LoadoutAbilityChangeEvent(uuid, LoadoutAbilityChangeEvent.ChangeReason.EQUIP,
                null, newKey, 0);

        assertEquals(uuid, event.getPlayerUUID());
        assertEquals(LoadoutAbilityChangeEvent.ChangeReason.EQUIP, event.getReason());
        assertTrue(event.getNewAbility().isPresent());
        assertEquals(newKey, event.getNewAbility().get());
        assertTrue(event.getPreviousAbility().isEmpty());
        assertEquals(0, event.getLoadoutSlot());
    }

    @DisplayName("UNEQUIP reason: previousAbility present, newAbility empty")
    @Test
    public void unequip_hasPreviousAbility_noNewAbility() {
        NamespacedKey oldKey = new NamespacedKey("mcrpg", "bleed");
        var event = new LoadoutAbilityChangeEvent(UUID.randomUUID(),
                LoadoutAbilityChangeEvent.ChangeReason.UNEQUIP, oldKey, null, 1);

        assertTrue(event.getPreviousAbility().isPresent());
        assertEquals(oldKey, event.getPreviousAbility().get());
        assertTrue(event.getNewAbility().isEmpty());
        assertEquals(1, event.getLoadoutSlot());
    }

    @DisplayName("SWAP reason: both abilities present")
    @Test
    public void swap_hasBothAbilities() {
        NamespacedKey oldKey = new NamespacedKey("mcrpg", "bleed");
        NamespacedKey newKey = new NamespacedKey("mcrpg", "deeper_wound");
        var event = new LoadoutAbilityChangeEvent(UUID.randomUUID(),
                LoadoutAbilityChangeEvent.ChangeReason.SWAP, oldKey, newKey, 2);

        assertTrue(event.getPreviousAbility().isPresent());
        assertTrue(event.getNewAbility().isPresent());
        assertEquals(oldKey, event.getPreviousAbility().get());
        assertEquals(newKey, event.getNewAbility().get());
        assertEquals(LoadoutAbilityChangeEvent.ChangeReason.SWAP, event.getReason());
    }

    @DisplayName("getHandlerList returns non-null static instance")
    @Test
    public void handlerList_isNotNull() {
        assertNotNull(LoadoutAbilityChangeEvent.getHandlerList());
    }

    @DisplayName("getHandlers returns non-null instance")
    @Test
    public void getHandlers_isNotNull() {
        var event = new LoadoutAbilityChangeEvent(UUID.randomUUID(),
                LoadoutAbilityChangeEvent.ChangeReason.EQUIP, null, new NamespacedKey("mcrpg", "bleed"), 0);
        assertNotNull(event.getHandlers());
    }
}
