package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DistanceTraveledObjectiveTypeTest extends McRPGBaseTest {

    private DistanceTraveledObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new DistanceTraveledObjectiveType();
    }

    @DisplayName("Given a DistanceTraveledQuestContext, when calling canProcess, then it returns true")
    @Test
    public void canProcess_returnsTrue_forDistanceTraveledContext() {
        org.bukkit.event.player.PlayerMoveEvent mockEvent =
                org.mockito.Mockito.mock(org.bukkit.event.player.PlayerMoveEvent.class);
        DistanceTraveledQuestContext context = new DistanceTraveledQuestContext(mockEvent, 5L);
        assertTrue(type.canProcess(context));
    }

    @DisplayName("Given a non-DistanceTraveled context, when calling canProcess, then it returns false")
    @Test
    public void canProcess_returnsFalse_forOtherContext() {
        QuestObjectiveProgressContext context = org.mockito.Mockito.mock(QuestObjectiveProgressContext.class);
        assertFalse(type.canProcess(context));
    }

    @DisplayName("Given the type, when calling getKey, then it returns the distance_traveled key")
    @Test
    public void getKey_returnsDistanceTraveledKey() {
        assertEquals(DistanceTraveledObjectiveType.KEY, type.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }
}
