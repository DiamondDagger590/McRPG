package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageState;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Tests QuestManager's track/retire/index methods using a mock that delegates
 * to real ConcurrentHashMap-backed state, keeping the test independent of
 * the full QuestManager constructor (which requires plugin config, scope providers, etc.).
 */
public class QuestManagerStartQuestTest extends McRPGBaseTest {

    private QuestManager questManager;
    private UUID playerUUID;

    private final Map<UUID, QuestInstance> activeQuests = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> playerIndex = new ConcurrentHashMap<>();

    @BeforeEach
    public void setup() {
        server.getPluginManager().clearEvents();
        playerUUID = UUID.randomUUID();
        activeQuests.clear();
        playerIndex.clear();

        questManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);

        // Stub trackActiveQuest: adds to active map + indexes scope players
        doAnswer(inv -> {
            QuestInstance q = inv.getArgument(0);
            activeQuests.put(q.getQuestUUID(), q);
            q.getQuestScope().ifPresent(scope ->
                    scope.getCurrentPlayersInScope().forEach(p ->
                            playerIndex.computeIfAbsent(p, k -> new HashSet<>()).add(q.getQuestUUID())));
            return null;
        }).when(questManager).trackActiveQuest(any());

        // Stub retireQuest: removes from active map + deindexes scope players
        doAnswer(inv -> {
            QuestInstance q = inv.getArgument(0);
            activeQuests.remove(q.getQuestUUID());
            q.getQuestScope().ifPresent(scope ->
                    scope.getCurrentPlayersInScope().forEach(p -> {
                        Set<UUID> set = playerIndex.get(p);
                        if (set != null) {
                            set.remove(q.getQuestUUID());
                            if (set.isEmpty()) playerIndex.remove(p);
                        }
                    }));
            return null;
        }).when(questManager).retireQuest(any());

        when(questManager.isQuestActive(any(UUID.class)))
                .thenAnswer(inv -> activeQuests.containsKey(inv.getArgument(0)));

        when(questManager.getActiveQuests())
                .thenAnswer(inv -> List.copyOf(activeQuests.values()));

        when(questManager.getActiveQuestsForPlayer(any(UUID.class)))
                .thenAnswer(inv -> {
                    UUID p = inv.getArgument(0);
                    Set<UUID> questUUIDs = playerIndex.getOrDefault(p, Set.of());
                    return activeQuests.values().stream()
                            .filter(q -> questUUIDs.contains(q.getQuestUUID()))
                            .toList();
                });

        when(questManager.hasActiveInstanceOfDefinition(any(UUID.class), any(NamespacedKey.class)))
                .thenAnswer(inv -> {
                    UUID p = inv.getArgument(0);
                    NamespacedKey defKey = inv.getArgument(1);
                    Set<UUID> questUUIDs = playerIndex.getOrDefault(p, Set.of());
                    return activeQuests.values().stream()
                            .filter(q -> questUUIDs.contains(q.getQuestUUID()))
                            .anyMatch(q -> q.getQuestKey().equals(defKey));
                });

        // Stub index methods
        doAnswer(inv -> {
            UUID qUUID = inv.getArgument(0);
            UUID pUUID = inv.getArgument(1);
            playerIndex.computeIfAbsent(pUUID, k -> new HashSet<>()).add(qUUID);
            return null;
        }).when(questManager).indexQuestForPlayer(any(UUID.class), any(UUID.class));

        doAnswer(inv -> {
            UUID qUUID = inv.getArgument(0);
            UUID pUUID = inv.getArgument(1);
            Set<UUID> set = playerIndex.get(pUUID);
            if (set != null) {
                set.remove(qUUID);
                if (set.isEmpty()) playerIndex.remove(pUUID);
            }
            return null;
        }).when(questManager).deindexQuestForPlayer(any(UUID.class), any(UUID.class));

        doAnswer(inv -> {
            playerIndex.remove((UUID) inv.getArgument(0));
            return null;
        }).when(questManager).deindexPlayer(any(UUID.class));

        when(questManager.getIndexedQuestUUIDs(any(UUID.class)))
                .thenAnswer(inv -> Set.copyOf(playerIndex.getOrDefault(inv.getArgument(0), Set.of())));
    }

    // ── trackActiveQuest / retireQuest / isQuestActive ──

    @DisplayName("Given a quest instance, when trackActiveQuest is called, then isQuestActive returns true")
    @Test
    public void trackActiveQuest_addsToActiveMap() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("track_test");
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        questManager.trackActiveQuest(instance);
        assertTrue(questManager.isQuestActive(instance.getQuestUUID()));
    }

    @DisplayName("Given a tracked quest, when trackActiveQuest is called, then scope players are indexed")
    @Test
    public void trackActiveQuest_indexesScopePlayers() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("index_test");
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        questManager.trackActiveQuest(instance);
        assertTrue(questManager.getIndexedQuestUUIDs(playerUUID).contains(instance.getQuestUUID()));
    }

    @DisplayName("Given a tracked quest, when retireQuest is called, then isQuestActive returns false")
    @Test
    public void retireQuest_removesFromActiveMap() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("retire_test");
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        questManager.trackActiveQuest(instance);
        questManager.retireQuest(instance);
        assertFalse(questManager.isQuestActive(instance.getQuestUUID()));
    }

    @DisplayName("Given a tracked quest, when retireQuest is called, then player is deindexed")
    @Test
    public void retireQuest_deindexesPlayer() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("retire_deindex_test");
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        questManager.trackActiveQuest(instance);
        questManager.retireQuest(instance);
        assertFalse(questManager.getIndexedQuestUUIDs(playerUUID).contains(instance.getQuestUUID()));
    }

    @DisplayName("Given a retired quest, when isQuestActive is called, then it returns false")
    @Test
    public void isQuestActive_returnsFalse_afterRetire() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("retired_active_test");
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        questManager.trackActiveQuest(instance);
        questManager.retireQuest(instance);
        assertFalse(questManager.isQuestActive(instance.getQuestUUID()));
    }

    // ── getActiveQuestsForPlayer ──

    @DisplayName("Given no tracked quests, when getActiveQuestsForPlayer is called, then returns empty")
    @Test
    public void getActiveQuestsForPlayer_returnsEmpty_forUnknownPlayer() {
        assertTrue(questManager.getActiveQuestsForPlayer(UUID.randomUUID()).isEmpty());
    }

    @DisplayName("Given a tracked quest, when getActiveQuestsForPlayer is called, then it returns the quest")
    @Test
    public void getActiveQuestsForPlayer_returnsTrackedQuest() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("player_quests_test");
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        questManager.trackActiveQuest(instance);

        List<QuestInstance> result = questManager.getActiveQuestsForPlayer(playerUUID);
        assertEquals(1, result.size());
        assertEquals(instance.getQuestUUID(), result.get(0).getQuestUUID());
    }

    // ── hasActiveInstanceOfDefinition ──

    @DisplayName("Given a tracked quest, when hasActiveInstanceOfDefinition is called, then returns true")
    @Test
    public void hasActiveInstanceOfDefinition_returnsTrue_whenActive() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("has_active_test");
        QuestInstance instance = QuestTestHelper.startedQuestWithPlayer(def, playerUUID);
        questManager.trackActiveQuest(instance);
        assertTrue(questManager.hasActiveInstanceOfDefinition(playerUUID, def.getQuestKey()));
    }

    @DisplayName("Given no tracked quests, when hasActiveInstanceOfDefinition is called, then returns false")
    @Test
    public void hasActiveInstanceOfDefinition_returnsFalse_whenNoneActive() {
        assertFalse(questManager.hasActiveInstanceOfDefinition(
                playerUUID, new NamespacedKey("mcrpg", "nonexistent")));
    }

    // ── Index management ──

    @DisplayName("Given a player and quest UUID, when indexQuestForPlayer is called, then it appears in index")
    @Test
    public void indexQuestForPlayer_addsToIndex() {
        UUID questUUID = UUID.randomUUID();
        questManager.indexQuestForPlayer(questUUID, playerUUID);
        assertTrue(questManager.getIndexedQuestUUIDs(playerUUID).contains(questUUID));
    }

    @DisplayName("Given an indexed quest, when deindexQuestForPlayer is called, then it is removed")
    @Test
    public void deindexQuestForPlayer_removesFromIndex() {
        UUID questUUID = UUID.randomUUID();
        questManager.indexQuestForPlayer(questUUID, playerUUID);
        questManager.deindexQuestForPlayer(questUUID, playerUUID);
        assertFalse(questManager.getIndexedQuestUUIDs(playerUUID).contains(questUUID));
    }

    @DisplayName("Given a player with multiple indexed quests, when deindexPlayer is called, then all removed")
    @Test
    public void deindexPlayer_removesAllMappings() {
        questManager.indexQuestForPlayer(UUID.randomUUID(), playerUUID);
        questManager.indexQuestForPlayer(UUID.randomUUID(), playerUUID);
        questManager.deindexPlayer(playerUUID);
        assertTrue(questManager.getIndexedQuestUUIDs(playerUUID).isEmpty());
    }
}
