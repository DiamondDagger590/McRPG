package us.eunoians.mcrpg.quest.chain.condition.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("TimeGateChainConditionType")
public class TimeGateChainConditionTypeTest extends McRPGBaseTest {

    private TimeGateChainConditionType type;

    @BeforeEach
    public void setup() {
        type = new TimeGateChainConditionType();
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("returns the time_gate key")
        public void getKey_returnsTimeGateKey() {
            assertEquals(TimeGateChainConditionType.KEY, type.getKey());
            assertEquals("mcrpg", type.getKey().getNamespace());
            assertEquals("time_gate", type.getKey().getKey());
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
        @DisplayName("valid after and timezone returns TimeGateCondition")
        public void parse_validAfterAndTimezone_returnsCondition() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn("America/New_York");

            QuestChainStartCondition result = type.parse(section);

            TimeGateCondition condition = assertInstanceOf(TimeGateCondition.class, result);
            assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0, 0), condition.after());
            assertEquals(ZoneId.of("America/New_York"), condition.timezone());
            assertEquals(TimeGateChainConditionType.KEY, condition.getKey());
        }

        @Test
        @DisplayName("valid after without timezone uses system default")
        public void parse_validAfterNoTimezone_usesSystemDefault() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-01-15T08:30:00");
            when(section.getString("timezone")).thenReturn(null);

            QuestChainStartCondition result = type.parse(section);

            TimeGateCondition condition = assertInstanceOf(TimeGateCondition.class, result);
            assertEquals(LocalDateTime.of(2026, 1, 15, 8, 30, 0), condition.after());
            assertEquals(ZoneId.systemDefault(), condition.timezone());
        }

        @Test
        @DisplayName("UTC timezone parses correctly")
        public void parse_utcTimezone_parsesCorrectly() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2025-03-01T12:00:00");
            when(section.getString("timezone")).thenReturn("UTC");

            QuestChainStartCondition result = type.parse(section);

            TimeGateCondition condition = assertInstanceOf(TimeGateCondition.class, result);
            assertEquals(ZoneId.of("UTC"), condition.timezone());
        }

        @Test
        @DisplayName("missing after field throws IllegalArgumentException")
        public void parse_missingAfter_throwsIllegalArgument() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn(null);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> type.parse(section));
            assertEquals("TimeGateChainConditionType requires an 'after' field", exception.getMessage());
        }

        @Test
        @DisplayName("invalid after format throws IllegalArgumentException")
        public void parse_invalidAfterFormat_throwsIllegalArgument() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("not-a-date");
            when(section.getString("timezone")).thenReturn(null);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> type.parse(section));
            assertTrue(exception.getMessage().contains("Invalid 'after' value 'not-a-date'"));
            assertTrue(exception.getMessage().contains("expected ISO-8601 format"));
        }

        @Test
        @DisplayName("invalid timezone throws IllegalArgumentException")
        public void parse_invalidTimezone_throwsIllegalArgument() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn("Invalid/Timezone");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> type.parse(section));
            assertTrue(exception.getMessage().contains("Invalid 'timezone' value 'Invalid/Timezone'"));
            assertTrue(exception.getMessage().contains("expected a valid IANA timezone ID"));
        }
    }
}
