package us.eunoians.mcrpg.quest;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.listener.quest.QuestCancelListener;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;
import us.eunoians.mcrpg.quest.impl.scope.QuestScopeProvider;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;
import us.eunoians.mcrpg.quest.source.builtin.BoardPersonalQuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class QuestManagerRescopeExpirySweepTest extends McRPGBaseTest {

    private QuestManager questManager;

    @BeforeEach
    public void setup() throws Exception {
        questManager = new QuestManager(mcRPG);

        McRPGDatabaseManager mockDbManager = mock(McRPGDatabaseManager.class);
        Database mockDatabase = mock(Database.class);
        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        Connection mockConnection = mock(Connection.class);

        when(mockDbManager.getDatabase()).thenReturn(mockDatabase);
        when(mockDatabase.getDatabaseExecutorService()).thenReturn(mockExecutor);
        when(mockDatabase.getConnection()).thenReturn(mockConnection);
        when(mockExecutor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        });

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockDbManager);
    }

    @DisplayName("Given an expired active quest discovered during player rescope, when rescope runs, then quest.expire is triggered")
    @Test
    public void rescopePlayer_expiredQuest_triggersExpire() {
        UUID playerUUID = UUID.randomUUID();
        QuestDefinition definition = QuestTestHelper.singlePhaseQuest("rescope_expired_sweep");
        QuestInstance quest = spy(QuestTestHelper.startedQuestWithPlayer(definition, playerUUID));
        UUID questUUID = quest.getQuestUUID();
        quest.setExpirationTime(mcRPG.getTimeProvider().now().toEpochMilli() - 1_000L);
        questManager.trackActiveQuest(quest);

        @SuppressWarnings("unchecked")
        QuestScopeProvider<QuestScope> provider = mock(QuestScopeProvider.class);
        when(provider.resolveActiveQuestUUIDs(eq(playerUUID), any(Connection.class)))
                .thenReturn(List.of(questUUID));

        questManager.rescopePlayer(playerUUID, provider);
        server.getScheduler().performOneTick();

        verify(quest, times(1)).expire();
        assertEquals(us.eunoians.mcrpg.quest.impl.QuestState.CANCELLED, quest.getQuestState());
    }

    @DisplayName("Given a non-expired active quest discovered during player rescope, when rescope runs, then quest.expire is not triggered")
    @Test
    public void rescopePlayer_nonExpiredQuest_doesNotTriggerExpire() {
        UUID playerUUID = UUID.randomUUID();
        QuestDefinition definition = QuestTestHelper.singlePhaseQuest("rescope_non_expired_sweep");
        QuestInstance quest = spy(QuestTestHelper.startedQuestWithPlayer(definition, playerUUID));
        UUID questUUID = quest.getQuestUUID();
        quest.setExpirationTime(mcRPG.getTimeProvider().now().toEpochMilli() + 60_000L);
        questManager.trackActiveQuest(quest);

        @SuppressWarnings("unchecked")
        QuestScopeProvider<QuestScope> provider = mock(QuestScopeProvider.class);
        when(provider.resolveActiveQuestUUIDs(eq(playerUUID), any(Connection.class)))
                .thenReturn(List.of(questUUID));

        questManager.rescopePlayer(playerUUID, provider);
        server.getScheduler().performOneTick();

        verify(quest, times(0)).expire();
        assertEquals(us.eunoians.mcrpg.quest.impl.QuestState.IN_PROGRESS, quest.getQuestState());
    }

    @DisplayName("Given tracked player with expired board quest during rescope, then board count decrements")
    @Test
    public void rescopePlayer_expiredBoardQuest_decrementsBoardCount(McRPGPlayer mcRPGPlayer) {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        server.getPluginManager().registerEvents(new QuestCancelListener(), mcRPG);

        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .addPlayer(mcRPGPlayer);

        mcRPGPlayer.asQuestHolder().setActiveBoardQuestCount(1);

        QuestDefinition definition = QuestTestHelper.singlePhaseQuest("rescope_expired_board_count");
        QuestInstance quest = new QuestInstance(definition, null, Map.of(), new BoardPersonalQuestSource(), null);
        SinglePlayerQuestScope scope = new SinglePlayerQuestScope(quest.getQuestUUID());
        scope.setPlayerInScope(mcRPGPlayer.getUUID());
        quest.setQuestScope(scope);
        quest.start(definition);
        quest.setExpirationTime(mcRPG.getTimeProvider().now().toEpochMilli() - 1_000L);
        questManager.trackActiveQuest(quest);

        @SuppressWarnings("unchecked")
        QuestScopeProvider<QuestScope> provider = mock(QuestScopeProvider.class);
        when(provider.resolveActiveQuestUUIDs(eq(mcRPGPlayer.getUUID()), any(Connection.class)))
                .thenReturn(List.of(quest.getQuestUUID()));

        questManager.rescopePlayer(mcRPGPlayer.getUUID(), provider);
        server.getScheduler().performOneTick();

        assertEquals(us.eunoians.mcrpg.quest.impl.QuestState.CANCELLED, quest.getQuestState());
        assertEquals(0, mcRPGPlayer.asQuestHolder().getActiveBoardQuestCount());
    }
}
