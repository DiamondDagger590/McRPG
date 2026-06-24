package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

public class DealDamageObjectiveTypeTest extends McRPGBaseTest {

    private DealDamageObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new DealDamageObjectiveType();
    }

    @Nested
    @DisplayName("Identity")
    class Identity {

        @DisplayName("getKey returns deal_damage key")
        @Test
        public void getKey_returnsDealDamageKey() {
            assertEquals(DealDamageObjectiveType.KEY, type.getKey());
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

        @DisplayName("canProcess returns true for DealDamageQuestContext")
        @Test
        public void canProcess_returnsTrue_forDealDamageContext() {
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
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

        @DisplayName("processProgress returns rounded damage for any entity with empty filter")
        @Test
        public void processProgress_returnsRoundedDamage_whenNoFilter() {
            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(7.3);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(7, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns 0 when damage rounds to zero")
        @Test
        public void processProgress_returnsZero_whenDamageRoundsToZero() {
            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(0.2);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns 0 when damage is zero")
        @Test
        public void processProgress_returnsZero_whenDamageIsZero() {
            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(0.0);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns 0 when damage is negative")
        @Test
        public void processProgress_returnsZero_whenDamageIsNegative() {
            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(-5.0);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, type.processProgress(instance, context));
        }

        @DisplayName("processProgress rounds damage correctly at 0.5 boundary")
        @Test
        public void processProgress_roundsCorrectly_atHalfBoundary() {
            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(4.5);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(5, type.processProgress(instance, context));
        }

        @DisplayName("processProgress returns exact value for whole number damage")
        @Test
        public void processProgress_returnsExact_whenWholeNumberDamage() {
            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(15.0);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(15, type.processProgress(instance, context));
        }
    }

    @Nested
    @DisplayName("ParseConfig")
    class ParseConfig {

        @DisplayName("parseConfig with empty section accepts damage to any entity")
        @Test
        public void parseConfig_acceptsAnyEntity_whenSectionEmpty() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(false);

            DealDamageObjectiveType configured = type.parseConfig(section);

            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(5.0);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(5, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with entity filter accepts matching entity")
        @Test
        public void parseConfig_acceptsMatchingEntity() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("ZOMBIE"));

            DealDamageObjectiveType configured = type.parseConfig(section);

            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(5.0);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(5, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with entity filter rejects non-matching entity")
        @Test
        public void parseConfig_rejectsNonMatchingEntity() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("SKELETON"));

            DealDamageObjectiveType configured = type.parseConfig(section);

            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(5.0);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with multiple entities accepts any listed entity")
        @Test
        public void parseConfig_acceptsAnyListedEntity_whenMultipleEntities() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("ZOMBIE", "SKELETON", "CREEPER"));

            DealDamageObjectiveType configured = type.parseConfig(section);

            Skeleton mockEntity = mock(Skeleton.class);
            when(mockEntity.getType()).thenReturn(EntityType.SKELETON);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(8.0);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(8, configured.processProgress(instance, context));
        }

        @DisplayName("parseConfig with entity filter still returns 0 for zero damage")
        @Test
        public void parseConfig_returnsZero_whenDamageIsZeroEvenWithMatchingFilter() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("ZOMBIE"));

            DealDamageObjectiveType configured = type.parseConfig(section);

            Zombie mockEntity = mock(Zombie.class);
            when(mockEntity.getType()).thenReturn(EntityType.ZOMBIE);
            EntityDamageByEntityEvent mockEvent = mock(EntityDamageByEntityEvent.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            when(mockEvent.getFinalDamage()).thenReturn(0.0);

            DealDamageQuestContext context = new DealDamageQuestContext(mockEvent);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0, configured.processProgress(instance, context));
        }
    }
}
