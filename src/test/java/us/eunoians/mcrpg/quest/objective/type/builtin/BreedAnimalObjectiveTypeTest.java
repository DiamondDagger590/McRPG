package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.entity.LivingEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BreedAnimalObjectiveTypeTest extends McRPGBaseTest {

    private BreedAnimalObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new BreedAnimalObjectiveType();
    }

    @DisplayName("Given a BreedAnimalQuestContext, when calling canProcess, then it returns true")
    @Test
    public void canProcess_returnsTrue_forBreedAnimalContext() {
        org.bukkit.event.entity.EntityBreedEvent mockEvent =
                org.mockito.Mockito.mock(org.bukkit.event.entity.EntityBreedEvent.class);
        LivingEntity mockEntity = org.mockito.Mockito.mock(LivingEntity.class);
        org.mockito.Mockito.when(mockEvent.getEntity()).thenReturn(mockEntity);
        BreedAnimalQuestContext context = new BreedAnimalQuestContext(mockEvent);
        assertTrue(type.canProcess(context));
    }

    @DisplayName("Given a non-BreedAnimal context, when calling canProcess, then it returns false")
    @Test
    public void canProcess_returnsFalse_forOtherContext() {
        QuestObjectiveProgressContext context = org.mockito.Mockito.mock(QuestObjectiveProgressContext.class);
        assertFalse(type.canProcess(context));
    }

    @DisplayName("Given the type, when calling getKey, then it returns the breed_animal key")
    @Test
    public void getKey_returnsBreedAnimalKey() {
        assertEquals(BreedAnimalObjectiveType.KEY, type.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }
}
