package us.eunoians.mcrpg.setting.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisableBonusExperienceConsumptionSettingTest {

    @DisplayName("DISABLED cycles to ENABLED")
    @Test
    void getNextSetting_disabled_cyclesToEnabled() {
        DisableBonusExperienceConsumptionSetting next =
                (DisableBonusExperienceConsumptionSetting) DisableBonusExperienceConsumptionSetting.DISABLED.getNextSetting().getNodeValue();
        assertEquals(DisableBonusExperienceConsumptionSetting.ENABLED, next);
    }

    @DisplayName("ENABLED cycles back to DISABLED")
    @Test
    void getNextSetting_enabled_cyclesToDisabled() {
        DisableBonusExperienceConsumptionSetting next =
                (DisableBonusExperienceConsumptionSetting) DisableBonusExperienceConsumptionSetting.ENABLED.getNextSetting().getNodeValue();
        assertEquals(DisableBonusExperienceConsumptionSetting.DISABLED, next);
    }

    @DisplayName("getFirstSetting() always returns DISABLED")
    @Test
    void getFirstSetting_returnsDisabled() {
        assertEquals(DisableBonusExperienceConsumptionSetting.DISABLED,
                DisableBonusExperienceConsumptionSetting.ENABLED.getFirstSetting().getNodeValue());
        assertEquals(DisableBonusExperienceConsumptionSetting.DISABLED,
                DisableBonusExperienceConsumptionSetting.DISABLED.getFirstSetting().getNodeValue());
    }

    @DisplayName("Full cycle returns to start")
    @Test
    void fullCycle_returnsToStart() {
        var start = DisableBonusExperienceConsumptionSetting.DISABLED;
        var second = (DisableBonusExperienceConsumptionSetting) start.getNextSetting().getNodeValue();
        var third = (DisableBonusExperienceConsumptionSetting) second.getNextSetting().getNodeValue();
        assertEquals(start, third);
    }

    @DisplayName("fromString round-trips for both variants")
    @Test
    void fromString_roundTrip_bothVariants() {
        assertEquals(DisableBonusExperienceConsumptionSetting.ENABLED,
                DisableBonusExperienceConsumptionSetting.ENABLED.fromString("ENABLED").orElseThrow());
        assertEquals(DisableBonusExperienceConsumptionSetting.DISABLED,
                DisableBonusExperienceConsumptionSetting.DISABLED.fromString("DISABLED").orElseThrow());
    }

    @DisplayName("fromString is case-insensitive")
    @Test
    void fromString_caseInsensitive() {
        assertEquals(DisableBonusExperienceConsumptionSetting.ENABLED,
                DisableBonusExperienceConsumptionSetting.ENABLED.fromString("enabled").orElseThrow());
        assertEquals(DisableBonusExperienceConsumptionSetting.DISABLED,
                DisableBonusExperienceConsumptionSetting.DISABLED.fromString("Disabled").orElseThrow());
    }

    @DisplayName("fromString returns empty for unknown value")
    @Test
    void fromString_unknownValue_returnsEmpty() {
        assertTrue(DisableBonusExperienceConsumptionSetting.ENABLED.fromString("NOT_A_SETTING").isEmpty());
    }

    @DisplayName("getSettingKey returns expected key")
    @Test
    void getSettingKey_returnsExpectedKey() {
        assertEquals("mcrpg:disable-bonus-experience-consumption-setting",
                DisableBonusExperienceConsumptionSetting.ENABLED.getSettingKey().toString());
    }
}
