package us.eunoians.mcrpg.quest.board.template.variable;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolvedPoolTest {

    @DisplayName("Constructor stores averageDifficulty correctly")
    @Test
    void averageDifficulty_returnsConstructorValue() {
        ResolvedPool pool = new ResolvedPool(List.of("IRON_ORE"), 3.5);
        assertEquals(3.5, pool.averageDifficulty());
    }

    @DisplayName("mergedValues() returns defensive copy")
    @Test
    void mergedValues_returnsDefensiveCopy() {
        List<String> original = new ArrayList<>(List.of("IRON_ORE"));

        ResolvedPool pool = new ResolvedPool(original, 1.0);
        original.add("DIAMOND_ORE");

        assertEquals(1, pool.mergedValues().size());
        assertEquals("IRON_ORE", pool.mergedValues().get(0));
    }

    @DisplayName("mergedValues() is unmodifiable")
    @Test
    void mergedValues_isUnmodifiable() {
        ResolvedPool pool = new ResolvedPool(List.of("IRON_ORE"), 1.0);
        assertThrows(UnsupportedOperationException.class, () -> pool.mergedValues().add("DIAMOND_ORE"));
    }

    @DisplayName("Empty merged values list is valid")
    @Test
    void mergedValues_emptyList_isValid() {
        ResolvedPool pool = new ResolvedPool(List.of(), 0.0);
        assertTrue(pool.mergedValues().isEmpty());
    }

    @DisplayName("mergedValues preserves order")
    @Test
    void mergedValues_preservesOrder() {
        List<String> values = List.of("IRON_ORE", "COPPER_ORE", "DIAMOND_ORE");
        ResolvedPool pool = new ResolvedPool(values, 2.0);
        assertEquals(values, pool.mergedValues());
    }

    @DisplayName("averageDifficulty can be zero")
    @Test
    void averageDifficulty_canBeZero() {
        ResolvedPool pool = new ResolvedPool(List.of(), 0.0);
        assertEquals(0.0, pool.averageDifficulty());
    }

    @DisplayName("averageDifficulty can be negative")
    @Test
    void averageDifficulty_canBeNegative() {
        ResolvedPool pool = new ResolvedPool(List.of(), -1.5);
        assertEquals(-1.5, pool.averageDifficulty());
    }
}
