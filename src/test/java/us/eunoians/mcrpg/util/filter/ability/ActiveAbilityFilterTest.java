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
@DisplayName("ActiveAbilityFilter")
class ActiveAbilityFilterTest extends McRPGBaseTest {

    private ActiveAbilityFilter filter;
    private McRPGPlayer mcRPGPlayer;

    @BeforeEach
    void setUp(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.filter = new ActiveAbilityFilter();
        addPlayerToServer(mcRPGPlayer);
    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        @DisplayName("keeps unlockable active abilities")
        void filter_keepsUnlockableActiveAbilities() {
            Ability activeUnlockable = new StubUnlockableActiveAbility(mcRPG, new NamespacedKey("test", "active_unlockable"));
            List<Ability> input = List.of(activeUnlockable);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertEquals(1, result.size());
            assertTrue(result.contains(activeUnlockable));
        }

        @Test
        @DisplayName("removes passive unlockable abilities")
        void filter_removesPassiveUnlockableAbilities() {
            Ability passiveUnlockable = new StubUnlockablePassiveAbility(mcRPG, new NamespacedKey("test", "passive_unlockable"));
            List<Ability> input = List.of(passiveUnlockable);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("removes non-unlockable abilities")
        void filter_removesNonUnlockableAbilities() {
            Ability plain = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain"), false);
            List<Ability> input = List.of(plain);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("filters mixed collection correctly")
        void filter_mixedCollection_keepsOnlyActiveUnlockable() {
            Ability activeUnlockable = new StubUnlockableActiveAbility(mcRPG, new NamespacedKey("test", "active"));
            Ability passiveUnlockable = new StubUnlockablePassiveAbility(mcRPG, new NamespacedKey("test", "passive"));
            Ability plain = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain"), false);
            List<Ability> input = List.of(activeUnlockable, passiveUnlockable, plain);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertEquals(1, result.size());
            assertTrue(result.contains(activeUnlockable));
        }

        @Test
        @DisplayName("returns empty for empty input")
        void filter_emptyInput_returnsEmpty() {
            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("keeps multiple active unlockable abilities")
        void filter_multipleActiveUnlockable_keepsAll() {
            Ability first = new StubUnlockableActiveAbility(mcRPG, new NamespacedKey("test", "first"));
            Ability second = new StubUnlockableActiveAbility(mcRPG, new NamespacedKey("test", "second"));
            List<Ability> input = List.of(first, second);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertEquals(2, result.size());
        }
    }
}
