package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("DisableTutorialSetting")
public class DisableTutorialSettingTest {

    @DisplayName("DISABLED reports isDisabled() as true")
    @Test
    void isDisabled_disabledVariant_returnsTrue() {
        assertTrue(DisableTutorialSetting.DISABLED.isDisabled());
    }

    @DisplayName("ENABLED reports isDisabled() as false")
    @Test
    void isDisabled_enabledVariant_returnsFalse() {
        assertFalse(DisableTutorialSetting.ENABLED.isDisabled());
    }

    @DisplayName("ENABLED cycles to DISABLED")
    @Test
    void getNextSetting_enabled_cyclesToDisabled() {
        DisableTutorialSetting next =
                (DisableTutorialSetting) DisableTutorialSetting.ENABLED.getNextSetting().getNodeValue();
        assertEquals(DisableTutorialSetting.DISABLED, next);
    }

    @DisplayName("DISABLED cycles back to ENABLED")
    @Test
    void getNextSetting_disabled_cyclesToEnabled() {
        DisableTutorialSetting next =
                (DisableTutorialSetting) DisableTutorialSetting.DISABLED.getNextSetting().getNodeValue();
        assertEquals(DisableTutorialSetting.ENABLED, next);
    }

    @DisplayName("getFirstSetting() always returns ENABLED")
    @Test
    void getFirstSetting_returnsEnabled() {
        assertEquals(DisableTutorialSetting.ENABLED,
                DisableTutorialSetting.ENABLED.getFirstSetting().getNodeValue());
        assertEquals(DisableTutorialSetting.ENABLED,
                DisableTutorialSetting.DISABLED.getFirstSetting().getNodeValue());
    }

    @DisplayName("fromString round-trips for both variants")
    @Test
    void fromString_roundTrip_bothVariants() {
        assertEquals(DisableTutorialSetting.ENABLED,
                DisableTutorialSetting.ENABLED.fromString("ENABLED").orElseThrow());
        assertEquals(DisableTutorialSetting.DISABLED,
                DisableTutorialSetting.DISABLED.fromString("DISABLED").orElseThrow());
    }

    @DisplayName("fromString is case-insensitive")
    @Test
    void fromString_caseInsensitive() {
        assertEquals(DisableTutorialSetting.ENABLED,
                DisableTutorialSetting.ENABLED.fromString("enabled").orElseThrow());
        assertEquals(DisableTutorialSetting.DISABLED,
                DisableTutorialSetting.DISABLED.fromString("disabled").orElseThrow());
    }

    @DisplayName("fromString returns empty for unknown value")
    @Test
    void fromString_unknownValue_returnsEmpty() {
        assertTrue(DisableTutorialSetting.ENABLED.fromString("NOT_A_SETTING").isEmpty());
    }

    @DisplayName("getSettingKey returns the expected namespaced key")
    @Test
    void getSettingKey_returnsExpectedKey() {
        assertEquals("mcrpg:disable-tutorial-setting",
                DisableTutorialSetting.ENABLED.getSettingKey().toString());
    }

    @DisplayName("onSettingChange is a no-op and does not throw")
    @Test
    void onSettingChange_doesNotThrow() {
        CorePlayer mockPlayer = mock(CorePlayer.class);
        DisableTutorialSetting.ENABLED.onSettingChange(mockPlayer, Optional.empty());
        DisableTutorialSetting.DISABLED.onSettingChange(mockPlayer, Optional.of(mock(PlayerSetting.class)));
    }
}
