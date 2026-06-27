package us.eunoians.mcrpg.util.filter.ability;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.StubPlainAbility;
import us.eunoians.mcrpg.ability.StubTierableAbility;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityTierAttribute;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.ability.impl.type.TierableAbility;
import us.eunoians.mcrpg.builder.item.ability.AbilityItemBuilder;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.skill.Skill;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("AbilityUpgradeFilter")
class AbilityUpgradeFilterTest extends McRPGBaseTest {

    private AbilityUpgradeFilter filter;
    private McRPGPlayer mcRPGPlayer;

    @BeforeEach
    void setUp(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.filter = new AbilityUpgradeFilter();
        addPlayerToServer(mcRPGPlayer);

        StubTierableAbility defaultAbility = new StubTierableAbility(mcRPG, new NamespacedKey("test", "default"));
        AbilityRegistry mockAbilityRegistry = mock(AbilityRegistry.class);
        when(mockAbilityRegistry.registered(any(NamespacedKey.class))).thenReturn(true);
        when(mockAbilityRegistry.getRegisteredAbility(any(NamespacedKey.class))).thenReturn(defaultAbility);
        RegistryAccess.registryAccess().register(mockAbilityRegistry);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);
    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        @DisplayName("keeps upgradeable tierable ability")
        void filter_upgradeableTierableAbility_kept() {
            StubTierableAbility ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "upgradeable"))
                    .withMaxTier(5);
            AbilityData data = new AbilityData(ability.getAbilityKey(),
                    new AbilityUnlockedAttribute(true),
                    new AbilityTierAttribute(2));
            mcRPGPlayer.asSkillHolder().addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertEquals(1, result.size());
            assertTrue(result.contains(ability));
        }

        @Test
        @DisplayName("removes ability at max tier")
        void filter_abilityAtMaxTier_removed() {
            StubTierableAbility ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "maxed"))
                    .withMaxTier(3);
            AbilityData data = new AbilityData(ability.getAbilityKey(),
                    new AbilityUnlockedAttribute(true),
                    new AbilityTierAttribute(3));
            mcRPGPlayer.asSkillHolder().addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removes ability that is not unlocked")
        void filter_abilityNotUnlocked_removed() {
            StubTierableAbility ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "locked"))
                    .withMaxTier(5);
            AbilityData data = new AbilityData(ability.getAbilityKey(),
                    new AbilityUnlockedAttribute(false),
                    new AbilityTierAttribute(1));
            mcRPGPlayer.asSkillHolder().addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removes ability with no data")
        void filter_abilityWithNoData_removed() {
            StubTierableAbility ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "no_data"))
                    .withMaxTier(5);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removes ability with no tier attribute")
        void filter_abilityWithNoTierAttribute_removed() {
            StubTierableAbility ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "no_tier"))
                    .withMaxTier(5);
            AbilityData data = new AbilityData(ability.getAbilityKey(),
                    new AbilityUnlockedAttribute(true));
            mcRPGPlayer.asSkillHolder().addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removes non-tierable ability")
        void filter_nonTierableAbility_removed() {
            Ability plainAbility = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain"), false);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(plainAbility));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removes skill ability when skill level is too low")
        void filter_skillAbilityLevelTooLow_removed() {
            NamespacedKey skillKey = new NamespacedKey("test", "test_skill");
            Skill mockSkill = createMockSkill(skillKey);

            StubTierableSkillAbility ability = new StubTierableSkillAbility(mcRPG,
                    new NamespacedKey("test", "skill_ability"), skillKey, 5, 50);

            AbilityData data = new AbilityData(ability.getAbilityKey(),
                    new AbilityUnlockedAttribute(true),
                    new AbilityTierAttribute(2));
            mcRPGPlayer.asSkillHolder().addAbilityData(data);
            mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(mockSkill, 1);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("keeps skill ability when skill level is sufficient")
        void filter_skillAbilityLevelSufficient_kept() {
            NamespacedKey skillKey = new NamespacedKey("test", "test_skill");
            Skill mockSkill = createMockSkill(skillKey);

            StubTierableSkillAbility ability = new StubTierableSkillAbility(mcRPG,
                    new NamespacedKey("test", "skill_ok"), skillKey, 5, 5);

            AbilityData data = new AbilityData(ability.getAbilityKey(),
                    new AbilityUnlockedAttribute(true),
                    new AbilityTierAttribute(2));
            mcRPGPlayer.asSkillHolder().addAbilityData(data);
            mcRPGPlayer.asSkillHolder().addSkillHolderDataAtLevel(mockSkill, 10);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("non-skill tierable ability bypasses skill level check")
        void filter_nonSkillTierable_bypassesSkillCheck() {
            StubTierableAbility ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "no_skill"))
                    .withMaxTier(5);
            AbilityData data = new AbilityData(ability.getAbilityKey(),
                    new AbilityUnlockedAttribute(true),
                    new AbilityTierAttribute(2));
            mcRPGPlayer.asSkillHolder().addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("returns empty for empty input")
        void filter_emptyInput_returnsEmpty() {
            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("filters mixed collection correctly")
        void filter_mixedCollection_keepsOnlyUpgradeable() {
            StubTierableAbility upgradeable = new StubTierableAbility(mcRPG, new NamespacedKey("test", "upgradeable"))
                    .withMaxTier(5);
            AbilityData upgradeableData = new AbilityData(upgradeable.getAbilityKey(),
                    new AbilityUnlockedAttribute(true),
                    new AbilityTierAttribute(2));
            mcRPGPlayer.asSkillHolder().addAbilityData(upgradeableData);

            StubTierableAbility maxed = new StubTierableAbility(mcRPG, new NamespacedKey("test", "maxed"))
                    .withMaxTier(3);
            AbilityData maxedData = new AbilityData(maxed.getAbilityKey(),
                    new AbilityUnlockedAttribute(true),
                    new AbilityTierAttribute(3));
            mcRPGPlayer.asSkillHolder().addAbilityData(maxedData);

            StubTierableAbility locked = new StubTierableAbility(mcRPG, new NamespacedKey("test", "locked"))
                    .withMaxTier(5);
            AbilityData lockedData = new AbilityData(locked.getAbilityKey(),
                    new AbilityUnlockedAttribute(false),
                    new AbilityTierAttribute(1));
            mcRPGPlayer.asSkillHolder().addAbilityData(lockedData);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(upgradeable, maxed, locked));

            assertEquals(1, result.size());
            assertTrue(result.contains(upgradeable));
        }
    }

    private static Skill createMockSkill(NamespacedKey key) {
        Skill skill = mock(Skill.class);
        when(skill.getSkillKey()).thenReturn(key);
        Parser parser = new Parser("100");
        when(skill.getLevelUpEquation()).thenReturn(parser);
        when(skill.getMaxLevel()).thenReturn(100);
        return skill;
    }

    private static class StubTierableSkillAbility implements TierableAbility, SkillAbility {

        private final Plugin plugin;
        private final NamespacedKey key;
        private final NamespacedKey skillKey;
        private final int maxTier;
        private final int unlockLevelPerTier;

        StubTierableSkillAbility(Plugin plugin, NamespacedKey key, NamespacedKey skillKey, int maxTier, int unlockLevelPerTier) {
            this.plugin = plugin;
            this.key = key;
            this.skillKey = skillKey;
            this.maxTier = maxTier;
            this.unlockLevelPerTier = unlockLevelPerTier;
        }

        @Override
        public NamespacedKey getSkillKey() {
            return skillKey;
        }

        @Override
        public int getMaxTier() {
            return maxTier;
        }

        @Override
        public int getUnlockLevelForTier(int tier) {
            return unlockLevelPerTier * tier;
        }

        @Override
        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public NamespacedKey getAbilityKey() {
            return key;
        }

        @Override
        public Set<NamespacedKey> getApplicableAttributes() {
            return TierableAbility.super.getApplicableAttributes();
        }

        @Override
        public String getDatabaseName() {
            return key.getKey();
        }

        @Override
        public String getName(McRPGPlayer player) {
            return key.getKey();
        }

        @Override
        public String getName() {
            return key.getKey();
        }

        @Override
        public Component getDisplayName(McRPGPlayer player) {
            return Component.text(key.getKey());
        }

        @Override
        public Component getDisplayName() {
            return Component.text(key.getKey());
        }

        @Override
        public AbilityItemBuilder getDisplayItemBuilder(McRPGPlayer player) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean activateAbility(AbilityHolder abilityHolder, Event event) {
            return true;
        }

        @Override
        public boolean isPassive() {
            return true;
        }

        @Override
        public Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
