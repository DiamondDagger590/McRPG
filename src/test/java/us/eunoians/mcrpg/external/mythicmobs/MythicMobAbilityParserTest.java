package us.eunoians.mcrpg.external.mythicmobs;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import io.lumine.mythic.api.config.MythicConfig;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.skills.ISkillMechanic;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillTrigger;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.mechanics.CustomMechanic;
import io.lumine.mythic.core.skills.mechanics.MetaSkillMechanic;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MythicMobAbilityParser}.
 */
public class MythicMobAbilityParserTest extends McRPGBaseTest {

    private MockedStatic<SkillTrigger> skillTriggerStatic;
    private MythicMobAbilityParser parser;
    private YamlDocument mainConfig;

    @BeforeEach
    public void setup() {
        MythicMobsHook hook = new MythicMobsHook(mcRPG);
        mcRPG.registryAccess().registry(RegistryKey.PLUGIN_HOOK).register(hook);

        mainConfig = mock(YamlDocument.class);
        // The ReloadableInteger inside the parser reads the TTL via Section::getInt(Route);
        // returning a positive value lets the Caffeine cache build normally.
        when(mainConfig.getInt(any(Route.class))).thenReturn(60);
        parser = new MythicMobAbilityParser(mainConfig);
        // Mock SkillTrigger.values() to return a controllable set of triggers
        skillTriggerStatic = mockStatic(SkillTrigger.class);
    }

    @AfterEach
    public void tearDown() {
        skillTriggerStatic.close();
    }

    @Test
    public void parseAbilities_returnsEmptyListForMobWithNoSkills() {
        MythicMob mythicMob = mock(MythicMob.class);
        when(mythicMob.getInternalName()).thenReturn("EmptyMob");
        skillTriggerStatic.when(SkillTrigger::values).thenReturn(List.of());
        when(mythicMob.getTimerSkills()).thenReturn(null);

        List<MythicMobAbilityParser.ParsedAbilityInfo> result = parser.parseAbilities(mythicMob);

        assertTrue(result.isEmpty());
    }

    @Test
    public void parseAbilities_extractsAbilityFromCustomMechanic() {
        NamespacedKey expectedKey = NamespacedKey.fromString("mcrpg:phase_shift");

        McRPGAbilityMechanic mcrpgMechanic = mock(McRPGAbilityMechanic.class);
        when(mcrpgMechanic.getAbilityKey()).thenReturn(expectedKey);
        when(mcrpgMechanic.getAttributes()).thenReturn(List.of(new AbilityTierAttribute(2)));

        CustomMechanic customMechanic = mock(CustomMechanic.class);
        when(customMechanic.getMechanic()).thenReturn(Optional.of(mcrpgMechanic));

        SkillTrigger<?> trigger = mock(SkillTrigger.class);
        skillTriggerStatic.when(SkillTrigger::values).thenReturn(List.of(trigger));

        Queue<SkillMechanic> queue = new LinkedList<>();
        queue.add(customMechanic);

        MythicMob mythicMob = mock(MythicMob.class);
        when(mythicMob.getInternalName()).thenReturn("TestMob");
        when(mythicMob.getSkills(trigger)).thenReturn(queue);
        when(mythicMob.getTimerSkills()).thenReturn(null);

        List<MythicMobAbilityParser.ParsedAbilityInfo> result = parser.parseAbilities(mythicMob);

        assertEquals(1, result.size());
        assertEquals(expectedKey, result.get(0).abilityKey());
        assertEquals(1, result.get(0).attributes().size());
        AbilityAttribute<?> attr = result.get(0).attributes().get(0);
        assertInstanceOf(AbilityTierAttribute.class, attr);
        assertEquals(2, ((AbilityTierAttribute) attr).getContent());
    }

    @Test
    public void parseAbilities_extractsAbilityFromMetaSkillMechanicConfig() {
        // MetaSkillMechanic wraps a named skill whose config has mcrpg_ability lines
        Skill namedSkill = mock(Skill.class);
        when(namedSkill.getInternalName()).thenReturn("PhaseShift");

        MythicConfig config = mock(MythicConfig.class);
        when(config.getStringList("Skills")).thenReturn(List.of(
                "- mcrpg_ability{ability=mcrpg:phase_shift;tier=3} @target"
        ));
        when(namedSkill.getConfig()).thenReturn(config);

        MetaSkillMechanic metaSkillMechanic = mock(MetaSkillMechanic.class);
        when(metaSkillMechanic.getSkill()).thenReturn(namedSkill);

        SkillTrigger<?> trigger = mock(SkillTrigger.class);
        skillTriggerStatic.when(SkillTrigger::values).thenReturn(List.of(trigger));

        Queue<SkillMechanic> queue = new LinkedList<>();
        queue.add(metaSkillMechanic);

        MythicMob mythicMob = mock(MythicMob.class);
        when(mythicMob.getInternalName()).thenReturn("TestMob2");
        when(mythicMob.getSkills(trigger)).thenReturn(queue);
        when(mythicMob.getTimerSkills()).thenReturn(null);

        List<MythicMobAbilityParser.ParsedAbilityInfo> result = parser.parseAbilities(mythicMob);

        assertEquals(1, result.size());
        assertEquals(NamespacedKey.fromString("mcrpg:phase_shift"), result.get(0).abilityKey());
        assertEquals(1, result.get(0).attributes().size());
        AbilityAttribute<?> attr = result.get(0).attributes().get(0);
        assertInstanceOf(AbilityTierAttribute.class, attr);
        assertEquals(3, ((AbilityTierAttribute) attr).getContent());
    }

    @Test
    public void parseAbilities_returnsEmptyAttributesWhenTierNotSpecified() {
        Skill namedSkill = mock(Skill.class);
        when(namedSkill.getInternalName()).thenReturn("Whirlpool");

        MythicConfig config = mock(MythicConfig.class);
        when(config.getStringList("Skills")).thenReturn(List.of(
                "- mcrpg_ability{ability=mcrpg:whirlpool} @target"
        ));
        when(namedSkill.getConfig()).thenReturn(config);

        MetaSkillMechanic metaSkillMechanic = mock(MetaSkillMechanic.class);
        when(metaSkillMechanic.getSkill()).thenReturn(namedSkill);

        SkillTrigger<?> trigger = mock(SkillTrigger.class);
        skillTriggerStatic.when(SkillTrigger::values).thenReturn(List.of(trigger));

        Queue<SkillMechanic> queue = new LinkedList<>();
        queue.add(metaSkillMechanic);

        MythicMob mythicMob = mock(MythicMob.class);
        when(mythicMob.getInternalName()).thenReturn("TestMob3");
        when(mythicMob.getSkills(trigger)).thenReturn(queue);
        when(mythicMob.getTimerSkills()).thenReturn(null);

        List<MythicMobAbilityParser.ParsedAbilityInfo> result = parser.parseAbilities(mythicMob);

        assertEquals(1, result.size());
        assertTrue(result.get(0).attributes().isEmpty(),
                "Attributes should be empty when no tier is specified");
    }

    @Test
    public void parseAbilities_extractsMultipleAbilitiesAcrossTriggersAndTimers() {
        // Ability from trigger skills
        McRPGAbilityMechanic mcrpgMechanic1 = mock(McRPGAbilityMechanic.class);
        when(mcrpgMechanic1.getAbilityKey()).thenReturn(NamespacedKey.fromString("mcrpg:phase_shift"));
        when(mcrpgMechanic1.getAttributes()).thenReturn(List.of(new AbilityTierAttribute(1)));

        CustomMechanic customMechanic1 = mock(CustomMechanic.class);
        when(customMechanic1.getMechanic()).thenReturn(Optional.of(mcrpgMechanic1));

        // Ability from timer skills
        Skill timerSkill = mock(Skill.class);
        when(timerSkill.getInternalName()).thenReturn("Whirlpool");
        MythicConfig timerConfig = mock(MythicConfig.class);
        when(timerConfig.getStringList("Skills")).thenReturn(List.of(
                "- mcrpg_ability{ability=mcrpg:whirlpool;tier=2} @self"
        ));
        when(timerSkill.getConfig()).thenReturn(timerConfig);

        MetaSkillMechanic timerMeta = mock(MetaSkillMechanic.class);
        when(timerMeta.getSkill()).thenReturn(timerSkill);

        SkillTrigger<?> trigger = mock(SkillTrigger.class);
        skillTriggerStatic.when(SkillTrigger::values).thenReturn(List.of(trigger));

        Queue<SkillMechanic> triggerQueue = new LinkedList<>();
        triggerQueue.add(customMechanic1);

        Queue<SkillMechanic> timerQueue = new LinkedList<>();
        timerQueue.add(timerMeta);

        MythicMob mythicMob = mock(MythicMob.class);
        when(mythicMob.getInternalName()).thenReturn("TestMob4");
        when(mythicMob.getSkills(trigger)).thenReturn(triggerQueue);
        when(mythicMob.getTimerSkills()).thenReturn(timerQueue);

        List<MythicMobAbilityParser.ParsedAbilityInfo> result = parser.parseAbilities(mythicMob);

        assertEquals(2, result.size());

        assertEquals(NamespacedKey.fromString("mcrpg:phase_shift"), result.get(0).abilityKey());
        assertEquals(1, result.get(0).attributes().size());
        assertEquals(1, ((AbilityTierAttribute) result.get(0).attributes().get(0)).getContent());

        assertEquals(NamespacedKey.fromString("mcrpg:whirlpool"), result.get(1).abilityKey());
        assertEquals(1, result.get(1).attributes().size());
        assertEquals(2, ((AbilityTierAttribute) result.get(1).attributes().get(0)).getContent());
    }

    @Test
    public void parseAbilities_cachesResultsByMobType() {
        MythicMob mythicMob = mock(MythicMob.class);
        when(mythicMob.getInternalName()).thenReturn("CachedMob");
        skillTriggerStatic.when(SkillTrigger::values).thenReturn(List.of());
        when(mythicMob.getTimerSkills()).thenReturn(null);

        List<MythicMobAbilityParser.ParsedAbilityInfo> result1 = parser.parseAbilities(mythicMob);
        List<MythicMobAbilityParser.ParsedAbilityInfo> result2 = parser.parseAbilities(mythicMob);

        // Should be the same object (cached)
        assertTrue(result1 == result2, "Second call should return cached result");
    }

    @Test
    public void clearCache_allowsReparsing() {
        MythicMob mythicMob = mock(MythicMob.class);
        when(mythicMob.getInternalName()).thenReturn("ClearableMob");
        skillTriggerStatic.when(SkillTrigger::values).thenReturn(List.of());
        when(mythicMob.getTimerSkills()).thenReturn(null);

        List<MythicMobAbilityParser.ParsedAbilityInfo> result1 = parser.parseAbilities(mythicMob);
        parser.clearCache();
        List<MythicMobAbilityParser.ParsedAbilityInfo> result2 = parser.parseAbilities(mythicMob);

        // After clearing cache, should be a new object
        assertTrue(result1 != result2, "After cache clear, should return new result");
    }

    @Test
    public void parseAbilities_handlesNullSkillInMetaSkillMechanic() {
        MetaSkillMechanic metaSkillMechanic = mock(MetaSkillMechanic.class);
        when(metaSkillMechanic.getSkill()).thenReturn(null);

        SkillTrigger<?> trigger = mock(SkillTrigger.class);
        skillTriggerStatic.when(SkillTrigger::values).thenReturn(List.of(trigger));

        Queue<SkillMechanic> queue = new LinkedList<>();
        queue.add(metaSkillMechanic);

        MythicMob mythicMob = mock(MythicMob.class);
        when(mythicMob.getInternalName()).thenReturn("NullSkillMob");
        when(mythicMob.getSkills(trigger)).thenReturn(queue);
        when(mythicMob.getTimerSkills()).thenReturn(null);

        // Should not throw
        List<MythicMobAbilityParser.ParsedAbilityInfo> result = parser.parseAbilities(mythicMob);

        assertTrue(result.isEmpty());
    }

    @Test
    public void parseAbilities_skipsNonMcRPGCustomMechanics() {
        // A CustomMechanic wrapping some other ISkillMechanic (not McRPGAbilityMechanic)
        ISkillMechanic otherMechanic = mock(ISkillMechanic.class);
        CustomMechanic customMechanic = mock(CustomMechanic.class);
        when(customMechanic.getMechanic()).thenReturn(Optional.of(otherMechanic));

        SkillTrigger<?> trigger = mock(SkillTrigger.class);
        skillTriggerStatic.when(SkillTrigger::values).thenReturn(List.of(trigger));

        Queue<SkillMechanic> queue = new LinkedList<>();
        queue.add(customMechanic);

        MythicMob mythicMob = mock(MythicMob.class);
        when(mythicMob.getInternalName()).thenReturn("OtherMechanicMob");
        when(mythicMob.getSkills(trigger)).thenReturn(queue);
        when(mythicMob.getTimerSkills()).thenReturn(null);

        List<MythicMobAbilityParser.ParsedAbilityInfo> result = parser.parseAbilities(mythicMob);

        assertTrue(result.isEmpty());
    }
}
