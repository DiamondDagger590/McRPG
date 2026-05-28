package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.event.skill.SkillGainLevelEvent;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SkillLevelQuestContextTest extends McRPGBaseTest {

    @DisplayName("getSkillKey returns the skill key from the event")
    @Test
    public void getSkillKey_returnsSkillKey() {
        NamespacedKey skillKey = new NamespacedKey("mcrpg", "swords");
        SkillHolder holder = mock(SkillHolder.class);
        when(holder.getUUID()).thenReturn(UUID.randomUUID());
        SkillGainLevelEvent event = new SkillGainLevelEvent(holder, skillKey, 1);
        SkillLevelQuestContext context = new SkillLevelQuestContext(event);
        assertEquals(skillKey, context.getSkillKey());
    }

    @DisplayName("getLevelsGained returns the levels gained from the event")
    @Test
    public void getLevelsGained_returnsLevels() {
        NamespacedKey skillKey = new NamespacedKey("mcrpg", "swords");
        SkillHolder holder = mock(SkillHolder.class);
        when(holder.getUUID()).thenReturn(UUID.randomUUID());
        SkillGainLevelEvent event = new SkillGainLevelEvent(holder, skillKey, 3);
        SkillLevelQuestContext context = new SkillLevelQuestContext(event);
        assertEquals(3, context.getLevelsGained());
    }

    @DisplayName("getNewLevel returns skill holder current level")
    @Test
    public void getNewLevel_returnsCurrentLevelFromHolder() {
        NamespacedKey skillKey = new NamespacedKey("mcrpg", "swords");
        SkillHolder holder = mock(SkillHolder.class);
        when(holder.getUUID()).thenReturn(UUID.randomUUID());
        SkillHolder.SkillHolderData skillData = mock(SkillHolder.SkillHolderData.class);
        when(skillData.getCurrentLevel()).thenReturn(15);
        when(holder.getSkillHolderData(skillKey)).thenReturn(Optional.of(skillData));
        SkillGainLevelEvent event = new SkillGainLevelEvent(holder, skillKey, 2);
        SkillLevelQuestContext context = new SkillLevelQuestContext(event);
        assertEquals(15, context.getNewLevel());
    }

    @DisplayName("getNewLevel returns 0 when skill data not available")
    @Test
    public void getNewLevel_returnsZero_whenSkillDataAbsent() {
        NamespacedKey skillKey = new NamespacedKey("mcrpg", "swords");
        SkillHolder holder = mock(SkillHolder.class);
        when(holder.getUUID()).thenReturn(UUID.randomUUID());
        when(holder.getSkillHolderData(skillKey)).thenReturn(Optional.empty());
        SkillGainLevelEvent event = new SkillGainLevelEvent(holder, skillKey, 1);
        SkillLevelQuestContext context = new SkillLevelQuestContext(event);
        assertEquals(0, context.getNewLevel());
    }

    @DisplayName("getPlayerUUID returns the skill holder UUID")
    @Test
    public void getPlayerUUID_returnsHolderUUID() {
        NamespacedKey skillKey = new NamespacedKey("mcrpg", "swords");
        UUID uuid = UUID.randomUUID();
        SkillHolder holder = mock(SkillHolder.class);
        when(holder.getUUID()).thenReturn(uuid);
        SkillGainLevelEvent event = new SkillGainLevelEvent(holder, skillKey, 1);
        SkillLevelQuestContext context = new SkillLevelQuestContext(event);
        assertEquals(uuid, context.getPlayerUUID());
    }
}
