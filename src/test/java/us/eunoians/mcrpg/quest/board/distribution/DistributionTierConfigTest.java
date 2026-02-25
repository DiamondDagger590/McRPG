package us.eunoians.mcrpg.quest.board.distribution;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.distribution.builtin.TopPlayersDistributionType;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DistributionTierConfigTest extends McRPGBaseTest {

    private static final NamespacedKey EXPANSION = McRPGExpansion.EXPANSION_KEY;
    private static final NamespacedKey TYPE_KEY = TopPlayersDistributionType.KEY;

    private QuestRarityRegistry rarityRegistry;
    private NamespacedKey commonKey;
    private NamespacedKey uncommonKey;
    private NamespacedKey rareKey;
    private NamespacedKey legendaryKey;

    @BeforeEach
    void setUp() {
        rarityRegistry = new QuestRarityRegistry();
        String ns = McRPGMethods.getMcRPGNamespace();
        commonKey = new NamespacedKey(ns, "common");
        uncommonKey = new NamespacedKey(ns, "uncommon");
        rareKey = new NamespacedKey(ns, "rare");
        legendaryKey = new NamespacedKey(ns, "legendary");

        rarityRegistry.register(new QuestRarity(commonKey, 60, 1.0, 1.0, EXPANSION));
        rarityRegistry.register(new QuestRarity(uncommonKey, 25, 1.2, 1.2, EXPANSION));
        rarityRegistry.register(new QuestRarity(rareKey, 10, 1.5, 1.5, EXPANSION));
        rarityRegistry.register(new QuestRarity(legendaryKey, 5, 2.0, 2.0, EXPANSION));
    }

    @DisplayName("no rarity gate → always passes")
    @Test
    void noRarityGateAlwaysPasses() {
        var tier = new DistributionTierConfig("test", TYPE_KEY, RewardSplitMode.INDIVIDUAL,
                List.of(), Map.of(), null, null);

        assertTrue(tier.passesRarityGate(commonKey, rarityRegistry));
        assertTrue(tier.passesRarityGate(legendaryKey, rarityRegistry));
        assertTrue(tier.passesRarityGate(null, rarityRegistry));
    }

    @DisplayName("min-rarity: RARE includes RARE and LEGENDARY, excludes UNCOMMON")
    @Test
    void minRarityIncludesHigherTiers() {
        var tier = new DistributionTierConfig("test", TYPE_KEY, RewardSplitMode.INDIVIDUAL,
                List.of(), Map.of(), rareKey, null);

        assertTrue(tier.passesRarityGate(rareKey, rarityRegistry));
        assertTrue(tier.passesRarityGate(legendaryKey, rarityRegistry));
        assertFalse(tier.passesRarityGate(uncommonKey, rarityRegistry));
        assertFalse(tier.passesRarityGate(commonKey, rarityRegistry));
    }

    @DisplayName("required-rarity: RARE requires exact match")
    @Test
    void requiredRarityExactMatch() {
        var tier = new DistributionTierConfig("test", TYPE_KEY, RewardSplitMode.INDIVIDUAL,
                List.of(), Map.of(), null, rareKey);

        assertTrue(tier.passesRarityGate(rareKey, rarityRegistry));
        assertFalse(tier.passesRarityGate(legendaryKey, rarityRegistry));
        assertFalse(tier.passesRarityGate(commonKey, rarityRegistry));
    }

    @DisplayName("null quest rarity fails rarity-gated tiers")
    @Test
    void nullQuestRarityFailsGatedTiers() {
        var minTier = new DistributionTierConfig("test", TYPE_KEY, RewardSplitMode.INDIVIDUAL,
                List.of(), Map.of(), rareKey, null);
        var reqTier = new DistributionTierConfig("test", TYPE_KEY, RewardSplitMode.INDIVIDUAL,
                List.of(), Map.of(), null, rareKey);

        assertFalse(minTier.passesRarityGate(null, rarityRegistry));
        assertFalse(reqTier.passesRarityGate(null, rarityRegistry));
    }

    @DisplayName("null quest rarity passes non-gated tier")
    @Test
    void nullQuestRarityPassesNonGated() {
        var tier = new DistributionTierConfig("test", TYPE_KEY, RewardSplitMode.INDIVIDUAL,
                List.of(), Map.of(), null, null);

        assertTrue(tier.passesRarityGate(null, rarityRegistry));
    }

    @DisplayName("unregistered rarity key fails gracefully")
    @Test
    void unregisteredRarityKeyFails() {
        var tier = new DistributionTierConfig("test", TYPE_KEY, RewardSplitMode.INDIVIDUAL,
                List.of(), Map.of(), rareKey, null);
        var unknownKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown");

        assertFalse(tier.passesRarityGate(unknownKey, rarityRegistry));
    }
}
