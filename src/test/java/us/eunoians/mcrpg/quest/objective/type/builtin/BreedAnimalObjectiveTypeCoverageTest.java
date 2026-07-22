package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityBreedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BreedAnimalObjectiveTypeCoverageTest extends McRPGBaseTest {

    private BreedAnimalObjectiveType type;

    @BeforeEach
    void setUp() {
        type = new BreedAnimalObjectiveType();
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("no entities key accepts any entity")
        void parseConfig_acceptsAnyEntity_whenNoEntitiesKey() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(false);

            BreedAnimalObjectiveType configured = type.parseConfig(section);

            LivingEntity childEntity = mock(LivingEntity.class);
            when(childEntity.getType()).thenReturn(EntityType.RABBIT);
            EntityBreedEvent event = mock(EntityBreedEvent.class);
            when(event.getEntity()).thenReturn(childEntity);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            BreedAnimalQuestContext context = new BreedAnimalQuestContext(event);
            assertEquals(1L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("parses entities list and restricts matching")
        void parseConfig_restrictsToListedEntities() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("COW", "PIG"));

            BreedAnimalObjectiveType configured = type.parseConfig(section);

            LivingEntity cowEntity = mock(LivingEntity.class);
            when(cowEntity.getType()).thenReturn(EntityType.COW);
            EntityBreedEvent cowEvent = mock(EntityBreedEvent.class);
            when(cowEvent.getEntity()).thenReturn(cowEntity);

            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1L, configured.processProgress(instance, new BreedAnimalQuestContext(cowEvent)));

            LivingEntity wolfEntity = mock(LivingEntity.class);
            when(wolfEntity.getType()).thenReturn(EntityType.WOLF);
            EntityBreedEvent wolfEvent = mock(EntityBreedEvent.class);
            when(wolfEvent.getEntity()).thenReturn(wolfEntity);

            assertEquals(0L, configured.processProgress(instance, new BreedAnimalQuestContext(wolfEvent)));
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("returns 0 for wrong context type")
        void processProgress_returnsZero_whenWrongContextType() {
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0L, type.processProgress(instance, wrongContext));
        }

        @Test
        @DisplayName("unconfigured type returns 1 for any breed event")
        void processProgress_returnsOne_whenUnconfigured() {
            LivingEntity childEntity = mock(LivingEntity.class);
            when(childEntity.getType()).thenReturn(EntityType.COW);

            EntityBreedEvent event = mock(EntityBreedEvent.class);
            when(event.getEntity()).thenReturn(childEntity);

            BreedAnimalQuestContext context = new BreedAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1L, type.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 1 when entity matches filter")
        void processProgress_returnsOne_whenEntityMatchesFilter() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("COW"));
            BreedAnimalObjectiveType configured = type.parseConfig(section);

            LivingEntity childEntity = mock(LivingEntity.class);
            when(childEntity.getType()).thenReturn(EntityType.COW);

            EntityBreedEvent event = mock(EntityBreedEvent.class);
            when(event.getEntity()).thenReturn(childEntity);

            BreedAnimalQuestContext context = new BreedAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when entity does not match filter")
        void processProgress_returnsZero_whenEntityDoesNotMatchFilter() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("COW"));
            BreedAnimalObjectiveType configured = type.parseConfig(section);

            LivingEntity childEntity = mock(LivingEntity.class);
            when(childEntity.getType()).thenReturn(EntityType.PIG);

            EntityBreedEvent event = mock(EntityBreedEvent.class);
            when(event.getEntity()).thenReturn(childEntity);

            BreedAnimalQuestContext context = new BreedAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 1 when one of multiple entities matches")
        void processProgress_returnsOne_whenOneOfMultipleEntitiesMatches() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("COW", "SHEEP", "PIG"));
            BreedAnimalObjectiveType configured = type.parseConfig(section);

            LivingEntity childEntity = mock(LivingEntity.class);
            when(childEntity.getType()).thenReturn(EntityType.SHEEP);

            EntityBreedEvent event = mock(EntityBreedEvent.class);
            when(event.getEntity()).thenReturn(childEntity);

            BreedAnimalQuestContext context = new BreedAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("returns 0 when entity type matches none in multi-entity filter")
        void processProgress_returnsZero_whenEntityMatchesNoneInMultiFilter() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("COW", "SHEEP"));
            BreedAnimalObjectiveType configured = type.parseConfig(section);

            LivingEntity childEntity = mock(LivingEntity.class);
            when(childEntity.getType()).thenReturn(EntityType.CHICKEN);

            EntityBreedEvent event = mock(EntityBreedEvent.class);
            when(event.getEntity()).thenReturn(childEntity);

            BreedAnimalQuestContext context = new BreedAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(0L, configured.processProgress(instance, context));
        }

        @Test
        @DisplayName("parsed config with no entities key returns 1 for any entity")
        void processProgress_returnsOne_whenParsedWithNoEntitiesKey() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(false);
            BreedAnimalObjectiveType configured = type.parseConfig(section);

            LivingEntity childEntity = mock(LivingEntity.class);
            when(childEntity.getType()).thenReturn(EntityType.WOLF);

            EntityBreedEvent event = mock(EntityBreedEvent.class);
            when(event.getEntity()).thenReturn(childEntity);

            BreedAnimalQuestContext context = new BreedAnimalQuestContext(event);
            QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
            assertEquals(1L, configured.processProgress(instance, context));
        }
    }
}
