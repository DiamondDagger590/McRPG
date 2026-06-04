package us.eunoians.mcrpg.quest.board.rarity;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestRarityTest extends McRPGBaseTest {

    private static final NamespacedKey EXPANSION_KEY = new NamespacedKey("mcrpg", "mcrpg");
    private static final NamespacedKey COMMON_KEY = new NamespacedKey("mcrpg", "common");
    private static final NamespacedKey RARE_KEY = new NamespacedKey("mcrpg", "rare");

    @Nested
    @DisplayName("Minimal constructor")
    class MinimalConstructor {

        @DisplayName("getKey returns the key passed to constructor")
        @Test
        void getKey_returnsConstructorKey() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);

            assertEquals(COMMON_KEY, rarity.getKey());
        }

        @DisplayName("getWeight returns the weight passed to constructor")
        @Test
        void getWeight_returnsConstructorWeight() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);

            assertEquals(60, rarity.getWeight());
        }

        @DisplayName("getDifficultyMultiplier returns constructor value")
        @Test
        void getDifficultyMultiplier_returnsConstructorValue() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.5, 1.0, EXPANSION_KEY);

            assertEquals(1.5, rarity.getDifficultyMultiplier(), 0.001);
        }

        @DisplayName("getRewardMultiplier returns constructor value")
        @Test
        void getRewardMultiplier_returnsConstructorValue() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 2.5, EXPANSION_KEY);

            assertEquals(2.5, rarity.getRewardMultiplier(), 0.001);
        }

        @DisplayName("getExpansionKey returns the expansion key")
        @Test
        void getExpansionKey_returnsExpansionKey() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);

            assertEquals(Optional.of(EXPANSION_KEY), rarity.getExpansionKey());
        }

        @DisplayName("getNameColor returns empty when constructed without name color")
        @Test
        void getNameColor_returnsEmpty_whenNotProvided() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);

            assertTrue(rarity.getNameColor().isEmpty());
        }
    }

    @Nested
    @DisplayName("Full constructor")
    class FullConstructor {

        @DisplayName("getNameColor returns the configured color tag")
        @Test
        void getNameColor_returnsConfiguredColor() {
            QuestRarity rarity = new QuestRarity(
                    RARE_KEY, 10, 2.0, 3.0, EXPANSION_KEY, null, "<gold>");

            assertEquals(Optional.of("<gold>"), rarity.getNameColor());
        }

        @DisplayName("getNameColor returns empty when null is passed")
        @Test
        void getNameColor_returnsEmpty_whenNullPassed() {
            QuestRarity rarity = new QuestRarity(
                    RARE_KEY, 10, 2.0, 3.0, EXPANSION_KEY, null, null);

            assertTrue(rarity.getNameColor().isEmpty());
        }
    }

    @Nested
    @DisplayName("configureIcon")
    class ConfigureIcon {

        @DisplayName("configureIcon returns builder unchanged when no icon section")
        @Test
        void configureIcon_noSection_returnsBuilderUnchanged() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 60, 1.0, 1.0, EXPANSION_KEY);
            ItemBuilder builder = ItemBuilder.from(new ItemStack(Material.PAPER));

            ItemBuilder result = rarity.configureIcon(builder);

            assertSame(builder, result);
        }

    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @DisplayName("zero weight is accepted")
        @Test
        void zeroWeight_isAccepted() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 0, 1.0, 1.0, EXPANSION_KEY);

            assertEquals(0, rarity.getWeight());
        }

        @DisplayName("negative multipliers are accepted")
        @Test
        void negativeMultipliers_areAccepted() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 10, -1.0, -0.5, EXPANSION_KEY);

            assertEquals(-1.0, rarity.getDifficultyMultiplier(), 0.001);
            assertEquals(-0.5, rarity.getRewardMultiplier(), 0.001);
        }

        @DisplayName("zero multipliers are accepted")
        @Test
        void zeroMultipliers_areAccepted() {
            QuestRarity rarity = new QuestRarity(COMMON_KEY, 10, 0.0, 0.0, EXPANSION_KEY);

            assertEquals(0.0, rarity.getDifficultyMultiplier(), 0.001);
            assertEquals(0.0, rarity.getRewardMultiplier(), 0.001);
        }
    }
}
