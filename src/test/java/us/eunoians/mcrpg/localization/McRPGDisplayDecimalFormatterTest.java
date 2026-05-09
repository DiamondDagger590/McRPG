package us.eunoians.mcrpg.localization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link McRPGDisplayDecimalFormatter}.
 */
class McRPGDisplayDecimalFormatterTest {

    private final McRPGDisplayDecimalFormatter formatter = new McRPGDisplayDecimalFormatter();

    @DisplayName("Given negative min fraction digits, when formatDisplayDecimal is called, then IllegalArgumentException")
    @Test
    void formatDisplayDecimal_negativeMinFractionDigits_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> formatter.formatDisplayDecimal(Locale.ENGLISH, 1.0, -1, 2));
    }

    @DisplayName("Given min fraction digits greater than max, when formatDisplayDecimal is called, then IllegalArgumentException")
    @Test
    void formatDisplayDecimal_minExceedsMax_throws() {
        assertThrows(
                IllegalArgumentException.class,
                () -> formatter.formatDisplayDecimal(Locale.ENGLISH, 1.0, 3, 2));
    }

    @DisplayName("Given valid bounds, when formatDisplayDecimal is called, then no exception")
    @Test
    void formatDisplayDecimal_validBounds_formats() {
        assertDoesNotThrow(() -> formatter.formatDisplayDecimal(Locale.US, Math.PI, 1, 4));
    }

    @DisplayName("Given US locale default bounds, when formatting a decimal, then output uses a decimal point")
    @Test
    void formatDisplayDecimal_usLocale_defaultBounds_usesDecimalPoint() {
        String result = formatter.formatDisplayDecimal(Locale.US, 3.14159);
        assertTrue(result.contains("."), () -> "expected US-style decimal separator in: " + result);
    }
}
