package us.eunoians.mcrpg.quest.chain;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.source.QuestSource;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ChainQuestStarter}.
 * <p>
 * Validates the three registry-resolution branches in {@link ChainQuestStarter#startStepQuest}:
 * quest definition not found, quest source not found, and QuestManager returning empty.
 * <p>
 * {@link ManualQuestSource#KEY} is used as the happy-path source key because
 * {@link QuestSource#getKey()} is {@code final} (and thus cannot be stubbed on a mock) and
 * {@code ManualQuestSource} is pre-registered in the {@link us.eunoians.mcrpg.TestBootstrap}.
 */
public class ChainQuestStarterTest extends McRPGBaseTest {

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "test_chain");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("mcrpg", "test_quest");
    private static final NamespacedKey UNKNOWN_SOURCE_KEY = new NamespacedKey("mcrpg", "unknown_source");
    private static final UUID PLAYER_UUID = UUID.randomUUID();

    private ChainQuestStarter questStarter;
    private QuestDefinitionRegistry definitionRegistry;
    private QuestChainDefinition mockChainDefinition;
    private QuestChainStep testStep;

    @BeforeEach
    void setUp() {
        questStarter = new ChainQuestStarter(mcRPG);
        definitionRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_DEFINITION);

        mockChainDefinition = mock(QuestChainDefinition.class);
        when(mockChainDefinition.getChainKey()).thenReturn(CHAIN_KEY);
        when(mockChainDefinition.getSourceKey()).thenReturn(ManualQuestSource.KEY);

        testStep = QuestChainStep.simple(QUEST_KEY);
    }

    @Test
    @DisplayName("Given quest definition is not registered, When startStepQuest is called, Then it returns false")
    void startStepQuest_returnsFalse_whenDefinitionMissing() {
        boolean result = questStarter.startStepQuest(PLAYER_UUID, mockChainDefinition, testStep);

        assertFalse(result, "startStepQuest should return false when the quest definition is not registered");
    }

    @Test
    @DisplayName("Given quest definition exists but source is not registered, When startStepQuest is called, Then it returns false")
    void startStepQuest_returnsFalse_whenSourceMissing() {
        QuestDefinition mockDefinition = mock(QuestDefinition.class);
        when(mockDefinition.getQuestKey()).thenReturn(QUEST_KEY);
        definitionRegistry.register(mockDefinition);

        when(mockChainDefinition.getSourceKey()).thenReturn(UNKNOWN_SOURCE_KEY);

        boolean result = questStarter.startStepQuest(PLAYER_UUID, mockChainDefinition, testStep);

        assertFalse(result, "startStepQuest should return false when the quest source is not registered");
    }

    @Test
    @DisplayName("Given definition and source exist but QuestManager returns empty, When startStepQuest is called, Then it returns false")
    void startStepQuest_returnsFalse_whenQuestManagerReturnsEmpty() {
        QuestDefinition mockDefinition = mock(QuestDefinition.class);
        when(mockDefinition.getQuestKey()).thenReturn(QUEST_KEY);
        definitionRegistry.register(mockDefinition);

        QuestManager questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        when(questManager.startQuest(any(QuestDefinition.class), eq(PLAYER_UUID),
                any(), any(QuestSource.class))).thenReturn(Optional.empty());

        boolean result = questStarter.startStepQuest(PLAYER_UUID, mockChainDefinition, testStep);

        assertFalse(result, "startStepQuest should return false when QuestManager.startQuest returns empty");
    }

    @Test
    @DisplayName("Given all dependencies resolve, When startStepQuest is called, Then it returns true")
    void startStepQuest_returnsTrue_whenQuestStarted() {
        QuestDefinition mockDefinition = mock(QuestDefinition.class);
        when(mockDefinition.getQuestKey()).thenReturn(QUEST_KEY);
        definitionRegistry.register(mockDefinition);

        QuestInstance mockInstance = mock(QuestInstance.class);
        QuestManager questManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.QUEST);
        when(questManager.startQuest(any(QuestDefinition.class), eq(PLAYER_UUID),
                any(), any(QuestSource.class))).thenReturn(Optional.of(mockInstance));

        boolean result = questStarter.startStepQuest(PLAYER_UUID, mockChainDefinition, testStep);

        assertTrue(result, "startStepQuest should return true when all dependencies resolve and QuestManager starts the quest");
    }
}
