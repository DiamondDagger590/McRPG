package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.util.item.CustomEntityWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityBreedEvent;
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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("BreedAnimalObjectiveType")
public class BreedAnimalObjectiveTypeTest extends McRPGBaseTest {

    private BreedAnimalObjectiveType type;
    private final QuestObjectiveInstance mockInstance = mock(QuestObjectiveInstance.class);

    @BeforeEach
    public void setup() {
        type = new BreedAnimalObjectiveType();
    }

    @Nested
    @DisplayName("canProcess")
    class CanProcess {

        @Test
        @DisplayName("returns true for BreedAnimalQuestContext")
        public void canProcess_returnsTrue_forBreedAnimalContext() {
            EntityBreedEvent mockEvent = mock(EntityBreedEvent.class);
            LivingEntity mockEntity = mock(LivingEntity.class);
            when(mockEvent.getEntity()).thenReturn(mockEntity);
            BreedAnimalQuestContext context = new BreedAnimalQuestContext(mockEvent);
            assertTrue(type.canProcess(context));
        }

        @Test
        @DisplayName("returns false for generic context")
        public void canProcess_returnsFalse_forOtherContext() {
            QuestObjectiveProgressContext context = mock(QuestObjectiveProgressContext.class);
            assertFalse(type.canProcess(context));
        }
    }

    @Nested
    @DisplayName("identity")
    class Identity {

        @Test
        @DisplayName("key returns breed_animal")
        public void getKey_returnsBreedAnimalKey() {
            assertEquals(BreedAnimalObjectiveType.KEY, type.getKey());
        }

        @Test
        @DisplayName("expansion key is McRPGExpansion")
        public void getExpansionKey_returnsMcRPGExpansionKey() {
            assertTrue(type.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
        }
    }

    @Nested
    @DisplayName("parseConfig")
    class ParseConfig {

        @Test
        @DisplayName("returns a new instance")
        public void parseConfig_returnsNewInstance() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(false);
            BreedAnimalObjectiveType parsed = type.parseConfig(section);
            assertNotSame(type, parsed);
        }

        @Test
        @DisplayName("no entities key produces empty filter")
        public void parseConfig_noEntitiesKey_producesEmptyFilter() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(false);
            BreedAnimalObjectiveType parsed = type.parseConfig(section);

            EntityBreedEvent event = createMockBreedEvent(EntityType.COW);
            assertEquals(1, parsed.processProgress(mockInstance, new BreedAnimalQuestContext(event)));
        }

        @Test
        @DisplayName("entities key with entries produces filtering type")
        public void parseConfig_withEntities_producesFilteringType() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("COW"));
            BreedAnimalObjectiveType parsed = type.parseConfig(section);

            EntityBreedEvent matchEvent = createMockBreedEvent(EntityType.COW);
            assertEquals(1, parsed.processProgress(mockInstance, new BreedAnimalQuestContext(matchEvent)));

            EntityBreedEvent noMatchEvent = createMockBreedEvent(EntityType.PIG);
            assertEquals(0, parsed.processProgress(mockInstance, new BreedAnimalQuestContext(noMatchEvent)));
        }

        @Test
        @DisplayName("parsed instance preserves key")
        public void parseConfig_preservesKey() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(false);
            BreedAnimalObjectiveType parsed = type.parseConfig(section);
            assertEquals(BreedAnimalObjectiveType.KEY, parsed.getKey());
        }
    }

    @Nested
    @DisplayName("processProgress")
    class ProcessProgress {

        @Test
        @DisplayName("wrong context type returns 0")
        public void processProgress_returnsZero_forWrongContextType() {
            QuestObjectiveProgressContext wrongContext = mock(QuestObjectiveProgressContext.class);
            assertEquals(0, type.processProgress(mockInstance, wrongContext));
        }

        @Test
        @DisplayName("unconfigured type accepts any entity")
        public void processProgress_returnsOne_whenNoFilter() {
            EntityBreedEvent event = createMockBreedEvent(EntityType.SHEEP);
            BreedAnimalQuestContext context = new BreedAnimalQuestContext(event);
            assertEquals(1, type.processProgress(mockInstance, context));
        }

        @Test
        @DisplayName("configured type returns 1 for matching entity")
        public void processProgress_returnsOne_forMatchingEntity() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("COW", "SHEEP"));
            BreedAnimalObjectiveType configured = type.parseConfig(section);

            EntityBreedEvent event = createMockBreedEvent(EntityType.SHEEP);
            assertEquals(1, configured.processProgress(mockInstance, new BreedAnimalQuestContext(event)));
        }

        @Test
        @DisplayName("configured type returns 0 for non-matching entity")
        public void processProgress_returnsZero_forNonMatchingEntity() {
            Section section = mock(Section.class);
            when(section.contains("entities")).thenReturn(true);
            when(section.getStringList("entities")).thenReturn(List.of("COW"));
            BreedAnimalObjectiveType configured = type.parseConfig(section);

            EntityBreedEvent event = createMockBreedEvent(EntityType.PIG);
            assertEquals(0, configured.processProgress(mockInstance, new BreedAnimalQuestContext(event)));
        }
    }

    private EntityBreedEvent createMockBreedEvent(EntityType childType) {
        EntityBreedEvent event = mock(EntityBreedEvent.class);
        LivingEntity child = mock(LivingEntity.class);
        when(child.getType()).thenReturn(childType);
        when(event.getEntity()).thenReturn(child);
        return event;
    }
}
