package us.eunoians.mcrpg.setting.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeepHandEmptySettingTest extends McRPGBaseTest {

    @DisplayName("DISABLED cycles to ENABLED")
    @Test
    void getNextSetting_disabled_cyclesToEnabled() {
        KeepHandEmptySetting next =
                (KeepHandEmptySetting) KeepHandEmptySetting.DISABLED.getNextSetting().getNodeValue();
        assertEquals(KeepHandEmptySetting.ENABLED, next);
    }

    @DisplayName("ENABLED cycles back to DISABLED")
    @Test
    void getNextSetting_enabled_cyclesToDisabled() {
        KeepHandEmptySetting next =
                (KeepHandEmptySetting) KeepHandEmptySetting.ENABLED.getNextSetting().getNodeValue();
        assertEquals(KeepHandEmptySetting.DISABLED, next);
    }

    @DisplayName("getFirstSetting() always returns DISABLED")
    @Test
    void getFirstSetting_returnsDisabled() {
        assertEquals(KeepHandEmptySetting.DISABLED,
                KeepHandEmptySetting.ENABLED.getFirstSetting().getNodeValue());
        assertEquals(KeepHandEmptySetting.DISABLED,
                KeepHandEmptySetting.DISABLED.getFirstSetting().getNodeValue());
    }

    @DisplayName("Full cycle returns to start")
    @Test
    void fullCycle_returnsToStart() {
        var start = KeepHandEmptySetting.DISABLED;
        var second = (KeepHandEmptySetting) start.getNextSetting().getNodeValue();
        var third = (KeepHandEmptySetting) second.getNextSetting().getNodeValue();
        assertEquals(start, third);
    }

    @DisplayName("fromString round-trips for both variants")
    @Test
    void fromString_roundTrip_bothVariants() {
        assertEquals(KeepHandEmptySetting.ENABLED,
                KeepHandEmptySetting.ENABLED.fromString("ENABLED").orElseThrow());
        assertEquals(KeepHandEmptySetting.DISABLED,
                KeepHandEmptySetting.DISABLED.fromString("DISABLED").orElseThrow());
    }

    @DisplayName("fromString is case-insensitive")
    @Test
    void fromString_caseInsensitive() {
        assertEquals(KeepHandEmptySetting.ENABLED,
                KeepHandEmptySetting.ENABLED.fromString("enabled").orElseThrow());
        assertEquals(KeepHandEmptySetting.DISABLED,
                KeepHandEmptySetting.DISABLED.fromString("Disabled").orElseThrow());
    }

    @DisplayName("fromString returns empty for unknown value")
    @Test
    void fromString_unknownValue_returnsEmpty() {
        assertTrue(KeepHandEmptySetting.ENABLED.fromString("NOT_A_SETTING").isEmpty());
    }

    @DisplayName("getSettingKey returns expected key")
    @Test
    void getSettingKey_returnsExpectedKey() {
        assertEquals("mcrpg:keep-hand-empty-setting",
                KeepHandEmptySetting.ENABLED.getSettingKey().toString());
    }

    @DisplayName("ENABLED getDeniedSlots returns the player's held item slot")
    @Test
    void getDeniedSlots_enabled_returnsHeldItemSlot() {
        UUID uuid = UUID.randomUUID();
        PlayerMock playerMock = new PlayerMock(server, "TestPlayer", uuid);
        server.addPlayer(playerMock);
        playerMock.getInventory().setHeldItemSlot(3);

        McRPGPlayer mockMcRPGPlayer = mock(McRPGPlayer.class);
        when(mockMcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));

        List<Integer> denied = KeepHandEmptySetting.ENABLED.getDeniedSlots(mockMcRPGPlayer);
        assertEquals(List.of(3), denied);
    }

    @DisplayName("DISABLED getDeniedSlots returns empty list")
    @Test
    void getDeniedSlots_disabled_returnsEmptyList() {
        UUID uuid = UUID.randomUUID();
        PlayerMock playerMock = new PlayerMock(server, "TestPlayer", uuid);
        server.addPlayer(playerMock);
        playerMock.getInventory().setHeldItemSlot(3);

        McRPGPlayer mockMcRPGPlayer = mock(McRPGPlayer.class);
        when(mockMcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));

        List<Integer> denied = KeepHandEmptySetting.DISABLED.getDeniedSlots(mockMcRPGPlayer);
        assertTrue(denied.isEmpty());
    }

    @DisplayName("ENABLED getDeniedSlots returns empty when Bukkit player is absent")
    @Test
    void getDeniedSlots_enabled_noBukkitPlayer_returnsEmptyList() {
        McRPGPlayer mockMcRPGPlayer = mock(McRPGPlayer.class);
        when(mockMcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

        List<Integer> denied = KeepHandEmptySetting.ENABLED.getDeniedSlots(mockMcRPGPlayer);
        assertTrue(denied.isEmpty());
    }

    @DisplayName("ENABLED getDeniedSlots reflects different held item slots")
    @Test
    void getDeniedSlots_enabled_differentSlots() {
        for (int slot = 0; slot < 9; slot++) {
            UUID uuid = UUID.randomUUID();
            PlayerMock playerMock = new PlayerMock(server, "Slot" + slot, uuid);
            server.addPlayer(playerMock);
            playerMock.getInventory().setHeldItemSlot(slot);

            McRPGPlayer mockMcRPGPlayer = mock(McRPGPlayer.class);
            when(mockMcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));

            List<Integer> denied = KeepHandEmptySetting.ENABLED.getDeniedSlots(mockMcRPGPlayer);
            assertEquals(List.of(slot), denied);
        }
    }
}
