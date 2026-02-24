package us.eunoians.mcrpg.quest.upgrade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the ability upgrade quest lifecycle.
 * These tests verify the core mechanics of upgrade quests (creation, scoping, state).
 * Full end-to-end upgrade tests (auto-start, cascade, sanity checks) require the
 * ability system bootstrap which goes beyond the quest unit test scope.
 */
public class AbilityUpgradeQuestTest extends McRPGBaseTest {

    private QuestDefinition upgradeQuestDef;
    private UUID playerUUID;

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        upgradeQuestDef = QuestTestHelper.singlePhaseQuest("upgrade_quest");
        playerUUID = UUID.randomUUID();
    }

    @DisplayName("Given an upgrade quest definition, when creating instance with single player scope, then scope is set correctly")
    @Test
    public void upgradeQuest_canBeCreatedWithSinglePlayerScope() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(upgradeQuestDef);
        SinglePlayerQuestScope scope = new SinglePlayerQuestScope(instance.getQuestUUID());
        scope.setPlayerInScope(playerUUID);
        instance.setQuestScope(scope);

        assertTrue(instance.getQuestScope().isPresent());
        assertTrue(instance.getQuestScope().get().isPlayerInScope(playerUUID));
    }

    @DisplayName("Given an upgrade quest, when starting, then state is IN_PROGRESS")
    @Test
    public void upgradeQuest_startsCorrectly() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(upgradeQuestDef);
        instance.start(upgradeQuestDef);
        assertEquals(QuestState.IN_PROGRESS, instance.getQuestState());
    }

    @DisplayName("Given a started upgrade quest, when completing, then state is COMPLETED")
    @Test
    public void upgradeQuest_completesCorrectly() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(upgradeQuestDef);
        instance.complete(upgradeQuestDef);
        assertEquals(QuestState.COMPLETED, instance.getQuestState());
        assertTrue(instance.getEndTime().isPresent());
    }

    @DisplayName("Given a started upgrade quest, when cancelling, then state is CANCELLED")
    @Test
    public void upgradeQuest_cancelsCorrectly() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(upgradeQuestDef);
        instance.cancel();
        assertEquals(QuestState.CANCELLED, instance.getQuestState());
    }

    @DisplayName("Given a completed upgrade quest, when cancelling, then it does nothing")
    @Test
    public void upgradeQuest_cannotBeCancelledAfterCompletion() {
        QuestInstance instance = QuestTestHelper.startedQuestInstance(upgradeQuestDef);
        instance.complete(upgradeQuestDef);
        instance.cancel();
        assertEquals(QuestState.COMPLETED, instance.getQuestState());
    }

    @DisplayName("Given a quest UUID, when checking if it is active on a fresh instance, then it reflects current state")
    @Test
    public void questState_reflectsActiveStatus() {
        QuestInstance instance = QuestTestHelper.newQuestInstance(upgradeQuestDef);
        assertEquals(QuestState.NOT_STARTED, instance.getQuestState());
        assertFalse(instance.getQuestState() == QuestState.IN_PROGRESS);

        instance.start(upgradeQuestDef);
        assertTrue(instance.getQuestState() == QuestState.IN_PROGRESS);
    }
}
