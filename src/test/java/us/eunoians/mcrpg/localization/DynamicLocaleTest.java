package us.eunoians.mcrpg.localization;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicLocaleTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Parses language-only locale code")
        void constructor_parsesLanguageOnlyCode() {
            YamlDocument doc = mock(YamlDocument.class);
            when(doc.getString("locale")).thenReturn("en");

            DynamicLocale locale = new DynamicLocale(doc);

            assertEquals(Locale.of("en"), locale.getLocale());
        }

        @Test
        @DisplayName("Parses language-country locale code")
        void constructor_parsesLanguageCountryCode() {
            YamlDocument doc = mock(YamlDocument.class);
            when(doc.getString("locale")).thenReturn("en_US");

            DynamicLocale locale = new DynamicLocale(doc);

            assertEquals(Locale.of("en", "US"), locale.getLocale());
        }

        @Test
        @DisplayName("Throws on null locale key")
        void constructor_throwsOnNullLocaleKey() {
            YamlDocument doc = mock(YamlDocument.class);
            when(doc.getString("locale")).thenReturn(null);

            assertThrows(IllegalArgumentException.class, () -> new DynamicLocale(doc));
        }

        @Test
        @DisplayName("Throws on blank locale key")
        void constructor_throwsOnBlankLocaleKey() {
            YamlDocument doc = mock(YamlDocument.class);
            when(doc.getString("locale")).thenReturn("   ");

            assertThrows(IllegalArgumentException.class, () -> new DynamicLocale(doc));
        }

        @Test
        @DisplayName("Throws on empty locale key")
        void constructor_throwsOnEmptyLocaleKey() {
            YamlDocument doc = mock(YamlDocument.class);
            when(doc.getString("locale")).thenReturn("");

            assertThrows(IllegalArgumentException.class, () -> new DynamicLocale(doc));
        }

        @Test
        @DisplayName("Three-segment locale uses only language and country")
        void constructor_threeSegmentLocale_usesLanguageAndCountry() {
            YamlDocument doc = mock(YamlDocument.class);
            when(doc.getString("locale")).thenReturn("en_US_POSIX");

            DynamicLocale locale = new DynamicLocale(doc);

            assertEquals("en", locale.getLocale().getLanguage());
            assertEquals("US", locale.getLocale().getCountry());
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {

        @Test
        @DisplayName("getConfigurationFile returns the document passed to the constructor")
        void getConfigurationFile_returnsConstructorDocument() {
            YamlDocument doc = mock(YamlDocument.class);
            when(doc.getString("locale")).thenReturn("fr");

            DynamicLocale locale = new DynamicLocale(doc);

            assertEquals(doc, locale.getConfigurationFile());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPGExpansion key")
        void getExpansionKey_returnsMcRPGExpansionKey() {
            YamlDocument doc = mock(YamlDocument.class);
            when(doc.getString("locale")).thenReturn("de");

            DynamicLocale locale = new DynamicLocale(doc);

            assertTrue(locale.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, locale.getExpansionKey().get());
        }

        @Test
        @DisplayName("getLocale returns correctly parsed locale")
        void getLocale_returnsParsedLocale() {
            YamlDocument doc = mock(YamlDocument.class);
            when(doc.getString("locale")).thenReturn("pt_BR");

            DynamicLocale locale = new DynamicLocale(doc);

            Locale result = locale.getLocale();
            assertNotNull(result);
            assertEquals("pt", result.getLanguage());
            assertEquals("BR", result.getCountry());
        }
    }
}
