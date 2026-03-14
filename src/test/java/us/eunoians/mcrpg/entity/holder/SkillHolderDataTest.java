package us.eunoians.mcrpg.entity.holder;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.context.McRPGGainReason;
import us.eunoians.mcrpg.skill.impl.MockSkill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class SkillHolderDataTest extends McRPGBaseTest {

    private MockSkill skill;

    @BeforeEach
    public void setup() {
        SkillRegistry skillRegistry = new SkillRegistry();
        RegistryAccess.registryAccess().register(skillRegistry);
        skill = spy(MockSkill.class);
        when(skill.getMaxLevel()).thenReturn(5);
        when(skill.getLevelUpEquation()).thenReturn(new Parser("100"));
        skillRegistry.register(skill);
    }

    @DisplayName("Given a skill at max level, when adding experience, then totalExperience still increases")
    @Test
    public void addExperience_atMaxLevel_stillAccumulatesXP(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 5);
        SkillHolder.SkillHolderData data = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();
        int totalBefore = data.getTotalExperience();
        assertEquals(5, data.getCurrentLevel());

        data.addExperience(200, McRPGGainReason.OTHER);

        assertEquals(totalBefore + 200, data.getTotalExperience());
        assertEquals(5, data.getCurrentLevel());
    }

    @DisplayName("Given a skill at max level, when adding experience, then getCurrentLevel() remains clamped to maxLevel")
    @Test
    public void addExperience_atMaxLevel_getCurrentLevelClamped(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 5);
        SkillHolder.SkillHolderData data = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        // Add enough XP that would represent many levels past max
        data.addExperience(10000, McRPGGainReason.OTHER);

        assertEquals(5, data.getCurrentLevel());
    }

    @DisplayName("Given a skill at max level, when adding experience, then getCurrentExperience() returns 0")
    @Test
    public void getCurrentExperience_atMaxLevel_returnsZero(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 5);
        SkillHolder.SkillHolderData data = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        data.addExperience(50, McRPGGainReason.OTHER);

        assertEquals(0, data.getCurrentExperience());
    }

    @DisplayName("Given a skill at max level, when adding experience, then addExperience returns 0 (no leftover)")
    @Test
    public void addExperience_atMaxLevel_returnsZero(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 5);
        SkillHolder.SkillHolderData data = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        int result = data.addExperience(500, McRPGGainReason.OTHER);

        assertEquals(0, result);
    }

    @DisplayName("Given a skill crossing max level, when adding experience, then level-up fires to max and all XP is consumed")
    @Test
    public void addExperience_crossingMaxLevel_allXpConsumed(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 4);
        SkillHolder.SkillHolderData data = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        assertEquals(4, data.getCurrentLevel());

        // Add enough XP to cross max (need 100 for level 5, adding 500)
        int result = data.addExperience(500, McRPGGainReason.OTHER);

        assertEquals(5, data.getCurrentLevel());
        assertEquals(0, result);
        // Total experience should be level 4 total + 500
        int expectedTotal = data.calculateTotalExperienceForLevel(4) + 500;
        assertEquals(expectedTotal, data.getTotalExperience());
    }

    @DisplayName("Given maxLevel is raised after overflow XP accumulated, when recalculating, then getCurrentLevel() reflects the higher level")
    @Test
    public void maxLevelIncrease_retroactiveLevel(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 5);
        SkillHolder.SkillHolderData data = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        // Accumulate overflow XP (enough for 3 more levels at 100 each)
        data.addExperience(300, McRPGGainReason.OTHER);
        assertEquals(5, data.getCurrentLevel());

        // Raise max level
        when(skill.getMaxLevel()).thenReturn(10);
        data.invalidateLevelCache();

        // Should now reflect the higher level from accumulated XP
        assertEquals(8, data.getCurrentLevel());
    }

    @DisplayName("Given a skill below max level, when adding experience that doesn't reach max, then behavior is unchanged")
    @Test
    public void addExperience_belowMaxLevel_normalBehavior(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 1);
        SkillHolder.SkillHolderData data = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        assertEquals(1, data.getCurrentLevel());
        assertEquals(0, data.getCurrentExperience());

        data.addExperience(50, McRPGGainReason.OTHER);

        assertEquals(1, data.getCurrentLevel());
        assertEquals(50, data.getCurrentExperience());
    }

    @DisplayName("Given a skill below max level, when adding enough experience to level up, then level increases correctly")
    @Test
    public void addExperience_levelUp_normalBehavior(@NotNull McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(skill, 1);
        SkillHolder.SkillHolderData data = mcRPGPlayer.asSkillHolder().getSkillHolderData(skill).get();

        data.addExperience(150, McRPGGainReason.OTHER);

        assertEquals(2, data.getCurrentLevel());
        assertEquals(50, data.getCurrentExperience());
    }
}
