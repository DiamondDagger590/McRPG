package us.eunoians.mcrpg.ability.impl.type;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnlockableAbilityCoverageTest extends McRPGBaseTest {

    private static final NamespacedKey ABILITY_KEY = new NamespacedKey("test", "unlockable_test");
    private static final NamespacedKey SKILL_KEY = new NamespacedKey("test", "test_skill");

    private TestUnlockableAbility ability;

    @BeforeEach
    void setUp() {
        RegistryAccess registryAccess = RegistryAccess.registryAccess();
        if (registryAccess.registry(McRPGRegistryKey.ABILITY) == null) {
            registryAccess.register(new AbilityRegistry(mcRPG));
        }
        if (registryAccess.registry(McRPGRegistryKey.ABILITY_ATTRIBUTE) == null) {
            registryAccess.register(new AbilityAttributeRegistry());
        }
        ability = new TestUnlockableAbility(mcRPG, ABILITY_KEY, 5);
    }

    @Nested
    @DisplayName("getApplicableAttributes")
    class GetApplicableAttributes {

        @Test
        @DisplayName("contains ABILITY_TOGGLED_OFF and ABILITY_UNLOCKED")
        void containsToggledOffAndUnlocked() {
            Set<NamespacedKey> attributes = ability.getApplicableAttributes();
            assertEquals(2, attributes.size());
            assertTrue(attributes.contains(AbilityAttributeRegistry.ABILITY_TOGGLED_OFF_ATTRIBUTE_KEY));
            assertTrue(attributes.contains(AbilityAttributeRegistry.ABILITY_UNLOCKED_ATTRIBUTE));
        }
    }

    @Nested
    @DisplayName("checkIfAbilityCanBeUnlocked")
    class CheckIfAbilityCanBeUnlocked {

        @Test
        @DisplayName("returns true when skill level meets unlock level")
        void returnsTrue_whenLevelMeetsRequirement() {
            SkillHolder holder = createSkillHolderWithLevel(5);
            Skill skill = mockSkill(SKILL_KEY);
            assertTrue(ability.checkIfAbilityCanBeUnlocked(holder, skill));
        }

        @Test
        @DisplayName("returns true when skill level exceeds unlock level")
        void returnsTrue_whenLevelExceedsRequirement() {
            SkillHolder holder = createSkillHolderWithLevel(10);
            Skill skill = mockSkill(SKILL_KEY);
            assertTrue(ability.checkIfAbilityCanBeUnlocked(holder, skill));
        }

        @Test
        @DisplayName("returns false when skill level is below unlock level")
        void returnsFalse_whenLevelBelowRequirement() {
            SkillHolder holder = createSkillHolderWithLevel(4);
            Skill skill = mockSkill(SKILL_KEY);
            assertFalse(ability.checkIfAbilityCanBeUnlocked(holder, skill));
        }

        @Test
        @DisplayName("returns false when skill holder has no data for skill")
        void returnsFalse_whenNoSkillData() {
            SkillHolder holder = new SkillHolder(mcRPG, UUID.randomUUID());
            Skill skill = mockSkill(SKILL_KEY);
            assertFalse(ability.checkIfAbilityCanBeUnlocked(holder, skill));
        }
    }

    @Nested
    @DisplayName("isAbilityUnlocked")
    class IsAbilityUnlocked {

        @Test
        @DisplayName("returns false when ability is not registered")
        void returnsFalse_whenAbilityNotRegistered() {
            AbilityHolder holder = new AbilityHolder(mcRPG, UUID.randomUUID());
            assertFalse(ability.isAbilityUnlocked(holder));
        }

        @Test
        @DisplayName("returns false when unlocked attribute defaults to false")
        void returnsFalse_whenDefaultUnlockedAttribute() {
            AbilityHolder holder = new AbilityHolder(mcRPG, UUID.randomUUID());
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY).register(ability);
            holder.addAvailableAbility(ABILITY_KEY);

            AbilityData data = new AbilityData(ABILITY_KEY, new AbilityUnlockedAttribute(false));
            holder.addAbilityData(data);

            assertFalse(ability.isAbilityUnlocked(holder));
        }

        @Test
        @DisplayName("returns true when unlocked attribute is true")
        void returnsTrue_whenUnlockedAttributeTrue() {
            AbilityHolder holder = new AbilityHolder(mcRPG, UUID.randomUUID());
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY).register(ability);
            holder.addAvailableAbility(ABILITY_KEY);

            AbilityData data = new AbilityData(ABILITY_KEY, new AbilityUnlockedAttribute(true));
            holder.addAbilityData(data);

            assertTrue(ability.isAbilityUnlocked(holder));
        }
    }

    private SkillHolder createSkillHolderWithLevel(int level) {
        SkillHolder holder = new SkillHolder(mcRPG, UUID.randomUUID());
        Skill skill = mockSkill(SKILL_KEY);
        holder.addSkillHolderDataAtLevel(skill, level);
        return holder;
    }

    private Skill mockSkill(NamespacedKey skillKey) {
        Skill skill = mock(Skill.class);
        when(skill.getSkillKey()).thenReturn(skillKey);
        when(skill.getMaxLevel()).thenReturn(100);
        when(skill.getLevelUpEquation()).thenReturn(new Parser("100"));
        return skill;
    }

    private static class TestUnlockableAbility implements UnlockableAbility {

        private final Plugin plugin;
        private final NamespacedKey key;
        private final int unlockLevel;

        TestUnlockableAbility(@NotNull Plugin plugin, @NotNull NamespacedKey key, int unlockLevel) {
            this.plugin = plugin;
            this.key = key;
            this.unlockLevel = unlockLevel;
        }

        @Override
        public int getUnlockLevel() {
            return unlockLevel;
        }

        @NotNull
        @Override
        public Plugin getPlugin() {
            return plugin;
        }

        @NotNull
        @Override
        public NamespacedKey getAbilityKey() {
            return key;
        }

        @NotNull
        @Override
        public String getDatabaseName() {
            return key.getKey();
        }

        @NotNull
        @Override
        public String getName(@NotNull McRPGPlayer player) {
            return key.getKey();
        }

        @NotNull
        @Override
        public String getName() {
            return key.getKey();
        }

        @NotNull
        @Override
        public Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text(key.getKey());
        }

        @NotNull
        @Override
        public Component getDisplayName() {
            return Component.text(key.getKey());
        }

        @NotNull
        @Override
        public AbilityItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean activateAbility(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            return true;
        }

        @Override
        public boolean isAbilityEnabled() {
            return true;
        }

        @NotNull
        @Override
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
