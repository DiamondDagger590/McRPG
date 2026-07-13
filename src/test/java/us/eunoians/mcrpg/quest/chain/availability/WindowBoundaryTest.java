package us.eunoians.mcrpg.quest.chain.availability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WindowBoundaryTest {

    private static final ZoneId ZONE = ZoneId.of("UTC");

    @Nested
    @DisplayName("Fixed")
    class FixedTests {

        @Test
        @DisplayName("resolves to exact date and time in reference zone")
        void resolvesToExactDateTime() {
            LocalDateTime dt = LocalDateTime.of(2025, 6, 15, 14, 30);
            WindowBoundary.Fixed fixed = new WindowBoundary.Fixed(dt);
            ZonedDateTime reference = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZONE);

            ZonedDateTime result = fixed.toZonedDateTime(reference, 0);

            assertEquals(2025, result.getYear());
            assertEquals(6, result.getMonthValue());
            assertEquals(15, result.getDayOfMonth());
            assertEquals(14, result.getHour());
            assertEquals(30, result.getMinute());
            assertEquals(ZONE, result.getZone());
        }

        @Test
        @DisplayName("ignores year offset")
        void ignoresYearOffset() {
            LocalDateTime dt = LocalDateTime.of(2025, 3, 1, 0, 0);
            WindowBoundary.Fixed fixed = new WindowBoundary.Fixed(dt);
            ZonedDateTime reference = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZONE);

            ZonedDateTime withOffset = fixed.toZonedDateTime(reference, 5);
            ZonedDateTime withoutOffset = fixed.toZonedDateTime(reference, 0);

            assertEquals(withoutOffset.getYear(), withOffset.getYear());
        }

        @Test
        @DisplayName("uses reference zone not fixed zone")
        void usesReferenceZone() {
            LocalDateTime dt = LocalDateTime.of(2025, 12, 25, 8, 0);
            WindowBoundary.Fixed fixed = new WindowBoundary.Fixed(dt);
            ZoneId tokyo = ZoneId.of("Asia/Tokyo");
            ZonedDateTime reference = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, tokyo);

            ZonedDateTime result = fixed.toZonedDateTime(reference, 0);

            assertEquals(tokyo, result.getZone());
        }

        @Test
        @DisplayName("dateTime accessor returns constructor value")
        void dateTimeAccessor() {
            LocalDateTime dt = LocalDateTime.of(2025, 7, 4, 12, 0);
            WindowBoundary.Fixed fixed = new WindowBoundary.Fixed(dt);

            assertEquals(dt, fixed.dateTime());
        }
    }

    @Nested
    @DisplayName("Recurring")
    class RecurringTests {

        @Test
        @DisplayName("resolves to reference year with zero offset")
        void resolvesToReferenceYearWithZeroOffset() {
            MonthDay md = MonthDay.of(12, 25);
            LocalTime time = LocalTime.of(9, 0);
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(md, time);
            ZonedDateTime reference = ZonedDateTime.of(2024, 6, 1, 0, 0, 0, 0, ZONE);

            ZonedDateTime result = recurring.toZonedDateTime(reference, 0);

            assertEquals(2024, result.getYear());
            assertEquals(12, result.getMonthValue());
            assertEquals(25, result.getDayOfMonth());
            assertEquals(9, result.getHour());
            assertEquals(0, result.getMinute());
        }

        @Test
        @DisplayName("applies positive year offset")
        void appliesPositiveYearOffset() {
            MonthDay md = MonthDay.of(1, 3);
            LocalTime time = LocalTime.of(0, 0);
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(md, time);
            ZonedDateTime reference = ZonedDateTime.of(2024, 12, 1, 0, 0, 0, 0, ZONE);

            ZonedDateTime result = recurring.toZonedDateTime(reference, 1);

            assertEquals(2025, result.getYear());
            assertEquals(1, result.getMonthValue());
            assertEquals(3, result.getDayOfMonth());
        }

        @Test
        @DisplayName("applies negative year offset")
        void appliesNegativeYearOffset() {
            MonthDay md = MonthDay.of(6, 15);
            LocalTime time = LocalTime.of(18, 30);
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(md, time);
            ZonedDateTime reference = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZONE);

            ZonedDateTime result = recurring.toZonedDateTime(reference, -1);

            assertEquals(2023, result.getYear());
        }

        @Test
        @DisplayName("uses reference zone")
        void usesReferenceZone() {
            MonthDay md = MonthDay.of(3, 14);
            LocalTime time = LocalTime.of(15, 9, 26);
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(md, time);
            ZoneId berlin = ZoneId.of("Europe/Berlin");
            ZonedDateTime reference = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, berlin);

            ZonedDateTime result = recurring.toZonedDateTime(reference, 0);

            assertEquals(berlin, result.getZone());
        }

        @Test
        @DisplayName("preserves seconds in time")
        void preservesSecondsInTime() {
            MonthDay md = MonthDay.of(10, 31);
            LocalTime time = LocalTime.of(23, 59, 59);
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(md, time);
            ZonedDateTime reference = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZONE);

            ZonedDateTime result = recurring.toZonedDateTime(reference, 0);

            assertEquals(23, result.getHour());
            assertEquals(59, result.getMinute());
            assertEquals(59, result.getSecond());
        }

        @Test
        @DisplayName("monthDay accessor returns constructor value")
        void monthDayAccessor() {
            MonthDay md = MonthDay.of(2, 14);
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(md, LocalTime.NOON);

            assertEquals(md, recurring.monthDay());
        }

        @Test
        @DisplayName("time accessor returns constructor value")
        void timeAccessor() {
            LocalTime time = LocalTime.of(8, 30);
            WindowBoundary.Recurring recurring = new WindowBoundary.Recurring(MonthDay.of(5, 1), time);

            assertEquals(time, recurring.time());
        }
    }
}
