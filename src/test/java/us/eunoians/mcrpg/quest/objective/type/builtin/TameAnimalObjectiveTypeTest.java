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

public class TameAnimalObjectiveTypeTest extends McRPGBaseTest {

    private TameAnimalObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new TameAnimalObjectiveType();
    }

    @DisplayName("Given a TameAnimalQuestContext, when calling canProcess, then it returns true")
    @Test
    public void canProcess_returnsTrue_forTameAnimalContext() {
        org.bukkit.event.entity.EntityTameEvent mockEvent =
                org.mockito.Mockito.mock(org.bukkit.event.entity.EntityTameEvent.class);
        LivingEntity mockEntity = org.mockito.Mockito.mock(LivingEntity.class);
        org.mockito.Mockito.when(mockEvent.getEntity()).thenReturn(mockEntity);
        TameAnimalQuestContext context = new TameAnimalQuestContext(mockEvent);
        assertTrue(type.canProcess(context));
    }

    @DisplayName("Given a non-TameAnimal context, when calling canProcess, then it returns false")
    @Test
    public void canProcess_returnsFalse_forOtherContext() {
        QuestObjectiveProgressContext context = org.mockito.Mockito.mock(QuestObjectiveProgressContext.class);
        assertFalse(type.canProcess(context));
    }

    @DisplayName("Given the type, when calling getKey, then it returns the tame_animal key")
    @Test
    public void getKey_returnsTameAnimalKey() {
        assertEquals(TameAnimalObjectiveType.KEY, type.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }
}
