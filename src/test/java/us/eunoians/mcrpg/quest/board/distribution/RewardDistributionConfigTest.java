package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("RewardDistributionConfig")
public class RewardDistributionConfigTest extends McRPGBaseTest {

    private DistributionTierConfig createTier(String tierKey) {
        return new DistributionTierConfig(
                tierKey,
                new NamespacedKey("mcrpg", "top_players"),
                RewardSplitMode.INDIVIDUAL,
                List.of(new DistributionRewardEntry(mock(QuestRewardType.class))),
                Map.of(),
                null,
                null);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @DisplayName("creates defensive copy of tiers list")
        @Test
        void constructor_defensiveCopy_inputMutationDoesNotAffectConfig() {
            List<DistributionTierConfig> mutableList = new ArrayList<>();
            mutableList.add(createTier("tier-1"));
            var config = new RewardDistributionConfig(mutableList);

            mutableList.add(createTier("tier-2"));

            assertEquals(1, config.getTiers().size());
        }

        @DisplayName("getTiers returns immutable list")
        @Test
        void getTiers_returnsImmutableList() {
            var config = new RewardDistributionConfig(List.of(createTier("tier-1")));

            assertThrows(UnsupportedOperationException.class,
                    () -> config.getTiers().add(createTier("tier-2")));
        }

        @DisplayName("preserves tier order")
        @Test
        void constructor_preservesTierOrder() {
            DistributionTierConfig first = createTier("first");
            DistributionTierConfig second = createTier("second");
            DistributionTierConfig third = createTier("third");

            var config = new RewardDistributionConfig(List.of(first, second, third));

            assertEquals(3, config.getTiers().size());
            assertEquals("first", config.getTiers().get(0).getTierKey());
            assertEquals("second", config.getTiers().get(1).getTierKey());
            assertEquals("third", config.getTiers().get(2).getTierKey());
        }

        @DisplayName("accepts empty tier list")
        @Test
        void constructor_emptyList_isValid() {
            var config = new RewardDistributionConfig(List.of());

            assertTrue(config.getTiers().isEmpty());
        }
    }

    @Nested
    @DisplayName("isEmpty")
    class IsEmptyTests {

        @DisplayName("returns true when no tiers")
        @Test
        void isEmpty_noTiers_returnsTrue() {
            var config = new RewardDistributionConfig(List.of());

            assertTrue(config.isEmpty());
        }

        @DisplayName("returns false when tiers present")
        @Test
        void isEmpty_tiersPresent_returnsFalse() {
            var config = new RewardDistributionConfig(List.of(createTier("tier-1")));

            assertFalse(config.isEmpty());
        }

        @DisplayName("returns false with multiple tiers")
        @Test
        void isEmpty_multipleTiers_returnsFalse() {
            var config = new RewardDistributionConfig(
                    List.of(createTier("tier-1"), createTier("tier-2")));

            assertFalse(config.isEmpty());
        }
    }

    @Nested
    @DisplayName("getTiers")
    class GetTiersTests {

        @DisplayName("returns all configured tiers")
        @Test
        void getTiers_returnsAllTiers() {
            DistributionTierConfig tier1 = createTier("alpha");
            DistributionTierConfig tier2 = createTier("beta");
            var config = new RewardDistributionConfig(List.of(tier1, tier2));

            List<DistributionTierConfig> tiers = config.getTiers();

            assertEquals(2, tiers.size());
            assertTrue(tiers.contains(tier1));
            assertTrue(tiers.contains(tier2));
        }

        @DisplayName("returns same list on repeated calls")
        @Test
        void getTiers_repeatedCalls_returnsSameList() {
            var config = new RewardDistributionConfig(List.of(createTier("tier-1")));

            List<DistributionTierConfig> first = config.getTiers();
            List<DistributionTierConfig> second = config.getTiers();

            assertEquals(first, second);
        }
    }
}
