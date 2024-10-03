package us.eunoians.mcrpg.ability;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.exception.ability.AbilityNotRegisteredException;
import us.eunoians.mcrpg.mock.ability.MockAbility;
import us.eunoians.mcrpg.mock.skill.MockSkill;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AbilityRegistryTest {

    private static ServerMock serverMock;
    private static McRPG plugin;
    private static AbilityRegistry abilityRegistry;
    private static NamespacedKey abilityKey;
    private static NamespacedKey skillKey;


    @BeforeAll
    public static void load() {
        serverMock = MockBukkit.mock();
        plugin = spy(MockBukkit.load(McRPG.class));
        abilityRegistry = new AbilityRegistry(plugin);
        when(plugin.getAbilityRegistry()).thenReturn(abilityRegistry);
        abilityKey = new NamespacedKey(plugin, "test");
        skillKey = new NamespacedKey(plugin, "test-skill");
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @AfterEach
    public void unregisterAbility() {
        abilityRegistry.unregisterAbility(abilityKey);
    }

    @Test
    public void testIsAbilityRegistered() {
        BaseAbility ability = new MockAbility(plugin, abilityKey);
        abilityRegistry.registerAbility(ability);
        assertTrue(abilityRegistry.isAbilityRegistered(ability));
    }

    @Test
    public void testIsAbilityNotRegistered() {
        BaseAbility ability = new MockAbility(plugin, abilityKey);
        assertFalse(abilityRegistry.isAbilityRegistered(ability));
    }

    @Test
    public void testUnregisterAbility() {
        BaseAbility ability = new MockAbility(plugin, abilityKey);
        abilityRegistry.registerAbility(ability);
        assertTrue(abilityRegistry.isAbilityRegistered(ability));
        abilityRegistry.unregisterAbility(abilityKey);
        assertFalse(abilityRegistry.isAbilityRegistered(ability));
    }

    @Test
    public void testUnregisterAbilityWithSkill() {
        BaseAbility ability = new MockAbility(plugin, abilityKey, skillKey);
        abilityRegistry.registerAbility(ability);
        assertTrue(abilityRegistry.isAbilityRegistered(ability));
        abilityRegistry.unregisterAbility(abilityKey);
        assertFalse(abilityRegistry.isAbilityRegistered(ability));
    }

    @Test
    public void testDoesSkillHaveAbilities() {
        Skill skill = new MockSkill(skillKey);
        BaseAbility ability = new MockAbility(plugin, abilityKey, skillKey);
        abilityRegistry.registerAbility(ability);
        assertTrue(abilityRegistry.doesSkillHaveAbilities(skill));
    }

    @Test
    public void testGetAbilitiesBelongingToSkill() {
        Skill skill = new MockSkill(skillKey);
        BaseAbility ability = new MockAbility(plugin, abilityKey, skillKey);
        abilityRegistry.registerAbility(ability);
        Set<NamespacedKey> abilities = abilityRegistry.getAbilitiesBelongingToSkill(skill);
        assertEquals(abilities.size(), 1);
        assertTrue(abilities.contains(abilityKey));
    }

    @Test
    public void testGetAbilitiesWithoutSkills() {
        Skill skill = new MockSkill(skillKey);
        BaseAbility skillAbility = new MockAbility(plugin, abilityKey, skillKey);
        BaseAbility noSkillAbility = new MockAbility(plugin, new NamespacedKey(plugin, "no-skill-ability"));
        abilityRegistry.registerAbility(skillAbility);
        abilityRegistry.registerAbility(noSkillAbility);
        Set<NamespacedKey> abilities = abilityRegistry.getAbilitiesBelongingToSkill(skill);
        assertEquals(abilities.size(), 1);
        assertTrue(abilities.contains(abilityKey));
        abilities = abilityRegistry.getAbilitiesWithoutSkills();
        assertEquals(abilities.size(), 1);
        assertTrue(abilities.contains(noSkillAbility.getAbilityKey()));
    }

    @Test
    public void testGetAllAbilities() {
        Skill skill = new MockSkill(skillKey);
        BaseAbility skillAbility = new MockAbility(plugin, abilityKey, skillKey);
        BaseAbility noSkillAbility = new MockAbility(plugin, new NamespacedKey(plugin, "no-skill-ability"));
        abilityRegistry.registerAbility(skillAbility);
        abilityRegistry.registerAbility(noSkillAbility);
        Set<NamespacedKey> abilities = abilityRegistry.getAllAbilities();
        assertEquals(abilities.size(), 2);
        assertTrue(abilities.contains(abilityKey));
        assertTrue(abilities.contains(noSkillAbility.getAbilityKey()));
    }

    @Test
    public void testGetAbility() {
        BaseAbility ability = new MockAbility(plugin, abilityKey);
        abilityRegistry.registerAbility(ability);
        Ability foundAbility = abilityRegistry.getRegisteredAbility(ability.getAbilityKey());
        assertEquals(ability, foundAbility);
    }

    @Test
    public void testAbilityNotRegisteredException() {
        assertThrows(AbilityNotRegisteredException.class, () -> {
            abilityRegistry.getRegisteredAbility(new NamespacedKey(plugin, "test"));
        });
    }

}
