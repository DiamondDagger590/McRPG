package us.eunoians.mcrpg.external.papi.placeholder.statistic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link StatisticPlaceholder#formatValue(Object)}.
 */
class StatisticPlaceholderTest {

    @Test
    void formatValue_null_returnsZero() {
        assertEquals("0", StatisticPlaceholder.formatValue(null));
    }

    @Test
    void formatValue_integer_formatsWithCommas() {
        assertEquals("1,542", StatisticPlaceholder.formatValue(1542));
    }

    @Test
    void formatValue_integerZero_returnsZero() {
        assertEquals("0", StatisticPlaceholder.formatValue(0));
    }

    @Test
    void formatValue_long_formatsWithCommas() {
        assertEquals("98,500", StatisticPlaceholder.formatValue(98500L));
    }

    @Test
    void formatValue_double_formatsTwoDecimalPlaces() {
        assertEquals("12,543.50", StatisticPlaceholder.formatValue(12543.5));
    }

    @Test
    void formatValue_doubleZero_formatsAsZero() {
        assertEquals("0.00", StatisticPlaceholder.formatValue(0.0));
    }

    @Test
    void formatValue_string_returnsToString() {
        assertEquals("hello", StatisticPlaceholder.formatValue("hello"));
    }
}
