package us.eunoians.mcrpg.ability;

import com.diamonddagger590.mccore.pair.Pair;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.check.AlliedAttackCheck;
import us.eunoians.mcrpg.ability.check.EntityAlliedCheck;
import us.eunoians.mcrpg.ability.impl.type.SkillAbility;
import us.eunoians.mcrpg.exception.ability.AbilityNotRegisteredException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AbilityRegistryCoverageTest extends McRPGBaseTest {

    private static final NamespacedKey SKILL_KEY = new NamespacedKey("test", "skill");

    private AbilityRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new AbilityRegistry(mcRPG);
    }

    @Nested
    @DisplayName("Register and Registered")
    class RegisterAndRegistered {

        @Test
        @DisplayName("registered returns false for unregistered key")
        void registered_returnsFalse_whenNotRegistered() {
            NamespacedKey key = new NamespacedKey("test", "unregistered");
            assertFalse(registry.registered(key));
        }

        @Test
        @DisplayName("registered returns true after registering ability")
        void registered_returnsTrue_afterRegister() {
            StubTierableAbility ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "reg_test"));
            registry.register(ability);
            assertTrue(registry.registered(ability));
        }

        @Test
        @DisplayName("registered by key returns true after registering ability")
        void registered_byKey_returnsTrue_afterRegister() {
            NamespacedKey key = new NamespacedKey("test", "reg_key");
            StubTierableAbility ability = new StubTierableAbility(mcRPG, key);
            registry.register(ability);
            assertTrue(registry.registered(key));
        }

        @Test
        @DisplayName("register tracks SkillAbility under its skill key")
        void register_tracksSkillAbility() {
            NamespacedKey abilityKey = new NamespacedKey("test", "skill_ability");
            SkillAbility skillAbility = createSkillAbility(abilityKey, SKILL_KEY);
            registry.register(skillAbility);

            assertTrue(registry.doesSkillHaveAbilities(SKILL_KEY));
            assertTrue(registry.getAbilitiesBelongingToSkill(SKILL_KEY).contains(abilityKey));
        }

        @Test
        @DisplayName("register tracks non-SkillAbility in without-skills set")
        void register_tracksNonSkillAbility() {
            NamespacedKey key = new NamespacedKey("test", "no_skill");
            StubTierableAbility ability = new StubTierableAbility(mcRPG, key);
            registry.register(ability);

            assertTrue(registry.getAbilitiesWithoutSkills().contains(key));
        }
    }

    @Nested
    @DisplayName("Unregister")
    class Unregister {

        @Test
        @DisplayName("unregisterAbility removes registered ability")
        void unregisterAbility_removesAbility() {
            NamespacedKey key = new NamespacedKey("test", "unreg");
            StubTierableAbility ability = new StubTierableAbility(mcRPG, key);
            registry.register(ability);
            assertTrue(registry.registered(key));

            registry.unregisterAbility(ability);
            assertFalse(registry.registered(key));
        }

        @Test
        @DisplayName("unregisterAbility by key removes registered ability")
        void unregisterAbility_byKey_removesAbility() {
            NamespacedKey key = new NamespacedKey("test", "unreg_key");
            StubTierableAbility ability = new StubTierableAbility(mcRPG, key);
            registry.register(ability);

            registry.unregisterAbility(key);
            assertFalse(registry.registered(key));
        }

        @Test
        @DisplayName("unregisterAbility on unregistered key is a no-op")
        void unregisterAbility_noOp_whenNotRegistered() {
            NamespacedKey key = new NamespacedKey("test", "never_registered");
            assertDoesNotThrow(() -> registry.unregisterAbility(key));
        }

        @Test
        @DisplayName("unregisterAbility removes SkillAbility from skill map")
        void unregisterAbility_removesFromSkillMap() {
            NamespacedKey abilityKey = new NamespacedKey("test", "skill_unreg");
            SkillAbility skillAbility = createSkillAbility(abilityKey, SKILL_KEY);
            registry.register(skillAbility);

            registry.unregisterAbility(abilityKey);

            assertFalse(registry.doesSkillHaveAbilities(SKILL_KEY));
        }

        @Test
        @DisplayName("unregisterAbility removes non-SkillAbility from without-skills set")
        void unregisterAbility_removesFromWithoutSkills() {
            NamespacedKey key = new NamespacedKey("test", "no_skill_unreg");
            StubTierableAbility ability = new StubTierableAbility(mcRPG, key);
            registry.register(ability);

            registry.unregisterAbility(key);

            assertFalse(registry.getAbilitiesWithoutSkills().contains(key));
        }

        @Test
        @DisplayName("unregisterAbility cleans up skill key when last ability removed")
        void unregisterAbility_cleansUpSkillKey_whenLastAbilityRemoved() {
            NamespacedKey abilityKey1 = new NamespacedKey("test", "skill_a1");
            NamespacedKey abilityKey2 = new NamespacedKey("test", "skill_a2");
            registry.register(createSkillAbility(abilityKey1, SKILL_KEY));
            registry.register(createSkillAbility(abilityKey2, SKILL_KEY));

            registry.unregisterAbility(abilityKey1);
            assertTrue(registry.doesSkillHaveAbilities(SKILL_KEY));

            registry.unregisterAbility(abilityKey2);
            assertFalse(registry.doesSkillHaveAbilities(SKILL_KEY));
        }
    }

    @Nested
    @DisplayName("getRegisteredAbility")
    class GetRegisteredAbility {

        @Test
        @DisplayName("getRegisteredAbility returns registered ability")
        void getRegisteredAbility_returnsAbility() {
            NamespacedKey key = new NamespacedKey("test", "get_ability");
            StubTierableAbility ability = new StubTierableAbility(mcRPG, key);
            registry.register(ability);

            assertEquals(ability, registry.getRegisteredAbility(key));
        }

        @Test
        @DisplayName("getRegisteredAbility throws for unregistered key")
        void getRegisteredAbility_throws_whenNotRegistered() {
            NamespacedKey key = new NamespacedKey("test", "missing");
            assertThrows(AbilityNotRegisteredException.class, () -> registry.getRegisteredAbility(key));
        }
    }

    @Nested
    @DisplayName("Skill association queries")
    class SkillAssociationQueries {

        @Test
        @DisplayName("doesSkillHaveAbilities returns false for unknown skill")
        void doesSkillHaveAbilities_returnsFalse_whenNoAbilities() {
            NamespacedKey unknownSkill = new NamespacedKey("test", "unknown_skill");
            assertFalse(registry.doesSkillHaveAbilities(unknownSkill));
        }

        @Test
        @DisplayName("getAbilitiesBelongingToSkill returns empty set for unknown skill")
        void getAbilitiesBelongingToSkill_returnsEmptySet_whenNoAbilities() {
            NamespacedKey unknownSkill = new NamespacedKey("test", "unknown_skill");
            Set<NamespacedKey> result = registry.getAbilitiesBelongingToSkill(unknownSkill);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getAbilitiesBelongingToSkill returns immutable copy")
        void getAbilitiesBelongingToSkill_returnsImmutableCopy() {
            NamespacedKey abilityKey = new NamespacedKey("test", "imm_check");
            registry.register(createSkillAbility(abilityKey, SKILL_KEY));

            Set<NamespacedKey> result = registry.getAbilitiesBelongingToSkill(SKILL_KEY);
            assertThrows(UnsupportedOperationException.class, () -> result.add(new NamespacedKey("test", "hack")));
        }

        @Test
        @DisplayName("getAbilitiesWithoutSkills returns immutable copy")
        void getAbilitiesWithoutSkills_returnsImmutableCopy() {
            NamespacedKey key = new NamespacedKey("test", "no_skill_imm");
            StubTierableAbility ability = new StubTierableAbility(mcRPG, key);
            registry.register(ability);

            Set<NamespacedKey> result = registry.getAbilitiesWithoutSkills();
            assertThrows(UnsupportedOperationException.class, () -> result.add(new NamespacedKey("test", "hack")));
        }

        @Test
        @DisplayName("getAllAbilities returns all registered ability keys")
        void getAllAbilities_returnsAll() {
            NamespacedKey k1 = new NamespacedKey("test", "all_1");
            NamespacedKey k2 = new NamespacedKey("test", "all_2");
            registry.register(new StubTierableAbility(mcRPG, k1));
            registry.register(createSkillAbility(k2, SKILL_KEY));

            Set<NamespacedKey> all = registry.getAllAbilities();
            assertEquals(2, all.size());
            assertTrue(all.contains(k1));
            assertTrue(all.contains(k2));
        }

        @Test
        @DisplayName("getAllAbilities returns immutable copy")
        void getAllAbilities_returnsImmutableCopy() {
            registry.register(new StubTierableAbility(mcRPG, new NamespacedKey("test", "imm_all")));
            Set<NamespacedKey> all = registry.getAllAbilities();
            assertThrows(UnsupportedOperationException.class, () -> all.add(new NamespacedKey("test", "hack")));
        }
    }

    @Nested
    @DisplayName("Entity allied checks")
    class EntityAlliedChecks {

        private final NamespacedKey alliedKey = new NamespacedKey("test", "allied_check");

        @Test
        @DisplayName("areEntitiesAllied returns false when no function registered")
        void areEntitiesAllied_returnsFalse_whenNoFunction() {
            Entity e1 = mock(Entity.class);
            Entity e2 = mock(Entity.class);

            assertFalse(registry.areEntitiesAllied(e1, e2, new NamespacedKey("test", "none")));
        }

        @Test
        @DisplayName("areEntitiesAllied with key delegates to registered function")
        void areEntitiesAllied_withKey_delegatesToFunction() {
            Entity e1 = mock(Entity.class);
            Entity e2 = mock(Entity.class);
            EntityAlliedCheck check = (a, b) -> true;
            registry.registerEntityAlliedFunction(alliedKey, check);

            assertTrue(registry.areEntitiesAllied(e1, e2, alliedKey));
        }

        @Test
        @DisplayName("areEntitiesAllied without key iterates all registered functions")
        void areEntitiesAllied_iteratesAll() {
            Entity e1 = mock(Entity.class);
            Entity e2 = mock(Entity.class);
            registry.registerEntityAlliedFunction(alliedKey, (a, b) -> true);

            Pair<Boolean, Optional<NamespacedKey>> result = registry.areEntitiesAllied(e1, e2);
            assertTrue(result.getLeft());
            assertTrue(result.getRight().isPresent());
            assertEquals(alliedKey, result.getRight().get());
        }

        @Test
        @DisplayName("areEntitiesAllied returns false pair when no functions match")
        void areEntitiesAllied_returnsFalsePair_whenNoMatch() {
            Entity e1 = mock(Entity.class);
            Entity e2 = mock(Entity.class);
            registry.registerEntityAlliedFunction(alliedKey, (a, b) -> false);

            Pair<Boolean, Optional<NamespacedKey>> result = registry.areEntitiesAllied(e1, e2);
            assertFalse(result.getLeft());
            assertTrue(result.getRight().isEmpty());
        }

        @Test
        @DisplayName("registerEntityAlliedFunction also registers default attack check")
        void registerEntityAlliedFunction_registersDefaultAttackCheck() {
            Entity e1 = mock(Entity.class);
            Entity e2 = mock(Entity.class);
            registry.registerEntityAlliedFunction(alliedKey, (a, b) -> true);

            Pair<Boolean, Optional<NamespacedKey>> result = registry.shouldAlliesBeUnableToDamage(e1, e2);
            assertTrue(result.getLeft());
        }
    }

    @Nested
    @DisplayName("shouldAlliesBeUnableToDamage")
    class ShouldAlliesBeUnableToDamage {

        private final NamespacedKey key = new NamespacedKey("test", "damage_check");

        @Test
        @DisplayName("returns false pair when no functions registered")
        void returnsFalsePair_whenNoFunctions() {
            Entity e1 = mock(Entity.class);
            Entity e2 = mock(Entity.class);

            Pair<Boolean, Optional<NamespacedKey>> result = registry.shouldAlliesBeUnableToDamage(e1, e2);
            assertFalse(result.getLeft());
            assertTrue(result.getRight().isEmpty());
        }

        @Test
        @DisplayName("returns false when allied but attack check allows damage")
        void returnsFalse_whenAlliedButAttackCheckAllowsDamage() {
            Entity e1 = mock(Entity.class);
            Entity e2 = mock(Entity.class);
            registry.registerEntityAlliedFunction(key, (a, b) -> true);
            registry.registerAlliedAttackCheckFunction(key, (a, b) -> false);

            Pair<Boolean, Optional<NamespacedKey>> result = registry.shouldAlliesBeUnableToDamage(e1, e2);
            assertFalse(result.getLeft());
        }

        @Test
        @DisplayName("returns true when allied and attack check prevents damage")
        void returnsTrue_whenAlliedAndUnableToDamage() {
            Entity e1 = mock(Entity.class);
            Entity e2 = mock(Entity.class);
            registry.registerEntityAlliedFunction(key, (a, b) -> true);
            registry.registerAlliedAttackCheckFunction(key, (a, b) -> true);

            Pair<Boolean, Optional<NamespacedKey>> result = registry.shouldAlliesBeUnableToDamage(e1, e2);
            assertTrue(result.getLeft());
            assertEquals(key, result.getRight().orElseThrow());
        }

        @Test
        @DisplayName("returns false when not allied even if attack check would block")
        void returnsFalse_whenNotAllied() {
            Entity e1 = mock(Entity.class);
            Entity e2 = mock(Entity.class);
            registry.registerEntityAlliedFunction(key, (a, b) -> false);
            registry.registerAlliedAttackCheckFunction(key, (a, b) -> true);

            Pair<Boolean, Optional<NamespacedKey>> result = registry.shouldAlliesBeUnableToDamage(e1, e2);
            assertFalse(result.getLeft());
        }
    }

    private SkillAbility createSkillAbility(NamespacedKey abilityKey, NamespacedKey skillKey) {
        return new StubSkillAbility(mcRPG, abilityKey, skillKey);
    }
}
