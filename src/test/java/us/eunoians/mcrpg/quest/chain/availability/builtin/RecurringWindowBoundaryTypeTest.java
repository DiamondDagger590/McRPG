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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("RecurringWindowBoundaryType")
class RecurringWindowBoundaryTypeTest extends McRPGBaseTest {

    private RecurringWindowBoundaryType boundaryType;
    private Logger logger;
    private File file;

    @BeforeEach
    void setUp() {
        boundaryType = new RecurringWindowBoundaryType();
        logger = Logger.getLogger(RecurringWindowBoundaryTypeTest.class.getName());
        file = new File("test-chain.yml");
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns the recurring boundary type key")
        void getKey_returnsRecurringKey() {
            assertEquals(RecurringWindowBoundaryType.KEY, boundaryType.getKey());
            assertEquals("mcrpg:recurring", boundaryType.getKey().toString());
        }
    }

    @Nested
    @DisplayName("getExpansionKey")
    class GetExpansionKey {

        @Test
        @DisplayName("returns empty for built-in type")
        void getExpansionKey_returnsEmpty() {
            assertTrue(boundaryType.getExpansionKey().isEmpty());
        }
    }

    @Nested
    @DisplayName("parse")
    class Parse {

        @Test
        @DisplayName("returns Recurring boundary when month-day and time are valid")
        void parse_returnsRecurringBoundary_whenFieldsAreValid() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--12-01");
            when(section.getString("time")).thenReturn("00:00:00");

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isPresent());
            assertInstanceOf(WindowBoundary.Recurring.class, result.get());
            WindowBoundary.Recurring recurring = (WindowBoundary.Recurring) result.get();
            assertEquals(MonthDay.of(12, 1), recurring.monthDay());
            assertEquals(LocalTime.MIDNIGHT, recurring.time());
        }

        @Test
        @DisplayName("returns empty when month-day field is null")
        void parse_returnsEmpty_whenMonthDayIsNull() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn(null);
            when(section.getString("time")).thenReturn("14:30:00");

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when time field is null")
        void parse_returnsEmpty_whenTimeIsNull() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--06-15");
            when(section.getString("time")).thenReturn(null);

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when both fields are null")
        void parse_returnsEmpty_whenBothFieldsAreNull() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn(null);
            when(section.getString("time")).thenReturn(null);

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when month-day is not valid ISO-8601")
        void parse_returnsEmpty_whenMonthDayIsInvalid() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("not-a-month-day");
            when(section.getString("time")).thenReturn("14:30:00");

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when time is not valid ISO-8601")
        void parse_returnsEmpty_whenTimeIsInvalid() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--06-15");
            when(section.getString("time")).thenReturn("25:99:99");

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parses time without seconds")
        void parse_parsesTimeWithoutSeconds() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--03-15");
            when(section.getString("time")).thenReturn("14:30");

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isPresent());
            WindowBoundary.Recurring recurring = (WindowBoundary.Recurring) result.get();
            assertEquals(MonthDay.of(3, 15), recurring.monthDay());
            assertEquals(LocalTime.of(14, 30), recurring.time());
        }
    }
}
