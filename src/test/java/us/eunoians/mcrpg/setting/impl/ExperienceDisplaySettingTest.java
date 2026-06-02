package us.eunoians.mcrpg.setting.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExperienceDisplaySettingTest {

    @Nested
    @DisplayName("Linked-node cycle")
    class LinkedNodeCycle {

        @DisplayName("BOSS_BAR cycles to ACTION_BAR")
        @Test
        void getNextSetting_bossBar_cyclesToActionBar() {
            ExperienceDisplaySetting next =
                    (ExperienceDisplaySetting) ExperienceDisplaySetting.BOSS_BAR.getNextSetting().getNodeValue();
            assertEquals(ExperienceDisplaySetting.ACTION_BAR, next);
        }

        @DisplayName("ACTION_BAR cycles back to BOSS_BAR")
        @Test
        void getNextSetting_actionBar_cyclesToBossBar() {
            ExperienceDisplaySetting next =
                    (ExperienceDisplaySetting) ExperienceDisplaySetting.ACTION_BAR.getNextSetting().getNodeValue();
            assertEquals(ExperienceDisplaySetting.BOSS_BAR, next);
        }

        @DisplayName("getFirstSetting() always returns BOSS_BAR")
        @Test
        void getFirstSetting_returnsBossBar() {
            assertEquals(ExperienceDisplaySetting.BOSS_BAR,
                    ExperienceDisplaySetting.BOSS_BAR.getFirstSetting().getNodeValue());
            assertEquals(ExperienceDisplaySetting.BOSS_BAR,
                    ExperienceDisplaySetting.ACTION_BAR.getFirstSetting().getNodeValue());
        }

        @DisplayName("Full cycle returns to start")
        @Test
        void fullCycle_returnsToStart() {
            var start = ExperienceDisplaySetting.BOSS_BAR;
            var second = (ExperienceDisplaySetting) start.getNextSetting().getNodeValue();
            var third = (ExperienceDisplaySetting) second.getNextSetting().getNodeValue();
            assertEquals(start, third);
        }

        @DisplayName("Every variant reaches every other variant via cycling")
        @ParameterizedTest
        @EnumSource(ExperienceDisplaySetting.class)
        void allVariants_reachableViaCycle(ExperienceDisplaySetting start) {
            var current = start;
            for (int i = 0; i < ExperienceDisplaySetting.values().length; i++) {
                current = (ExperienceDisplaySetting) current.getNextSetting().getNodeValue();
            }
            assertEquals(start, current, "Cycling through all values should return to start");
        }
    }

    @Nested
    @DisplayName("fromString")
    class FromString {

        @DisplayName("fromString round-trips for both variants")
        @Test
        void fromString_roundTrip_bothVariants() {
            assertEquals(ExperienceDisplaySetting.BOSS_BAR,
                    ExperienceDisplaySetting.BOSS_BAR.fromString("BOSS_BAR").orElseThrow());
            assertEquals(ExperienceDisplaySetting.ACTION_BAR,
                    ExperienceDisplaySetting.ACTION_BAR.fromString("ACTION_BAR").orElseThrow());
        }

        @DisplayName("fromString is case-insensitive")
        @Test
        void fromString_caseInsensitive() {
            assertEquals(ExperienceDisplaySetting.BOSS_BAR,
                    ExperienceDisplaySetting.BOSS_BAR.fromString("boss_bar").orElseThrow());
            assertEquals(ExperienceDisplaySetting.ACTION_BAR,
                    ExperienceDisplaySetting.ACTION_BAR.fromString("action_bar").orElseThrow());
        }

        @DisplayName("fromString handles mixed case")
        @Test
        void fromString_mixedCase() {
            assertEquals(ExperienceDisplaySetting.BOSS_BAR,
                    ExperienceDisplaySetting.BOSS_BAR.fromString("Boss_Bar").orElseThrow());
        }

        @DisplayName("fromString returns empty for unknown value")
        @Test
        void fromString_unknownValue_returnsEmpty() {
            assertTrue(ExperienceDisplaySetting.BOSS_BAR.fromString("NOT_A_SETTING").isEmpty());
        }

        @DisplayName("fromString returns empty for empty string")
        @Test
        void fromString_emptyString_returnsEmpty() {
            assertTrue(ExperienceDisplaySetting.BOSS_BAR.fromString("").isEmpty());
        }

        @DisplayName("fromString works from any variant instance")
        @ParameterizedTest
        @EnumSource(ExperienceDisplaySetting.class)
        void fromString_worksFromAnyInstance(ExperienceDisplaySetting setting) {
            assertEquals(ExperienceDisplaySetting.BOSS_BAR,
                    setting.fromString("BOSS_BAR").orElseThrow());
        }
    }

    @Nested
    @DisplayName("getSettingKey")
    class GetSettingKey {

        @DisplayName("getSettingKey returns expected key")
        @Test
        void getSettingKey_returnsExpectedKey() {
            assertEquals("mcrpg:experience-display-setting",
                    ExperienceDisplaySetting.BOSS_BAR.getSettingKey().toString());
        }

        @DisplayName("All variants share the same setting key")
        @Test
        void allVariants_shareSameKey() {
            assertEquals(ExperienceDisplaySetting.BOSS_BAR.getSettingKey(),
                    ExperienceDisplaySetting.ACTION_BAR.getSettingKey());
        }
    }
}
