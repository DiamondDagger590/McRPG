package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.ability.combo.ComboActivatable;
import us.eunoians.mcrpg.ability.impl.type.PassiveAbility;
import us.eunoians.mcrpg.ability.impl.type.UnlockableAbility;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AbilityObjectiveFilterTest {

    @Test
    @DisplayName("Given an EMPTY filter, when matchesAbility is called with any ability, then it returns true")
    public void matchesAbility_returnsTrue_whenFilterIsEmpty() {
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
        assertTrue(AbilityObjectiveFilter.EMPTY.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given a specific key filter, when matchesAbility is called with the same key, then it returns true")
    public void matchesAbility_returnsTrue_whenKeyMatchesFilter() {
        NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(key, null);
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getAbilityKey()).thenReturn(key);
        assertTrue(filter.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given a specific key filter, when matchesAbility is called with a different key, then it returns false")
    public void matchesAbility_returnsFalse_whenKeyDoesNotMatchFilter() {
        NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(key, null);
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "other"));
        assertFalse(filter.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given an ACTIVE type filter, when matchesAbility is called with an ACTIVE ability, then it returns true")
    public void matchesAbility_returnsTrue_whenActiveFilterAndAbilityIsActive() {
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.ACTIVE);
        Ability mockAbility = mock(Ability.class, withSettings().extraInterfaces(ComboActivatable.class));
        when(mockAbility.getAbilityType()).thenReturn(AbilityType.ACTIVE);
        assertTrue(filter.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given an ACTIVE type filter, when matchesAbility is called with an INNATE ability, then it returns false")
    public void matchesAbility_returnsFalse_whenActiveFilterAndAbilityIsInnate() {
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.ACTIVE);
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getAbilityType()).thenReturn(AbilityType.INNATE);
        assertFalse(filter.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given a PASSIVE type filter, when matchesAbility is called with a PASSIVE ability, then it returns true")
    public void matchesAbility_returnsTrue_whenPassiveFilterAndAbilityIsPassive() {
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.PASSIVE);
        Ability mockAbility = mock(Ability.class, withSettings().extraInterfaces(PassiveAbility.class, UnlockableAbility.class));
        when(mockAbility.getAbilityType()).thenReturn(AbilityType.PASSIVE);
        assertTrue(filter.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given a PASSIVE type filter, when matchesAbility is called with an INNATE passive ability, then it returns false")
    public void matchesAbility_returnsFalse_whenPassiveFilterAndAbilityIsInnatePassive() {
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.PASSIVE);
        Ability mockAbility = mock(Ability.class, withSettings().extraInterfaces(PassiveAbility.class));
        when(mockAbility.getAbilityType()).thenReturn(AbilityType.INNATE);
        assertFalse(filter.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given an INNATE type filter, when matchesAbility is called with an INNATE ability, then it returns true")
    public void matchesAbility_returnsTrue_whenInnateFilterAndAbilityIsInnate() {
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.INNATE);
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getAbilityType()).thenReturn(AbilityType.INNATE);
        assertTrue(filter.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given an INNATE type filter, when matchesAbility is called with an ACTIVE ability, then it returns false")
    public void matchesAbility_returnsFalse_whenInnateFilterAndAbilityIsActive() {
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.INNATE);
        Ability mockAbility = mock(Ability.class, withSettings().extraInterfaces(ComboActivatable.class));
        when(mockAbility.getAbilityType()).thenReturn(AbilityType.ACTIVE);
        assertFalse(filter.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given an INNATE type filter, when matchesAbility is called with a PASSIVE ability, then it returns false")
    public void matchesAbility_returnsFalse_whenInnateFilterAndAbilityIsPassive() {
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.INNATE);
        Ability mockAbility = mock(Ability.class, withSettings().extraInterfaces(PassiveAbility.class, UnlockableAbility.class));
        when(mockAbility.getAbilityType()).thenReturn(AbilityType.PASSIVE);
        assertFalse(filter.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given both key and type filters, when matchesAbility is called with matching key, then key filter takes priority")
    public void matchesAbility_returnsTrue_whenKeyMatchesRegardlessOfType() {
        NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(key, AbilityType.ACTIVE);
        Ability matchingKey = mock(Ability.class);
        when(matchingKey.getAbilityKey()).thenReturn(key);
        assertTrue(filter.matchesAbility(matchingKey));
    }

    @Test
    @DisplayName("Given both key and type filters, when matchesAbility is called with wrong key, then it returns false")
    public void matchesAbility_returnsFalse_whenKeyDoesNotMatchRegardlessOfType() {
        NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(key, AbilityType.ACTIVE);
        Ability wrongKey = mock(Ability.class, withSettings().extraInterfaces(ComboActivatable.class));
        when(wrongKey.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "other"));
        assertFalse(filter.matchesAbility(wrongKey));
    }

    @Test
    @DisplayName("Given no key filter, when matchesAbilityKey is called, then it returns true")
    public void matchesAbilityKey_returnsTrue_whenNoKeyFilter() {
        assertTrue(AbilityObjectiveFilter.EMPTY.matchesAbilityKey(new NamespacedKey("mcrpg", "anything")));
    }

    @Test
    @DisplayName("Given a key filter, when matchesAbilityKey is called with matching key, then it returns true")
    public void matchesAbilityKey_returnsTrue_whenKeyMatches() {
        NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(key, null);
        assertTrue(filter.matchesAbilityKey(key));
    }

    @Test
    @DisplayName("Given a key filter, when matchesAbilityKey is called with different key, then it returns false")
    public void matchesAbilityKey_returnsFalse_whenKeyDiffers() {
        NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(key, null);
        assertFalse(filter.matchesAbilityKey(new NamespacedKey("mcrpg", "other")));
    }

    @Test
    @DisplayName("Given an EMPTY filter, when getAbilityFilter is called, then it returns empty")
    public void getAbilityFilter_returnsEmpty_whenNoKeyFilter() {
        assertTrue(AbilityObjectiveFilter.EMPTY.getAbilityFilter().isEmpty());
    }

    @Test
    @DisplayName("Given a key filter, when getAbilityFilter is called, then it returns the key")
    public void getAbilityFilter_returnsPresent_whenKeyFilterSet() {
        NamespacedKey key = new NamespacedKey("mcrpg", "bleed");
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(key, null);
        assertTrue(filter.getAbilityFilter().isPresent());
        assertEquals(key, filter.getAbilityFilter().get());
    }

    @Test
    @DisplayName("Given an EMPTY filter, when getAbilityTypeFilter is called, then it returns empty")
    public void getAbilityTypeFilter_returnsEmpty_whenNoTypeFilter() {
        assertTrue(AbilityObjectiveFilter.EMPTY.getAbilityTypeFilter().isEmpty());
    }

    @Test
    @DisplayName("Given a type filter, when getAbilityTypeFilter is called, then it returns the type")
    public void getAbilityTypeFilter_returnsPresent_whenTypeFilterSet() {
        AbilityObjectiveFilter filter = new AbilityObjectiveFilter(null, AbilityType.PASSIVE);
        assertTrue(filter.getAbilityTypeFilter().isPresent());
        assertEquals(AbilityType.PASSIVE, filter.getAbilityTypeFilter().get());
    }

    @Test
    @DisplayName("Given valid ability type strings, when fromString is called, then it parses case-insensitively")
    public void fromString_returnsType_whenValueIsValid() {
        assertEquals(AbilityType.ACTIVE, AbilityType.fromString("ACTIVE").orElseThrow());
        assertEquals(AbilityType.PASSIVE, AbilityType.fromString("passive").orElseThrow());
        assertEquals(AbilityType.INNATE, AbilityType.fromString("Innate").orElseThrow());
    }

    @Test
    @DisplayName("Given invalid or blank strings, when fromString is called, then it returns empty")
    public void fromString_returnsEmpty_whenValueIsInvalidOrBlank() {
        assertTrue(AbilityType.fromString("UNKNOWN").isEmpty());
        assertTrue(AbilityType.fromString("").isEmpty());
        assertTrue(AbilityType.fromString(null).isEmpty());
    }

    @Test
    @DisplayName("Given a ComboActivatable ability, when getAbilityType is called, then it returns ACTIVE")
    public void getAbilityType_returnsActive_whenComboActivatable() {
        Ability active = mock(Ability.class, withSettings().extraInterfaces(ComboActivatable.class));
        when(active.getAbilityType()).thenCallRealMethod();
        assertEquals(AbilityType.ACTIVE, active.getAbilityType());
    }

    @Test
    @DisplayName("Given a PassiveAbility and UnlockableAbility, when getAbilityType is called, then it returns PASSIVE")
    public void getAbilityType_returnsPassive_whenPassiveAndUnlockable() {
        Ability passive = mock(Ability.class, withSettings().extraInterfaces(PassiveAbility.class, UnlockableAbility.class));
        when(passive.getAbilityType()).thenCallRealMethod();
        assertEquals(AbilityType.PASSIVE, passive.getAbilityType());
    }

    @Test
    @DisplayName("Given a PassiveAbility without UnlockableAbility, when getAbilityType is called, then it returns INNATE")
    public void getAbilityType_returnsInnate_whenPassiveWithoutUnlockable() {
        Ability innatePassive = mock(Ability.class, withSettings().extraInterfaces(PassiveAbility.class));
        when(innatePassive.getAbilityType()).thenCallRealMethod();
        assertEquals(AbilityType.INNATE, innatePassive.getAbilityType());
    }

    @Test
    @DisplayName("Given a plain ability with no special interfaces, when getAbilityType is called, then it returns INNATE")
    public void getAbilityType_returnsInnate_whenPlainAbility() {
        Ability plain = mock(Ability.class);
        when(plain.getAbilityType()).thenCallRealMethod();
        assertEquals(AbilityType.INNATE, plain.getAbilityType());
    }

    @Test
    @DisplayName("Given the NEVER_MATCH filter, when matchesAbility is called with an ability whose key is not mcrpg:unknown, then it returns false")
    public void matchesAbility_returnsFalse_whenFilterIsNeverMatch() {
        Ability mockAbility = mock(Ability.class);
        when(mockAbility.getAbilityKey()).thenReturn(new NamespacedKey("mcrpg", "bleed"));
        assertFalse(AbilityObjectiveFilter.NEVER_MATCH.matchesAbility(mockAbility));
    }

    @Test
    @DisplayName("Given the NEVER_MATCH filter, when matchesAbilityKey is called with a real ability key, then it returns false")
    public void matchesAbilityKey_returnsFalse_whenFilterIsNeverMatch() {
        assertFalse(AbilityObjectiveFilter.NEVER_MATCH.matchesAbilityKey(new NamespacedKey("mcrpg", "bleed")));
    }
}
