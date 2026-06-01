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

public class AnvilRepairObjectiveTypeTest extends McRPGBaseTest {

    private AnvilRepairObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new AnvilRepairObjectiveType();
    }

    @DisplayName("Given a AnvilRepairQuestContext, when calling canProcess, then it returns true")
    @Test
    public void canProcess_returnsTrue_forAnvilRepairContext() {
        org.bukkit.event.inventory.InventoryClickEvent mockEvent =
                org.mockito.Mockito.mock(org.bukkit.event.inventory.InventoryClickEvent.class);
        AnvilRepairQuestContext context = new AnvilRepairQuestContext(mockEvent);
        assertTrue(type.canProcess(context));
    }

    @DisplayName("Given a non-AnvilRepair context, when calling canProcess, then it returns false")
    @Test
    public void canProcess_returnsFalse_forOtherContext() {
        QuestObjectiveProgressContext context = org.mockito.Mockito.mock(QuestObjectiveProgressContext.class);
        assertFalse(type.canProcess(context));
    }

    @DisplayName("Given the type, when calling getKey, then it returns the anvil_repair key")
    @Test
    public void getKey_returnsAnvilRepairKey() {
        assertEquals(AnvilRepairObjectiveType.KEY, type.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }
}
