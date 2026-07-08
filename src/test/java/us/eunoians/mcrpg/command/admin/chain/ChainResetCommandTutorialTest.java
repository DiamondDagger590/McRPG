package us.eunoians.mcrpg.command.admin.chain;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRepeatMode;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;
import us.eunoians.mcrpg.quest.source.builtin.TutorialQuestSource;
import us.eunoians.mcrpg.setting.impl.DisableTutorialSetting;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that {@link ChainResetCommand#resetTutorialSettingIfNeeded} resets
 * {@link DisableTutorialSetting} to {@link DisableTutorialSetting#ENABLED} when the
 * chain uses the tutorial source, and leaves non-tutorial chains unaffected.
 */
@ExtendWith(McRPGPlayerExtension.class)
public class ChainResetCommandTutorialTest extends McRPGBaseTest {

    @Test
    @DisplayName("Given a tutorial chain, when resetTutorialSettingIfNeeded is called, then DisableTutorialSetting is set to ENABLED")
    public void resetTutorialSettingIfNeeded_setsEnabledForTutorialChain(@NotNull McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.setPlayerSetting(DisableTutorialSetting.DISABLED);
        assertDisabledSetting(mcRPGPlayer, true);

        QuestChainDefinition tutorialChain = buildChain(TutorialQuestSource.KEY);
        ChainResetCommand.resetTutorialSettingIfNeeded(mcRPGPlayer, tutorialChain);

        assertDisabledSetting(mcRPGPlayer, false);
    }

    @Test
    @DisplayName("Given a non-tutorial chain, when resetTutorialSettingIfNeeded is called, then DisableTutorialSetting is unchanged")
    public void resetTutorialSettingIfNeeded_doesNotTouchSettingForNonTutorialChain(@NotNull McRPGPlayer mcRPGPlayer) {
        mcRPGPlayer.setPlayerSetting(DisableTutorialSetting.DISABLED);
        assertDisabledSetting(mcRPGPlayer, true);

        NamespacedKey otherSourceKey = new NamespacedKey("mcrpg", "manual");
        QuestChainDefinition otherChain = buildChain(otherSourceKey);
        ChainResetCommand.resetTutorialSettingIfNeeded(mcRPGPlayer, otherChain);

        assertDisabledSetting(mcRPGPlayer, true);
    }

    /**
     * Asserts that the player's {@link DisableTutorialSetting} matches {@code expectedDisabled}.
     *
     * @param mcRPGPlayer     the player to check
     * @param expectedDisabled {@code true} if the setting should be DISABLED, {@code false} for ENABLED
     */
    private void assertDisabledSetting(@NotNull McRPGPlayer mcRPGPlayer, boolean expectedDisabled) {
        Optional<DisableTutorialSetting> settingOpt = mcRPGPlayer.getPlayerSetting(DisableTutorialSetting.SETTING_KEY)
                .filter(s -> s instanceof DisableTutorialSetting)
                .map(s -> (DisableTutorialSetting) s);

        if (expectedDisabled) {
            assertTrue(settingOpt.isPresent(), "DisableTutorialSetting should be present");
            assertTrue(settingOpt.get().isDisabled(), "Setting should be DISABLED");
        } else {
            settingOpt.ifPresent(s -> assertFalse(s.isDisabled(),
                    "Setting should be ENABLED (not disabled) after reset"));
        }
    }

    /**
     * Builds a minimal {@link QuestChainDefinition} using the given source key for testing.
     *
     * @param sourceKey the quest source key to associate with the chain
     * @return a test chain definition
     */
    @NotNull
    private QuestChainDefinition buildChain(@NotNull NamespacedKey sourceKey) {
        NamespacedKey stepQuest = new NamespacedKey("mcrpg", "test_quest");
        return new QuestChainDefinition.Builder(
                new NamespacedKey("mcrpg", "test_chain"),
                sourceKey,
                new NamespacedKey("mcrpg", "first_join"),
                List.of(QuestChainStep.simple(stepQuest))
        ).build();
    }
}
