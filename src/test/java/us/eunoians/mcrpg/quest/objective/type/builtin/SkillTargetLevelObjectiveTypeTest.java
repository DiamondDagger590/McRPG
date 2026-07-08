package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SkillTargetLevelObjectiveTypeTest extends McRPGBaseTest {

    private SkillTargetLevelObjectiveType type;

    @BeforeEach
    public void setup() {
        McRPGPlayerManager mockPlayerManager = mock(McRPGPlayerManager.class);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockPlayerManager);
        when(mockPlayerManager.getPlayer(any(UUID.class))).thenReturn(Optional.empty());
        type = new SkillTargetLevelObjectiveType();
    }

    @Test
    @DisplayName("Given the type, when getKey is called, then it returns skill_target_level")
    public void getKey_returnsExpectedKey() {
        assertEquals(SkillTargetLevelObjectiveType.KEY, type.getKey());
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
    @DisplayName("Given new level below target, when processProgress is called, then it returns 0")
    public void processProgress_belowTarget_returnsZero() {
        Section section = mock(Section.class);
        when(section.contains("skill")).thenReturn(false);
        when(section.contains("target-level")).thenReturn(true);
        when(section.getInt("target-level")).thenReturn(10);
        SkillTargetLevelObjectiveType configured = type.parseConfig(section);

        SkillGainLevelEvent event = mock(SkillGainLevelEvent.class);
        when(event.getSkillKey()).thenReturn(new NamespacedKey("mcrpg", "mining"));
        SkillHolder skillHolder = mock(SkillHolder.class);
        when(event.getSkillHolder()).thenReturn(skillHolder);
        when(skillHolder.getSkillHolderData(any(NamespacedKey.class))).thenReturn(Optional.empty());
        SkillLevelQuestContext context = new SkillLevelQuestContext(event);
        assertEquals(0, configured.processProgress(mock(QuestObjectiveInstance.class), context));
    }

    @Test
    @DisplayName("Given new level at target, when processProgress is called, then it returns 1")
    public void processProgress_atTarget_returnsOne() {
        Section section = mock(Section.class);
        when(section.contains("skill")).thenReturn(false);
        when(section.contains("target-level")).thenReturn(true);
        when(section.getInt("target-level")).thenReturn(10);
        SkillTargetLevelObjectiveType configured = type.parseConfig(section);

        NamespacedKey skillKey = new NamespacedKey("mcrpg", "mining");
        SkillGainLevelEvent event = mock(SkillGainLevelEvent.class);
        when(event.getSkillKey()).thenReturn(skillKey);
        SkillHolder skillHolder = mock(SkillHolder.class);
        when(event.getSkillHolder()).thenReturn(skillHolder);
        SkillHolder.SkillHolderData skillData = mock(SkillHolder.SkillHolderData.class);
        when(skillData.getCurrentLevel()).thenReturn(10);
        when(skillHolder.getSkillHolderData(skillKey)).thenReturn(Optional.of(skillData));

        SkillLevelQuestContext context = new SkillLevelQuestContext(event);
        assertEquals(1, configured.processProgress(mock(QuestObjectiveInstance.class), context));
    }

    @Test
    @DisplayName("Given a wrong context type, when canProcess is called, then it returns false")
    public void canProcess_wrongContextType_returnsFalse() {
        assertFalse(type.canProcess(mock(QuestObjectiveProgressContext.class)));
    }

    @Test
    @DisplayName("Given a skill filter mismatch, when processProgress is called, then it returns 0")
    public void processProgress_skillFilterMismatch_returnsZero() {
        Section section = mock(Section.class);
        when(section.contains("skill")).thenReturn(true);
        when(section.getString("skill")).thenReturn("mcrpg:swords");
        when(section.contains("target-level")).thenReturn(true);
        when(section.getInt("target-level")).thenReturn(5);
        SkillTargetLevelObjectiveType configured = type.parseConfig(section);

        NamespacedKey miningKey = new NamespacedKey("mcrpg", "mining");
        SkillGainLevelEvent event = mock(SkillGainLevelEvent.class);
        when(event.getSkillKey()).thenReturn(miningKey);
        SkillHolder skillHolder = mock(SkillHolder.class);
        when(event.getSkillHolder()).thenReturn(skillHolder);
        SkillHolder.SkillHolderData data = mock(SkillHolder.SkillHolderData.class);
        when(data.getCurrentLevel()).thenReturn(10);
        when(skillHolder.getSkillHolderData(miningKey)).thenReturn(Optional.of(data));
        assertEquals(0, configured.processProgress(mock(QuestObjectiveInstance.class), new SkillLevelQuestContext(event)));
    }

    @Test
    @DisplayName("Given an unknown player UUID, when checkAutoComplete is called, then it returns empty because no player is loaded")
    public void checkAutoComplete_returnsEmpty_whenPlayerNotLoaded() {
        assertTrue(type.checkAutoComplete(UUID.randomUUID()).isEmpty());
    }

    @Test
    @DisplayName("Given new level above target, when processProgress is called, then it returns 1")
    public void processProgress_aboveTarget_returnsOne() {
        Section section = mock(Section.class);
        when(section.contains("skill")).thenReturn(false);
        when(section.contains("target-level")).thenReturn(true);
        when(section.getInt("target-level")).thenReturn(10);
        SkillTargetLevelObjectiveType configured = type.parseConfig(section);

        NamespacedKey skillKey = new NamespacedKey("mcrpg", "mining");
        SkillGainLevelEvent event = mock(SkillGainLevelEvent.class);
        when(event.getSkillKey()).thenReturn(skillKey);
        SkillHolder skillHolder = mock(SkillHolder.class);
        when(event.getSkillHolder()).thenReturn(skillHolder);
        SkillHolder.SkillHolderData skillData = mock(SkillHolder.SkillHolderData.class);
        when(skillData.getCurrentLevel()).thenReturn(15);
        when(skillHolder.getSkillHolderData(skillKey)).thenReturn(Optional.of(skillData));

        SkillLevelQuestContext context = new SkillLevelQuestContext(event);
        assertEquals(1, configured.processProgress(mock(QuestObjectiveInstance.class), context));
    }
}
