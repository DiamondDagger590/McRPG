package us.eunoians.mcrpg.skill;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.event.skill.SkillRegisterEvent;
import us.eunoians.mcrpg.event.skill.SkillUnregisterEvent;
import us.eunoians.mcrpg.exception.skill.SkillNotRegisteredException;
import us.eunoians.mcrpg.skill.impl.MockSkill;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasFiredEventInstance;
import static org.mockbukkit.mockbukkit.matcher.plugin.PluginManagerFiredEventClassMatcher.hasNotFiredEventInstance;
import static org.mockito.Mockito.spy;

public class SkillRegistryTest extends McRPGBaseTest {

    private SkillRegistry skillRegistry;

    @BeforeEach
    public void setup() {
        skillRegistry = spy(new SkillRegistry(mcRPG));
        RegistryAccess.registryAccess().register(skillRegistry);
    }

    @DisplayName("Given a skill already registered, when registering again, then it throws IllegalArgumentException")
    @Test
    public void register_throwsIllegalArgumentException_whenSkillAlreadyRegistered() {
        MockSkill mockSkill = spy(MockSkill.class);
        skillRegistry.register(mockSkill);
        assertThrows(IllegalArgumentException.class, () -> skillRegistry.register(mockSkill));
    }

    @DisplayName("Given a registered skill, when checking registered, then it returns true and fires SkillRegisterEvent")
    @Test
    public void registered_returnsTrue_whenSkillRegistered() {
        MockSkill mockSkill = spy(MockSkill.class);
        skillRegistry.register(mockSkill);
        assertTrue(skillRegistry.registered(mockSkill));
        hasFiredEventInstance(SkillRegisterEvent.class);
    }

    @DisplayName("Given an unregistered skill, when checking registered, then it returns false and does not fire register event")
    @Test
    public void registered_returnsFalse_whenSkillNotRegistered() {
        MockSkill mockSkill = spy(MockSkill.class);
        assertFalse(skillRegistry.registered(mockSkill));
        hasNotFiredEventInstance(SkillRegisterEvent.class);
    }

    @DisplayName("Given a registered skill, when getting by key, then it returns the same skill")
    @Test
    public void getRegisteredSkill_returnsSkill_whenSkillRegistered() {
        MockSkill mockSkill = spy(MockSkill.class);
        skillRegistry.register(mockSkill);
        assertEquals(mockSkill, skillRegistry.getRegisteredSkill(mockSkill.getSkillKey()));
    }

    @DisplayName("Given an unregistered skill, when getting by key, then it throws SkillNotRegisteredException")
    @Test
    public void getRegisteredSkill_throwsSkillNotRegisteredException_whenSkillNotRegistered() {
        MockSkill mockSkill = spy(MockSkill.class);
        assertThrows(SkillNotRegisteredException.class, () -> skillRegistry.getRegisteredSkill(mockSkill.getSkillKey()));
    }

    @DisplayName("Given a registered skill, when fetching registered keys, then it contains the skill key")
    @Test
    public void getRegisteredSkillKeys_containsSkill_whenSkillRegistered() {
        MockSkill mockSkill = spy(MockSkill.class);
        skillRegistry.register(mockSkill);
        assertEquals(Set.of(mockSkill.getSkillKey()), skillRegistry.getRegisteredSkillKeys());
    }

    @DisplayName("Given no registered skills, when fetching registered keys, then it returns an empty set")
    @Test
    public void getRegisteredSkillKeys_returnsEmpty_whenNoSkillsRegistered() {
        assertEquals(Set.of(), skillRegistry.getRegisteredSkillKeys());
    }

    @DisplayName("Given a registered skill, when fetching registered skills, then it contains the skill")
    @Test
    public void getRegisteredSkills_containsSkill_whenSkillRegistered() {
        MockSkill mockSkill = spy(MockSkill.class);
        skillRegistry.register(mockSkill);
        assertEquals(Set.of(mockSkill), skillRegistry.getRegisteredSkills());
    }

    @DisplayName("Given no registered skills, when fetching registered skills, then it returns an empty set")
    @Test
    public void getRegisteredSkills_returnsEmpty_whenNoSkillsRegistered() {
        assertEquals(Set.of(), skillRegistry.getRegisteredSkills());
    }

    @DisplayName("Given a registered skill, when unregistering, then it fires SkillUnregisterEvent and registry becomes empty")
    @Test
    public void unregisterSkill_firesEvent_whenSkillRegistered() {
        MockSkill mockSkill = spy(MockSkill.class);
        skillRegistry.register(mockSkill);
        skillRegistry.unregisterSkill(mockSkill);
        hasFiredEventInstance(SkillUnregisterEvent.class);
        assertEquals(Set.of(), skillRegistry.getRegisteredSkills());
    }

    @DisplayName("Given an unregistered skill, when asserting unregister event, then it has not fired")
    @Test
    public void unregisterSkill_doesNotFireEvent_whenSkillNotRegistered() {
        MockSkill mockSkill = spy(MockSkill.class);
        hasNotFiredEventInstance(SkillUnregisterEvent.class);
    }
}
