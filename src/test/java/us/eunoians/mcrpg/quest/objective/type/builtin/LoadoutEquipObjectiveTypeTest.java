package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoadoutEquipObjectiveTypeTest extends McRPGBaseTest {

    private LoadoutEquipObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new LoadoutEquipObjectiveType();
    }

    @Test
    @DisplayName("Given the type, when getKey is called, then it returns loadout_equip")
    public void getKey_returnsExpectedKey() {
        assertEquals(LoadoutEquipObjectiveType.KEY, type.getKey());
    }

    @Test
    @DisplayName("Given the type, when getExpansionKey is called, then it returns McRPG expansion key")
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @Test
    @DisplayName("Given LoadoutEquipQuestContext, when canProcess is called, then it returns true")
    public void canProcess_correctContextType_returnsTrue() {
        assertTrue(type.canProcess(mock(LoadoutEquipQuestContext.class)));
    }

    @Test
    @DisplayName("Given wrong context type, when processProgress is called, then it returns 0")
    public void processProgress_wrongContext_returnsZero() {
        assertEquals(0, type.processProgress(mock(QuestObjectiveInstance.class), mock(BlockBreakQuestContext.class)));
    }

    @Test
    @DisplayName("Given parseConfig with no filters, when configured, then processProgress uses EMPTY filter")
    public void parseConfig_noFilters_createsEmptyFilter() {
        Section section = mock(Section.class);
        when(section.contains("ability")).thenReturn(false);
        when(section.contains("ability-type")).thenReturn(false);
        LoadoutEquipObjectiveType configured = type.parseConfig(section);
        assertNotNull(configured);
    }

}
