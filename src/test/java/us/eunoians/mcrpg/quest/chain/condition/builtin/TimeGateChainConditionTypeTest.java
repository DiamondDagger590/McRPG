package us.eunoians.mcrpg.quest.chain.condition.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;

import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeGateChainConditionTypeTest {

    private final TimeGateChainConditionType type = new TimeGateChainConditionType();

    @Nested
    @DisplayName("getKey")
    class GetKeyTests {

        @Test
        @DisplayName("getKey returns mcrpg:time_gate")
        void getKey_returnsTimeGateKey() {
            assertEquals("mcrpg", type.getKey().getNamespace());
            assertEquals("time_gate", type.getKey().getKey());
        }
    }

    @Nested
    @DisplayName("getExpansionKey")
    class GetExpansionKeyTests {

        @Test
        @DisplayName("getExpansionKey returns empty for built-in type")
        void getExpansionKey_returnsEmpty() {
            assertTrue(type.getExpansionKey().isEmpty());
        }
    }

    @Nested
    @DisplayName("parse")
    class ParseTests {

        @Test
        @DisplayName("parse returns TimeGateCondition with valid after and timezone")
        void parse_returnsCondition_whenValidAfterAndTimezone() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn("America/New_York");

            QuestChainStartCondition result = type.parse(section);

            assertInstanceOf(TimeGateCondition.class, result);
            TimeGateCondition condition = (TimeGateCondition) result;
            assertEquals(2026, condition.after().getYear());
            assertEquals(7, condition.after().getMonthValue());
            assertEquals(1, condition.after().getDayOfMonth());
            assertEquals(ZoneId.of("America/New_York"), condition.timezone());
        }

        @Test
        @DisplayName("parse defaults to system timezone when timezone field is absent")
        void parse_defaultsToSystemTimezone_whenTimezoneAbsent() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn(null);

            QuestChainStartCondition result = type.parse(section);

            assertInstanceOf(TimeGateCondition.class, result);
            TimeGateCondition condition = (TimeGateCondition) result;
            assertEquals(ZoneId.systemDefault(), condition.timezone());
        }

        @Test
        @DisplayName("parse throws when after field is missing")
        void parse_throws_whenAfterMissing() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> type.parse(section));
            assertTrue(ex.getMessage().contains("'after'"));
        }

        @Test
        @DisplayName("parse throws when after field has invalid format")
        void parse_throws_whenAfterInvalid() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("not-a-date");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> type.parse(section));
            assertTrue(ex.getMessage().contains("not-a-date"));
        }

        @Test
        @DisplayName("parse throws when timezone field has invalid value")
        void parse_throws_whenTimezoneInvalid() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn("Invalid/Zone");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> type.parse(section));
            assertTrue(ex.getMessage().contains("Invalid/Zone"));
        }

        @Test
        @DisplayName("parse preserves time component of after field")
        void parse_preservesTimeComponent() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-12-25T14:30:45");
            when(section.getString("timezone")).thenReturn(null);

            QuestChainStartCondition result = type.parse(section);

            TimeGateCondition condition = (TimeGateCondition) result;
            assertEquals(14, condition.after().getHour());
            assertEquals(30, condition.after().getMinute());
            assertEquals(45, condition.after().getSecond());
        }

        @Test
        @DisplayName("parse sets the correct key on the returned condition")
        void parse_setsCorrectKey() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn(null);

            QuestChainStartCondition result = type.parse(section);

            assertEquals(TimeGateChainConditionType.KEY, result.getKey());
        }
    }
}
