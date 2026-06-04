package us.eunoians.mcrpg.localization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McRPGDisplayDecimalFormatterTest {

    private final McRPGDisplayDecimalFormatter formatter = new McRPGDisplayDecimalFormatter();

    @Nested
    @DisplayName("Input validation")
    class InputValidation {

        @DisplayName("negative min fraction digits throws IllegalArgumentException")
        @Test
        void formatDisplayDecimal_negativeMinFractionDigits_throws() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> formatter.formatDisplayDecimal(Locale.ENGLISH, 1.0, -1, 2));
        }

        @DisplayName("negative max fraction digits throws IllegalArgumentException")
        @Test
        void formatDisplayDecimal_negativeMaxFractionDigits_throws() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> formatter.formatDisplayDecimal(Locale.ENGLISH, 1.0, 0, -1));
        }

        @DisplayName("min exceeds max throws IllegalArgumentException")
        @Test
        void formatDisplayDecimal_minExceedsMax_throws() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> formatter.formatDisplayDecimal(Locale.ENGLISH, 1.0, 3, 2));
        }

        @DisplayName("min equals max is valid")
        @Test
        void formatDisplayDecimal_minEqualsMax_doesNotThrow() {
            assertDoesNotThrow(() -> formatter.formatDisplayDecimal(Locale.US, 1.0, 2, 2));
        }

        @DisplayName("zero min and zero max is valid")
        @Test
        void formatDisplayDecimal_zeroMinZeroMax_doesNotThrow() {
            String result = formatter.formatDisplayDecimal(Locale.US, 3.14, 0, 0);
            assertEquals("3", result);
        }
    }

    @Nested
    @DisplayName("US locale formatting")
    class UsLocale {

        @DisplayName("default bounds format Pi with decimal point")
        @Test
        void formatDisplayDecimal_pi_usesDecimalPoint() {
            String result = formatter.formatDisplayDecimal(Locale.US, 3.14159);

            assertTrue(result.contains("."));
        }

        @DisplayName("default bounds format 1.5 as '1.5'")
        @Test
        void formatDisplayDecimal_onePointFive_formats() {
            String result = formatter.formatDisplayDecimal(Locale.US, 1.5);

            assertEquals("1.5", result);
        }

        @DisplayName("default bounds format 0.0 as '0.0'")
        @Test
        void formatDisplayDecimal_zero_showsMinDigit() {
            String result = formatter.formatDisplayDecimal(Locale.US, 0.0);

            assertEquals("0.0", result);
        }

        @DisplayName("default bounds format integer value shows at least 1 decimal place")
        @Test
        void formatDisplayDecimal_wholeNumber_showsMinDecimal() {
            String result = formatter.formatDisplayDecimal(Locale.US, 42.0);

            assertEquals("42.0", result);
        }

        @DisplayName("default bounds truncate beyond 2 decimal places")
        @Test
        void formatDisplayDecimal_manyDecimals_truncatesAtTwo() {
            String result = formatter.formatDisplayDecimal(Locale.US, 3.14159);

            assertEquals("3.14", result);
        }

        @DisplayName("custom bounds with 4 max fraction digits")
        @Test
        void formatDisplayDecimal_customBounds_fourDecimals() {
            String result = formatter.formatDisplayDecimal(Locale.US, 3.14159, 1, 4);

            assertEquals("3.1416", result);
        }

        @DisplayName("large number uses grouping separator")
        @Test
        void formatDisplayDecimal_largeNumber_usesGrouping() {
            String result = formatter.formatDisplayDecimal(Locale.US, 1234567.89);

            assertTrue(result.contains(","));
        }

        @DisplayName("negative value formats correctly")
        @Test
        void formatDisplayDecimal_negativeValue_formatsCorrectly() {
            String result = formatter.formatDisplayDecimal(Locale.US, -42.5);

            assertEquals("-42.5", result);
        }
    }

    @Nested
    @DisplayName("German locale formatting")
    class GermanLocale {

        @DisplayName("German locale uses comma as decimal separator")
        @Test
        void formatDisplayDecimal_germanLocale_usesComma() {
            String result = formatter.formatDisplayDecimal(Locale.GERMANY, 3.14);

            assertTrue(result.contains(","), "Expected comma as decimal separator in: " + result);
            assertNotNull(result);
        }

        @DisplayName("German locale uses period as grouping separator")
        @Test
        void formatDisplayDecimal_germanLocale_usesGroupingDot() {
            String result = formatter.formatDisplayDecimal(Locale.GERMANY, 1234567.89);

            assertTrue(result.contains("."), "Expected period as grouping separator in: " + result);
        }
    }

    @Nested
    @DisplayName("French locale formatting")
    class FrenchLocale {

        @DisplayName("French locale uses comma as decimal separator")
        @Test
        void formatDisplayDecimal_frenchLocale_usesComma() {
            String result = formatter.formatDisplayDecimal(Locale.FRANCE, 3.14);

            assertTrue(result.contains(","), "Expected comma as decimal separator in: " + result);
        }
    }

    @Nested
    @DisplayName("Float overloads")
    class FloatOverloads {

        @DisplayName("float overload with default bounds formats correctly")
        @Test
        void formatDisplayDecimal_float_defaultBounds() {
            String result = formatter.formatDisplayDecimal(Locale.US, 2.5f);

            assertEquals("2.5", result);
        }

        @DisplayName("float overload with custom bounds formats correctly")
        @Test
        void formatDisplayDecimal_float_customBounds() {
            String result = formatter.formatDisplayDecimal(Locale.US, 2.567f, 1, 2);

            assertEquals("2.57", result);
        }

        @DisplayName("float overload validation throws for negative min")
        @Test
        void formatDisplayDecimal_float_negativeMin_throws() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> formatter.formatDisplayDecimal(Locale.US, 1.0f, -1, 2));
        }
    }

    @Nested
    @DisplayName("Cache behavior")
    class CacheBehavior {

        @DisplayName("same locale reuses cached NumberFormat instance")
        @Test
        void formatDisplayDecimal_sameLocale_reusesCache() {
            String first = formatter.formatDisplayDecimal(Locale.US, 1.23);
            String second = formatter.formatDisplayDecimal(Locale.US, 1.23);

            assertEquals(first, second);
        }

        @DisplayName("different locales produce different formatting")
        @Test
        void formatDisplayDecimal_differentLocales_differentFormatting() {
            String usResult = formatter.formatDisplayDecimal(Locale.US, 1234.56);
            String germanResult = formatter.formatDisplayDecimal(Locale.GERMANY, 1234.56);

            // US uses period for decimal, Germany uses comma
            assertTrue(usResult.contains("."));
            assertTrue(germanResult.contains(","));
        }

        @DisplayName("different digit bounds on same locale do not corrupt results")
        @Test
        void formatDisplayDecimal_varyingDigitBounds_correctResults() {
            String result0 = formatter.formatDisplayDecimal(Locale.US, 3.14159, 0, 0);
            String result2 = formatter.formatDisplayDecimal(Locale.US, 3.14159, 2, 2);
            String result4 = formatter.formatDisplayDecimal(Locale.US, 3.14159, 4, 4);

            assertEquals("3", result0);
            assertEquals("3.14", result2);
            assertEquals("3.1416", result4);
        }
    }

    @Nested
    @DisplayName("Concurrency")
    class Concurrency {

        @DisplayName("concurrent formatting from multiple threads does not corrupt results")
        @Test
        void formatDisplayDecimal_concurrentAccess_noCorruption() throws InterruptedException {
            int threadCount = 8;
            int iterationsPerThread = 100;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicBoolean failure = new AtomicBoolean(false);

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < iterationsPerThread; i++) {
                            Locale locale = (threadId % 2 == 0) ? Locale.US : Locale.GERMANY;
                            double value = 1234.56;
                            String result = formatter.formatDisplayDecimal(locale, value);

                            if (locale == Locale.US && !result.contains(".")) {
                                failure.set(true);
                            }
                            if (locale == Locale.GERMANY && !result.contains(",")) {
                                failure.set(true);
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();
            executor.shutdown();

            assertFalse(failure.get(), "Concurrent formatting produced corrupted results");
        }
    }

    @Nested
    @DisplayName("Player/Audience overloads without manager")
    class NoManagerOverloads {

        @DisplayName("no-arg constructor formatter throws NullPointerException on McRPGPlayer overload")
        @Test
        void formatDisplayDecimal_playerOverload_throwsWithoutManager() {
            assertThrows(
                    NullPointerException.class,
                    () -> formatter.formatDisplayDecimal((us.eunoians.mcrpg.entity.player.McRPGPlayer) null, 1.0));
        }
    }
}
