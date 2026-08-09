package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerShearEntityEvent;
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

@DisplayName("ShearEntityObjectiveType — extended coverage")
public class ShearEntityObjectiveTypeCoverageTest extends McRPGBaseTest {

    private ShearEntityObjectiveType baseType;

    @BeforeEach
    public void setup() {
        baseType = new ShearEntityObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for ShearEntityQuestContext")
        public void canProcess_returnsTrue_forShearEntityContext() {
            PlayerShearEntityEvent mockEvent = createMockShearEvent(EntityType.SHEEP);
            ShearEntityQuestContext context = new ShearEntityQuestContext(mockEvent);
            assertTrue(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic mock context")
        public void canProcess_returnsFalse_forGenericContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(baseType.canProcess(context));
        }

        @Test
        @DisplayName("returns false for TameAnimalQuestContext")
        public void canProcess_returnsFalse_forTameAnimalContext() {
            TameAnimalQuestContext context = mock(TameAnimalQuestContext.class);
            assertFalse(baseType.canProcess(context));
        }
    }

    @Nested
    @DisplayName("processProgress — empty entity filter (base type)")
    class ProcessProgressEmptyFilter {

        @Test
        @DisplayName("returns 1 for any entity when no filter is set")
        public void processProgress_returnsOne_whenNoFilter() {
            PlayerShearEntityEvent event = createMockShearEvent(EntityType.SHEEP);
            ShearEntityQuestContext context = new ShearEntityQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 1 for mooshroom when no filter is set")
        public void processProgress_returnsOne_forMooshroom_whenNoFilter() {
            PlayerShearEntityEvent event = createMockShearEvent(EntityType.MOOSHROOM);
            ShearEntityQuestContext context = new ShearEntityQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = baseType.processProgress(instance, context);
            assertEquals(1, progress);
        }
    }

    @Nested
    @DisplayName("processProgress — with entity filter via parseConfig")
    class ProcessProgressWithFilter {

        private ShearEntityObjectiveType filteredType;

        @BeforeEach
        public void setupFilteredType() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("SHEEP", "MOOSHROOM"));
            filteredType = baseType.parseConfig(section);
        }

        @Test
        @DisplayName("returns 1 for matching entity")
        public void processProgress_returnsOne_forMatchingEntity() {
            PlayerShearEntityEvent event = createMockShearEvent(EntityType.SHEEP);
            ShearEntityQuestContext context = new ShearEntityQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(1, progress);
        }

        @Test
        @DisplayName("returns 0 for non-matching entity")
        public void processProgress_returnsZero_forNonMatchingEntity() {
            PlayerShearEntityEvent event = createMockShearEvent(EntityType.SNOW_GOLEM);
            ShearEntityQuestContext context = new ShearEntityQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            long progress = filteredType.processProgress(instance, context);
            assertEquals(0, progress);
        }

        @Test
        @DisplayName("returns 1 for second matching entity in filter")
        public void processProgress_returnsOne_forSecondMatchingEntity() {
            PlayerShearEntityEvent event = createMockShearEvent(EntityType.MOOSHROOM);
            ShearEntityQuestContext context = new ShearEntityQuestContext(event);
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
        @DisplayName("key value is shear_entity")
        public void getKey_valueIsShearEntity() {
            assertEquals("shear_entity", baseType.getKey().getKey());
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

            ShearEntityObjectiveType parsed = baseType.parseConfig(section);
            assertNotSame(baseType, parsed);
        }

        @Test
        @DisplayName("no entities key produces empty filter that accepts any entity")
        public void parseConfig_noEntitiesKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(false);

            ShearEntityObjectiveType parsed = baseType.parseConfig(section);

            PlayerShearEntityEvent event = createMockShearEvent(EntityType.SNOW_GOLEM);
            ShearEntityQuestContext context = new ShearEntityQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            assertEquals(1, parsed.processProgress(instance, context));
        }

        @Test
        @DisplayName("entities key with entries produces filtering type")
        public void parseConfig_withEntities_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("SHEEP"));

            ShearEntityObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerShearEntityEvent matchEvent = createMockShearEvent(EntityType.SHEEP);
            assertEquals(1, parsed.processProgress(instance, new ShearEntityQuestContext(matchEvent)));

            PlayerShearEntityEvent noMatchEvent = createMockShearEvent(EntityType.MOOSHROOM);
            assertEquals(0, parsed.processProgress(instance, new ShearEntityQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("entities key with empty list produces empty filter")
        public void parseConfig_emptyEntitiesList_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of());

            ShearEntityObjectiveType parsed = baseType.parseConfig(section);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);

            PlayerShearEntityEvent event = createMockShearEvent(EntityType.SHEEP);
            assertEquals(1, parsed.processProgress(instance, new ShearEntityQuestContext(event)));
        }
    }

    private PlayerShearEntityEvent createMockShearEvent(EntityType entityType) {
        PlayerShearEntityEvent event = mock(PlayerShearEntityEvent.class);
        Entity entity = mock(Entity.class);
        when(entity.getType()).thenReturn(entityType);
        when(event.getEntity()).thenReturn(entity);
        return event;
    }
}
