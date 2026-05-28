package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.event.gui.CoreGuiOpenEvent;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GuiOpenObjectiveTypeTest extends McRPGBaseTest {

    private GuiOpenObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new GuiOpenObjectiveType();
    }

    @Test
    @DisplayName("Given the type, when getKey is called, then it returns gui_open")
    public void getKey_returnsExpectedKey() {
        assertEquals(GuiOpenObjectiveType.KEY, type.getKey());
    }

    @Test
    @DisplayName("Given the type, when getExpansionKey is called, then it returns McRPG expansion key")
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @Test
    @DisplayName("Given GuiOpenQuestContext, when canProcess is called, then it returns true")
    public void canProcess_correctContextType_returnsTrue() {
        assertTrue(type.canProcess(mock(GuiOpenQuestContext.class)));
    }

    @Test
    @DisplayName("Given wrong context, when canProcess is called, then it returns false")
    public void canProcess_wrongContextType_returnsFalse() {
        assertFalse(type.canProcess(mock(BlockBreakQuestContext.class)));
    }

    @Test
    @DisplayName("Given matching gui-type config, when processProgress is called, then it returns 1")
    public void processProgress_matchingGuiKey_returnsOne() {
        Section section = mock(Section.class);
        when(section.getString("gui-type", "")).thenReturn("mcrpg:home_gui");
        GuiOpenObjectiveType configured = type.parseConfig(section);

        CoreGuiOpenEvent event = mock(CoreGuiOpenEvent.class);
        when(event.getGuiKey()).thenReturn(Optional.of(new NamespacedKey("mcrpg", "home_gui")));
        assertEquals(1, configured.processProgress(mock(QuestObjectiveInstance.class), new GuiOpenQuestContext(event)));
    }

    @Test
    @DisplayName("Given non-matching gui key, when processProgress is called, then it returns 0")
    public void processProgress_mismatchedGuiKey_returnsZero() {
        Section section = mock(Section.class);
        when(section.getString("gui-type", "")).thenReturn("mcrpg:home_gui");
        GuiOpenObjectiveType configured = type.parseConfig(section);

        CoreGuiOpenEvent event = mock(CoreGuiOpenEvent.class);
        when(event.getGuiKey()).thenReturn(Optional.of(new NamespacedKey("mcrpg", "skill_gui")));
        assertEquals(0, configured.processProgress(mock(QuestObjectiveInstance.class), new GuiOpenQuestContext(event)));
    }
}
