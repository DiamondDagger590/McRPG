package us.eunoians.mcrpg.setting.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecificLocaleSettingTest {

    @Nested
    @DisplayName("Constructor and locale parsing")
    class ConstructorParsing {

        @DisplayName("Single-part locale code creates correct Locale")
        @Test
        void singlePartCode_createsCorrectLocale() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            assertEquals(Locale.of("en"), setting.getLocale());
        }

        @DisplayName("Two-part locale code creates Locale with country")
        @Test
        void twoPartCode_createsLocaleWithCountry() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en_US");
            assertEquals(Locale.of("en", "US"), setting.getLocale());
        }

        @DisplayName("getLocaleCode returns the original code")
        @Test
        void getLocaleCode_returnsOriginalCode() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("fr");
            assertEquals("fr", setting.getLocaleCode());
        }

        @DisplayName("getLocaleCode preserves two-part code")
        @Test
        void getLocaleCode_preservesTwoPartCode() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("pt_BR");
            assertEquals("pt_BR", setting.getLocaleCode());
        }

        @DisplayName("getLocale is not null")
        @Test
        void getLocale_isNotNull() {
            assertNotNull(new SpecificLocaleSetting("de").getLocale());
        }
    }

    @Nested
    @DisplayName("name()")
    class Name {

        @DisplayName("name() returns the locale code")
        @Test
        void name_returnsLocaleCode() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("ja");
            assertEquals("ja", setting.name());
        }

        @DisplayName("name() returns two-part locale code")
        @Test
        void name_returnsTwoPartLocaleCode() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("zh_CN");
            assertEquals("zh_CN", setting.name());
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @DisplayName("Same locale code is equal")
        @Test
        void sameCode_isEqual() {
            SpecificLocaleSetting a = new SpecificLocaleSetting("en");
            SpecificLocaleSetting b = new SpecificLocaleSetting("en");
            assertEquals(a, b);
        }

        @DisplayName("Different locale codes are not equal")
        @Test
        void differentCodes_areNotEqual() {
            SpecificLocaleSetting a = new SpecificLocaleSetting("en");
            SpecificLocaleSetting b = new SpecificLocaleSetting("fr");
            assertNotEquals(a, b);
        }

        @DisplayName("Same code has same hashCode")
        @Test
        void sameCode_sameHashCode() {
            SpecificLocaleSetting a = new SpecificLocaleSetting("en");
            SpecificLocaleSetting b = new SpecificLocaleSetting("en");
            assertEquals(a.hashCode(), b.hashCode());
        }

        @DisplayName("Not equal to null")
        @Test
        void notEqualToNull() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            assertFalse(setting.equals(null));
        }

        @DisplayName("Not equal to different type")
        @Test
        void notEqualToDifferentType() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            assertFalse(setting.equals("en"));
        }

        @DisplayName("Equal to itself")
        @Test
        void equalToItself() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            assertEquals(setting, setting);
        }

        @DisplayName("Two-part codes with same values are equal")
        @Test
        void twoPartCodes_sameValues_areEqual() {
            SpecificLocaleSetting a = new SpecificLocaleSetting("en_US");
            SpecificLocaleSetting b = new SpecificLocaleSetting("en_US");
            assertEquals(a, b);
        }

        @DisplayName("Two-part code differs from one-part code")
        @Test
        void twoPartCode_differsFromOnePartCode() {
            SpecificLocaleSetting a = new SpecificLocaleSetting("en");
            SpecificLocaleSetting b = new SpecificLocaleSetting("en_US");
            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("getSettingKey")
    class GetSettingKey {

        @DisplayName("getSettingKey returns locale setting key")
        @Test
        void getSettingKey_returnsLocaleKey() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            assertEquals("mcrpg:locale-setting", setting.getSettingKey().toString());
        }
    }

    @Nested
    @DisplayName("fromString")
    class FromString {

        @DisplayName("fromString matches CLIENT_LOCALE case-insensitively")
        @Test
        void fromString_matchesClientLocale_caseInsensitive() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            var result = setting.fromString("client_locale");
            assertTrue(result.isPresent());
            assertEquals(LocaleSetting.CLIENT_LOCALE, result.get());
        }

        @DisplayName("fromString matches CLIENT_LOCALE in uppercase")
        @Test
        void fromString_matchesClientLocale_uppercase() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            var result = setting.fromString("CLIENT_LOCALE");
            assertTrue(result.isPresent());
            assertEquals(LocaleSetting.CLIENT_LOCALE, result.get());
        }

        @DisplayName("fromString matches SERVER_LOCALE case-insensitively")
        @Test
        void fromString_matchesServerLocale_caseInsensitive() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            var result = setting.fromString("server_locale");
            assertTrue(result.isPresent());
            assertEquals(LocaleSetting.SERVER_LOCALE, result.get());
        }

        @DisplayName("fromString matches SERVER_LOCALE in uppercase")
        @Test
        void fromString_matchesServerLocale_uppercase() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            var result = setting.fromString("SERVER_LOCALE");
            assertTrue(result.isPresent());
            assertEquals(LocaleSetting.SERVER_LOCALE, result.get());
        }
    }
}
