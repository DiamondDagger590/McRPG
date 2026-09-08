package us.eunoians.mcrpg.quest.board;

import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.quest.source.builtin.BoardPersonalQuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("QuestBoardTerminator")
@ExtendWith(McRPGPlayerExtension.class)
class QuestBoardTerminatorTest extends McRPGBaseTest {

    private QuestBoardTerminator terminator;

    @BeforeEach
    void setUp() {
        terminator = new QuestBoardTerminator(mcRPG);
    }

    @Nested
    @DisplayName("decrementBoardCount")
    class DecrementBoardCountTests {

        @Test
        @DisplayName("decrements board quest count for players in scope when source is board personal")
        void decrementBoardCount_decrementsForPlayersInScope(McRPGPlayer mcRPGPlayer) {
            UUID playerUUID = mcRPGPlayer.getUUID();

            QuestInstance questInstance = mock(QuestInstance.class);
            QuestSource source = mock(QuestSource.class);
            QuestScope scope = mock(QuestScope.class);

            when(source.getKey()).thenReturn(BoardPersonalQuestSource.KEY);
            when(questInstance.getQuestSource()).thenReturn(source);
            when(questInstance.getQuestScope()).thenReturn(Optional.of(scope));
            when(scope.getCurrentPlayersInScope()).thenReturn(Set.of(playerUUID));

            QuestHolder questHolder = mcRPGPlayer.asQuestHolder();
            questHolder.incrementBoardQuestCount();
            int before = questHolder.getActiveBoardQuestCount();

            terminator.decrementBoardCount(questInstance);

            assertEquals(before - 1, questHolder.getActiveBoardQuestCount());
        }

        @Test
        @DisplayName("does nothing when quest source is not board personal")
        void decrementBoardCount_skips_whenSourceIsNotBoardPersonal(McRPGPlayer mcRPGPlayer) {
            QuestInstance questInstance = mock(QuestInstance.class);
            QuestSource source = mock(QuestSource.class);
            NamespacedKey otherSourceKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "other_source");

            when(source.getKey()).thenReturn(otherSourceKey);
            when(questInstance.getQuestSource()).thenReturn(source);

            QuestHolder questHolder = mcRPGPlayer.asQuestHolder();
            questHolder.incrementBoardQuestCount();
            int before = questHolder.getActiveBoardQuestCount();

            terminator.decrementBoardCount(questInstance);

            assertEquals(before, questHolder.getActiveBoardQuestCount());
        }

        @Test
        @DisplayName("does nothing when the quest has no scope")
        void decrementBoardCount_skips_whenNoScope() {
            QuestInstance questInstance = mock(QuestInstance.class);
            QuestSource source = mock(QuestSource.class);

            when(source.getKey()).thenReturn(BoardPersonalQuestSource.KEY);
            when(questInstance.getQuestSource()).thenReturn(source);
            when(questInstance.getQuestScope()).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> terminator.decrementBoardCount(questInstance));
        }

        @Test
        @DisplayName("skips players not loaded in the player manager")
        void decrementBoardCount_skipsUnloadedPlayers() {
            UUID unknownUUID = UUID.randomUUID();

            QuestInstance questInstance = mock(QuestInstance.class);
            QuestSource source = mock(QuestSource.class);
            QuestScope scope = mock(QuestScope.class);

            when(source.getKey()).thenReturn(BoardPersonalQuestSource.KEY);
            when(questInstance.getQuestSource()).thenReturn(source);
            when(questInstance.getQuestScope()).thenReturn(Optional.of(scope));
            when(scope.getCurrentPlayersInScope()).thenReturn(Set.of(unknownUUID));

            assertDoesNotThrow(() -> terminator.decrementBoardCount(questInstance));
        }
    }

    @Nested
    @DisplayName("deregisterEphemeralDefinition")
    class DeregisterEphemeralDefinitionTests {

        @Test
        @DisplayName("deregisters a definition with the gen_ prefix")
        void deregisterEphemeralDefinition_deregistersGenPrefixed() {
            NamespacedKey genKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "gen_test_quest");
            QuestDefinitionRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.QUEST_DEFINITION);

            QuestDefinition definition = mock(QuestDefinition.class);
            when(definition.getQuestKey()).thenReturn(genKey);
            registry.register(definition);

            terminator.deregisterEphemeralDefinition(genKey);

            assertFalse(registry.isRegistered(genKey));
        }

        @Test
        @DisplayName("does not deregister a definition without the gen_ prefix")
        void deregisterEphemeralDefinition_skipsNonGenPrefixed() {
            NamespacedKey normalKey = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "normal_quest");
            QuestDefinitionRegistry registry = mcRPG.registryAccess().registry(McRPGRegistryKey.QUEST_DEFINITION);

            QuestDefinition definition = mock(QuestDefinition.class);
            when(definition.getQuestKey()).thenReturn(normalKey);
            registry.register(definition);

            terminator.deregisterEphemeralDefinition(normalKey);

            assertTrue(registry.isRegistered(normalKey));
        }
    }

    @Nested
    @DisplayName("releaseBoardSlot")
    class ReleaseBoardSlotTests {

        @Test
        @DisplayName("does not throw when no database manager is registered")
        void releaseBoardSlot_handlesNullDatabaseManager() {
            QuestInstance questInstance = mock(QuestInstance.class);
            when(questInstance.getQuestUUID()).thenReturn(UUID.randomUUID());

            assertDoesNotThrow(() -> terminator.releaseBoardSlot(questInstance, "COMPLETED"));
        }

        @Test
        @DisplayName("submits database work when database manager is available")
        void releaseBoardSlot_submitsDatabaseWork_whenManagerAvailable() {
            McRPGDatabaseManager dbManager = mock(McRPGDatabaseManager.class);
            Database database = mock(Database.class);
            ThreadPoolExecutor executor = mock(ThreadPoolExecutor.class);
            when(dbManager.getDatabase()).thenReturn(database);
            when(database.getDatabaseExecutorService()).thenReturn(executor);
            mcRPG.registryAccess().registry(RegistryKey.MANAGER).register(dbManager);

            QuestInstance questInstance = mock(QuestInstance.class);
            when(questInstance.getQuestUUID()).thenReturn(UUID.randomUUID());

            terminator.releaseBoardSlot(questInstance, "COMPLETED");

            verify(executor).submit(org.mockito.ArgumentMatchers.any(Runnable.class));
        }
    }
}
