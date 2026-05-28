package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.event.player.PlayerLoadEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainManager;
import us.eunoians.mcrpg.quest.chain.QuestChainRegistry;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;
import us.eunoians.mcrpg.quest.chain.trigger.builtin.FirstJoinChainAutoStartTrigger;
import us.eunoians.mcrpg.quest.chain.trigger.builtin.ManualChainAutoStartTrigger;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestChainFirstJoinListenerTest extends McRPGBaseTest {

    private static final NamespacedKey SOURCE_KEY = new NamespacedKey("mcrpg", "manual");
    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("test", "first_join_chain");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("test", "step_quest");

    private QuestChainManager mockChainManager;
    private QuestChainRegistry chainRegistry;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();

        mockChainManager = mock(QuestChainManager.class);
        chainRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);

        server.getPluginManager().registerEvents(new QuestChainFirstJoinListener(mockChainManager), mcRPG);
    }

    @AfterEach
    public void tearDown() {
        chainRegistry.clear();
        HandlerList.unregisterAll(mcRPG);
    }

    @DisplayName("Given a PlayerLoadEvent for an McRPGPlayer with a first-join chain registered, when fired, then tryStartChain is called")
    @Test
    public void onPlayerLoad_withFirstJoinChain_callsTryStartChain() {
        QuestChainDefinition chain = buildFirstJoinChain(CHAIN_KEY, QUEST_KEY);
        chainRegistry.register(chain);

        PlayerMock playerMock = server.addPlayer();
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
        when(mcRPGPlayer.getUUID()).thenReturn(playerMock.getUniqueId());

        server.getPluginManager().callEvent(new PlayerLoadEvent(mcRPGPlayer));

        verify(mockChainManager).tryStartChain(eq(playerMock), eq(CHAIN_KEY));
    }

    @DisplayName("Given a PlayerLoadEvent with no first-join chains registered, when fired, then tryStartChain is never called")
    @Test
    public void onPlayerLoad_withNoFirstJoinChains_doesNotCallTryStartChain() {
        PlayerMock playerMock = server.addPlayer();
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
        when(mcRPGPlayer.getUUID()).thenReturn(playerMock.getUniqueId());

        server.getPluginManager().callEvent(new PlayerLoadEvent(mcRPGPlayer));

        verify(mockChainManager, never()).tryStartChain(eq(playerMock), eq(CHAIN_KEY));
    }

    @DisplayName("Given a PlayerLoadEvent for a non-McRPGPlayer CorePlayer, when fired, then tryStartChain is never called")
    @Test
    public void onPlayerLoad_nonMcRPGPlayer_doesNothing() {
        QuestChainDefinition chain = buildFirstJoinChain(CHAIN_KEY, QUEST_KEY);
        chainRegistry.register(chain);

        CorePlayer genericPlayer = mock(CorePlayer.class);
        server.getPluginManager().callEvent(new PlayerLoadEvent(genericPlayer));

        verify(mockChainManager, never()).tryStartChain(eq((Player) null), eq(CHAIN_KEY));
    }

    @DisplayName("Given a chain with a non-first-join trigger registered, when PlayerLoadEvent fires, then tryStartChain is not called for it")
    @Test
    public void onPlayerLoad_chainWithOtherTrigger_isIgnored() {
        QuestChainDefinition manualChain = new QuestChainDefinition.Builder(
                new NamespacedKey("test", "manual_chain"),
                SOURCE_KEY,
                ManualChainAutoStartTrigger.KEY,
                List.of(QuestChainStep.simple(QUEST_KEY))
        ).build();
        chainRegistry.register(manualChain);

        PlayerMock playerMock = server.addPlayer();
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
        when(mcRPGPlayer.getUUID()).thenReturn(playerMock.getUniqueId());

        server.getPluginManager().callEvent(new PlayerLoadEvent(mcRPGPlayer));

        verify(mockChainManager, never()).tryStartChain(eq(playerMock), eq(new NamespacedKey("test", "manual_chain")));
    }

    /**
     * Builds a minimal {@link QuestChainDefinition} with a first-join trigger.
     *
     * @param chainKey the chain's key
     * @param questKey the quest key for the single step
     * @return a first-join chain definition
     */
    private QuestChainDefinition buildFirstJoinChain(NamespacedKey chainKey, NamespacedKey questKey) {
        return new QuestChainDefinition.Builder(
                chainKey,
                SOURCE_KEY,
                FirstJoinChainAutoStartTrigger.KEY,
                List.of(QuestChainStep.simple(questKey))
        ).build();
    }
}
