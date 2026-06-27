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
import us.eunoians.mcrpg.ability.StubTierableAbility;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("UnlockableAbilityFilter")
class UnlockableAbilityFilterTest extends McRPGBaseTest {

    private UnlockableAbilityFilter filter;
    private McRPGPlayer mcRPGPlayer;

    @BeforeEach
    void setUp(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.filter = new UnlockableAbilityFilter();
        addPlayerToServer(mcRPGPlayer);
    }

    @Nested
    @DisplayName("filter")
    class Filter {

        @Test
        @DisplayName("keeps unlockable abilities")
        void filter_unlockableAbility_kept() {
            Ability unlockable = new StubTierableAbility(mcRPG, new NamespacedKey("test", "unlockable"));
            List<Ability> input = List.of(unlockable);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertEquals(1, result.size());
            assertTrue(result.contains(unlockable));
        }

        @Test
        @DisplayName("removes non-unlockable abilities")
        void filter_nonUnlockableAbility_removed() {
            Ability plain = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain"), false);
            List<Ability> input = List.of(plain);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("filters mixed collection correctly")
        void filter_mixedCollection_keepsOnlyUnlockable() {
            Ability unlockable = new StubTierableAbility(mcRPG, new NamespacedKey("test", "unlockable"));
            Ability plain = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain"), true);
            List<Ability> input = List.of(unlockable, plain);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertEquals(1, result.size());
            assertTrue(result.contains(unlockable));
        }

        @Test
        @DisplayName("returns empty for empty input")
        void filter_emptyInput_returnsEmpty() {
            Collection<Ability> result = filter.filter(mcRPGPlayer, List.of());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("keeps multiple unlockable abilities")
        void filter_multipleUnlockable_keepsAll() {
            Ability first = new StubTierableAbility(mcRPG, new NamespacedKey("test", "first"));
            Ability second = new StubTierableAbility(mcRPG, new NamespacedKey("test", "second"));
            List<Ability> input = List.of(first, second);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("all non-unlockable input returns empty")
        void filter_allNonUnlockable_returnsEmpty() {
            Ability first = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain1"), false);
            Ability second = new StubPlainAbility(mcRPG, new NamespacedKey("test", "plain2"), true);
            List<Ability> input = List.of(first, second);

            Collection<Ability> result = filter.filter(mcRPGPlayer, input);

            assertTrue(result.isEmpty());
        }
    }
}
