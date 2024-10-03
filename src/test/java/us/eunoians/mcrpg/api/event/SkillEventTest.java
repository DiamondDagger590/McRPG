package us.eunoians.mcrpg.api.event;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.skill.PostSkillGainExpEvent;
import us.eunoians.mcrpg.api.event.skill.SkillGainExpEvent;
import us.eunoians.mcrpg.api.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.api.event.skill.SkillRegisterEvent;
import us.eunoians.mcrpg.api.event.skill.SkillUnregisterEvent;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.mock.skill.MockSkill;
import us.eunoians.mcrpg.skill.Skill;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SkillEventTest {

    private static ServerMock serverMock;
    private static McRPG plugin;

    @BeforeAll
    public static void load() {
        serverMock = MockBukkit.mock();
        plugin = MockBukkit.load(McRPG.class);
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @BeforeEach
    public void beforeEach() {
        serverMock.getPluginManager().clearEvents();
    }

    @Test
    public void testSkillGainExpEvent() {
        Skill skill = new MockSkill(new NamespacedKey(plugin, "test"));
        plugin.getSkillRegistry().registerSkill(skill);
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(skill);
        var skillHolderData = skillHolder.getSkillHolderData(skill).get();
        skillHolderData.addExperience(500);
        serverMock.getPluginManager().assertEventFired(SkillGainExpEvent.class, skillGainExpEvent -> {
            assertEquals(skillGainExpEvent.getExperience(), 500);
            assertEquals(skillGainExpEvent.getSkillHolder(), skillHolder);
            assertEquals(skillGainExpEvent.getSkillKey(), skill.getSkillKey());
            return true;
        });
    }

    @Test
    public void testPostSkillGainExpEvent() {
        Skill skill = new MockSkill(new NamespacedKey(plugin, "test"));
        plugin.getSkillRegistry().registerSkill(skill);
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(skill);
        var skillHolderData = skillHolder.getSkillHolderData(skill).get();
        skillHolderData.addExperience(500);
        serverMock.getPluginManager().assertEventFired(PostSkillGainExpEvent.class, postSkillGainExpEvent -> {
            assertEquals(postSkillGainExpEvent.getSkillHolder(), skillHolder);
            assertEquals(postSkillGainExpEvent.getSkillKey(), skill.getSkillKey());
            return true;
        });
    }

    @Test
    public void testGainLevelEventThroughExperience() {
        Skill skill = new MockSkill(new NamespacedKey(plugin, "test"));
        plugin.getSkillRegistry().registerSkill(skill);
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(skill);
        var skillHolderData = skillHolder.getSkillHolderData(skill).get();
        skillHolderData.addExperience(skillHolderData.getExperienceForNextLevel());
        serverMock.getPluginManager().assertEventFired(SkillGainLevelEvent.class, skillGainLevelEvent -> {
            assertEquals(skillGainLevelEvent.getSkillHolder(), skillHolder);
            assertEquals(skillGainLevelEvent.getSkillKey(), skill.getSkillKey());
            assertEquals(skillGainLevelEvent.getLevels(), 1);
            return true;
        });
    }

    @Test
    public void testGainLevelEventThroughAdding() {
        Skill skill = new MockSkill(new NamespacedKey(plugin, "test"));
        plugin.getSkillRegistry().registerSkill(skill);
        SkillHolder skillHolder = new SkillHolder(UUID.randomUUID());
        skillHolder.addSkillHolderData(skill);
        var skillHolderData = skillHolder.getSkillHolderData(skill).get();
        skillHolderData.addLevel(2);
        serverMock.getPluginManager().assertEventFired(SkillGainLevelEvent.class, skillGainLevelEvent -> {
            assertEquals(skillGainLevelEvent.getSkillHolder(), skillHolder);
            assertEquals(skillGainLevelEvent.getSkillKey(), skill.getSkillKey());
            assertEquals(skillGainLevelEvent.getLevels(), 2);
            return true;
        });
    }

    @Test
    public void testSkillRegisterEvent() {
        Skill skill = new MockSkill(new NamespacedKey(plugin, "test"));
        plugin.getSkillRegistry().registerSkill(skill);
        serverMock.getPluginManager().assertEventFired(SkillRegisterEvent.class, skillRegisterEvent -> {
            assertEquals(skillRegisterEvent.getSkillKey(), skill.getSkillKey());
            return true;
        });
    }

    @Test
    public void testSkillUnregisterEvent() {
        Skill skill = new MockSkill(new NamespacedKey(plugin, "test"));
        plugin.getSkillRegistry().registerSkill(skill);
        plugin.getSkillRegistry().unregisterSkill(skill);
        serverMock.getPluginManager().assertEventFired(SkillUnregisterEvent.class, skillUnregisterEvent -> {
            assertEquals(skillUnregisterEvent.getSkillKey(), skill.getSkillKey());
            return true;
        });
    }
}
