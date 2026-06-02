package us.eunoians.mcrpg.stat.impl;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerStatImplTest {

    private static final NamespacedKey TEST_KEY = new NamespacedKey("test", "stat");

    @Nested
    @DisplayName("FlatPlayerStat")
    class FlatPlayerStatTests {

        @DisplayName("isResourcePool returns false")
        @Test
        void isResourcePool_returnsFalse() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 10.0);
            assertFalse(stat.isResourcePool());
        }

        @DisplayName("getBaseValue returns constructor value")
        @Test
        void getBaseValue_returnsDefault() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 42.5);
            assertEquals(42.5, stat.getBaseValue());
        }

        @DisplayName("getRegenPerSecond always returns 0")
        @Test
        void getRegenPerSecond_returnsZero() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 100.0);
            assertEquals(0.0, stat.getRegenPerSecond());
        }

        @DisplayName("getKey returns constructor key")
        @Test
        void getKey_returnsKey() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 10.0);
            assertEquals(TEST_KEY, stat.getKey());
        }

        @DisplayName("getReloadableBaseValue returns empty")
        @Test
        void getReloadableBaseValue_returnsEmpty() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 10.0);
            assertTrue(stat.getReloadableBaseValue().isEmpty());
        }

        @DisplayName("getReloadableRegenPerSecond returns empty")
        @Test
        void getReloadableRegenPerSecond_returnsEmpty() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 10.0);
            assertTrue(stat.getReloadableRegenPerSecond().isEmpty());
        }

        @DisplayName("getReloadableContent returns empty set")
        @Test
        void getReloadableContent_returnsEmptySet() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 10.0);
            assertTrue(stat.getReloadableContent().isEmpty());
        }

        @DisplayName("getExpansionKey returns empty")
        @Test
        void getExpansionKey_returnsEmpty() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 10.0);
            assertTrue(stat.getExpansionKey().isEmpty());
        }

        @DisplayName("getDisplayNameRoute derived from key")
        @Test
        void getDisplayNameRoute_derivedFromKey() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 10.0);
            assertEquals(Route.fromString("stat." + TEST_KEY.getKey() + ".display-name"), stat.getDisplayNameRoute());
        }

        @DisplayName("getDisplaySymbolRoute derived from key")
        @Test
        void getDisplaySymbolRoute_derivedFromKey() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 10.0);
            assertEquals(Route.fromString("stat." + TEST_KEY.getKey() + ".display-symbol"), stat.getDisplaySymbolRoute());
        }

        @DisplayName("getBaseValue returns zero when constructed with zero")
        @Test
        void getBaseValue_returnsZero_whenZeroDefault() {
            var stat = new FlatPlayerStat(TEST_KEY, "Test", "T", 0.0);
            assertEquals(0.0, stat.getBaseValue());
        }
    }

    @Nested
    @DisplayName("ResourcePoolPlayerStat")
    class ResourcePoolPlayerStatTests {

        @DisplayName("isResourcePool returns true")
        @Test
        void isResourcePool_returnsTrue() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "HP", "❤", 200, 5);
            assertTrue(stat.isResourcePool());
        }

        @DisplayName("getBaseValue returns constructor value")
        @Test
        void getBaseValue_returnsDefault() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "HP", "❤", 200, 5);
            assertEquals(200.0, stat.getBaseValue());
        }

        @DisplayName("getRegenPerSecond returns constructor value")
        @Test
        void getRegenPerSecond_returnsDefault() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "HP", "❤", 200, 5);
            assertEquals(5.0, stat.getRegenPerSecond());
        }

        @DisplayName("getRegenPerSecond can be zero")
        @Test
        void getRegenPerSecond_canBeZero() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "HP", "❤", 200, 0);
            assertEquals(0.0, stat.getRegenPerSecond());
        }

        @DisplayName("getReloadableBaseValue returns empty")
        @Test
        void getReloadableBaseValue_returnsEmpty() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "HP", "❤", 200, 5);
            assertTrue(stat.getReloadableBaseValue().isEmpty());
        }

        @DisplayName("getReloadableRegenPerSecond returns empty")
        @Test
        void getReloadableRegenPerSecond_returnsEmpty() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "HP", "❤", 200, 5);
            assertTrue(stat.getReloadableRegenPerSecond().isEmpty());
        }

        @DisplayName("getReloadableContent returns empty set")
        @Test
        void getReloadableContent_returnsEmptySet() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "HP", "❤", 200, 5);
            assertTrue(stat.getReloadableContent().isEmpty());
        }

        @DisplayName("getBaseValue returns zero when constructed with zero")
        @Test
        void getBaseValue_returnsZero_whenZeroDefault() {
            var stat = new ResourcePoolPlayerStat(TEST_KEY, "HP", "❤", 0, 0);
            assertEquals(0.0, stat.getBaseValue());
        }
    }

    @Nested
    @DisplayName("ConfigurableResourcePoolPlayerStat")
    class ConfigurableResourcePoolPlayerStatTests {

        @SuppressWarnings("unchecked")
        private ReloadableContent<Double> mockReloadable(double value) {
            ReloadableContent<Double> reloadable = mock(ReloadableContent.class);
            when(reloadable.getContent()).thenReturn(value);
            return reloadable;
        }

        @DisplayName("isResourcePool returns true")
        @Test
        void isResourcePool_returnsTrue() {
            var stat = new ConfigurableResourcePoolPlayerStat(
                    TEST_KEY, "Mana", "✦", 100, 2,
                    mockReloadable(100.0), mockReloadable(2.0));
            assertTrue(stat.isResourcePool());
        }

        @DisplayName("getBaseValue delegates to reloadable content")
        @Test
        void getBaseValue_delegatesToReloadable() {
            var stat = new ConfigurableResourcePoolPlayerStat(
                    TEST_KEY, "Mana", "✦", 100, 2,
                    mockReloadable(150.0), mockReloadable(2.0));
            assertEquals(150.0, stat.getBaseValue());
        }

        @DisplayName("getRegenPerSecond delegates to reloadable content")
        @Test
        void getRegenPerSecond_delegatesToReloadable() {
            var stat = new ConfigurableResourcePoolPlayerStat(
                    TEST_KEY, "Mana", "✦", 100, 2,
                    mockReloadable(100.0), mockReloadable(3.5));
            assertEquals(3.5, stat.getRegenPerSecond());
        }

        @DisplayName("getReloadableBaseValue returns present optional")
        @Test
        void getReloadableBaseValue_returnsPresent() {
            var reloadableBase = mockReloadable(100.0);
            var stat = new ConfigurableResourcePoolPlayerStat(
                    TEST_KEY, "Mana", "✦", 100, 2,
                    reloadableBase, mockReloadable(2.0));
            assertTrue(stat.getReloadableBaseValue().isPresent());
            assertEquals(reloadableBase, stat.getReloadableBaseValue().get());
        }

        @DisplayName("getReloadableRegenPerSecond returns present optional")
        @Test
        void getReloadableRegenPerSecond_returnsPresent() {
            var reloadableRegen = mockReloadable(2.0);
            var stat = new ConfigurableResourcePoolPlayerStat(
                    TEST_KEY, "Mana", "✦", 100, 2,
                    mockReloadable(100.0), reloadableRegen);
            assertTrue(stat.getReloadableRegenPerSecond().isPresent());
            assertEquals(reloadableRegen, stat.getReloadableRegenPerSecond().get());
        }

        @DisplayName("getReloadableContent returns both reloadables")
        @Test
        void getReloadableContent_returnsBoth() {
            var reloadableBase = mockReloadable(100.0);
            var reloadableRegen = mockReloadable(2.0);
            var stat = new ConfigurableResourcePoolPlayerStat(
                    TEST_KEY, "Mana", "✦", 100, 2,
                    reloadableBase, reloadableRegen);
            assertEquals(2, stat.getReloadableContent().size());
            assertTrue(stat.getReloadableContent().contains(reloadableBase));
            assertTrue(stat.getReloadableContent().contains(reloadableRegen));
        }

        @DisplayName("getBaseValue returns zero when reloadable returns zero")
        @Test
        void getBaseValue_returnsZero_whenReloadableReturnsZero() {
            var stat = new ConfigurableResourcePoolPlayerStat(
                    TEST_KEY, "Mana", "✦", 100, 2,
                    mockReloadable(0.0), mockReloadable(2.0));
            assertEquals(0.0, stat.getBaseValue());
        }

        @DisplayName("getRegenPerSecond returns zero when reloadable returns zero")
        @Test
        void getRegenPerSecond_returnsZero_whenReloadableReturnsZero() {
            var stat = new ConfigurableResourcePoolPlayerStat(
                    TEST_KEY, "Mana", "✦", 100, 2,
                    mockReloadable(100.0), mockReloadable(0.0));
            assertEquals(0.0, stat.getRegenPerSecond());
        }

        @DisplayName("getBaseValue reflects updated reloadable content")
        @Test
        void getBaseValue_reflectsUpdatedReloadable() {
            @SuppressWarnings("unchecked")
            ReloadableContent<Double> reloadableBase = mock(ReloadableContent.class);
            when(reloadableBase.getContent()).thenReturn(100.0).thenReturn(200.0);

            var stat = new ConfigurableResourcePoolPlayerStat(
                    TEST_KEY, "Mana", "✦", 50, 2,
                    reloadableBase, mockReloadable(2.0));

            assertEquals(100.0, stat.getBaseValue());
            assertEquals(200.0, stat.getBaseValue());
        }
    }
}
