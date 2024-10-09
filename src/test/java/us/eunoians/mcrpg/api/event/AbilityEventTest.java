package us.eunoians.mcrpg.api.event;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.BaseAbility;
import us.eunoians.mcrpg.event.ability.AbilityRegisterEvent;
import us.eunoians.mcrpg.event.ability.AbilityUnregisterEvent;
import us.eunoians.mcrpg.mock.ability.MockAbility;
import us.eunoians.mcrpg.mock.skill.MockSkill;
import us.eunoians.mcrpg.skill.Skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbilityEventTest {

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

    @BeforeEach
    public void beforeEach() {
        serverMock.getPluginManager().clearEvents();
    }

    @Test
    public void testAbilityRegisterEvent() {
        BaseAbility ability = new MockAbility(plugin, new NamespacedKey(plugin, "test"));
        plugin.getAbilityRegistry().registerAbility(ability);
        serverMock.getPluginManager().assertEventFired(AbilityRegisterEvent.class, abilityRegisterEvent -> {
            assertEquals(abilityRegisterEvent.getAbility(), ability);
            return true;
        });
    }

    @Test
    public void testAbilityUnregisterEvent() {
        BaseAbility ability = new MockAbility(plugin, new NamespacedKey(plugin, "test"));
        plugin.getAbilityRegistry().registerAbility(ability);
        plugin.getAbilityRegistry().unregisterAbility(ability);
        serverMock.getPluginManager().assertEventFired(AbilityUnregisterEvent.class, abilityUnregisterEvent -> {
            assertEquals(abilityUnregisterEvent.getAbility(), ability);
            return true;
        });
    }
}
