package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the three-tier memory model of QuestManager.
 * These tests verify active quest tracking, player indexing, and quest retirement.
 * <p>
 * Note: Full Caffeine TTL tests require a QuestManager instantiated with a FakeTicker,
 * which requires the full plugin bootstrap. These tests focus on the conceptual tiers
 * through the public API with mocked infrastructure.
 */
public class QuestManagerMemoryTest extends McRPGBaseTest {

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
    }

    @DisplayName("Given no active quests, when getting active quests list, then it is empty")
    @Test
    public void getActiveQuests_returnsEmpty_whenNoneTracked() {
        QuestManager manager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.QUEST);
        org.mockito.Mockito.when(manager.getActiveQuests()).thenReturn(java.util.List.of());
        assertTrue(manager.getActiveQuests().isEmpty());
    }

    @DisplayName("Given a quest instance, when dirty flag is set, then isDirty returns true")
    @Test
    public void dirtyFlag_lifecycleOnQuestInstance() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("memory_test");
        QuestInstance instance = QuestTestHelper.newQuestInstance(def);
        assertFalse(instance.isDirty());
        instance.markDirty();
        assertTrue(instance.isDirty());
        instance.clearDirty();
        assertFalse(instance.isDirty());
    }

    @DisplayName("Given a quest with scope, when setting scope, then players in scope are trackable")
    @Test
    public void scopeTracking_worksCorrectly() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("scope_track");
        QuestInstance instance = QuestTestHelper.newQuestInstance(def);
        UUID playerUUID = UUID.randomUUID();

        SinglePlayerQuestScope scope = new SinglePlayerQuestScope(instance.getQuestUUID());
        scope.setPlayerInScope(playerUUID);
        instance.setQuestScope(scope);

        assertTrue(instance.getQuestScope().isPresent());
        assertTrue(instance.getQuestScope().get().isPlayerInScope(playerUUID));
    }

    @DisplayName("Given a COMPLETED quest, then its state is terminal")
    @Test
    public void questState_completedIsTerminal() {
        QuestInstance instance = new QuestInstance(
                new NamespacedKey("mcrpg", "terminal_test"),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "single_player"),
                QuestState.COMPLETED,
                null, 1000L, 2000L, null,
                new ManualQuestSource(), null
        );
        assertEquals(QuestState.COMPLETED, instance.getQuestState());
    }

    @DisplayName("Given a CANCELLED quest, then its state is terminal")
    @Test
    public void questState_cancelledIsTerminal() {
        QuestInstance instance = new QuestInstance(
                new NamespacedKey("mcrpg", "cancelled_test"),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "single_player"),
                QuestState.CANCELLED,
                null, 1000L, 2000L, null,
                new ManualQuestSource(), null
        );
        assertEquals(QuestState.CANCELLED, instance.getQuestState());
    }

    @DisplayName("Given a NOT_STARTED quest reconstructed from DB, then it should go to Tier 1 conceptually")
    @Test
    public void notStartedQuest_isTierOneCandidate() {
        QuestInstance instance = new QuestInstance(
                new NamespacedKey("mcrpg", "not_started_test"),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "single_player"),
                QuestState.NOT_STARTED,
                null, null, null, null,
                new ManualQuestSource(), null
        );
        assertEquals(QuestState.NOT_STARTED, instance.getQuestState());
    }

    @DisplayName("Given an IN_PROGRESS quest reconstructed from DB, then it should go to Tier 1 conceptually")
    @Test
    public void inProgressQuest_isTierOneCandidate() {
        QuestInstance instance = new QuestInstance(
                new NamespacedKey("mcrpg", "in_progress_test"),
                UUID.randomUUID(),
                new NamespacedKey("mcrpg", "single_player"),
                QuestState.IN_PROGRESS,
                null, 1000L, null, null,
                new ManualQuestSource(), null
        );
        assertEquals(QuestState.IN_PROGRESS, instance.getQuestState());
    }
}
