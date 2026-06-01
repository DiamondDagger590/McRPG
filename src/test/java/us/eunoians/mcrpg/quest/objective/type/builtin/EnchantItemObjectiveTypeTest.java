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

public class EnchantItemObjectiveTypeTest extends McRPGBaseTest {

    private EnchantItemObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new EnchantItemObjectiveType();
    }

    @DisplayName("Given a EnchantItemQuestContext, when calling canProcess, then it returns true")
    @Test
    public void canProcess_returnsTrue_forEnchantItemContext() {
        org.bukkit.event.enchantment.EnchantItemEvent mockEvent =
                org.mockito.Mockito.mock(org.bukkit.event.enchantment.EnchantItemEvent.class);
        EnchantItemQuestContext context = new EnchantItemQuestContext(mockEvent);
        assertTrue(type.canProcess(context));
    }

    @DisplayName("Given a non-EnchantItem context, when calling canProcess, then it returns false")
    @Test
    public void canProcess_returnsFalse_forOtherContext() {
        QuestObjectiveProgressContext context = org.mockito.Mockito.mock(QuestObjectiveProgressContext.class);
        assertFalse(type.canProcess(context));
    }

    @DisplayName("Given the type, when calling getKey, then it returns the enchant_item key")
    @Test
    public void getKey_returnsEnchantItemKey() {
        assertEquals(EnchantItemObjectiveType.KEY, type.getKey());
    }

    @DisplayName("Given the type, when calling getExpansionKey, then it returns McRPGExpansion key")
    @Test
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }
}
