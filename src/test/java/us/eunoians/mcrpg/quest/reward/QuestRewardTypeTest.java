package us.eunoians.mcrpg.quest.reward;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for default methods on {@link QuestRewardType}.
 */
class QuestRewardTypeTest {

    private QuestRewardType reward;
    private McRPGLocalizationManager localization;
    private McRPGPlayer player;

    @BeforeEach
    void setup() {
        NamespacedKey key = new NamespacedKey("mcrpg", "mock");
        NamespacedKey expansionKey = new NamespacedKey("mcrpg", "expansion");
        reward = new MockQuestRewardType(key, expansionKey);
        localization = mock(McRPGLocalizationManager.class);
        player = mock(McRPGPlayer.class);
    }

    @DisplayName("Given a non-blank default color, when prependDefaultColor is called, then the color is prepended to the label")
    @Test
    void prependDefaultColor_nonBlankColor_prependsColor() {
        when(localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_DEFAULT_COLOR))
                .thenReturn("<gold>");

        String result = reward.prependDefaultColor(localization, player, "500 Mining XP");

        assertEquals("<gold>500 Mining XP", result);
    }

    @DisplayName("Given a blank default color, when prependDefaultColor is called, then the label is returned unchanged")
    @Test
    void prependDefaultColor_blankColor_returnsLabelUnchanged() {
        when(localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_DEFAULT_COLOR))
                .thenReturn("");

        String result = reward.prependDefaultColor(localization, player, "500 Mining XP");

        assertEquals("500 Mining XP", result);
    }

    @DisplayName("Given a locale lookup that throws, when prependDefaultColor is called, then the label is returned unchanged")
    @Test
    void prependDefaultColor_localeThrows_returnsLabelUnchanged() {
        when(localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_DEFAULT_COLOR))
                .thenThrow(new RuntimeException("locale unavailable"));

        String result = reward.prependDefaultColor(localization, player, "500 Mining XP");

        assertEquals("500 Mining XP", result);
    }
}
