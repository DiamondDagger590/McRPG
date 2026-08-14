package us.eunoians.mcrpg.quest.chain.condition.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeGateChainConditionTypeTest extends McRPGBaseTest {

    private final TimeGateChainConditionType conditionType = new TimeGateChainConditionType();

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("Returns the time_gate key")
        void getKey_returnsTimeGateKey() {
            assertEquals(TimeGateChainConditionType.KEY, conditionType.getKey());
        }

        @Test
        @DisplayName("Key namespace is mcrpg")
        void getKey_namespacedCorrectly() {
            assertEquals("mcrpg", conditionType.getKey().getNamespace());
            assertEquals("time_gate", conditionType.getKey().getKey());
        }
    }

    @Nested
    @DisplayName("getExpansionKey")
    class GetExpansionKey {

        @Test
        @DisplayName("Returns empty — built-in type")
        void getExpansionKey_returnsEmpty() {
            Optional<org.bukkit.NamespacedKey> result = conditionType.getExpansionKey();
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("parse")
    class Parse {

        @Test
        @DisplayName("Parses valid ISO-8601 after with default timezone")
        void parse_validAfterNoTimezone_returnsConditionWithSystemDefault() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn(null);

            QuestChainStartCondition result = conditionType.parse(section);
            assertInstanceOf(TimeGateCondition.class, result);

            TimeGateCondition condition = (TimeGateCondition) result;
            assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0, 0), condition.after());
            assertEquals(ZoneId.systemDefault(), condition.timezone());
            assertEquals(TimeGateChainConditionType.KEY, condition.key());
        }

        @Test
        @DisplayName("Parses valid after with explicit timezone")
        void parse_validAfterWithTimezone_returnsConditionWithSpecifiedZone() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-12-25T10:30:00");
            when(section.getString("timezone")).thenReturn("America/New_York");

            QuestChainStartCondition result = conditionType.parse(section);
            assertInstanceOf(TimeGateCondition.class, result);

            TimeGateCondition condition = (TimeGateCondition) result;
            assertEquals(LocalDateTime.of(2026, 12, 25, 10, 30, 0), condition.after());
            assertEquals(ZoneId.of("America/New_York"), condition.timezone());
        }

        @Test
        @DisplayName("Throws when 'after' field is missing")
        void parse_missingAfter_throwsIllegalArgument() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn(null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> conditionType.parse(section));
            assertTrue(exception.getMessage().contains("'after'"));
        }

        @Test
        @DisplayName("Throws when 'after' field is not valid ISO-8601")
        void parse_invalidAfterFormat_throwsIllegalArgument() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("not-a-date");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> conditionType.parse(section));
            assertTrue(exception.getMessage().contains("not-a-date"));
            assertTrue(exception.getMessage().contains("ISO-8601"));
        }

        @Test
        @DisplayName("Throws when 'timezone' field is not a valid IANA zone")
        void parse_invalidTimezone_throwsIllegalArgument() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T00:00:00");
            when(section.getString("timezone")).thenReturn("Invalid/Zone");

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> conditionType.parse(section));
            assertTrue(exception.getMessage().contains("Invalid/Zone"));
            assertTrue(exception.getMessage().contains("IANA"));
        }

        @Test
        @DisplayName("Parses date-time without seconds")
        void parse_afterWithoutSeconds_parsesSuccessfully() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-07-01T14:30");
            when(section.getString("timezone")).thenReturn(null);

            QuestChainStartCondition result = conditionType.parse(section);
            assertInstanceOf(TimeGateCondition.class, result);
            TimeGateCondition condition = (TimeGateCondition) result;
            assertEquals(LocalDateTime.of(2026, 7, 1, 14, 30, 0), condition.after());
        }

        @Test
        @DisplayName("Parses with UTC timezone string")
        void parse_utcTimezone_parsesSuccessfully() {
            Section section = mock(Section.class);
            when(section.getString("after")).thenReturn("2026-01-01T00:00:00");
            when(section.getString("timezone")).thenReturn("UTC");

            QuestChainStartCondition result = conditionType.parse(section);
            TimeGateCondition condition = (TimeGateCondition) result;
            assertEquals(ZoneId.of("UTC"), condition.timezone());
        }
    }
}
