package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.OnStartMessage;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.scope.impl.SinglePlayerQuestScope;
import us.eunoians.mcrpg.quest.chain.CascadeContext;
import us.eunoians.mcrpg.quest.chain.CascadeOrchestrator;
import us.eunoians.mcrpg.quest.message.QuestMessageDeliverer;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

public class QuestStartMessageListenerTest extends McRPGBaseTest {

    private QuestMessageDeliverer mockDeliverer;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        McRPGPlayerManager mockPlayerManager = mock(McRPGPlayerManager.class);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockPlayerManager);
        when(mockPlayerManager.getPlayer(any(UUID.class))).thenReturn(java.util.Optional.empty());
        mockDeliverer = mock(QuestMessageDeliverer.class);
        CascadeOrchestrator mockCascadeOrchestrator = mock(CascadeOrchestrator.class);
        when(mockCascadeOrchestrator.isInCascade(any(UUID.class))).thenReturn(false);
        server.getPluginManager().registerEvents(
                new QuestStartMessageListener(mcRPG, mockDeliverer, mockCascadeOrchestrator), mcRPG);
    }

    @Test
    @DisplayName("Given a quest definition with no on-start messages, when QuestStartEvent fires, then deliver is never called")
    public void onQuestStart_doesNothing_whenNoMessages() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("msg_listener_no_msg");
        UUID playerUUID = UUID.randomUUID();
        QuestInstance instance = instanceWithScope(def, playerUUID);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), playerUUID));

        verifyNoInteractions(mockDeliverer);
    }

    @Test
    @DisplayName("Given a quest with messages and an empty scope, when QuestStartEvent fires, then deliver is never called")
    public void onQuestStart_doesNothing_whenScopeIsEmpty() {
        QuestDefinition def = buildDefWithInlineMessage("msg_listener_empty_scope");
        QuestInstance instance = QuestTestHelper.newQuestInstance(def);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), null));

        verifyNoInteractions(mockDeliverer);
    }

    @Test
    @DisplayName("Given a quest with messages and an offline player in scope, when QuestStartEvent fires, then deliver is never called")
    public void onQuestStart_skipsPlayer_whenPlayerOffline() {
        QuestDefinition def = buildDefWithInlineMessage("msg_listener_offline");
        UUID offlineUUID = UUID.randomUUID();
        QuestInstance instance = instanceWithScope(def, offlineUUID);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), offlineUUID));

        verifyNoInteractions(mockDeliverer);
    }

    @Test
    @DisplayName("Given a quest with messages and an online player in scope, when QuestStartEvent fires, then deliver is called once per message")
    public void onQuestStart_callsDeliver_whenPlayerIsOnline() {
        QuestDefinition def = buildDefWithInlineMessage("msg_listener_online");
        PlayerMock player = server.addPlayer("MsgListenerPlayer");
        UUID playerUUID = player.getUniqueId();
        QuestInstance instance = instanceWithScope(def, playerUUID);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), playerUUID));

        verify(mockDeliverer, times(1)).deliver(eq(player), any(), nullable(Route.class), any());
    }

    @Test
    @DisplayName("Given a quest with two messages and one online player, when QuestStartEvent fires, then deliver is called twice")
    public void onQuestStart_callsDeliverTwice_whenTwoMessages() {
        List<OnStartMessage> twoMessages = List.of(
                OnStartMessage.fromInline(List.of("<primary>First")),
                OnStartMessage.fromInline(List.of("<body>Second"))
        );
        QuestDefinition def = new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", "msg_listener_two_msg"),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(QuestTestHelper.singlePhaseDef(
                        PhaseCompletionMode.ALL,
                        QuestTestHelper.singleStageDef("msg_two_msg_stage", "msg_two_msg_obj")))
        ).onStartMessages(twoMessages).build();

        PlayerMock player = server.addPlayer("TwoMsgPlayer");
        UUID playerUUID = player.getUniqueId();
        QuestInstance instance = instanceWithScope(def, playerUUID);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), playerUUID));

        verify(mockDeliverer, times(2)).deliver(eq(player), any(), nullable(Route.class), any());
    }

    @Test
    @DisplayName("Given a quest with messages and the starter is in a cascade, when QuestStartEvent fires, then messages are deferred to the cascade context")
    public void onQuestStart_defersMessages_whenInCascade() {
        CascadeContext cascadeContext = new CascadeContext(new NamespacedKey("mcrpg", "test_chain"));
        CascadeOrchestrator cascadeOrch = mock(CascadeOrchestrator.class);
        when(cascadeOrch.isInCascade(any(UUID.class))).thenReturn(true);
        when(cascadeOrch.getCascadeContext(any(UUID.class))).thenReturn(Optional.of(cascadeContext));

        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        QuestMessageDeliverer localDeliverer = mock(QuestMessageDeliverer.class);
        server.getPluginManager().registerEvents(
                new QuestStartMessageListener(mcRPG, localDeliverer, cascadeOrch), mcRPG);

        QuestDefinition def = buildDefWithInlineMessage("cascade_defer_test");
        PlayerMock player = server.addPlayer("CascadePlayer");
        UUID playerUUID = player.getUniqueId();
        QuestInstance instance = instanceWithScope(def, playerUUID);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), playerUUID));

        verifyNoInteractions(localDeliverer);
        assertFalse(cascadeContext.getDeferredMessagesFor(def.getQuestKey()).isEmpty());
        verify(cascadeOrch).notifyStepStarted(eq(playerUUID), eq(def.getQuestKey()));
    }

    @Test
    @DisplayName("Given a quest with no messages and the starter is in a cascade, when QuestStartEvent fires, then notifyStepStarted is still called")
    public void onQuestStart_notifiesStepStarted_whenInCascadeWithNoMessages() {
        CascadeContext cascadeContext = new CascadeContext(new NamespacedKey("mcrpg", "test_chain"));
        CascadeOrchestrator cascadeOrch = mock(CascadeOrchestrator.class);
        when(cascadeOrch.isInCascade(any(UUID.class))).thenReturn(true);
        when(cascadeOrch.getCascadeContext(any(UUID.class))).thenReturn(Optional.of(cascadeContext));

        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        QuestMessageDeliverer localDeliverer = mock(QuestMessageDeliverer.class);
        server.getPluginManager().registerEvents(
                new QuestStartMessageListener(mcRPG, localDeliverer, cascadeOrch), mcRPG);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("cascade_no_msg");
        PlayerMock player = server.addPlayer("CascadeNoMsgPlayer");
        UUID playerUUID = player.getUniqueId();
        QuestInstance instance = instanceWithScope(def, playerUUID);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), playerUUID));

        verifyNoInteractions(localDeliverer);
        verify(cascadeOrch).notifyStepStarted(eq(playerUUID), eq(def.getQuestKey()));
    }

    @Test
    @DisplayName("Given a quest with messages and the starter is in a cascade but context is missing, when QuestStartEvent fires, then messages are delivered immediately")
    public void onQuestStart_deliversImmediately_whenCascadeContextMissing() {
        CascadeOrchestrator cascadeOrch = mock(CascadeOrchestrator.class);
        when(cascadeOrch.isInCascade(any(UUID.class))).thenReturn(true);
        when(cascadeOrch.getCascadeContext(any(UUID.class))).thenReturn(Optional.empty());

        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        QuestMessageDeliverer localDeliverer = mock(QuestMessageDeliverer.class);
        server.getPluginManager().registerEvents(
                new QuestStartMessageListener(mcRPG, localDeliverer, cascadeOrch), mcRPG);

        QuestDefinition def = buildDefWithInlineMessage("cascade_missing_ctx");
        PlayerMock player = server.addPlayer("CascadeMissingCtxPlayer");
        UUID playerUUID = player.getUniqueId();
        QuestInstance instance = instanceWithScope(def, playerUUID);

        server.getPluginManager().callEvent(new QuestStartEvent(instance, def, new ManualQuestSource(), playerUUID));

        verify(localDeliverer, times(1)).deliver(eq(player), any(), nullable(Route.class), any());
    }

    /**
     * Creates a quest instance with a scope containing the given player but without calling
     * {@link QuestInstance#start}, which would fire a second {@link QuestStartEvent}.
     *
     * @param def        the quest definition
     * @param playerUUID the player to place in scope
     * @return an unstarted instance with scope set
     */
    @NotNull
    private QuestInstance instanceWithScope(@NotNull QuestDefinition def, @NotNull UUID playerUUID) {
        QuestInstance instance = new QuestInstance(def, null, Map.of(), new ManualQuestSource(), null);
        SinglePlayerQuestScope scope = new SinglePlayerQuestScope(instance.getQuestUUID());
        scope.setPlayerInScope(playerUUID);
        instance.setQuestScope(scope);
        return instance;
    }

    @NotNull
    private QuestDefinition buildDefWithInlineMessage(@NotNull String questKey) {
        return new QuestDefinition.Builder(
                new NamespacedKey("mcrpg", questKey),
                new NamespacedKey("mcrpg", "single_player"),
                List.of(QuestTestHelper.singlePhaseDef(
                        PhaseCompletionMode.ALL,
                        QuestTestHelper.singleStageDef(questKey + "_stage", questKey + "_obj")))
        ).onStartMessages(List.of(OnStartMessage.fromInline(List.of("<primary>Quest started!")))).build();
    }
}
