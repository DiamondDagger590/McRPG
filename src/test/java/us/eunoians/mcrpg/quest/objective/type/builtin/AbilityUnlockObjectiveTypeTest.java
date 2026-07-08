package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AbilityUnlockObjectiveTypeTest extends McRPGBaseTest {

    private AbilityUnlockObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new AbilityUnlockObjectiveType();
    }

    @Test
    @DisplayName("Given the type, when getKey is called, then it returns ability_unlock")
    public void getKey_returnsExpectedKey() {
        assertEquals(AbilityUnlockObjectiveType.KEY, type.getKey());
    }

    @Test
    @DisplayName("Given the type, when getExpansionKey is called, then it returns McRPG expansion key")
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @Test
    @DisplayName("Given AbilityUnlockQuestContext, when canProcess is called, then it returns true")
    public void canProcess_correctContextType_returnsTrue() {
        assertTrue(type.canProcess(mock(AbilityUnlockQuestContext.class)));
    }

    @Test
    @DisplayName("Given matching ability unlock, when processProgress is called, then it returns 1")
    public void processProgress_matchingAbility_returnsOne() {
        Section section = mock(Section.class);
        when(section.contains("ability")).thenReturn(true);
        when(section.getString("ability")).thenReturn("mcrpg:bleed");
        when(section.contains("ability-type")).thenReturn(false);
        AbilityUnlockObjectiveType configured = type.parseConfig(section);

        UnlockableAbility ability = mock(UnlockableAbility.class);
        when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
        AbilityUnlockEvent event = mock(AbilityUnlockEvent.class);
        when(event.getAbility()).thenReturn(ability);
        assertEquals(1, configured.processProgress(mock(QuestObjectiveInstance.class), new AbilityUnlockQuestContext(event)));
    }
}
