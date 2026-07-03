package us.eunoians.mcrpg.quest.board.template.variable;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PoolTest extends McRPGBaseTest {

    private static final NamespacedKey COMMON_KEY = new NamespacedKey("mcrpg", "common");
    private static final NamespacedKey RARE_KEY = new NamespacedKey("mcrpg", "rare");

    @DisplayName("Constructor stores name correctly")
    @Test
    void name_returnsConstructorValue() {
        Pool pool = new Pool("ores", 1.0, Map.of(), List.of());
        assertEquals("ores", pool.name());
    }

    @DisplayName("Constructor stores difficulty correctly")
    @Test
    void difficulty_returnsConstructorValue() {
        Pool pool = new Pool("ores", 2.5, Map.of(), List.of());
        assertEquals(2.5, pool.difficulty());
    }

    @DisplayName("weights() returns defensive copy")
    @Test
    void weights_returnsDefensiveCopy() {
        Map<NamespacedKey, Integer> original = new HashMap<>();
        original.put(COMMON_KEY, 10);

        Pool pool = new Pool("ores", 1.0, original, List.of());
        original.put(RARE_KEY, 5);

        assertEquals(1, pool.weights().size());
        assertEquals(10, pool.weights().get(COMMON_KEY));
    }

    @DisplayName("weights() is unmodifiable")
    @Test
    void weights_isUnmodifiable() {
        Pool pool = new Pool("ores", 1.0, Map.of(COMMON_KEY, 10), List.of());
        assertThrows(UnsupportedOperationException.class, () -> pool.weights().put(RARE_KEY, 5));
    }

    @DisplayName("values() returns defensive copy")
    @Test
    void values_returnsDefensiveCopy() {
        List<String> original = new ArrayList<>(List.of("IRON_ORE"));

        Pool pool = new Pool("ores", 1.0, Map.of(), original);
        original.add("DIAMOND_ORE");

        assertEquals(1, pool.values().size());
        assertEquals("IRON_ORE", pool.values().get(0));
    }

    @DisplayName("values() is unmodifiable")
    @Test
    void values_isUnmodifiable() {
        Pool pool = new Pool("ores", 1.0, Map.of(), List.of("IRON_ORE"));
        assertThrows(UnsupportedOperationException.class, () -> pool.values().add("DIAMOND_ORE"));
    }

    @DisplayName("getWeightForRarity returns weight for known key")
    @Test
    void getWeightForRarity_returnsWeight_whenKeyPresent() {
        Pool pool = new Pool("ores", 1.0, Map.of(COMMON_KEY, 10, RARE_KEY, 3), List.of());
        assertEquals(10, pool.getWeightForRarity(COMMON_KEY));
        assertEquals(3, pool.getWeightForRarity(RARE_KEY));
    }

    @DisplayName("getWeightForRarity returns 0 for unknown key")
    @Test
    void getWeightForRarity_returnsZero_whenKeyAbsent() {
        Pool pool = new Pool("ores", 1.0, Map.of(COMMON_KEY, 10), List.of());
        NamespacedKey unknownKey = new NamespacedKey("mcrpg", "legendary");
        assertEquals(0, pool.getWeightForRarity(unknownKey));
    }

    @DisplayName("Pool preserves multiple values in order")
    @Test
    void values_preservesOrder() {
        List<String> values = List.of("IRON_ORE", "COPPER_ORE", "DIAMOND_ORE");
        Pool pool = new Pool("ores", 1.0, Map.of(), values);
        assertEquals(values, pool.values());
    }
}
