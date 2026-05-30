package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.event.player.PlayerLoadEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.NamespacedKey;
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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuestChainLoginListenerTest extends McRPGBaseTest {

    private static final NamespacedKey SOURCE_KEY = new NamespacedKey("mcrpg", "manual");
    private static final NamespacedKey LOGIN_TRIGGER_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "login");
    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("test", "login_chain");
    private static final NamespacedKey QUEST_KEY = new NamespacedKey("test", "login_quest");

    private QuestChainManager mockChainManager;
    private QuestChainRegistry chainRegistry;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();

        mockChainManager = mock(QuestChainManager.class);
        chainRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.QUEST_CHAIN);

        server.getPluginManager().registerEvents(new QuestChainLoginListener(mockChainManager), mcRPG);
    }

    @AfterEach
    public void tearDown() {
        chainRegistry.clear();
        HandlerList.unregisterAll(mcRPG);
    }

    @DisplayName("Given a PlayerLoadEvent for an McRPGPlayer, when fired, then reResolveOnLogin is called with the player's UUID")
    @Test
    public void onPlayerLoad_callsReResolveOnLogin() {
        PlayerMock playerMock = server.addPlayer();
        UUID uuid = playerMock.getUniqueId();
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
        when(mcRPGPlayer.getUUID()).thenReturn(uuid);

        server.getPluginManager().callEvent(new PlayerLoadEvent(mcRPGPlayer));

        verify(mockChainManager).reResolveOnLogin(eq(uuid), any(Runnable.class));
    }

    @DisplayName("Given a login chain registered, when PlayerLoadEvent fires, then tryStartChain is called for that chain")
    @Test
    public void onPlayerLoad_withLoginChain_callsTryStartChain() {
        QuestChainDefinition chain = new QuestChainDefinition.Builder(
                CHAIN_KEY,
                SOURCE_KEY,
                LOGIN_TRIGGER_KEY,
                List.of(QuestChainStep.simple(QUEST_KEY))
        ).build();
        chainRegistry.register(chain);

        PlayerMock playerMock = server.addPlayer();
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
        when(mcRPGPlayer.getUUID()).thenReturn(playerMock.getUniqueId());

        // Invoke the completion callback so the login trigger evaluation runs
        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(1);
            onComplete.run();
            return null;
        }).when(mockChainManager).reResolveOnLogin(eq(playerMock.getUniqueId()), any(Runnable.class));

        server.getPluginManager().callEvent(new PlayerLoadEvent(mcRPGPlayer));

        verify(mockChainManager).tryStartChain(eq(playerMock), eq(CHAIN_KEY));
    }

    @DisplayName("Given a PlayerLoadEvent for a non-McRPGPlayer CorePlayer, when fired, then neither reResolveOnLogin nor tryStartChain is called")
    @Test
    public void onPlayerLoad_nonMcRPGPlayer_doesNothing() {
        CorePlayer genericPlayer = mock(CorePlayer.class);
        UUID uuid = UUID.randomUUID();
        when(genericPlayer.getUUID()).thenReturn(uuid);

        server.getPluginManager().callEvent(new PlayerLoadEvent(genericPlayer));

        verify(mockChainManager, never()).reResolveOnLogin(eq(uuid));
    }

    @DisplayName("Given a chain with a non-login trigger registered, when PlayerLoadEvent fires, then tryStartChain is not called for it")
    @Test
    public void onPlayerLoad_chainWithOtherTrigger_isIgnored() {
        NamespacedKey otherTrigger = new NamespacedKey("test", "other_trigger");
        QuestChainDefinition chain = new QuestChainDefinition.Builder(
                new NamespacedKey("test", "other_chain"),
                SOURCE_KEY,
                otherTrigger,
                List.of(QuestChainStep.simple(QUEST_KEY))
        ).build();
        chainRegistry.register(chain);

        PlayerMock playerMock = server.addPlayer();
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.of(playerMock));
        when(mcRPGPlayer.getUUID()).thenReturn(playerMock.getUniqueId());

        server.getPluginManager().callEvent(new PlayerLoadEvent(mcRPGPlayer));

        verify(mockChainManager, never()).tryStartChain(eq(playerMock), eq(new NamespacedKey("test", "other_chain")));
    }
}
