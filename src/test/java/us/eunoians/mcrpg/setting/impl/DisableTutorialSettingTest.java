package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DisableTutorialSettingTest {

    @DisplayName("getSettingKey returns mcrpg:disable-tutorial-setting")
    @Test
    void getSettingKey_returnsExpectedKey() {
        assertEquals("mcrpg:disable-tutorial-setting",
                DisableTutorialSetting.ENABLED.getSettingKey().toString());
    }

    @DisplayName("getSettingKey is consistent across all variants")
    @ParameterizedTest
    @EnumSource(DisableTutorialSetting.class)
    void getSettingKey_consistentAcrossVariants(DisableTutorialSetting setting) {
        assertEquals(DisableTutorialSetting.SETTING_KEY, setting.getSettingKey());
    }

    @DisplayName("getFirstSetting returns ENABLED node")
    @Test
    void getFirstSetting_returnsEnabled() {
        assertEquals(DisableTutorialSetting.ENABLED,
                DisableTutorialSetting.ENABLED.getFirstSetting().getNodeValue());
    }

    @DisplayName("getFirstSetting is consistent across all variants")
    @ParameterizedTest
    @EnumSource(DisableTutorialSetting.class)
    void getFirstSetting_consistentAcrossVariants(DisableTutorialSetting setting) {
        assertEquals(DisableTutorialSetting.ENABLED, setting.getFirstSetting().getNodeValue());
    }

    @DisplayName("getNextSetting from ENABLED returns DISABLED node")
    @Test
    void getNextSetting_fromEnabled_returnsDisabled() {
        LinkedNode<DisableTutorialSetting> next = DisableTutorialSetting.ENABLED.getNextSetting();
        assertEquals(DisableTutorialSetting.DISABLED, next.getNodeValue());
    }

    @DisplayName("getNextSetting from DISABLED cycles back to ENABLED node")
    @Test
    void getNextSetting_fromDisabled_cyclesToEnabled() {
        LinkedNode<DisableTutorialSetting> next = DisableTutorialSetting.DISABLED.getNextSetting();
        assertEquals(DisableTutorialSetting.ENABLED, next.getNodeValue());
    }

    @DisplayName("Full cycle through all variants returns to start")
    @Test
    void fullCycle_returnsToStart() {
        DisableTutorialSetting[] allValues = DisableTutorialSetting.values();
        DisableTutorialSetting current = DisableTutorialSetting.ENABLED;
        for (int i = 0; i < allValues.length; i++) {
            current = (DisableTutorialSetting) current.getNextSetting().getNodeValue();
        }
        assertEquals(DisableTutorialSetting.ENABLED, current);
    }

    @DisplayName("fromString round-trips for all variants")
    @ParameterizedTest
    @EnumSource(DisableTutorialSetting.class)
    void fromString_roundTrip_allVariants(DisableTutorialSetting setting) {
        assertEquals(setting, setting.fromString(setting.name()).orElseThrow());
    }

    @DisplayName("fromString is case-insensitive")
    @Test
    void fromString_caseInsensitive() {
        assertEquals(DisableTutorialSetting.ENABLED,
                DisableTutorialSetting.ENABLED.fromString("enabled").orElseThrow());
        assertEquals(DisableTutorialSetting.DISABLED,
                DisableTutorialSetting.DISABLED.fromString("Disabled").orElseThrow());
    }

    @DisplayName("fromString returns empty for unknown value")
    @Test
    void fromString_unknownValue_returnsEmpty() {
        assertTrue(DisableTutorialSetting.ENABLED.fromString("UNKNOWN").isEmpty());
    }

    @DisplayName("isDisabled returns true for DISABLED")
    @Test
    void isDisabled_disabled_returnsTrue() {
        assertTrue(DisableTutorialSetting.DISABLED.isDisabled());
    }

    @DisplayName("isDisabled returns false for ENABLED")
    @Test
    void isDisabled_enabled_returnsFalse() {
        assertFalse(DisableTutorialSetting.ENABLED.isDisabled());
    }

    @DisplayName("onSettingChange is a no-op (does not throw)")
    @Test
    void onSettingChange_doesNotThrow() {
        CorePlayer mockPlayer = mock(CorePlayer.class);
        assertDoesNotThrow(() -> {
            DisableTutorialSetting.ENABLED.onSettingChange(mockPlayer, Optional.empty());
            DisableTutorialSetting.DISABLED.onSettingChange(mockPlayer, Optional.of(DisableTutorialSetting.ENABLED));
        });
    }

    @DisplayName("getExpansionKey returns McRPGExpansion key")
    @Test
    void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(DisableTutorialSetting.ENABLED.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, DisableTutorialSetting.ENABLED.getExpansionKey().get());
    }
}
