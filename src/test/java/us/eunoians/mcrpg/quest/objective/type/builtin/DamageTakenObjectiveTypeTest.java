package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DamageTakenObjectiveTypeTest extends McRPGBaseTest {

    private DamageTakenObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new DamageTakenObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns damage_taken key")
        @Test
        public void getKey_returnsDamageTakenKey() {
            assertEquals(DamageTakenObjectiveType.KEY, type.getKey());
        }

        @DisplayName("getExpansionKey returns McRPGExpansion key")
        @Test
        public void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("CanProcess")
    class CanProcess {

        @DisplayName("canProcess returns true for DamageTakenQuestContext")
        @Test
        public void canProcess_returnsTrue_forDamageTakenContext() {
            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @DisplayName("canProcess returns false for other context type")
        @Test
        public void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("ProcessProgress")
    class ProcessProgress {

        @DisplayName("processProgress returns 0 for wrong context type")
        @Test
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, wrongContext));
        }

        @DisplayName("processProgress returns rounded damage for any cause with empty filter")
        @Test
        public void processProgress_returnsRoundedDamage_whenNoFilter() {
            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(5.7);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(6, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns 0 when damage rounds to zero")
        @Test
        public void processProgress_returnsZero_whenDamageRoundsToZero() {
            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(0.3);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns 0 when damage is zero")
        @Test
        public void processProgress_returnsZero_whenDamageIsZero() {
            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(0.0);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns 0 when damage is negative")
        @Test
        public void processProgress_returnsZero_whenDamageIsNegative() {
            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(-2.0);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, context));
        }

        @DisplayName("processProgress rounds damage correctly at 0.5 boundary")
        @Test
        public void processProgress_roundsCorrectly_atHalfBoundary() {
            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(3.5);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(4, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns exact value for whole number damage")
        @Test
        public void processProgress_returnsExact_whenWholeNumberDamage() {
            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(10.0);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(10, type.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("ParseConfig")
    class ParseConfig {

        @DisplayName("parseConfig with empty section accepts any damage cause")
        @Test
        public void parseConfig_acceptsAnyCause_whenSectionEmpty() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(false);

            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(5.0);
            when(mockEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(5, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with cause filter accepts matching cause")
        @Test
        public void parseConfig_acceptsMatchingCause() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(true);
            when(section.getStringList("causes")).thenReturn(List.of("FALL"));

            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(8.0);
            when(mockEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(8, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with cause filter rejects non-matching cause")
        @Test
        public void parseConfig_rejectsNonMatchingCause() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(true);
            when(section.getStringList("causes")).thenReturn(List.of("FALL"));

            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(8.0);
            when(mockEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with multiple causes accepts any listed cause")
        @Test
        public void parseConfig_acceptsAnyListedCause_whenMultipleCauses() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(true);
            when(section.getStringList("causes")).thenReturn(List.of("FALL", "FIRE", "DROWNING"));

            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(3.0);
            when(mockEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(3, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig is case-insensitive for cause names")
        @Test
        public void parseConfig_caseInsensitive_forCauseNames() {
            Section section = mock(Section.class);
            when(section.contains("causes")).thenReturn(true);
            when(section.getStringList("causes")).thenReturn(List.of("fall"));

            DamageTakenObjectiveType configured = type.parseConfig(section);

            EntityDamageEvent mockEvent = mock(EntityDamageEvent.class);
            when(mockEvent.getFinalDamage()).thenReturn(5.0);
            when(mockEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
            DamageTakenQuestContext context = new DamageTakenQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(5, configured.processProgress(instance, context));
        }
    }
}
