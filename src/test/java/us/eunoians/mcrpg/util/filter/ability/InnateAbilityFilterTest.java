package us.eunoians.mcrpg.util.filter.ability;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.StubTierableAbility;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityUnlockedAttribute;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("InnateAbilityFilter")
class InnateAbilityFilterTest extends McRPGBaseTest {

    private InnateAbilityFilter filter;
    private McRPGPlayer mcRPGPlayer;
    private Set<NamespacedKey> registeredAbilityKeys;

    @BeforeEach
    void setUp(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.filter = new InnateAbilityFilter();
        this.registeredAbilityKeys = new HashSet<>();
        addPlayerToServer(mcRPGPlayer);

        StubTierableAbility defaultAbility = new StubTierableAbility(mcRPG, new NamespacedKey("test", "default"));
        AbilityRegistry mockAbilityRegistry = mock(AbilityRegistry.class);
        when(mockAbilityRegistry.registered(any(NamespacedKey.class)))
                .thenAnswer(invocation -> registeredAbilityKeys.contains(invocation.getArgument(0)));
        when(mockAbilityRegistry.getRegisteredAbility(any(NamespacedKey.class))).thenReturn(defaultAbility);
        RegistryAccess.registryAccess().register(mockAbilityRegistry);

        AbilityAttributeRegistry abilityAttributeRegistry = new AbilityAttributeRegistry();
        RegistryAccess.registryAccess().register(abilityAttributeRegistry);
    }

    private void registerAbilityKey(NamespacedKey key) {
        registeredAbilityKeys.add(key);
    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        @DisplayName("keeps ability with data but no unlocked attribute")
        void filter_abilityWithDataNoUnlockedAttribute_kept() {
            Ability ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "innate"));
            registerAbilityKey(ability.getAbilityKey());
            AbilityData data = new AbilityData(ability.getAbilityKey());
            mcRPGPlayer.asSkillHolder().addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertEquals(1, result.size());
            assertTrue(result.contains(ability));
        }

        @Test
        @DisplayName("removes ability with unlocked attribute present")
        void filter_abilityWithUnlockedAttribute_removed() {
            Ability ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "unlockable"));
            registerAbilityKey(ability.getAbilityKey());
            AbilityData data = new AbilityData(ability.getAbilityKey(), new AbilityUnlockedAttribute(true));
            mcRPGPlayer.asSkillHolder().addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removes ability with unlocked attribute set to false")
        void filter_abilityWithUnlockedAttributeFalse_removed() {
            Ability ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "locked"));
            registerAbilityKey(ability.getAbilityKey());
            AbilityData data = new AbilityData(ability.getAbilityKey(), new AbilityUnlockedAttribute(false));
            mcRPGPlayer.asSkillHolder().addAbilityData(data);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removes ability with no data at all")
        void filter_abilityWithNoData_removed() {
            Ability ability = new StubTierableAbility(mcRPG, new NamespacedKey("test", "no_data"));

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(ability));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("filters mixed collection correctly")
        void filter_mixedCollection_keepsOnlyInnate() {
            Ability innate = new StubTierableAbility(mcRPG, new NamespacedKey("test", "innate"));
            registerAbilityKey(innate.getAbilityKey());
            AbilityData innateData = new AbilityData(innate.getAbilityKey());
            mcRPGPlayer.asSkillHolder().addAbilityData(innateData);

            Ability unlockable = new StubTierableAbility(mcRPG, new NamespacedKey("test", "unlockable"));
            registerAbilityKey(unlockable.getAbilityKey());
            AbilityData unlockableData = new AbilityData(unlockable.getAbilityKey(), new AbilityUnlockedAttribute(true));
            mcRPGPlayer.asSkillHolder().addAbilityData(unlockableData);

            Ability noData = new StubTierableAbility(mcRPG, new NamespacedKey("test", "no_data"));

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(innate, unlockable, noData));

            assertEquals(1, result.size());
            assertTrue(result.contains(innate));
        }

        @Test
        @DisplayName("returns empty for empty input")
        void filter_emptyInput_returnsEmpty() {
            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("keeps multiple innate abilities")
        void filter_multipleInnate_keepsAll() {
            Ability first = new StubTierableAbility(mcRPG, new NamespacedKey("test", "innate1"));
            registerAbilityKey(first.getAbilityKey());
            AbilityData firstData = new AbilityData(first.getAbilityKey());
            mcRPGPlayer.asSkillHolder().addAbilityData(firstData);

            Ability second = new StubTierableAbility(mcRPG, new NamespacedKey("test", "innate2"));
            registerAbilityKey(second.getAbilityKey());
            AbilityData secondData = new AbilityData(second.getAbilityKey());
            mcRPGPlayer.asSkillHolder().addAbilityData(secondData);

            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of(first, second));

            assertEquals(2, result.size());
        }
    }
}
