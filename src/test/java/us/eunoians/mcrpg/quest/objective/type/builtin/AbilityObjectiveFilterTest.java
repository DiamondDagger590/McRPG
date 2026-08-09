package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@DisplayName("AbilityObjectiveFilter")
public class AbilityObjectiveFilterTest extends McRPGBaseTest {

    @Nested
    @DisplayName("EMPTY sentinel")
    class EmptySentinel {

        @Test
        @DisplayName("matches any ability")
        public void matchesAbility_returnsTrue_forAnyAbility() {
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            assertTrue(AbilityObjectiveFilter.EMPTY.matchesAbility(ability));
        }

        @Test
        @DisplayName("matchesAbilityKey returns true for any key")
        public void matchesAbilityKey_returnsTrue_forAnyKey() {
            assertTrue(AbilityObjectiveFilter.EMPTY.matchesAbilityKey(new NamespacedKey("mcrpg", "bleed")));
        }

        @Test
        @DisplayName("getAbilityFilter returns empty")
        public void getAbilityFilter_returnsEmpty() {
            assertTrue(AbilityObjectiveFilter.EMPTY.getAbilityFilter().isEmpty());
        }

        @Test
        @DisplayName("getAbilityTypeFilter returns empty")
        public void getAbilityTypeFilter_returnsEmpty() {
            assertTrue(AbilityObjectiveFilter.EMPTY.getAbilityTypeFilter().isEmpty());
        }
    }

    @Nested
    @DisplayName("NEVER_MATCH sentinel")
    class NeverMatchSentinel {

        @Test
        @DisplayName("does not match any ability")
        public void matchesAbility_returnsFalse_forAnyAbility() {
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            assertFalse(AbilityObjectiveFilter.NEVER_MATCH.matchesAbility(ability));
        }

        @Test
        @DisplayName("matchesAbilityKey returns false for non-matching key")
        public void matchesAbilityKey_returnsFalse_forNonMatchingKey() {
            assertFalse(AbilityObjectiveFilter.NEVER_MATCH.matchesAbilityKey(new NamespacedKey("mcrpg", "bleed")));
        }

        @Test
        @DisplayName("getAbilityFilter returns the unknown sentinel key")
        public void getAbilityFilter_returnsSentinelKey() {
            assertTrue(AbilityObjectiveFilter.NEVER_MATCH.getAbilityFilter().isPresent());
            assertEquals(new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown"),
                    AbilityObjectiveFilter.NEVER_MATCH.getAbilityFilter().get());
        }
    }

    @Nested
    @DisplayName("Specific ability key filter")
    class SpecificAbilityFilter {

        private final NamespacedKey targetKey = new NamespacedKey("mcrpg", "bleed");
        private final AbilityObjectiveFilter filter = new AbilityObjectiveFilter(targetKey, null);

        @Test
        @DisplayName("matches ability with same key")
        public void matchesAbility_returnsTrue_whenKeyMatches() {
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(targetKey);
            assertTrue(filter.matchesAbility(ability));
        }

        @Test
        @DisplayName("does not match ability with different key")
        public void matchesAbility_returnsFalse_whenKeyDiffers() {
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "vampire"));
            assertFalse(filter.matchesAbility(ability));
        }

        @Test
        @DisplayName("matchesAbilityKey returns true for matching key")
        public void matchesAbilityKey_returnsTrue_whenKeyMatches() {
            assertTrue(filter.matchesAbilityKey(targetKey));
        }

        @Test
        @DisplayName("matchesAbilityKey returns false for non-matching key")
        public void matchesAbilityKey_returnsFalse_whenKeyDiffers() {
            assertFalse(filter.matchesAbilityKey(new NamespacedKey("mcrpg", "vampire")));
        }

        @Test
        @DisplayName("getAbilityFilter returns the configured key")
        public void getAbilityFilter_returnsConfiguredKey() {
            assertTrue(filter.getAbilityFilter().isPresent());
            assertEquals(targetKey, filter.getAbilityFilter().get());
        }

        @Test
        @DisplayName("getAbilityTypeFilter returns empty when only key set")
        public void getAbilityTypeFilter_returnsEmpty() {
            assertTrue(filter.getAbilityTypeFilter().isEmpty());
        }

        @Test
        @DisplayName("key filter takes priority over type even when ability type matches")
        public void matchesAbility_usesKeyPriority_overType() {
            AbilityObjectiveFilter filterWithBoth = new AbilityObjectiveFilter(targetKey, AbilityType.PASSIVE);
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "vampire"));
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            assertFalse(filterWithBoth.matchesAbility(ability));
        }

        @Test
        @DisplayName("key filter matches regardless of type when key matches")
        public void matchesAbility_returnsTrue_whenKeyMatchesRegardlessOfType() {
            AbilityObjectiveFilter filterWithBoth = new AbilityObjectiveFilter(targetKey, AbilityType.ACTIVE);
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(targetKey);
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            assertTrue(filterWithBoth.matchesAbility(ability));
        }
    }

    @Nested
    @DisplayName("Ability type filter")
    class AbilityTypeFilterTests {

        @Test
        @DisplayName("PASSIVE filter matches PASSIVE abilities")
        public void matchesAbility_returnsTrue_whenPassiveMatchesPassive() {
            AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.PASSIVE);
            Ability ability = mock(Ability.class);
            when(ability.getAbilityType()).thenReturn(AbilityType.PASSIVE);
            assertTrue(filter.matchesAbility(ability));
        }

        @Test
        @DisplayName("PASSIVE filter does not match ACTIVE abilities")
        public void matchesAbility_returnsFalse_whenPassiveDoesNotMatchActive() {
            AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.PASSIVE);
            Ability ability = mock(Ability.class);
            when(ability.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            assertFalse(filter.matchesAbility(ability));
        }

        @Test
        @DisplayName("PASSIVE filter does not match INNATE abilities")
        public void matchesAbility_returnsFalse_whenPassiveDoesNotMatchInnate() {
            AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.PASSIVE);
            Ability ability = mock(Ability.class);
            when(ability.getAbilityType()).thenReturn(AbilityType.INNATE);
            assertFalse(filter.matchesAbility(ability));
        }

        @Test
        @DisplayName("ACTIVE filter matches ACTIVE abilities")
        public void matchesAbility_returnsTrue_whenActiveMatchesActive() {
            AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.ACTIVE);
            Ability ability = mock(Ability.class);
            when(ability.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            assertTrue(filter.matchesAbility(ability));
        }

        @Test
        @DisplayName("ACTIVE filter does not match INNATE abilities")
        public void matchesAbility_returnsFalse_whenActiveDoesNotMatchInnate() {
            AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.ACTIVE);
            Ability ability = mock(Ability.class);
            when(ability.getAbilityType()).thenReturn(AbilityType.INNATE);
            assertFalse(filter.matchesAbility(ability));
        }

        @Test
        @DisplayName("INNATE filter matches INNATE abilities")
        public void matchesAbility_returnsTrue_whenInnateMatchesInnate() {
            AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.INNATE);
            Ability ability = mock(Ability.class);
            when(ability.getAbilityType()).thenReturn(AbilityType.INNATE);
            assertTrue(filter.matchesAbility(ability));
        }

        @Test
        @DisplayName("INNATE filter does not match ACTIVE abilities")
        public void matchesAbility_returnsFalse_whenInnateDoesNotMatchActive() {
            AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.INNATE);
            Ability ability = mock(Ability.class);
            when(ability.getAbilityType()).thenReturn(AbilityType.ACTIVE);
            assertFalse(filter.matchesAbility(ability));
        }

        @Test
        @DisplayName("getAbilityFilter returns empty when only type set")
        public void getAbilityFilter_returnsEmpty() {
            AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.PASSIVE);
            assertTrue(filter.getAbilityFilter().isEmpty());
        }

        @Test
        @DisplayName("getAbilityTypeFilter returns the configured type")
        public void getAbilityTypeFilter_returnsConfiguredType() {
            AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.PASSIVE);
            assertTrue(filter.getAbilityTypeFilter().isPresent());
            assertEquals(AbilityType.PASSIVE, filter.getAbilityTypeFilter().get());
        }
    }

    @Nested
    @DisplayName("resolveAbilityName")
    class ResolveAbilityName {

        @BeforeEach
        public void ensureAbilityRegistry() {
            RegistryAccess registryAccess = RegistryAccess.registryAccess();
            if (registryAccess.registry(McRPGRegistryKey.ABILITY) == null) {
                registryAccess.register(new AbilityRegistry(mcRPG));
            }
        }

        @Test
        @DisplayName("returns raw key for unregistered ability")
        public void resolveAbilityName_returnsRawKey_whenNotRegistered() {
            NamespacedKey unknownKey = new NamespacedKey("mcrpg", "nonexistent_ability");
            String name = AbilityObjectiveFilter.EMPTY.resolveAbilityName(unknownKey);
            assertEquals("nonexistent_ability", name);
        }

        @Test
        @DisplayName("returns ability name for registered ability")
        public void resolveAbilityName_returnsAbilityName_whenRegistered() {
            NamespacedKey bleedKey = new NamespacedKey("mcrpg", "test_resolve");
            Ability ability = mock(Ability.class);
            when(ability.getAbilityKey()).thenReturn(bleedKey);
            when(ability.getName()).thenReturn("Test Resolve");
            RegistryAccess.registryAccess().registry(McRPGRegistryKey.ABILITY).register(ability);

            String name = AbilityObjectiveFilter.EMPTY.resolveAbilityName(bleedKey);
            assertEquals("Test Resolve", name);
        }
    }

    @Nested
    @DisplayName("AbilityType.fromString")
    class AbilityTypeFromString {

        @Test
        @DisplayName("parses valid types case-insensitively")
        public void fromString_returnsType_whenValid() {
            assertEquals(AbilityType.ACTIVE, AbilityType.fromString("ACTIVE").orElseThrow());
            assertEquals(AbilityType.PASSIVE, AbilityType.fromString("passive").orElseThrow());
            assertEquals(AbilityType.INNATE, AbilityType.fromString("Innate").orElseThrow());
        }

        @Test
        @DisplayName("returns empty for invalid values")
        public void fromString_returnsEmpty_whenInvalid() {
            assertTrue(AbilityType.fromString("UNKNOWN").isEmpty());
            assertTrue(AbilityType.fromString("").isEmpty());
        }

        @Test
        @DisplayName("returns empty for null")
        public void fromString_returnsEmpty_whenNull() {
            assertTrue(AbilityType.fromString(null).isEmpty());
        }
    }

    @Nested
    @DisplayName("Ability.getAbilityType default method")
    class AbilityTypeDefault {

        @Test
        @DisplayName("ComboActivatable returns ACTIVE")
        public void getAbilityType_returnsActive_whenComboActivatable() {
            Ability active = mock(Ability.class, withSettings().extraInterfaces(ComboActivatable.class));
            when(active.getAbilityType()).thenCallRealMethod();
            assertEquals(AbilityType.ACTIVE, active.getAbilityType());
        }

        @Test
        @DisplayName("PassiveAbility + UnlockableAbility returns PASSIVE")
        public void getAbilityType_returnsPassive_whenPassiveAndUnlockable() {
            Ability passive = mock(Ability.class, withSettings().extraInterfaces(PassiveAbility.class, UnlockableAbility.class));
            when(passive.getAbilityType()).thenCallRealMethod();
            assertEquals(AbilityType.PASSIVE, passive.getAbilityType());
        }

        @Test
        @DisplayName("PassiveAbility without UnlockableAbility returns INNATE")
        public void getAbilityType_returnsInnate_whenPassiveWithoutUnlockable() {
            Ability innate = mock(Ability.class, withSettings().extraInterfaces(PassiveAbility.class));
            when(innate.getAbilityType()).thenCallRealMethod();
            assertEquals(AbilityType.INNATE, innate.getAbilityType());
        }

        @Test
        @DisplayName("plain ability with no interfaces returns INNATE")
        public void getAbilityType_returnsInnate_whenPlainAbility() {
            Ability plain = mock(Ability.class);
            when(plain.getAbilityType()).thenCallRealMethod();
            assertEquals(AbilityType.INNATE, plain.getAbilityType());
        }
    }
}
