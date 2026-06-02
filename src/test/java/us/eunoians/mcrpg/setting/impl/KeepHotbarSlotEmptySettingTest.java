package us.eunoians.mcrpg.setting.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class KeepHotbarSlotEmptySettingTest {

    @DisplayName("getFirstSetting() always returns DISABLED")
    @Test
    void getFirstSetting_returnsDisabled() {
        for (KeepHotbarSlotEmptySetting setting : KeepHotbarSlotEmptySetting.values()) {
            assertEquals(KeepHotbarSlotEmptySetting.DISABLED, setting.getFirstSetting().getNodeValue());
        }
    }

    @DisplayName("Full cycle through all 10 variants returns to start")
    @Test
    void fullCycle_returnsToStart() {
        KeepHotbarSlotEmptySetting[] allValues = KeepHotbarSlotEmptySetting.values();
        var current = KeepHotbarSlotEmptySetting.DISABLED;
        for (int i = 0; i < allValues.length; i++) {
            current = (KeepHotbarSlotEmptySetting) current.getNextSetting().getNodeValue();
        }
        assertEquals(KeepHotbarSlotEmptySetting.DISABLED, current);
    }

    @DisplayName("Cycling visits every variant exactly once before looping")
    @Test
    void cycle_visitsEveryVariant() {
        KeepHotbarSlotEmptySetting[] allValues = KeepHotbarSlotEmptySetting.values();
        boolean[] visited = new boolean[allValues.length];

        var current = KeepHotbarSlotEmptySetting.DISABLED;
        visited[current.ordinal()] = true;
        for (int i = 1; i < allValues.length; i++) {
            current = (KeepHotbarSlotEmptySetting) current.getNextSetting().getNodeValue();
            visited[current.ordinal()] = true;
        }

        for (int i = 0; i < allValues.length; i++) {
            assertTrue(visited[i], "Variant " + allValues[i] + " was never visited");
        }
    }

    @DisplayName("DISABLED getSlot returns -1")
    @Test
    void getSlot_disabled_returnsNegativeOne() {
        assertEquals(-1, KeepHotbarSlotEmptySetting.DISABLED.getSlot());
    }

    @DisplayName("SLOT_ONE through SLOT_NINE have correct slot indices 0-8")
    @Test
    void getSlot_slotVariants_returnCorrectIndices() {
        assertEquals(0, KeepHotbarSlotEmptySetting.SLOT_ONE.getSlot());
        assertEquals(1, KeepHotbarSlotEmptySetting.SLOT_TWO.getSlot());
        assertEquals(2, KeepHotbarSlotEmptySetting.SLOT_THREE.getSlot());
        assertEquals(3, KeepHotbarSlotEmptySetting.SLOT_FOUR.getSlot());
        assertEquals(4, KeepHotbarSlotEmptySetting.SLOT_FIVE.getSlot());
        assertEquals(5, KeepHotbarSlotEmptySetting.SLOT_SIX.getSlot());
        assertEquals(6, KeepHotbarSlotEmptySetting.SLOT_SEVEN.getSlot());
        assertEquals(7, KeepHotbarSlotEmptySetting.SLOT_EIGHT.getSlot());
        assertEquals(8, KeepHotbarSlotEmptySetting.SLOT_NINE.getSlot());
    }

    @DisplayName("DISABLED getDeniedSlots returns empty list")
    @Test
    void getDeniedSlots_disabled_returnsEmptyList() {
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        assertTrue(KeepHotbarSlotEmptySetting.DISABLED.getDeniedSlots(mockPlayer).isEmpty());
    }

    @DisplayName("SLOT_X getDeniedSlots returns the slot index")
    @ParameterizedTest
    @EnumSource(value = KeepHotbarSlotEmptySetting.class, mode = EnumSource.Mode.EXCLUDE, names = "DISABLED")
    void getDeniedSlots_slotVariants_returnSlotIndex(KeepHotbarSlotEmptySetting setting) {
        McRPGPlayer mockPlayer = mock(McRPGPlayer.class);
        List<Integer> denied = setting.getDeniedSlots(mockPlayer);
        assertEquals(1, denied.size());
        assertEquals(setting.getSlot(), denied.get(0));
    }

    @DisplayName("fromString round-trips for all variants")
    @ParameterizedTest
    @EnumSource(KeepHotbarSlotEmptySetting.class)
    void fromString_roundTrip_allVariants(KeepHotbarSlotEmptySetting setting) {
        assertEquals(setting,
                setting.fromString(setting.name()).orElseThrow());
    }

    @DisplayName("fromString is case-insensitive")
    @Test
    void fromString_caseInsensitive() {
        assertEquals(KeepHotbarSlotEmptySetting.DISABLED,
                KeepHotbarSlotEmptySetting.DISABLED.fromString("disabled").orElseThrow());
        assertEquals(KeepHotbarSlotEmptySetting.SLOT_ONE,
                KeepHotbarSlotEmptySetting.SLOT_ONE.fromString("slot_one").orElseThrow());
        assertEquals(KeepHotbarSlotEmptySetting.SLOT_NINE,
                KeepHotbarSlotEmptySetting.SLOT_NINE.fromString("Slot_Nine").orElseThrow());
    }

    @DisplayName("fromString returns empty for unknown value")
    @Test
    void fromString_unknownValue_returnsEmpty() {
        assertTrue(KeepHotbarSlotEmptySetting.DISABLED.fromString("SLOT_TEN").isEmpty());
    }

    @DisplayName("getSettingKey returns expected key")
    @Test
    void getSettingKey_returnsExpectedKey() {
        assertEquals("mcrpg:keep-hotbar-slot-empty-setting",
                KeepHotbarSlotEmptySetting.DISABLED.getSettingKey().toString());
    }
}
