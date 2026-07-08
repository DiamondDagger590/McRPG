package us.eunoians.mcrpg.quest.chain.availability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPG;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class AvailabilityWindowTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Nested
    @DisplayName("AvailabilityConfig")
    class AvailabilityConfigTests {

        @Test
        @DisplayName("isCurrentlyAvailable returns true when one window is open")
        void isCurrentlyAvailable_returnsTrue_whenOneWindowOpen() {
            AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("spring",
                    new WindowBoundary.Recurring(MonthDay.of(3, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(3, 31), LocalTime.of(23, 59, 59)));
            AvailabilityConfig config = new AvailabilityConfig(
                    Map.of("spring", window), UTC, WindowClosePolicy.ALLOW_FINISH, null);

            ZonedDateTime march15 = ZonedDateTime.of(2025, 3, 15, 12, 0, 0, 0, UTC);
            assertTrue(config.isCurrentlyAvailable(march15));
        }

        @Test
        @DisplayName("isCurrentlyAvailable returns false when all windows closed")
        void isCurrentlyAvailable_returnsFalse_whenAllWindowsClosed() {
            AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("spring",
                    new WindowBoundary.Recurring(MonthDay.of(3, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(3, 31), LocalTime.of(23, 59, 59)));
            AvailabilityConfig config = new AvailabilityConfig(
                    Map.of("spring", window), UTC, WindowClosePolicy.ALLOW_FINISH, null);

            ZonedDateTime june15 = ZonedDateTime.of(2025, 6, 15, 12, 0, 0, 0, UTC);
            assertFalse(config.isCurrentlyAvailable(june15));
        }

        @Test
        @DisplayName("isCurrentlyAvailable returns true when one of multiple windows is open")
        void isCurrentlyAvailable_returnsTrue_whenOneOfMultipleWindowsOpen() {
            AvailabilityWindowDefinition spring = new AvailabilityWindowDefinition("spring",
                    new WindowBoundary.Recurring(MonthDay.of(3, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(3, 31), LocalTime.of(23, 59, 59)));
            AvailabilityWindowDefinition summer = new AvailabilityWindowDefinition("summer",
                    new WindowBoundary.Recurring(MonthDay.of(6, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(8, 31), LocalTime.of(23, 59, 59)));
            AvailabilityConfig config = new AvailabilityConfig(
                    Map.of("spring", spring, "summer", summer), UTC, WindowClosePolicy.ALLOW_FINISH, null);

            ZonedDateTime july4 = ZonedDateTime.of(2025, 7, 4, 12, 0, 0, 0, UTC);
            assertTrue(config.isCurrentlyAvailable(july4));
        }

        @Test
        @DisplayName("isCurrentlyAvailable returns false when windows map is empty")
        void isCurrentlyAvailable_returnsFalse_whenEmptyWindows() {
            AvailabilityConfig config = new AvailabilityConfig(
                    Map.of(), UTC, WindowClosePolicy.ALLOW_FINISH, null);

            ZonedDateTime now = ZonedDateTime.of(2025, 6, 15, 12, 0, 0, 0, UTC);
            assertFalse(config.isCurrentlyAvailable(now));
        }
    }

    @Nested
    @DisplayName("AvailabilityWindowDefinition")
    class WindowDefinitionTests {

        @Test
        @DisplayName("isActive returns true inside normal range")
        void isActive_returnsTrue_insideNormalRange() {
            AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("test",
                    new WindowBoundary.Recurring(MonthDay.of(3, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(3, 31), LocalTime.of(23, 59, 59)));

            ZonedDateTime march15 = ZonedDateTime.of(2025, 3, 15, 12, 0, 0, 0, UTC);
            assertTrue(window.isActive(march15));
        }

        @Test
        @DisplayName("isActive returns false outside normal range")
        void isActive_returnsFalse_outsideNormalRange() {
            AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("test",
                    new WindowBoundary.Recurring(MonthDay.of(3, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(3, 31), LocalTime.of(23, 59, 59)));

            ZonedDateTime april5 = ZonedDateTime.of(2025, 4, 5, 12, 0, 0, 0, UTC);
            assertFalse(window.isActive(april5));
        }

        @Test
        @DisplayName("isActive returns true inside year-wrapping range in December")
        void isActive_returnsTrue_yearWrappingDecember() {
            AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("holiday",
                    new WindowBoundary.Recurring(MonthDay.of(12, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(1, 31), LocalTime.of(23, 59, 59)));

            ZonedDateTime dec25 = ZonedDateTime.of(2025, 12, 25, 12, 0, 0, 0, UTC);
            assertTrue(window.isActive(dec25));
        }

        @Test
        @DisplayName("isActive returns true inside year-wrapping range in January")
        void isActive_returnsTrue_yearWrappingJanuary() {
            AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("holiday",
                    new WindowBoundary.Recurring(MonthDay.of(12, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(1, 31), LocalTime.of(23, 59, 59)));

            ZonedDateTime jan15 = ZonedDateTime.of(2025, 1, 15, 12, 0, 0, 0, UTC);
            assertTrue(window.isActive(jan15));
        }

        @Test
        @DisplayName("isActive returns false outside year-wrapping range")
        void isActive_returnsFalse_outsideYearWrappingRange() {
            AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("holiday",
                    new WindowBoundary.Recurring(MonthDay.of(12, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(1, 31), LocalTime.of(23, 59, 59)));

            ZonedDateTime feb15 = ZonedDateTime.of(2025, 2, 15, 12, 0, 0, 0, UTC);
            assertFalse(window.isActive(feb15));
        }

        @Test
        @DisplayName("isActive returns true at exact start boundary")
        void isActive_returnsTrue_atExactStartBoundary() {
            AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("test",
                    new WindowBoundary.Recurring(MonthDay.of(3, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(3, 31), LocalTime.of(23, 59, 59)));

            ZonedDateTime march1Midnight = ZonedDateTime.of(2025, 3, 1, 0, 0, 0, 0, UTC);
            assertTrue(window.isActive(march1Midnight));
        }

        @Test
        @DisplayName("isActive returns true at exact end boundary")
        void isActive_returnsTrue_atExactEndBoundary() {
            AvailabilityWindowDefinition window = new AvailabilityWindowDefinition("test",
                    new WindowBoundary.Recurring(MonthDay.of(3, 1), LocalTime.MIDNIGHT),
                    new WindowBoundary.Recurring(MonthDay.of(3, 31), LocalTime.of(23, 59, 59)));

            ZonedDateTime march31End = ZonedDateTime.of(2025, 3, 31, 23, 59, 59, 0, UTC);
            assertTrue(window.isActive(march31End));
        }
    }

    @Nested
    @DisplayName("WindowBoundary.Fixed")
    class FixedBoundaryTests {

        @Test
        @DisplayName("toZonedDateTime returns LocalDateTime at reference zone")
        void toZonedDateTime_returnsLocalDateTimeAtReferenceZone() {
            LocalDateTime dateTime = LocalDateTime.of(2025, 6, 15, 14, 30);
            WindowBoundary.Fixed fixed = new WindowBoundary.Fixed(dateTime);

            ZonedDateTime ref = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime result = fixed.toZonedDateTime(ref, 0);

            assertEquals(2025, result.getYear());
            assertEquals(6, result.getMonthValue());
            assertEquals(15, result.getDayOfMonth());
            assertEquals(14, result.getHour());
            assertEquals(30, result.getMinute());
            assertEquals(UTC, result.getZone());
        }

        @Test
        @DisplayName("toZonedDateTime ignores yearOffset")
        void toZonedDateTime_ignoresYearOffset() {
            LocalDateTime dateTime = LocalDateTime.of(2025, 6, 15, 14, 30);
            WindowBoundary.Fixed fixed = new WindowBoundary.Fixed(dateTime);

            ZonedDateTime ref = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime withOffset = fixed.toZonedDateTime(ref, 2);
            ZonedDateTime withoutOffset = fixed.toZonedDateTime(ref, 0);

            assertEquals(withoutOffset, withOffset);
        }

        @Test
        @DisplayName("toZonedDateTime uses reference timezone")
        void toZonedDateTime_usesReferenceTimezone() {
            LocalDateTime dateTime = LocalDateTime.of(2025, 6, 15, 14, 30);
            WindowBoundary.Fixed fixed = new WindowBoundary.Fixed(dateTime);

            ZoneId tokyo = ZoneId.of("Asia/Tokyo");
            ZonedDateTime ref = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, tokyo);
            ZonedDateTime result = fixed.toZonedDateTime(ref, 0);

            assertEquals(tokyo, result.getZone());
        }
    }

    @Nested
    @DisplayName("WindowBoundary.Recurring")
    class RecurringBoundaryTests {

        @Test
        @DisplayName("toZonedDateTime resolves to reference year")
        void toZonedDateTime_resolvesToReferenceYear() {
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(
                    MonthDay.of(6, 15), LocalTime.of(14, 30));

            ZonedDateTime ref2025 = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime result = recurring.toZonedDateTime(ref2025, 0);

            assertEquals(2025, result.getYear());
            assertEquals(6, result.getMonthValue());
            assertEquals(15, result.getDayOfMonth());
            assertEquals(14, result.getHour());
            assertEquals(30, result.getMinute());
        }

        @Test
        @DisplayName("toZonedDateTime applies positive yearOffset")
        void toZonedDateTime_appliesPositiveYearOffset() {
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(
                    MonthDay.of(6, 15), LocalTime.of(14, 30));

            ZonedDateTime ref2025 = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime result = recurring.toZonedDateTime(ref2025, 1);

            assertEquals(2026, result.getYear());
        }

        @Test
        @DisplayName("toZonedDateTime applies negative yearOffset")
        void toZonedDateTime_appliesNegativeYearOffset() {
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(
                    MonthDay.of(6, 15), LocalTime.of(14, 30));

            ZonedDateTime ref2025 = ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, UTC);
            ZonedDateTime result = recurring.toZonedDateTime(ref2025, -1);

            assertEquals(2024, result.getYear());
        }
    }

    @Nested
    @DisplayName("WindowClosePolicy")
    class WindowClosePolicyTests {

        @Test
        @DisplayName("fromString parses uppercase enum name")
        void fromString_parsesUppercase() {
            Optional<WindowClosePolicy> result = WindowClosePolicy.fromString("EXPIRE_ACTIVE");
            assertTrue(result.isPresent());
            assertEquals(WindowClosePolicy.EXPIRE_ACTIVE, result.get());
        }

        @Test
        @DisplayName("fromString parses kebab-case")
        void fromString_parsesKebabCase() {
            Optional<WindowClosePolicy> result = WindowClosePolicy.fromString("expire-active");
            assertTrue(result.isPresent());
            assertEquals(WindowClosePolicy.EXPIRE_ACTIVE, result.get());
        }

        @Test
        @DisplayName("fromString is case-insensitive")
        void fromString_isCaseInsensitive() {
            Optional<WindowClosePolicy> result = WindowClosePolicy.fromString("Allow-Finish");
            assertTrue(result.isPresent());
            assertEquals(WindowClosePolicy.ALLOW_FINISH, result.get());
        }

        @Test
        @DisplayName("fromString returns empty for unknown value")
        void fromString_returnsEmpty_whenUnknown() {
            McRPG mockPlugin = mock(McRPG.class);
            when(mockPlugin.getLogger()).thenReturn(Logger.getLogger("test"));

            try (MockedStatic<McRPG> staticMcRPG = mockStatic(McRPG.class)) {
                staticMcRPG.when(McRPG::getInstance).thenReturn(mockPlugin);

                Optional<WindowClosePolicy> result = WindowClosePolicy.fromString("nonexistent-policy");
                assertTrue(result.isEmpty());
            }
        }
    }
}
