package us.eunoians.mcrpg.quest.objective.type.builtin;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SkillLevelUpObjectiveTypeTest extends McRPGBaseTest {

    private SkillLevelUpObjectiveType type;

    @BeforeEach
    public void setup() {
        type = new SkillLevelUpObjectiveType();
    }

    @Test
    @DisplayName("Given the type, when getKey is called, then it returns skill_level_up")
    public void getKey_returnsExpectedKey() {
        assertEquals(SkillLevelUpObjectiveType.KEY, type.getKey());
    }

    @Test
    @DisplayName("Given the type, when getExpansionKey is called, then it returns McRPG expansion key")
    public void getExpansionKey_returnsMcRPGExpansionKey() {
        assertTrue(type.getExpansionKey().isPresent());
        assertEquals(McRPGExpansion.EXPANSION_KEY, type.getExpansionKey().get());
    }

    @Test
    @DisplayName("Given SkillLevelQuestContext, when canProcess is called, then it returns true")
    public void canProcess_correctContextType_returnsTrue() {
        assertTrue(type.canProcess(mock(SkillLevelQuestContext.class)));
    }

    @Test
    @DisplayName("Given BlockBreakQuestContext, when canProcess is called, then it returns false")
    public void canProcess_wrongContextType_returnsFalse() {
        assertFalse(type.canProcess(mock(BlockBreakQuestContext.class)));
    }

    @Test
    @DisplayName("Given matching skill level-up, when processProgress is called, then it returns levels gained")
    public void processProgress_matchingSkill_returnsLevelsGained() {
        SkillGainLevelEvent event = mock(SkillGainLevelEvent.class);
        when(event.getSkillKey()).thenReturn(new NamespacedKey("mcrpg", "mining"));
        when(event.getLevels()).thenReturn(2);
        SkillLevelQuestContext context = new SkillLevelQuestContext(event);
        QuestObjectiveInstance instance = mock(QuestObjectiveInstance.class);
        assertEquals(2, type.processProgress(instance, context));
    }

    @Test
    @DisplayName("Given skill filter mismatch, when processProgress is called, then it returns 0")
    public void processProgress_skillFilterMismatch_returnsZero() {
        Section section = mock(Section.class);
        when(section.contains("skill")).thenReturn(true);
        when(section.getString("skill")).thenReturn("mcrpg:mining");
        when(section.contains("levels")).thenReturn(false);
        SkillLevelUpObjectiveType configured = type.parseConfig(section);

        SkillGainLevelEvent event = mock(SkillGainLevelEvent.class);
        when(event.getSkillKey()).thenReturn(new NamespacedKey("mcrpg", "swords"));
        when(event.getLevels()).thenReturn(1);
        assertEquals(0, configured.processProgress(mock(QuestObjectiveInstance.class), new SkillLevelQuestContext(event)));
    }

    @Test
    @DisplayName("Given parseConfig with no skill filter, when processProgress runs, then any skill matches")
    public void parseConfig_noSkillFilter_matchesAnySkill() {
        Section section = mock(Section.class);
        when(section.contains("skill")).thenReturn(false);
        when(section.contains("levels")).thenReturn(false);
        SkillLevelUpObjectiveType configured = type.parseConfig(section);

        SkillGainLevelEvent event = mock(SkillGainLevelEvent.class);
        when(event.getSkillKey()).thenReturn(new NamespacedKey("mcrpg", "herbalism"));
        when(event.getLevels()).thenReturn(1);
        assertEquals(1, configured.processProgress(mock(QuestObjectiveInstance.class), new SkillLevelQuestContext(event)));
    }

    @Test
    @DisplayName("Given minLevelsPerEvent of 3 and an event with 1 level gained, when processProgress is called, then it returns 0")
    public void processProgress_belowMinLevelsPerEvent_returnsZero() {
        Section section = mock(Section.class);
        when(section.contains("skill")).thenReturn(false);
        when(section.contains("levels")).thenReturn(true);
        when(section.getInt("levels")).thenReturn(3);
        SkillLevelUpObjectiveType configured = type.parseConfig(section);

        SkillGainLevelEvent event = mock(SkillGainLevelEvent.class);
        when(event.getSkillKey()).thenReturn(new NamespacedKey("mcrpg", "mining"));
        when(event.getLevels()).thenReturn(1);
        assertEquals(0, configured.processProgress(mock(QuestObjectiveInstance.class), new SkillLevelQuestContext(event)));
    }
}
