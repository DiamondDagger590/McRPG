package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShearEntityObjectiveTypeTest extends McRPGBaseTest {

    private ShearEntityObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new ShearEntityObjectiveType();
    }

    @DisplayName("Given a ShearEntityQuestContext, when calling canProcess, then it returns true")
    @Test
    public void canProcess_returnsTrue_forShearEntityContext() {
        org.bukkit.event.player.PlayerShearEntityEvent mockEvent =
                org.mockito.Mockito.mock(org.bukkit.event.player.PlayerShearEntityEvent.class);
        Entity mockEntity = org.mockito.Mockito.mock(Entity.class);
        org.mockito.Mockito.when(mockEvent.getEntity()).thenReturn(mockEntity);
        ShearEntityQuestContext context = new ShearEntityQuestContext(mockEvent);
        assertTrue(type.canProcess(context));
    }

    @DisplayName("Given a non-ShearEntity context, when calling canProcess, then it returns false")
    @Test
    public void canProcess_returnsFalse_forOtherContext() {
        QuestObjectiveProgressContext context = org.mockito.Mockito.mock(QuestObjectiveProgressContext.class);
        assertFalse(type.canProcess(context));
    }

    @DisplayName("Given the type, when calling getKey, then it returns the shear_entity key")
    @Test
    public void getKey_returnsShearEntityKey() {
        assertEquals(ShearEntityObjectiveType.KEY, type.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }
}
