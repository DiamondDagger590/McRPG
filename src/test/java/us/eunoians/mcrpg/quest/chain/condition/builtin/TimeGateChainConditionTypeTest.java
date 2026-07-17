package us.eunoians.mcrpg.quest.chain.condition.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeGateChainConditionTypeTest extends McRPGBaseTest {

    private final TimeGateChainConditionType conditionType = new TimeGateChainConditionType();

    @Nested
    @DisplayName("identity")
    class IdentityTests {

        @Test
        @DisplayName("getKey returns mcrpg:time_gate")
        void getKey_returnsMcrpgTimeGate() {
            NamespacedKey key = conditionType.getKey();
            assertEquals("mcrpg", key.getNamespace());
            assertEquals("time_gate", key.getKey());
        }

        @Test
        @DisplayName("getExpansionKey returns empty")
        void getExpansionKey_returnsEmpty() {
            assertTrue(conditionType.getExpansionKey().isEmpty());
        }
    }

    @Nested
    @DisplayName("parse")
    class ParseTests {

        @Test
        @DisplayName("parses valid config with after and timezone")
        void parse_validConfigWithTimezone() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn("America/New_York");

            QuestChainStartCondition condition = conditionType.parse(section);

            assertInstanceOf(TimeGateCondition.class, condition);
            TimeGateCondition timeGate = (TimeGateCondition) condition;
            assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0, 0), timeGate.after());
            assertEquals(ZoneId.of("America/New_York"), timeGate.timezone());
            assertEquals(TimeGateChainConditionType.KEY, timeGate.getKey());
        }

        @Test
        @DisplayName("parses valid config without timezone, defaults to system default")
        void parse_validConfigWithoutTimezone_defaultsToSystemDefault() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-12-25T08:00:00");
            when(section.getString("timezone")).thenReturn(null);

            QuestChainStartCondition condition = conditionType.parse(section);

            assertInstanceOf(TimeGateCondition.class, condition);
            TimeGateCondition timeGate = (TimeGateCondition) condition;
            assertEquals(LocalDateTime.of(2026, 12, 25, 8, 0, 0), timeGate.after());
            assertEquals(ZoneId.systemDefault(), timeGate.timezone());
        }

        @Test
        @DisplayName("throws when after field is missing")
        void parse_throwsWhenAfterMissing() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn(null);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> conditionType.parse(section));
            assertTrue(exception.getMessage().contains("'after' field"));
        }

        @Test
        @DisplayName("throws when after field is not valid ISO-8601")
        void parse_throwsWhenAfterIsInvalidFormat() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("not-a-date");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> conditionType.parse(section));
            assertTrue(exception.getMessage().contains("not-a-date"));
            assertTrue(exception.getMessage().contains("ISO-8601"));
        }

        @Test
        @DisplayName("throws when timezone is invalid IANA ID")
        void parse_throwsWhenTimezoneIsInvalid() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn("Invalid/Timezone");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> conditionType.parse(section));
            assertTrue(exception.getMessage().contains("Invalid/Timezone"));
            assertTrue(exception.getMessage().contains("IANA timezone ID"));
        }

        @Test
        @DisplayName("parses date with time component")
        void parse_dateWithTimeComponent() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-03-15T14:30:45");
            when(section.getString("timezone")).thenReturn(null);

            QuestChainStartCondition condition = conditionType.parse(section);

            TimeGateCondition timeGate = (TimeGateCondition) condition;
            assertEquals(LocalDateTime.of(2026, 3, 15, 14, 30, 45), timeGate.after());
        }
    }
}
