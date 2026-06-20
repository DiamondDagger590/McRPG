package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SuppressWarnings("deprecation")
class RequireEmptyOffhandSettingTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Linked-node cycle")
    class LinkedNodeCycle {

        @Test
        @DisplayName("ENABLED cycles to DISABLED")
        void getNextSetting_enabled_cyclesToDisabled() {
            RequireEmptyOffhandSetting next =
                    (RequireEmptyOffhandSetting) RequireEmptyOffhandSetting.ENABLED.getNextSetting().getNodeValue();
            assertEquals(RequireEmptyOffhandSetting.DISABLED, next);
        }

        @Test
        @DisplayName("DISABLED cycles back to ENABLED")
        void getNextSetting_disabled_cyclesToEnabled() {
            RequireEmptyOffhandSetting next =
                    (RequireEmptyOffhandSetting) RequireEmptyOffhandSetting.DISABLED.getNextSetting().getNodeValue();
            assertEquals(RequireEmptyOffhandSetting.ENABLED, next);
        }

        @Test
        @DisplayName("getFirstSetting always returns ENABLED")
        void getFirstSetting_returnsEnabled() {
            assertEquals(RequireEmptyOffhandSetting.ENABLED,
                    RequireEmptyOffhandSetting.ENABLED.getFirstSetting().getNodeValue());
            assertEquals(RequireEmptyOffhandSetting.ENABLED,
                    RequireEmptyOffhandSetting.DISABLED.getFirstSetting().getNodeValue());
        }

        @Test
        @DisplayName("Full cycle returns to start")
        void fullCycle_returnsToStart() {
            var start = RequireEmptyOffhandSetting.ENABLED;
            var second = (RequireEmptyOffhandSetting) start.getNextSetting().getNodeValue();
            var third = (RequireEmptyOffhandSetting) second.getNextSetting().getNodeValue();
            assertEquals(start, third);
        }

        @ParameterizedTest
        @EnumSource(RequireEmptyOffhandSetting.class)
        @DisplayName("Every variant reaches every other variant via cycling")
        void allVariants_reachableViaCycle(RequireEmptyOffhandSetting start) {
            var current = start;
            for (int i = 0; i < RequireEmptyOffhandSetting.values().length; i++) {
                current = (RequireEmptyOffhandSetting) current.getNextSetting().getNodeValue();
            }
            assertEquals(start, current, "Cycling through all values should return to start");
        }
    }

    @Nested
    @DisplayName("fromString")
    class FromString {

        @Test
        @DisplayName("fromString round-trips for both variants")
        void fromString_roundTrip_bothVariants() {
            assertEquals(RequireEmptyOffhandSetting.ENABLED,
                    RequireEmptyOffhandSetting.ENABLED.fromString("ENABLED").orElseThrow());
            assertEquals(RequireEmptyOffhandSetting.DISABLED,
                    RequireEmptyOffhandSetting.DISABLED.fromString("DISABLED").orElseThrow());
        }

        @Test
        @DisplayName("fromString is case-insensitive")
        void fromString_caseInsensitive() {
            assertEquals(RequireEmptyOffhandSetting.ENABLED,
                    RequireEmptyOffhandSetting.ENABLED.fromString("enabled").orElseThrow());
            assertEquals(RequireEmptyOffhandSetting.DISABLED,
                    RequireEmptyOffhandSetting.DISABLED.fromString("disabled").orElseThrow());
        }

        @Test
        @DisplayName("fromString handles mixed case")
        void fromString_mixedCase() {
            assertEquals(RequireEmptyOffhandSetting.ENABLED,
                    RequireEmptyOffhandSetting.ENABLED.fromString("Enabled").orElseThrow());
        }

        @Test
        @DisplayName("fromString returns empty for unknown value")
        void fromString_unknownValue_returnsEmpty() {
            assertTrue(RequireEmptyOffhandSetting.ENABLED.fromString("NOT_A_SETTING").isEmpty());
        }

        @Test
        @DisplayName("fromString returns empty for empty string")
        void fromString_emptyString_returnsEmpty() {
            assertTrue(RequireEmptyOffhandSetting.ENABLED.fromString("").isEmpty());
        }

        @ParameterizedTest
        @EnumSource(RequireEmptyOffhandSetting.class)
        @DisplayName("fromString works from any variant instance")
        void fromString_worksFromAnyInstance(RequireEmptyOffhandSetting setting) {
            assertEquals(RequireEmptyOffhandSetting.ENABLED,
                    setting.fromString("ENABLED").orElseThrow());
        }
    }

    @Nested
    @DisplayName("getSettingKey")
    class GetSettingKey {

        @Test
        @DisplayName("getSettingKey returns expected key")
        void getSettingKey_returnsExpectedKey() {
            assertEquals("mcrpg:require-empty-offhand-setting",
                    RequireEmptyOffhandSetting.ENABLED.getSettingKey().toString());
        }

        @Test
        @DisplayName("All variants share the same setting key")
        void allVariants_shareSameKey() {
            assertEquals(RequireEmptyOffhandSetting.ENABLED.getSettingKey(),
                    RequireEmptyOffhandSetting.DISABLED.getSettingKey());
        }

        @Test
        @DisplayName("SETTING_KEY constant matches getSettingKey")
        void settingKeyConstant_matchesGetterValue() {
            assertEquals(RequireEmptyOffhandSetting.SETTING_KEY,
                    RequireEmptyOffhandSetting.ENABLED.getSettingKey());
        }
    }

    @Nested
    @DisplayName("onSettingChange")
    class OnSettingChange {

        @Test
        @DisplayName("onSettingChange does not throw")
        void onSettingChange_doesNotThrow() {
            var player = mock(CorePlayer.class);
            Optional<PlayerSetting> oldSetting = Optional.of(RequireEmptyOffhandSetting.DISABLED);
            RequireEmptyOffhandSetting.ENABLED.onSettingChange(player, oldSetting);
        }

        @Test
        @DisplayName("onSettingChange with empty old setting does not throw")
        void onSettingChange_emptyOldSetting_doesNotThrow() {
            var player = mock(CorePlayer.class);
            RequireEmptyOffhandSetting.ENABLED.onSettingChange(player, Optional.empty());
        }
    }

    @Nested
    @DisplayName("getExpansionKey")
    class GetExpansionKey {

        @Test
        @DisplayName("getExpansionKey is present")
        void getExpansionKey_isPresent() {
            assertTrue(RequireEmptyOffhandSetting.ENABLED.getExpansionKey().isPresent());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPG expansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertNotNull(RequireEmptyOffhandSetting.ENABLED.getExpansionKey().orElseThrow());
        }
    }
}
