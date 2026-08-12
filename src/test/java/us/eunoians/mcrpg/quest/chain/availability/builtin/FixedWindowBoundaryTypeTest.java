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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("FixedWindowBoundaryType")
class FixedWindowBoundaryTypeTest extends McRPGBaseTest {

    private FixedWindowBoundaryType boundaryType;
    private Logger logger;
    private File file;

    @BeforeEach
    void setUp() {
        boundaryType = new FixedWindowBoundaryType();
        logger = Logger.getLogger(FixedWindowBoundaryTypeTest.class.getName());
        file = new File("test-chain.yml");
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns the fixed boundary type key")
        void getKey_returnsFixedKey() {
            assertEquals(FixedWindowBoundaryType.KEY, boundaryType.getKey());
            assertEquals("mcrpg:fixed", boundaryType.getKey().toString());
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
        @DisplayName("returns Fixed boundary when date is valid ISO-8601")
        void parse_returnsFixedBoundary_whenDateIsValid() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2026-06-15T14:30:00");

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isPresent());
            assertInstanceOf(WindowBoundary.Fixed.class, result.get());
            WindowBoundary.Fixed fixed = (WindowBoundary.Fixed) result.get();
            assertEquals(LocalDateTime.of(2026, 6, 15, 14, 30, 0), fixed.dateTime());
        }

        @Test
        @DisplayName("returns empty when date field is null")
        void parse_returnsEmpty_whenDateFieldIsNull() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn(null);

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when date is not valid ISO-8601")
        void parse_returnsEmpty_whenDateIsInvalid() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("not-a-date");

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when date is partially valid")
        void parse_returnsEmpty_whenDateIsPartiallyValid() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2026-13-01T00:00:00");

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parses date without seconds")
        void parse_parsesDateWithoutSeconds() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2026-01-01T00:00");

            Optional<WindowBoundary> result = boundaryType.parse(section, file, logger);

            assertTrue(result.isPresent());
            WindowBoundary.Fixed fixed = (WindowBoundary.Fixed) result.get();
            assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), fixed.dateTime());
        }
    }
}
