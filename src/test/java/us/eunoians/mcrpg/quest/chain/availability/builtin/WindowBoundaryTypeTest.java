package us.eunoians.mcrpg.quest.chain.availability.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundary;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WindowBoundaryTypeTest {

    private final File mockFile = new File("test-chain.yml");
    private final Logger logger = mock(Logger.class);

    @Nested
    @DisplayName("FixedWindowBoundaryType")
    class FixedBoundaryTypeTests {

        private final FixedWindowBoundaryType type = new FixedWindowBoundaryType();

        @Test
        @DisplayName("getKey returns mcrpg:fixed")
        void getKey_returnsFixedKey() {
            assertEquals("mcrpg", type.getKey().getNamespace());
            assertEquals("fixed", type.getKey().getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns empty for built-in type")
        void getExpansionKey_returnsEmpty() {
            assertTrue(type.getExpansionKey().isEmpty());
        }

        @Test
        @DisplayName("parse returns Fixed boundary with valid ISO-8601 date")
        void parse_returnsFixedBoundary_whenValidDate() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2026-06-01T00:00:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isPresent());
            assertInstanceOf(WindowBoundary.Fixed.class, result.get());
            WindowBoundary.Fixed fixed = (WindowBoundary.Fixed) result.get();
            assertEquals(LocalDateTime.of(2026, 6, 1, 0, 0, 0), fixed.dateTime());
        }

        @Test
        @DisplayName("parse preserves time component in date field")
        void parse_preservesTimeComponent() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2026-12-25T14:30:45");

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isPresent());
            WindowBoundary.Fixed fixed = (WindowBoundary.Fixed) result.get();
            assertEquals(LocalDateTime.of(2026, 12, 25, 14, 30, 45), fixed.dateTime());
        }

        @Test
        @DisplayName("parse returns empty when date field is missing")
        void parse_returnsEmpty_whenDateMissing() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn(null);

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse logs warning when date field is missing")
        void parse_logsWarning_whenDateMissing() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn(null);

            type.parse(section, mockFile, logger);

            verify(logger).warning(
                    "[AvailabilityConfig] Fixed boundary in test-chain.yml is missing 'date' field — skipping window");
        }

        @Test
        @DisplayName("parse returns empty when date field has invalid format")
        void parse_returnsEmpty_whenDateInvalid() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("not-a-date");

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse logs warning when date field has invalid format")
        void parse_logsWarning_whenDateInvalid() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("bad-format");

            type.parse(section, mockFile, logger);

            verify(logger).warning(
                    "[AvailabilityConfig] Invalid date 'bad-format' in test-chain.yml — expected ISO-8601 format (e.g., 2026-06-01T00:00:00)");
        }
    }

    @Nested
    @DisplayName("RecurringWindowBoundaryType")
    class RecurringBoundaryTypeTests {

        private final RecurringWindowBoundaryType type = new RecurringWindowBoundaryType();

        @Test
        @DisplayName("getKey returns mcrpg:recurring")
        void getKey_returnsRecurringKey() {
            assertEquals("mcrpg", type.getKey().getNamespace());
            assertEquals("recurring", type.getKey().getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns empty for built-in type")
        void getExpansionKey_returnsEmpty() {
            assertTrue(type.getExpansionKey().isEmpty());
        }

        @Test
        @DisplayName("parse returns Recurring boundary with valid month-day and time")
        void parse_returnsRecurringBoundary_whenValid() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--12-01");
            when(section.getString("time")).thenReturn("00:00:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isPresent());
            assertInstanceOf(WindowBoundary.Recurring.class, result.get());
            WindowBoundary.Recurring recurring = (WindowBoundary.Recurring) result.get();
            assertEquals(MonthDay.of(12, 1), recurring.monthDay());
            assertEquals(LocalTime.MIDNIGHT, recurring.time());
        }

        @Test
        @DisplayName("parse preserves time component")
        void parse_preservesTimeComponent() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--06-15");
            when(section.getString("time")).thenReturn("14:30:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isPresent());
            WindowBoundary.Recurring recurring = (WindowBoundary.Recurring) result.get();
            assertEquals(MonthDay.of(6, 15), recurring.monthDay());
            assertEquals(LocalTime.of(14, 30, 0), recurring.time());
        }

        @Test
        @DisplayName("parse returns empty when month-day is missing")
        void parse_returnsEmpty_whenMonthDayMissing() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn(null);
            when(section.getString("time")).thenReturn("00:00:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse returns empty when time is missing")
        void parse_returnsEmpty_whenTimeMissing() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--12-01");
            when(section.getString("time")).thenReturn(null);

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse returns empty when both fields are missing")
        void parse_returnsEmpty_whenBothFieldsMissing() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn(null);
            when(section.getString("time")).thenReturn(null);

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse logs warning when fields are missing")
        void parse_logsWarning_whenFieldsMissing() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn(null);
            when(section.getString("time")).thenReturn(null);

            type.parse(section, mockFile, logger);

            verify(logger).warning(
                    "[AvailabilityConfig] Recurring boundary in test-chain.yml is missing 'month-day' or 'time' field — skipping window");
        }

        @Test
        @DisplayName("parse returns empty when month-day has invalid format")
        void parse_returnsEmpty_whenMonthDayInvalid() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("12-01");
            when(section.getString("time")).thenReturn("00:00:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse returns empty when time has invalid format")
        void parse_returnsEmpty_whenTimeInvalid() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--12-01");
            when(section.getString("time")).thenReturn("not-a-time");

            Optional<WindowBoundary> result = type.parse(section, mockFile, logger);

            assertTrue(result.isEmpty());
        }
    }
}
