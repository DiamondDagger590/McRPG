package us.eunoians.mcrpg.builder.item.ability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link AbilityItemPlaceholderKeys} to verify each enum constant
 * returns the expected placeholder key string.
 */
class AbilityItemPlaceholderKeysTest {

    @Test
    @DisplayName("Given MANA_COST, when getKey() is called, then it returns \"mana-cost\"")
    void manaCost_hasExpectedKey() {
        assertEquals("mana-cost", AbilityItemPlaceholderKeys.MANA_COST.getKey());
    }
}
