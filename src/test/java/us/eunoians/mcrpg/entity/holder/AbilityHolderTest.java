package us.eunoians.mcrpg.entity.holder;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.MockAbility;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilityHolderTest extends McRPGBaseTest {

    private AbilityHolder holder;
    private MockAbility mockAbility;
    private NamespacedKey abilityKey;

    @BeforeEach
    void setUp() {
        AbilityAttributeRegistry attributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(attributeRegistry);

        AbilityRegistry abilityRegistry = new AbilityRegistry(mcRPG);
        RegistryAccess.registryAccess().register(abilityRegistry);

        mockAbility = new MockAbility(mcRPG);
        abilityKey = mockAbility.getAbilityKey();
        abilityRegistry.register(mockAbility);

        holder = new AbilityHolder(mcRPG, UUID.randomUUID());
    }

    @Nested
    @DisplayName("Available abilities")
    class AvailableAbilities {

        @Test
        @DisplayName("getAvailableAbilities returns empty set initially")
        void getAvailableAbilities_returnsEmptySet_initially() {
            assertTrue(holder.getAvailableAbilities().isEmpty());
        }

        @Test
        @DisplayName("addAvailableAbility by key makes ability available")
        void addAvailableAbility_byKey_makesAbilityAvailable() {
            holder.addAvailableAbility(abilityKey);
            assertTrue(holder.isAbilityAvailable(abilityKey));
        }

        @Test
        @DisplayName("addAvailableAbility by ability makes ability available")
        void addAvailableAbility_byAbility_makesAbilityAvailable() {
            holder.addAvailableAbility(mockAbility);
            assertTrue(holder.isAbilityAvailable(mockAbility));
        }

        @Test
        @DisplayName("isAbilityAvailable returns false for unregistered ability")
        void isAbilityAvailable_returnsFalse_whenNotAdded() {
            assertFalse(holder.isAbilityAvailable(abilityKey));
        }

        @Test
        @DisplayName("removeAvailableAbility by key removes ability")
        void removeAvailableAbility_byKey_removesAbility() {
            holder.addAvailableAbility(abilityKey);
            holder.removeAvailableAbility(abilityKey);
            assertFalse(holder.isAbilityAvailable(abilityKey));
        }

        @Test
        @DisplayName("removeAvailableAbility by ability removes ability")
        void removeAvailableAbility_byAbility_removesAbility() {
            holder.addAvailableAbility(mockAbility);
            holder.removeAvailableAbility(mockAbility);
            assertFalse(holder.isAbilityAvailable(mockAbility));
        }

        @Test
        @DisplayName("getAvailableAbilities returns immutable copy")
        void getAvailableAbilities_returnsImmutableCopy() {
            holder.addAvailableAbility(abilityKey);
            Set<NamespacedKey> snapshot = holder.getAvailableAbilities();
            assertEquals(1, snapshot.size());
            assertTrue(snapshot.contains(abilityKey));
        }

        @Test
        @DisplayName("addAvailableAbility ignores unregistered ability key")
        void addAvailableAbility_ignoresUnregisteredKey() {
            NamespacedKey unknownKey = new NamespacedKey(mcRPG, "unknown-ability");
            holder.addAvailableAbility(unknownKey);
            assertFalse(holder.isAbilityAvailable(unknownKey));
            assertTrue(holder.getAvailableAbilities().isEmpty());
        }
    }

    @Nested
    @DisplayName("Ability data")
    class AbilityDataTests {

        @Test
        @DisplayName("hasAbilityData returns false initially")
        void hasAbilityData_returnsFalse_initially() {
            assertFalse(holder.hasAbilityData(abilityKey));
        }

        @Test
        @DisplayName("addAbilityData makes data retrievable")
        void addAbilityData_makesDataRetrievable() {
            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);

            assertTrue(holder.hasAbilityData(abilityKey));
            Optional<AbilityData> result = holder.getAbilityData(abilityKey);
            assertTrue(result.isPresent());
            assertSame(data, result.get());
        }

        @Test
        @DisplayName("hasAbilityData by ability delegates correctly")
        void hasAbilityData_byAbility_delegatesCorrectly() {
            assertFalse(holder.hasAbilityData(mockAbility));

            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);
            assertTrue(holder.hasAbilityData(mockAbility));
        }

        @Test
        @DisplayName("getAbilityData by ability delegates correctly")
        void getAbilityData_byAbility_delegatesCorrectly() {
            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);

            Optional<AbilityData> result = holder.getAbilityData(mockAbility);
            assertTrue(result.isPresent());
            assertSame(data, result.get());
        }

        @Test
        @DisplayName("removeAbilityData by key removes data")
        void removeAbilityData_byKey_removesData() {
            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);
            holder.removeAbilityData(abilityKey);

            assertFalse(holder.hasAbilityData(abilityKey));
        }

        @Test
        @DisplayName("removeAbilityData by ability removes data")
        void removeAbilityData_byAbility_removesData() {
            AbilityData data = new AbilityData(abilityKey);
            holder.addAbilityData(data);
            holder.removeAbilityData(mockAbility);

            assertFalse(holder.hasAbilityData(mockAbility));
        }

        @Test
        @DisplayName("addAbilityData ignores unregistered ability key")
        void addAbilityData_ignoresUnregisteredKey() {
            NamespacedKey unknownKey = new NamespacedKey(mcRPG, "unknown-ability");
            AbilityData data = new AbilityData(unknownKey);
            holder.addAbilityData(data);

            assertFalse(holder.hasAbilityData(unknownKey));
        }

        @Test
        @DisplayName("getAbilityData returns empty for unregistered key")
        void getAbilityData_returnsEmpty_forUnregisteredKey() {
            NamespacedKey unknownKey = new NamespacedKey(mcRPG, "unknown-ability");
            assertTrue(holder.getAbilityData(unknownKey).isEmpty());
        }

        @Test
        @DisplayName("getAbilityData creates default data when none stored")
        void getAbilityData_createsDefaultData_whenNoneStored() {
            Optional<AbilityData> result = holder.getAbilityData(abilityKey);
            assertTrue(result.isPresent());
            assertEquals(abilityKey, result.get().getAbilityKey());
            assertTrue(holder.hasAbilityData(abilityKey));
        }
    }

    @Nested
    @DisplayName("Active abilities")
    class ActiveAbilities {

        @Test
        @DisplayName("isAbilityActive returns false initially")
        void isAbilityActive_returnsFalse_initially() {
            assertFalse(holder.isAbilityActive(abilityKey));
        }

        @Test
        @DisplayName("addActiveAbility by key makes ability active")
        void addActiveAbility_byKey_makesAbilityActive() {
            holder.addActiveAbility(abilityKey);
            assertTrue(holder.isAbilityActive(abilityKey));
        }

        @Test
        @DisplayName("addActiveAbility by ability makes ability active")
        void addActiveAbility_byAbility_makesAbilityActive() {
            holder.addActiveAbility(mockAbility);
            assertTrue(holder.isAbilityActive(mockAbility));
        }

        @Test
        @DisplayName("removeActiveAbility by key makes ability inactive")
        void removeActiveAbility_byKey_makesAbilityInactive() {
            holder.addActiveAbility(abilityKey);
            holder.removeActiveAbility(abilityKey);
            assertFalse(holder.isAbilityActive(abilityKey));
        }

        @Test
        @DisplayName("removeActiveAbility by ability makes ability inactive")
        void removeActiveAbility_byAbility_makesAbilityInactive() {
            holder.addActiveAbility(mockAbility);
            holder.removeActiveAbility(mockAbility);
            assertFalse(holder.isAbilityActive(mockAbility));
        }

        @Test
        @DisplayName("getCurrentlyActiveAbilities returns immutable copy")
        void getCurrentlyActiveAbilities_returnsImmutableCopy() {
            holder.addActiveAbility(abilityKey);
            Set<NamespacedKey> active = holder.getCurrentlyActiveAbilities();
            assertEquals(1, active.size());
            assertTrue(active.contains(abilityKey));
        }

        @Test
        @DisplayName("getCurrentlyActiveAbilities returns empty set initially")
        void getCurrentlyActiveAbilities_returnsEmptySet_initially() {
            assertTrue(holder.getCurrentlyActiveAbilities().isEmpty());
        }
    }

    @Nested
    @DisplayName("Cleanup")
    class Cleanup {

        @Test
        @DisplayName("cleanupHolder clears active abilities")
        void cleanupHolder_clearsActiveAbilities() {
            holder.addActiveAbility(abilityKey);
            assertTrue(holder.isAbilityActive(abilityKey));

            holder.cleanupHolder();
            assertFalse(holder.isAbilityActive(abilityKey));
            assertTrue(holder.getCurrentlyActiveAbilities().isEmpty());
        }
    }

    @Nested
    @DisplayName("Equality")
    class EqualityTests {

        @Test
        @DisplayName("equals returns true for holders with same UUID")
        void equals_returnsTrue_forSameUuid() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder holder1 = new AbilityHolder(mcRPG, uuid);
            AbilityHolder holder2 = new AbilityHolder(mcRPG, uuid);
            assertEquals(holder1, holder2);
        }

        @Test
        @DisplayName("equals returns false for holders with different UUID")
        void equals_returnsFalse_forDifferentUuid() {
            AbilityHolder holder1 = new AbilityHolder(mcRPG, UUID.randomUUID());
            AbilityHolder holder2 = new AbilityHolder(mcRPG, UUID.randomUUID());
            assertFalse(holder1.equals(holder2));
        }

        @Test
        @DisplayName("equals returns false for non-AbilityHolder")
        void equals_returnsFalse_forNonAbilityHolder() {
            assertFalse(holder.equals("not a holder"));
        }
    }

    @Nested
    @DisplayName("UUID")
    class UuidTests {

        @Test
        @DisplayName("getUUID returns the UUID passed in constructor")
        void getUUID_returnsConstructorUuid() {
            UUID uuid = UUID.randomUUID();
            AbilityHolder uuidHolder = new AbilityHolder(mcRPG, uuid);
            assertEquals(uuid, uuidHolder.getUUID());
        }
    }
}
