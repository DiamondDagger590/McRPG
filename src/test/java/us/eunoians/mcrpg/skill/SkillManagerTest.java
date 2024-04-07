package us.eunoians.mcrpg.skill;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.exception.skill.SkillNotRegisteredException;
import us.eunoians.mcrpg.mock.skill.MockSkill;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SkillManagerTest {

    private static ServerMock serverMock;
    private static McRPG plugin;
    private static NamespacedKey skillKey;
    private static SkillRegistry skillRegistry;

    @BeforeAll
    public static void load() {
        serverMock = MockBukkit.mock();
        plugin = spy(MockBukkit.load(McRPG.class));
        // Isolate skill registry
        skillRegistry = new SkillRegistry(plugin);
        when(plugin.getSkillRegistry()).thenReturn(skillRegistry);
        skillKey = new NamespacedKey(plugin, "test");
        skillRegistry = plugin.getSkillRegistry();
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @AfterEach
    public void unregisterSkill() {
        skillRegistry.unregisterSkill(skillKey);
    }

    @Test
    public void testIsSkillRegistered() {
        Skill skill = new MockSkill(skillKey);
        skillRegistry.registerSkill(skill);
        assertTrue(skillRegistry.isSkillRegistered(skill));
    }

    @Test
    public void testIsSkillNotRegistered() {
        Skill skill = new MockSkill(skillKey);
        assertFalse(plugin.getSkillRegistry().isSkillRegistered(skill));
    }

    @Test
    public void testUnregisterSkill() {
        Skill skill = new MockSkill(skillKey);
        skillRegistry.registerSkill(skill);
        assertTrue(skillRegistry.isSkillRegistered(skill));
        skillRegistry.unregisterSkill(skill);
        assertFalse(skillRegistry.isSkillRegistered(skill));
    }

    @Test
    public void testGetSkill() {
        Skill skill = new MockSkill(skillKey);
        plugin.getSkillRegistry().registerSkill(skill);
        assertEquals(plugin.getSkillRegistry().getRegisteredSkill(skill.getSkillKey()), skill);
    }

    @Test
    public void testThrowsSkillNotRegisteredException() {
        assertThrows(SkillNotRegisteredException.class, () -> {
           plugin.getSkillRegistry().getRegisteredSkill(skillKey);
        });
    }

    @Test
    public void testGetSkillKeys() {
        Skill skill = new MockSkill(skillKey);
        skillRegistry.registerSkill(skill);
        Set<NamespacedKey> skillKeys = skillRegistry.getRegisteredSkillKeys();
        assertEquals(skillKeys.size(), 1);
        assertTrue(skillKeys.contains(skillKey));
    }

    @Test
    public void testGetRegisteredSkills() {
        Skill skill = new MockSkill(skillKey);
        skillRegistry.registerSkill(skill);
        Set<Skill> skills = skillRegistry.getRegisteredSkills();
        assertEquals(skills.size(), 1);
        assertTrue(skills.contains(skill));
    }


}
