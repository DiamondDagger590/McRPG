package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.event.loadout.LoadoutAbilityChangeEvent;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("LoadoutEquipQuestContext")
class LoadoutEquipQuestContextTest {

    @Test
    @DisplayName("getAbilityKey returns the new ability key from an equip event")
    void getAbilityKey_returnsNewAbility_forEquipEvent() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey newAbility = new NamespacedKey("mcrpg", "bleed");
        LoadoutAbilityChangeEvent event = new LoadoutAbilityChangeEvent(
                playerUUID, LoadoutAbilityChangeEvent.ChangeReason.EQUIP,
                null, newAbility, 0);
        LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

        assertEquals(newAbility, context.getAbilityKey());
    }

    @Test
    @DisplayName("getAbilityKey returns the new ability key from a swap event")
    void getAbilityKey_returnsNewAbility_forSwapEvent() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey oldAbility = new NamespacedKey("mcrpg", "bleed");
        NamespacedKey newAbility = new NamespacedKey("mcrpg", "vampire");
        LoadoutAbilityChangeEvent event = new LoadoutAbilityChangeEvent(
                playerUUID, LoadoutAbilityChangeEvent.ChangeReason.SWAP,
                oldAbility, newAbility, 1);
        LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

        assertEquals(newAbility, context.getAbilityKey());
    }

    @Test
    @DisplayName("getAbilityKey throws NoSuchElementException for unequip event with no new ability")
    void getAbilityKey_throws_forUnequipEvent() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey previousAbility = new NamespacedKey("mcrpg", "bleed");
        LoadoutAbilityChangeEvent event = new LoadoutAbilityChangeEvent(
                playerUUID, LoadoutAbilityChangeEvent.ChangeReason.UNEQUIP,
                previousAbility, null, 0);
        LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

        assertThrows(NoSuchElementException.class, context::getAbilityKey);
    }

    @Test
    @DisplayName("getPlayerUUID returns the player UUID from the event")
    void getPlayerUUID_returnsEventPlayerUUID() {
        UUID playerUUID = UUID.randomUUID();
        NamespacedKey newAbility = new NamespacedKey("mcrpg", "bleed");
        LoadoutAbilityChangeEvent event = new LoadoutAbilityChangeEvent(
                playerUUID, LoadoutAbilityChangeEvent.ChangeReason.EQUIP,
                null, newAbility, 0);
        LoadoutEquipQuestContext context = new LoadoutEquipQuestContext(event);

        assertEquals(playerUUID, context.getPlayerUUID());
    }
}
