package us.eunoians.mcrpg.quest.chain.availability.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundary;

import java.io.File;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RecurringWindowBoundaryType")
public class RecurringWindowBoundaryTypeTest extends McRPGBaseTest {

    private RecurringWindowBoundaryType type;
    private File mockFile;
    private Logger mockLogger;

    @BeforeEach
    public void setup() {
        type = new RecurringWindowBoundaryType();
        mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test-chain.yml");
        mockLogger = mock(Logger.class);
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns the recurring boundary key")
        public void getKey_returnsRecurringKey() {
            assertEquals(RecurringWindowBoundaryType.KEY, type.getKey());
            assertEquals("mcrpg", type.getKey().getNamespace());
            assertEquals("recurring", type.getKey().getKey());
        }
    }

    @Nested
    @DisplayName("getExpansionKey")
    class GetExpansionKey {

        @Test
        @DisplayName("returns empty for built-in type")
        public void getExpansionKey_returnsEmpty() {
            assertTrue(type.getExpansionKey().isEmpty());
        }
    }

    @Nested
    @DisplayName("parse")
    class Parse {

        @Test
        @DisplayName("valid month-day and time returns Recurring boundary")
        public void parse_validInputs_returnsRecurringBoundary() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--12-01");
            when(section.getString("time")).thenReturn("00:00:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertTrue(result.isPresent());
            WindowBoundary.Recurring recurring = assertInstanceOf(WindowBoundary.Recurring.class, result.get());
            assertEquals(MonthDay.of(12, 1), recurring.monthDay());
            assertEquals(LocalTime.MIDNIGHT, recurring.time());
        }

        @Test
        @DisplayName("month-day with specific time parses correctly")
        public void parse_specificTime_parsesCorrectly() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--06-15");
            when(section.getString("time")).thenReturn("14:30:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertTrue(result.isPresent());
            WindowBoundary.Recurring recurring = assertInstanceOf(WindowBoundary.Recurring.class, result.get());
            assertEquals(MonthDay.of(6, 15), recurring.monthDay());
            assertEquals(LocalTime.of(14, 30, 0), recurring.time());
        }

        @Test
        @DisplayName("null month-day returns empty and logs warning")
        public void parse_nullMonthDay_returnsEmptyAndLogsWarning() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn(null);
            when(section.getString("time")).thenReturn("12:00:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertFalse(result.isPresent());
            verify(mockLogger).warning("[AvailabilityConfig] Recurring boundary in test-chain.yml is missing 'month-day' or 'time' field — skipping window");
        }

        @Test
        @DisplayName("null time returns empty and logs warning")
        public void parse_nullTime_returnsEmptyAndLogsWarning() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--12-01");
            when(section.getString("time")).thenReturn(null);

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertFalse(result.isPresent());
            verify(mockLogger).warning("[AvailabilityConfig] Recurring boundary in test-chain.yml is missing 'month-day' or 'time' field — skipping window");
        }

        @Test
        @DisplayName("both fields null returns empty and logs warning")
        public void parse_bothNull_returnsEmptyAndLogsWarning() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn(null);
            when(section.getString("time")).thenReturn(null);

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertFalse(result.isPresent());
            verify(mockLogger).warning("[AvailabilityConfig] Recurring boundary in test-chain.yml is missing 'month-day' or 'time' field — skipping window");
        }

        @Test
        @DisplayName("invalid month-day format returns empty and logs warning")
        public void parse_invalidMonthDay_returnsEmptyAndLogsWarning() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("invalid");
            when(section.getString("time")).thenReturn("12:00:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertFalse(result.isPresent());
            verify(mockLogger).warning(org.mockito.ArgumentMatchers.startsWith("[AvailabilityConfig] Invalid recurring boundary in test-chain.yml:"));
        }

        @Test
        @DisplayName("invalid time format returns empty and logs warning")
        public void parse_invalidTime_returnsEmptyAndLogsWarning() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--12-01");
            when(section.getString("time")).thenReturn("not-a-time");

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertFalse(result.isPresent());
            verify(mockLogger).warning(org.mockito.ArgumentMatchers.startsWith("[AvailabilityConfig] Invalid recurring boundary in test-chain.yml:"));
        }
    }
}
