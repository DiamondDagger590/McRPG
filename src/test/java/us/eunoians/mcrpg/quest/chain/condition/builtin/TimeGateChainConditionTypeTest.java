package us.eunoians.mcrpg.quest.chain.condition.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeGateChainConditionTypeTest extends McRPGBaseTest {

    private final TimeGateChainConditionType type = new TimeGateChainConditionType();

    @DisplayName("getKey returns the time_gate key")
    @Test
    void getKey_returnsTimeGateKey() {
        NamespacedKey expected = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "time_gate");
        assertEquals(expected, type.getKey());
    }

    @DisplayName("getExpansionKey returns empty for built-in type")
    @Test
    void getExpansionKey_returnsEmpty() {
        assertEquals(Optional.empty(), type.getExpansionKey());
    }

    @DisplayName("parse produces a TimeGateCondition with valid after and timezone")
    @Test
    void parse_producesCondition_whenAfterAndTimezoneValid() {
        Section config = mock(Section.class);
        when(config.getString("after")).thenReturn("2026-07-01T00:00:00");
        when(config.getString("timezone")).thenReturn("America/New_York");

        QuestChainStartCondition condition = type.parse(config);

        assertInstanceOf(TimeGateCondition.class, condition);
        TimeGateCondition gate = (TimeGateCondition) condition;
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), gate.after());
        assertEquals(ZoneId.of("America/New_York"), gate.timezone());
        assertEquals(TimeGateChainConditionType.KEY, gate.getKey());
    }

    @DisplayName("parse defaults timezone to system default when timezone field is absent")
    @Test
    void parse_defaultsTimezone_whenTimezoneAbsent() {
        Section config = mock(Section.class);
        when(config.getString("after")).thenReturn("2026-12-25T08:30:00");
        when(config.getString("timezone")).thenReturn(null);

        QuestChainStartCondition condition = type.parse(config);

        assertInstanceOf(TimeGateCondition.class, condition);
        TimeGateCondition gate = (TimeGateCondition) condition;
        assertEquals(LocalDateTime.of(2026, 12, 25, 8, 30), gate.after());
        assertEquals(ZoneId.systemDefault(), gate.timezone());
    }

    @DisplayName("parse throws IllegalArgumentException when after field is missing")
    @Test
    void parse_throws_whenAfterMissing() {
        Section config = mock(Section.class);
        when(config.getString("after")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> type.parse(config));
        assertTrue(ex.getMessage().contains("'after'"));
    }

    @DisplayName("parse throws IllegalArgumentException when after field is not valid ISO-8601")
    @Test
    void parse_throws_whenAfterInvalid() {
        Section config = mock(Section.class);
        when(config.getString("after")).thenReturn("not-a-date");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> type.parse(config));
        assertTrue(ex.getMessage().contains("not-a-date"));
        assertTrue(ex.getMessage().contains("ISO-8601"));
    }

    @DisplayName("parse throws IllegalArgumentException when timezone is not a valid IANA ID")
    @Test
    void parse_throws_whenTimezoneInvalid() {
        Section config = mock(Section.class);
        when(config.getString("after")).thenReturn("2026-07-01T00:00:00");
        when(config.getString("timezone")).thenReturn("Not/A/Timezone");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> type.parse(config));
        assertTrue(ex.getMessage().contains("Not/A/Timezone"));
        assertTrue(ex.getMessage().contains("IANA"));
    }

    @DisplayName("parse handles date-time with seconds and nanoseconds")
    @Test
    void parse_handlesDateTimeWithSubSeconds() {
        Section config = mock(Section.class);
        when(config.getString("after")).thenReturn("2026-03-15T14:30:45.123");
        when(config.getString("timezone")).thenReturn("UTC");

        QuestChainStartCondition condition = type.parse(config);

        assertInstanceOf(TimeGateCondition.class, condition);
        TimeGateCondition gate = (TimeGateCondition) condition;
        assertEquals(LocalDateTime.of(2026, 3, 15, 14, 30, 45, 123_000_000), gate.after());
    }

    @DisplayName("KEY constant matches expected namespace and value")
    @Test
    void keyConstant_matchesExpected() {
        assertEquals("mcrpg", TimeGateChainConditionType.KEY.getNamespace());
        assertEquals("time_gate", TimeGateChainConditionType.KEY.getKey());
    }
}
