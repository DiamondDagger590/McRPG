package us.eunoians.mcrpg.quest.chain.availability.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundary;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundaryType;
import us.eunoians.mcrpg.quest.chain.availability.WindowBoundaryTypeRegistry;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WindowBoundaryTypeTest extends McRPGBaseTest {

    private static final File DUMMY_FILE = new File("test-config.yml");
    private static final Logger LOGGER = Logger.getLogger("WindowBoundaryTypeTest");

    @Nested
    @DisplayName("FixedWindowBoundaryType")
    class FixedTests {

        private final FixedWindowBoundaryType fixedType = new FixedWindowBoundaryType();

        @Test
        @DisplayName("getKey returns mcrpg:fixed")
        void getKey_returnsMcrpgFixed() {
            NamespacedKey key = fixedType.getKey();
            assertEquals("mcrpg", key.getNamespace());
            assertEquals("fixed", key.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns empty")
        void getExpansionKey_returnsEmpty() {
            assertTrue(fixedType.getExpansionKey().isEmpty());
        }

        @Test
        @DisplayName("parse returns Fixed boundary for valid ISO-8601 date")
        void parse_returnsFixed_whenDateValid() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2026-06-01T00:00:00");

            Optional<WindowBoundary> result = fixedType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isPresent());
            assertInstanceOf(WindowBoundary.Fixed.class, result.get());
            WindowBoundary.Fixed fixed = (WindowBoundary.Fixed) result.get();
            assertEquals(LocalDateTime.of(2026, 6, 1, 0, 0, 0), fixed.dateTime());
        }

        @Test
        @DisplayName("parse returns empty when date field is missing")
        void parse_returnsEmpty_whenDateMissing() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn(null);

            Optional<WindowBoundary> result = fixedType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse returns empty when date is not valid ISO-8601")
        void parse_returnsEmpty_whenDateInvalid() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("not-a-date");

            Optional<WindowBoundary> result = fixedType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse handles date with time component")
        void parse_handlesDateWithTimeComponent() {
            Section section = mock(Section.class);
            when(section.getString("date")).thenReturn("2026-12-25T14:30:45");

            Optional<WindowBoundary> result = fixedType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isPresent());
            WindowBoundary.Fixed fixed = (WindowBoundary.Fixed) result.get();
            assertEquals(LocalDateTime.of(2026, 12, 25, 14, 30, 45), fixed.dateTime());
        }
    }

    @Nested
    @DisplayName("RecurringWindowBoundaryType")
    class RecurringTests {

        private final RecurringWindowBoundaryType recurringType = new RecurringWindowBoundaryType();

        @Test
        @DisplayName("getKey returns mcrpg:recurring")
        void getKey_returnsMcrpgRecurring() {
            NamespacedKey key = recurringType.getKey();
            assertEquals("mcrpg", key.getNamespace());
            assertEquals("recurring", key.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns empty")
        void getExpansionKey_returnsEmpty() {
            assertTrue(recurringType.getExpansionKey().isEmpty());
        }

        @Test
        @DisplayName("parse returns Recurring boundary for valid month-day and time")
        void parse_returnsRecurring_whenValid() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--12-01");
            when(section.getString("time")).thenReturn("00:00:00");

            Optional<WindowBoundary> result = recurringType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isPresent());
            assertInstanceOf(WindowBoundary.Recurring.class, result.get());
            WindowBoundary.Recurring recurring = (WindowBoundary.Recurring) result.get();
            assertEquals(MonthDay.of(12, 1), recurring.monthDay());
            assertEquals(LocalTime.MIDNIGHT, recurring.time());
        }

        @Test
        @DisplayName("parse returns empty when month-day is missing")
        void parse_returnsEmpty_whenMonthDayMissing() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn(null);
            when(section.getString("time")).thenReturn("14:30:00");

            Optional<WindowBoundary> result = recurringType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse returns empty when time is missing")
        void parse_returnsEmpty_whenTimeMissing() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--06-15");
            when(section.getString("time")).thenReturn(null);

            Optional<WindowBoundary> result = recurringType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse returns empty when both fields are missing")
        void parse_returnsEmpty_whenBothMissing() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn(null);
            when(section.getString("time")).thenReturn(null);

            Optional<WindowBoundary> result = recurringType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse returns empty when month-day format is invalid")
        void parse_returnsEmpty_whenMonthDayInvalid() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("12-01");
            when(section.getString("time")).thenReturn("00:00:00");

            Optional<WindowBoundary> result = recurringType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse returns empty when time format is invalid")
        void parse_returnsEmpty_whenTimeInvalid() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--03-15");
            when(section.getString("time")).thenReturn("not-a-time");

            Optional<WindowBoundary> result = recurringType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("parse handles time with seconds precision")
        void parse_handlesTimeWithSeconds() {
            Section section = mock(Section.class);
            when(section.getString("month-day")).thenReturn("--07-04");
            when(section.getString("time")).thenReturn("23:59:59");

            Optional<WindowBoundary> result = recurringType.parse(section, DUMMY_FILE, LOGGER);

            assertTrue(result.isPresent());
            WindowBoundary.Recurring recurring = (WindowBoundary.Recurring) result.get();
            assertEquals(MonthDay.of(7, 4), recurring.monthDay());
            assertEquals(LocalTime.of(23, 59, 59), recurring.time());
        }
    }

    @Nested
    @DisplayName("WindowBoundaryTypeRegistry")
    class RegistryTests {

        @Test
        @DisplayName("register and retrieve a boundary type")
        void register_registersType() {
            WindowBoundaryTypeRegistry registry = new WindowBoundaryTypeRegistry();
            FixedWindowBoundaryType fixedType = new FixedWindowBoundaryType();

            registry.register(fixedType);

            assertTrue(registry.registered(fixedType));
        }

        @Test
        @DisplayName("get returns empty for unknown key")
        void get_returnsEmpty_whenKeyUnknown() {
            WindowBoundaryTypeRegistry registry = new WindowBoundaryTypeRegistry();
            NamespacedKey unknownKey = new NamespacedKey("mcrpg", "unknown");

            assertTrue(registry.get(unknownKey).isEmpty());
        }

        @Test
        @DisplayName("get returns registered type for known key")
        void get_returnsType_whenKeyKnown() {
            WindowBoundaryTypeRegistry registry = new WindowBoundaryTypeRegistry();
            RecurringWindowBoundaryType recurringType = new RecurringWindowBoundaryType();

            registry.register(recurringType);

            Optional<WindowBoundaryType> result = registry.get(RecurringWindowBoundaryType.KEY);
            assertTrue(result.isPresent());
            assertSame(recurringType, result.get());
        }

        @Test
        @DisplayName("registered returns false for unregistered type")
        void registered_returnsFalse_whenNotRegistered() {
            WindowBoundaryTypeRegistry registry = new WindowBoundaryTypeRegistry();
            FixedWindowBoundaryType fixedType = new FixedWindowBoundaryType();

            assertFalse(registry.registered(fixedType));
        }

        @Test
        @DisplayName("register replaces existing type with same key")
        void register_replacesExisting() {
            WindowBoundaryTypeRegistry registry = new WindowBoundaryTypeRegistry();
            WindowBoundaryType original = mock(WindowBoundaryType.class);
            WindowBoundaryType replacement = mock(WindowBoundaryType.class);
            when(original.getKey()).thenReturn(FixedWindowBoundaryType.KEY);
            when(replacement.getKey()).thenReturn(FixedWindowBoundaryType.KEY);

            registry.register(original);
            registry.register(replacement);

            Optional<WindowBoundaryType> result = registry.get(FixedWindowBoundaryType.KEY);
            assertTrue(result.isPresent());
            assertSame(replacement, result.get());
        }
    }
}
