package us.eunoians.mcrpg.setting.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link LocaleSetting}.
 */
public class LocaleSettingTest extends McRPGBaseTest {

    @Nested
    @DisplayName("fromString")
    class FromStringTest {

        @Test
        @DisplayName("fromString matches CLIENT_LOCALE exactly")
        void fromString_matchesClientLocale_whenExact() {
            Optional<?> result = LocaleSetting.CLIENT_LOCALE.fromString("CLIENT_LOCALE");

            assertTrue(result.isPresent());
            assertEquals(LocaleSetting.CLIENT_LOCALE, result.get());
        }

        @Test
        @DisplayName("fromString matches SERVER_LOCALE exactly")
        void fromString_matchesServerLocale_whenExact() {
            Optional<?> result = LocaleSetting.CLIENT_LOCALE.fromString("SERVER_LOCALE");

            assertTrue(result.isPresent());
            assertEquals(LocaleSetting.SERVER_LOCALE, result.get());
        }

        @Test
        @DisplayName("fromString is case-insensitive for CLIENT_LOCALE")
        void fromString_isCaseInsensitive_whenClientLocale() {
            Optional<?> result = LocaleSetting.CLIENT_LOCALE.fromString("client_locale");

            assertTrue(result.isPresent());
            assertEquals(LocaleSetting.CLIENT_LOCALE, result.get());
        }

        @Test
        @DisplayName("fromString is case-insensitive for SERVER_LOCALE")
        void fromString_isCaseInsensitive_whenServerLocale() {
            Optional<?> result = LocaleSetting.SERVER_LOCALE.fromString("server_locale");

            assertTrue(result.isPresent());
            assertEquals(LocaleSetting.SERVER_LOCALE, result.get());
        }

        @Test
        @DisplayName("fromString returns empty for unknown string")
        void fromString_returnsEmpty_whenUnknownString() {
            Optional<?> result = LocaleSetting.CLIENT_LOCALE.fromString("NONEXISTENT_SETTING");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("fromString returns empty for empty string")
        void fromString_returnsEmpty_whenEmptyString() {
            Optional<?> result = LocaleSetting.CLIENT_LOCALE.fromString("");

            assertFalse(result.isPresent());
        }

        @ParameterizedTest
        @EnumSource(LocaleSetting.class)
        @DisplayName("fromString matches each enum value by name")
        void fromString_matchesEachValue_byName(LocaleSetting setting) {
            Optional<?> result = setting.fromString(setting.name());

            assertTrue(result.isPresent());
            assertEquals(setting, result.get());
        }

        @ParameterizedTest
        @EnumSource(LocaleSetting.class)
        @DisplayName("fromString matches each enum value case-insensitively")
        void fromString_matchesEachValue_caseInsensitively(LocaleSetting setting) {
            Optional<?> result = setting.fromString(setting.name().toLowerCase());

            assertTrue(result.isPresent());
            assertEquals(setting, result.get());
        }
    }

    @Nested
    @DisplayName("getSettingKey")
    class SettingKeyTest {

        @Test
        @DisplayName("getSettingKey returns locale-setting NamespacedKey")
        void getSettingKey_returnsLocaleSettingKey() {
            assertEquals(LocalePlayerSetting.SETTING_KEY, LocaleSetting.CLIENT_LOCALE.getSettingKey());
        }

        @Test
        @DisplayName("getSettingKey is consistent across enum values")
        void getSettingKey_isConsistent_acrossEnumValues() {
            assertEquals(
                    LocaleSetting.CLIENT_LOCALE.getSettingKey(),
                    LocaleSetting.SERVER_LOCALE.getSettingKey());
        }
    }

    @Nested
    @DisplayName("enum values")
    class EnumValuesTest {

        @Test
        @DisplayName("CLIENT_LOCALE and SERVER_LOCALE are the only values")
        void enumValues_containsExpectedValues() {
            LocaleSetting[] values = LocaleSetting.values();

            assertEquals(2, values.length);
            assertEquals(LocaleSetting.CLIENT_LOCALE, values[0]);
            assertEquals(LocaleSetting.SERVER_LOCALE, values[1]);
        }

        @Test
        @DisplayName("valueOf returns correct value for CLIENT_LOCALE")
        void valueOf_returnsCorrectValue_whenClientLocale() {
            assertEquals(LocaleSetting.CLIENT_LOCALE, LocaleSetting.valueOf("CLIENT_LOCALE"));
        }

        @Test
        @DisplayName("valueOf returns correct value for SERVER_LOCALE")
        void valueOf_returnsCorrectValue_whenServerLocale() {
            assertEquals(LocaleSetting.SERVER_LOCALE, LocaleSetting.valueOf("SERVER_LOCALE"));
        }
    }
}
