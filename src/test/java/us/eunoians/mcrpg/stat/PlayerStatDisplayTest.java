package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.stat.impl.FlatPlayerStat;
import us.eunoians.mcrpg.stat.impl.ResourcePoolPlayerStat;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerStatDisplayTest {

    private static final NamespacedKey TEST_KEY = new NamespacedKey("test", "display_stat");

    @Nested
    @DisplayName("getDisplayName (no player)")
    class GetDisplayNameServerTests {

        @DisplayName("returns fallback when localization unavailable")
        @Test
        void getDisplayName_returnsFallback_whenLocalizationUnavailable() {
            var stat = new FlatPlayerStat(TEST_KEY, "Attack Power", "⚔", 10.0);
            assertEquals("Attack Power", stat.getDisplayName());
        }

        @DisplayName("returns fallback for resource pool stat")
        @Test
        void getDisplayName_returnsFallback_forResourcePool() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "Health", "❤", 200, 5);
            assertEquals("Health", stat.getDisplayName());
        }

        @DisplayName("returns different fallback per stat instance")
        @Test
        void getDisplayName_returnsDifferentFallback_perInstance() {
            var stat1 = new FlatPlayerStat(TEST_KEY, "Defense", "🛡", 5.0);
            var stat2 = new FlatPlayerStat(TEST_KEY, "Speed", "💨", 1.0);
            assertEquals("Defense", stat1.getDisplayName());
            assertEquals("Speed", stat2.getDisplayName());
        }
    }

    @Nested
    @DisplayName("getDisplaySymbol (no player)")
    class GetDisplaySymbolServerTests {

        @DisplayName("returns fallback when localization unavailable")
        @Test
        void getDisplaySymbol_returnsFallback_whenLocalizationUnavailable() {
            var stat = new FlatPlayerStat(TEST_KEY, "Attack Power", "⚔", 10.0);
            assertEquals("⚔", stat.getDisplaySymbol());
        }

        @DisplayName("returns fallback for resource pool stat")
        @Test
        void getDisplaySymbol_returnsFallback_forResourcePool() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "Health", "❤", 200, 5);
            assertEquals("❤", stat.getDisplaySymbol());
        }

        @DisplayName("returns different fallback per stat instance")
        @Test
        void getDisplaySymbol_returnsDifferentFallback_perInstance() {
            var stat1 = new FlatPlayerStat(TEST_KEY, "Defense", "🛡", 5.0);
            var stat2 = new FlatPlayerStat(TEST_KEY, "Mana", "✦", 100.0);
            assertEquals("🛡", stat1.getDisplaySymbol());
            assertEquals("✦", stat2.getDisplaySymbol());
        }
    }

    @Nested
    @DisplayName("Display name and symbol independence")
    class DisplayIndependenceTests {

        @DisplayName("name and symbol return distinct values")
        @Test
        void getDisplayName_andSymbol_returnDistinctValues() {
            var stat = new FlatPlayerStat(TEST_KEY, "MyName", "MySymbol", 10.0);
            assertEquals("MyName", stat.getDisplayName());
            assertEquals("MySymbol", stat.getDisplaySymbol());
        }

        @DisplayName("empty string fallback is preserved for name")
        @Test
        void getDisplayName_preservesEmptyStringFallback() {
            var stat = new FlatPlayerStat(TEST_KEY, "", "✦", 10.0);
            assertEquals("", stat.getDisplayName());
        }

        @DisplayName("empty string fallback is preserved for symbol")
        @Test
        void getDisplaySymbol_preservesEmptyStringFallback() {
            var stat = new FlatPlayerStat(TEST_KEY, "Mana", "", 10.0);
            assertEquals("", stat.getDisplaySymbol());
        }
    }
}
