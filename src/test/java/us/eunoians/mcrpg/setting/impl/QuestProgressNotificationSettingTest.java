package us.eunoians.mcrpg.setting.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestProgressNotificationSettingTest {

    @DisplayName("ENABLED reports isEnabled() as true")
    @Test
    void isEnabled_enabledVariant_returnsTrue() {
        assertTrue(QuestProgressNotificationSetting.ENABLED.isEnabled());
    }

    @DisplayName("DISABLED reports isEnabled() as false")
    @Test
    void isEnabled_disabledVariant_returnsFalse() {
        assertFalse(QuestProgressNotificationSetting.DISABLED.isEnabled());
    }

    @DisplayName("ENABLED cycles to DISABLED")
    @Test
    void getNextSetting_enabled_cyclestoDisabled() {
        QuestProgressNotificationSetting next =
                (QuestProgressNotificationSetting) QuestProgressNotificationSetting.ENABLED.getNextSetting().getNodeValue();
        assertEquals(QuestProgressNotificationSetting.DISABLED, next);
    }

    @DisplayName("DISABLED cycles back to ENABLED")
    @Test
    void getNextSetting_disabled_cyclesToEnabled() {
        QuestProgressNotificationSetting next =
                (QuestProgressNotificationSetting) QuestProgressNotificationSetting.DISABLED.getNextSetting().getNodeValue();
        assertEquals(QuestProgressNotificationSetting.ENABLED, next);
    }

    @DisplayName("getFirstSetting() always returns ENABLED")
    @Test
    void getFirstSetting_returnsEnabled() {
        assertEquals(QuestProgressNotificationSetting.ENABLED,
                QuestProgressNotificationSetting.ENABLED.getFirstSetting().getNodeValue());
        assertEquals(QuestProgressNotificationSetting.ENABLED,
                QuestProgressNotificationSetting.DISABLED.getFirstSetting().getNodeValue());
    }

    @DisplayName("fromString round-trips for both variants")
    @Test
    void fromString_roundTrip_bothVariants() {
        assertEquals(QuestProgressNotificationSetting.ENABLED,
                QuestProgressNotificationSetting.ENABLED.fromString("ENABLED").orElseThrow());
        assertEquals(QuestProgressNotificationSetting.DISABLED,
                QuestProgressNotificationSetting.DISABLED.fromString("DISABLED").orElseThrow());
    }

    @DisplayName("fromString is case-insensitive")
    @Test
    void fromString_caseInsensitive() {
        assertEquals(QuestProgressNotificationSetting.ENABLED,
                QuestProgressNotificationSetting.ENABLED.fromString("enabled").orElseThrow());
        assertEquals(QuestProgressNotificationSetting.DISABLED,
                QuestProgressNotificationSetting.DISABLED.fromString("disabled").orElseThrow());
    }

    @DisplayName("fromString returns empty for unknown value")
    @Test
    void fromString_unknownValue_returnsEmpty() {
        assertTrue(QuestProgressNotificationSetting.ENABLED.fromString("NOT_A_SETTING").isEmpty());
    }
}
