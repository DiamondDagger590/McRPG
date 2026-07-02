package us.eunoians.mcrpg.util;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class McRPGMethodsTest extends McRPGBaseTest {

    @Nested
    @DisplayName("getMcRPGNamespace")
    class GetMcRPGNamespace {

        @Test
        @DisplayName("Returns 'mcrpg'")
        void returnsMcrpg() {
            assertEquals("mcrpg", McRPGMethods.getMcRPGNamespace());
        }
    }

    @Nested
    @DisplayName("formatDuration")
    class FormatDuration {

        @Test
        @DisplayName("Zero milliseconds returns '<1m'")
        void zero_returnsLessThanOneMinute() {
            assertEquals("<1m", McRPGMethods.formatDuration(0));
        }

        @Test
        @DisplayName("Sub-minute duration returns '<1m'")
        void subMinute_returnsLessThanOneMinute() {
            assertEquals("<1m", McRPGMethods.formatDuration(59_999));
        }

        @Test
        @DisplayName("Exactly one minute returns '1m'")
        void exactlyOneMinute() {
            assertEquals("1m", McRPGMethods.formatDuration(60_000));
        }

        @Test
        @DisplayName("Minutes only returns '{n}m'")
        void minutesOnly() {
            assertEquals("45m", McRPGMethods.formatDuration(45 * 60_000L));
        }

        @Test
        @DisplayName("Exactly one hour returns '1h'")
        void exactlyOneHour() {
            assertEquals("1h", McRPGMethods.formatDuration(3_600_000));
        }

        @Test
        @DisplayName("Hours only returns '{n}h'")
        void hoursOnly() {
            assertEquals("3h", McRPGMethods.formatDuration(3 * 3_600_000L));
        }

        @Test
        @DisplayName("Hours and minutes returns '{h}h {m}m'")
        void hoursAndMinutes() {
            assertEquals("2h 30m", McRPGMethods.formatDuration(2 * 3_600_000L + 30 * 60_000L));
        }

        @Test
        @DisplayName("Large duration formats correctly")
        void largeDuration() {
            assertEquals("24h 59m", McRPGMethods.formatDuration(24 * 3_600_000L + 59 * 60_000L));
        }

        @Test
        @DisplayName("Leftover seconds below one minute ignored")
        void leftoverSecondsIgnored() {
            assertEquals("1h", McRPGMethods.formatDuration(3_600_000 + 30_000));
        }
    }

    @Nested
    @DisplayName("parseNamespacedKey")
    class ParseNamespacedKey {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t"})
        @DisplayName("Null or blank returns null")
        void nullOrBlank_returnsNull(String input) {
            assertNull(McRPGMethods.parseNamespacedKey(input));
        }

        @Test
        @DisplayName("Bare key auto-namespaces under mcrpg")
        void bareKey_autoNamespaces() {
            NamespacedKey key = McRPGMethods.parseNamespacedKey("bleed");
            assertNotNull(key);
            assertEquals("mcrpg", key.getNamespace());
            assertEquals("bleed", key.getKey());
        }

        @Test
        @DisplayName("Fully qualified key preserves namespace")
        void fullyQualified_preservesNamespace() {
            NamespacedKey key = McRPGMethods.parseNamespacedKey("custom:my_ability");
            assertNotNull(key);
            assertEquals("custom", key.getNamespace());
            assertEquals("my_ability", key.getKey());
        }

        @Test
        @DisplayName("Input is lowercased")
        void inputIsLowercased() {
            NamespacedKey key = McRPGMethods.parseNamespacedKey("BLEED");
            assertNotNull(key);
            assertEquals("bleed", key.getKey());
        }

        @ParameterizedTest
        @CsvSource({
                "Custom:MY_KEY, custom, my_key",
                "MCRPG:BLEED, mcrpg, bleed"
        })
        @DisplayName("Fully qualified input is lowercased")
        void fullyQualified_lowercased(String input, String expectedNamespace, String expectedKey) {
            NamespacedKey key = McRPGMethods.parseNamespacedKey(input);
            assertNotNull(key);
            assertEquals(expectedNamespace, key.getNamespace());
            assertEquals(expectedKey, key.getKey());
        }
    }
}
