package us.eunoians.mcrpg.quest.chain.availability.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundary;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FixedWindowBoundaryType")
public class FixedWindowBoundaryTypeTest extends McRPGBaseTest {

    private FixedWindowBoundaryType type;
    private File mockFile;
    private Logger mockLogger;

    @BeforeEach
    public void setup() {
        type = new FixedWindowBoundaryType();
        mockFile = mock(File.class);
        when(mockFile.getName()).thenReturn("test-chain.yml");
        mockLogger = mock(Logger.class);
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns the fixed boundary key")
        public void getKey_returnsFixedKey() {
            assertEquals(FixedWindowBoundaryType.KEY, type.getKey());
            assertEquals("mcrpg", type.getKey().getNamespace());
            assertEquals("fixed", type.getKey().getKey());
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
        @DisplayName("valid ISO-8601 date returns Fixed boundary")
        public void parse_validDate_returnsFixedBoundary() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2026-06-01T00:00:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertTrue(result.isPresent());
            WindowBoundary.Fixed fixed = assertInstanceOf(WindowBoundary.Fixed.class, result.get());
            assertEquals(LocalDateTime.of(2026, 6, 1, 0, 0, 0), fixed.dateTime());
        }

        @Test
        @DisplayName("date with time component parses correctly")
        public void parse_dateWithTime_parsesCorrectly() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2025-12-25T14:30:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertTrue(result.isPresent());
            WindowBoundary.Fixed fixed = assertInstanceOf(WindowBoundary.Fixed.class, result.get());
            assertEquals(LocalDateTime.of(2025, 12, 25, 14, 30, 0), fixed.dateTime());
        }

        @Test
        @DisplayName("null date field returns empty and logs warning")
        public void parse_nullDate_returnsEmptyAndLogsWarning() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn(null);

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertFalse(result.isPresent());
            verify(mockLogger).warning("[AvailabilityConfig] Fixed boundary in test-chain.yml is missing 'date' field — skipping window");
        }

        @Test
        @DisplayName("invalid date format returns empty and logs warning")
        public void parse_invalidDateFormat_returnsEmptyAndLogsWarning() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("not-a-date");

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertFalse(result.isPresent());
            verify(mockLogger).warning("[AvailabilityConfig] Invalid date 'not-a-date' in test-chain.yml — expected ISO-8601 format (e.g., 2026-06-01T00:00:00)");
        }

        @Test
        @DisplayName("date without explicit time component parses as midnight")
        public void parse_dateWithoutTime_parsesAsMidnight() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2026-06-01T00:00");

            Optional<WindowBoundary> result = type.parse(section, mockFile, mockLogger);

            assertTrue(result.isPresent());
            WindowBoundary.Fixed fixed = assertInstanceOf(WindowBoundary.Fixed.class, result.get());
            assertEquals(LocalDateTime.of(2026, 6, 1, 0, 0, 0), fixed.dateTime());
        }
    }
}
