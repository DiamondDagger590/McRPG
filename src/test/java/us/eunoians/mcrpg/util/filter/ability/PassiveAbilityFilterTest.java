package us.eunoians.mcrpg.util.filter.ability;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.StubPlainAbility;
import us.eunoians.mcrpg.ability.StubUnlockableActiveAbility;
import us.eunoians.mcrpg.ability.StubUnlockablePassiveAbility;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("PassiveAbilityFilter")
class PassiveAbilityFilterTest extends McRPGBaseTest {

    private PassiveAbilityFilter filter;
    private McRPGPlayer mcRPGPlayer;

    @BeforeEach
    void setUp(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.filter = new PassiveAbilityFilter();
        addPlayerToServer(mcRPGPlayer);
    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        @DisplayName("keeps unlockable passive abilities")
        void filter_keepsUnlockablePassiveAbilities() {
            Ability passiveUnlockable = new StubUnlockablePassiveAbility(mcRPG, new NamespacedKey("test", "passive_unlockable"));
            List<Ability> input = List.of(passiveUnlockable);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertEquals(1, result.size());
            assertTrue(result.contains(passiveUnlockable));
        }

        @Test
        @DisplayName("removes unlockable active abilities")
        void filter_removesUnlockableActiveAbilities() {
            Ability activeUnlockable = new StubUnlockableActiveAbility(mcRPG, new NamespacedKey("test", "active_unlockable"));
            List<Ability> input = List.of(activeUnlockable);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removes non-unlockable passive abilities")
        void filter_removesNonUnlockablePassiveAbilities() {
            Ability plain = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain_passive"), true);
            List<Ability> input = List.of(plain);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("filters mixed collection correctly")
        void filter_mixedCollection_keepsOnlyPassiveUnlockable() {
            Ability passiveUnlockable = new StubUnlockablePassiveAbility(mcRPG, new NamespacedKey("test", "passive"));
            Ability activeUnlockable = new StubUnlockableActiveAbility(mcRPG, new NamespacedKey("test", "active"));
            Ability plainPassive = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain_passive"), true);
            Ability plainActive = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain_active"), false);
            List<Ability> input = List.of(passiveUnlockable, activeUnlockable, plainPassive, plainActive);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertEquals(1, result.size());
            assertTrue(result.contains(passiveUnlockable));
        }

        @Test
        @DisplayName("returns empty for empty input")
        void filter_emptyInput_returnsEmpty() {
            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("keeps multiple passive unlockable abilities")
        void filter_multiplePassiveUnlockable_keepsAll() {
            Ability first = new StubUnlockablePassiveAbility(mcRPG, new NamespacedKey("test", "first"));
            Ability second = new StubUnlockablePassiveAbility(mcRPG, new NamespacedKey("test", "second"));
            List<Ability> input = List.of(first, second);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertEquals(2, result.size());
        }
    }
}
