package us.eunoians.mcrpg.skill;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.mock.skill.MockSkill;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SkillTest {

    private static ServerMock serverMock;
    private static McRPG plugin;
    private static Skill mockedSkill;

    @BeforeAll
    public static void load() {
        serverMock = MockBukkit.mock();
        plugin = MockBukkit.load(McRPG.class);
        mockedSkill = new MockSkill(new NamespacedKey(plugin, "test"));
        plugin.getSkillRegistry().registerSkill(mockedSkill);
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @Test
    public void testSkillGainExperience() {
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(mockedSkill);
        var skillHolderDataOptional = skillHolder.getSkillHolderData(mockedSkill);
        assertTrue(skillHolderDataOptional.isPresent());
        var skillHolderData = skillHolderDataOptional.get();
        assertEquals(skillHolderData.getCurrentExperience(), 0);
        assertEquals(skillHolderData.getCurrentLevel(), 1);

        skillHolderData.addExperience(500);
        assertEquals(skillHolderData.getCurrentExperience(), 500);
        assertEquals(skillHolderData.getCurrentLevel(), 1);
    }

    @Test
    public void testSkillGainLevel() {
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(mockedSkill);
        var skillHolderData = skillHolder.getSkillHolderData(mockedSkill).get();

        skillHolderData.addLevel(5);
        assertEquals(skillHolderData.getCurrentExperience(), 0);
        assertEquals(skillHolderData.getCurrentLevel(), 6);
    }

    @Test
    public void testSkillGainLevelAndResetExperience() {
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(mockedSkill);
        var skillHolderData = skillHolder.getSkillHolderData(mockedSkill).get();

        skillHolderData.addExperience(500);
        assertEquals(skillHolderData.getCurrentExperience(), 500);
        skillHolderData.addLevel(5, true);
        assertEquals(skillHolderData.getCurrentExperience(), 0);
        assertEquals(skillHolderData.getCurrentLevel(), 6);
    }

    @Test
    public void testSkillGainLevelAndDontResetExperience() {
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(mockedSkill);
        var skillHolderData = skillHolder.getSkillHolderData(mockedSkill).get();

        skillHolderData.addExperience(500);
        skillHolderData.addLevel(5, false);
        assertEquals(skillHolderData.getCurrentExperience(), 500);
        assertEquals(skillHolderData.getCurrentLevel(), 6);
    }

    @Test
    public void testMaxSkillLevelEnforced() {
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(mockedSkill);
        var skillHolderData = skillHolder.getSkillHolderData(mockedSkill).get();
        skillHolderData.addLevel(mockedSkill.getMaxLevel() + 1);
        assertEquals(skillHolderData.getCurrentLevel(), mockedSkill.getMaxLevel());
    }

    @Test
    public void testMaxSkillLevelEnforcedAfterExperienceGain() {
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(mockedSkill);
        var skillHolderData = skillHolder.getSkillHolderData(mockedSkill).get();
        skillHolderData.addLevel(mockedSkill.getMaxLevel() - (skillHolderData.getCurrentLevel() + 1));
        assertEquals(skillHolderData.getCurrentLevel(), mockedSkill.getMaxLevel() - 1);
        skillHolderData.addExperience(skillHolderData.getExperienceForNextLevel());
        assertEquals(skillHolderData.getCurrentLevel(), mockedSkill.getMaxLevel());
    }
}
