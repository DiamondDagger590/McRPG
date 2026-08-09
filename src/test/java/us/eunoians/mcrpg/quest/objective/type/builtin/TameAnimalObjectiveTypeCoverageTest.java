package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTameEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("TameAnimalObjectiveType — extended coverage")
public class TameAnimalObjectiveTypeCoverageTest extends McRPGBaseTest {

    private TameAnimalObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new TameAnimalObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for TameAnimalQuestContext")
        public void canProcess_returnsTrue_forTameAnimalContext() {
            EntityTameEvent mockEvent = createMockTameEvent(EntityType.WOLF);
            TameAnimalQuestContext context = new TameAnimalQuestContext(mockEvent);
            assertTrue(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic mock context")
        public void canProcess_returnsFalse_forGenericContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for BlockBreakQuestContext")
        public void canProcess_returnsFalse_forBlockBreakContext() {
            BlockBreakQuestContext context = mock(BlockBreakQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress — empty entity filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns 1 for any entity when no filter is set")
        public void processProgress_returnsOne_whenNoFilter() {
            EntityTameEvent event = createMockTameEvent(EntityType.WOLF);
            TameAnimalQuestContext context = new TameAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 for cat when no filter is set")
        public void processProgress_returnsOne_forCat_whenNoFilter() {
            EntityTameEvent event = createMockTameEvent(EntityType.CAT);
            TameAnimalQuestContext context = new TameAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with entity filter via parseConfig")
    class ProcessProgressWithFilter {

        private TameAnimalObjectiveType filteredType;

        @BeforeEach
        public void setupFilteredType() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("WOLF", "CAT"));
            filteredType = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns 1 for matching entity")
        public void processProgress_returnsOne_forMatchingEntity() {
            EntityTameEvent event = createMockTameEvent(EntityType.WOLF);
            TameAnimalQuestContext context = new TameAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching entity")
        public void processProgress_returnsZero_forNonMatchingEntity() {
            EntityTameEvent event = createMockTameEvent(EntityType.PARROT);
            TameAnimalQuestContext context = new TameAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for second matching entity in filter")
        public void processProgress_returnsOne_forSecondMatchingEntity() {
            EntityTameEvent event = createMockTameEvent(EntityType.CAT);
            TameAnimalQuestContext context = new TameAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — wrong context type")
    class ProcessProgressWrongContext {

        @Test
        @DisplayName("returns 0 for generic mock context")
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 0 for BlockBreakQuestContext")
        public void processProgress_returnsZero_forBlockBreakContext() {
            BlockBreakQuestContext context = mock(BlockBreakQuestContext.class);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(0, progress);
        }
    }

    @Nested
    @DisplayName("identity")
    class Identity {

        @Test
        @DisplayName("key namespace is mcrpg")
        public void getKey_namespaceIsMcrpg() {
            assertEquals("mcrpg", baseType.getKey().getNamespace());
        }

        @Test
        @DisplayName("key value is tame_animal")
        public void getKey_valueIsTameAnimal() {
            assertEquals("tame_animal", baseType.getKey().getKey());
        }

        @Test
        @DisplayName("expansion key is present")
        public void getExpansionKey_isPresent() {
            assertTrue(baseType.getExpansionKey().isPresent());
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("returns new instance")
        public void parseConfig_returnsNewInstance() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(false);

            TameAnimalObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no entities key produces empty filter that accepts any entity")
        public void parseConfig_noEntitiesKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(false);

            TameAnimalObjectiveType parsed = baseType.parseConfig(section);

            EntityTameEvent event = createMockTameEvent(EntityType.PARROT);
            TameAnimalQuestContext context = new TameAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("entities key with entries produces filtering type")
        public void parseConfig_withEntities_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("WOLF"));

            TameAnimalObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            EntityTameEvent matchEvent = createMockTameEvent(EntityType.WOLF);
            assertEquals(1, parsed.processProgress(instance, new TameAnimalQuestContext(matchEvent)));

            EntityTameEvent noMatchEvent = createMockTameEvent(EntityType.CAT);
            assertEquals(0, parsed.processProgress(instance, new TameAnimalQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("entities key with empty list produces empty filter")
        public void parseConfig_emptyEntitiesList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of());

            TameAnimalObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            EntityTameEvent event = createMockTameEvent(EntityType.WOLF);
            assertEquals(1, parsed.processProgress(instance, new TameAnimalQuestContext(event)));
        }
    }

    private EntityTameEvent createMockTameEvent(EntityType entityType) {
        EntityTameEvent event = mock(EntityTameEvent.class);
        LivingEntity entity = mock(LivingEntity.class);
        when(entity.getType()).thenReturn(entityType);
        when(event.getEntity()).thenReturn(entity);
        return event;
    }
}
