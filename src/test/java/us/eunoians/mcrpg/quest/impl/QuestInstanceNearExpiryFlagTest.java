package us.eunoians.mcrpg.quest.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestInstanceNearExpiryFlagTest {

    @DisplayName("Near-expiry flag is false by default on a new quest instance")
    @Test
    void nearExpiryNotified_defaultsFalse() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("near_expiry_flag_default");
        QuestInstance quest = QuestTestHelper.newQuestInstance(def);
        assertFalse(quest.isNearExpiryNotified());
    }

    @DisplayName("Setting near-expiry flag to true is reflected by the getter")
    @Test
    void nearExpiryNotified_setTrue_getterReturnsTrue() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("near_expiry_flag_set_true");
        QuestInstance quest = QuestTestHelper.newQuestInstance(def);

        quest.setNearExpiryNotified(true);

        assertTrue(quest.isNearExpiryNotified());
    }

    @DisplayName("Near-expiry flag can be reset to false after being set true")
    @Test
    void nearExpiryNotified_canBeReset() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("near_expiry_flag_reset");
        QuestInstance quest = QuestTestHelper.newQuestInstance(def);

        quest.setNearExpiryNotified(true);
        quest.setNearExpiryNotified(false);

        assertFalse(quest.isNearExpiryNotified());
    }

    @DisplayName("Near-expiry flag is independent across different quest instances")
    @Test
    void nearExpiryNotified_isPerInstance() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("near_expiry_flag_per_instance");
        QuestInstance questA = QuestTestHelper.newQuestInstance(def);
        QuestInstance questB = QuestTestHelper.newQuestInstance(def);

        questA.setNearExpiryNotified(true);

        assertTrue(questA.isNearExpiryNotified());
        assertFalse(questB.isNearExpiryNotified());
    }
}
