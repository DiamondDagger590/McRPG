package us.eunoians.mcrpg.gui.board;

import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that all localization keys used by {@link OfferingLoreBuilder} are declared
 * and have non-null routes. Full end-to-end lore generation is tested in integration
 * tests that have a running localization manager.
 */
class OfferingLoreBuilderTest {

    @Test
    @DisplayName("all referenced localization keys are non-null")
    void allLocalizationKeysNonNull() {
        assertNotNull(LocalizationKey.QUEST_BOARD_OFFERING_CATEGORY);
        assertNotNull(LocalizationKey.QUEST_BOARD_OBJECTIVES_HEADER);
        assertNotNull(LocalizationKey.QUEST_BOARD_OBJECTIVE_LINE);
        assertNotNull(LocalizationKey.QUEST_BOARD_REWARDS_HEADER);
        assertNotNull(LocalizationKey.QUEST_BOARD_REWARD_LINE);
        assertNotNull(LocalizationKey.QUEST_BOARD_REWARD_LINE_NO_AMOUNT);
        assertNotNull(LocalizationKey.QUEST_BOARD_EXPIRES_IN);
        assertNotNull(LocalizationKey.QUEST_BOARD_CLICK_TO_ACCEPT);
    }

    @Test
    @DisplayName("localization keys resolve to distinct routes")
    void localizationKeysDistinct() {
        Route[] keys = {
                LocalizationKey.QUEST_BOARD_OFFERING_CATEGORY,
                LocalizationKey.QUEST_BOARD_OBJECTIVES_HEADER,
                LocalizationKey.QUEST_BOARD_OBJECTIVE_LINE,
                LocalizationKey.QUEST_BOARD_REWARDS_HEADER,
                LocalizationKey.QUEST_BOARD_REWARD_LINE,
                LocalizationKey.QUEST_BOARD_REWARD_LINE_NO_AMOUNT,
                LocalizationKey.QUEST_BOARD_EXPIRES_IN,
                LocalizationKey.QUEST_BOARD_CLICK_TO_ACCEPT
        };
        for (int i = 0; i < keys.length; i++) {
            for (int j = i + 1; j < keys.length; j++) {
                assertNotEquals(keys[i], keys[j],
                        "Routes at index " + i + " and " + j + " should not be equal");
            }
        }
    }

    @Test
    @DisplayName("distribution preview localization keys are declared")
    void distributionPreviewKeysNonNull() {
        assertNotNull(LocalizationKey.QUEST_BOARD_DISTRIBUTION_HEADER);
        assertNotNull(LocalizationKey.QUEST_BOARD_DISTRIBUTION_TIER);
        assertNotNull(LocalizationKey.QUEST_BOARD_PREVIEW_QUALIFIES);
        assertNotNull(LocalizationKey.QUEST_BOARD_PREVIEW_NOT_QUALIFIES);
        assertNotNull(LocalizationKey.QUEST_BOARD_PREVIEW_CONTRIBUTION);
    }
}
