package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jetbrains.annotations.NotNull;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.quest.QuestCancelEvent;
import us.eunoians.mcrpg.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.event.quest.QuestExpireEvent;
import us.eunoians.mcrpg.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
public class QuestFeedbackListenerTest extends McRPGBaseTest {

    @AfterEach
    public void teardown() {
        HandlerList.unregisterAll(mcRPG);
    }

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        server.getPluginManager().registerEvents(new QuestFeedbackListener(), mcRPG);

        // Stub the shared localization mock so it returns a non-null Component for any call.
        // Using lenient() avoids "unnecessary stubbing" errors when the stub is not exercised
        // in a particular test, and avoids disrupting stubs set up by other test classes.
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        lenient().when(localizationManager.getLocalizedMessageAsComponent(
                        any(McRPGPlayer.class), any(), anyMap()))
                .thenReturn(Component.text("notification"));
    }

    @DisplayName("Start notification is sent to in-scope online player")
    @Test
    void onQuestStart_scopePlayerOnline_receivesMessage(McRPGPlayer mcRPGPlayer) {
        PlayerMock playerMock = addPlayerToServer(mcRPGPlayer);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("feedback_start");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, mcRPGPlayer.getUUID());

        server.getPluginManager().callEvent(new QuestStartEvent(quest, def));

        assertNotNull(playerMock.nextMessage());
    }

    @DisplayName("Complete notification is sent to in-scope online player")
    @Test
    void onQuestComplete_scopePlayerOnline_receivesMessage(McRPGPlayer mcRPGPlayer) {
        PlayerMock playerMock = addPlayerToServer(mcRPGPlayer);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("feedback_complete");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, mcRPGPlayer.getUUID());

        server.getPluginManager().callEvent(new QuestCompleteEvent(quest, def));

        assertNotNull(playerMock.nextMessage());
    }

    @DisplayName("Expire notification is sent to in-scope online player")
    @Test
    void onQuestExpire_scopePlayerOnline_receivesMessage(McRPGPlayer mcRPGPlayer) {
        PlayerMock playerMock = addPlayerToServer(mcRPGPlayer);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("feedback_expire");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, mcRPGPlayer.getUUID());

        server.getPluginManager().callEvent(new QuestExpireEvent(quest));

        assertNotNull(playerMock.nextMessage());
    }

    @DisplayName("Cancel notification is sent to in-scope player when quest is not expired")
    @Test
    void onQuestCancel_questNotExpired_notifiesScope(McRPGPlayer mcRPGPlayer) {
        PlayerMock playerMock = addPlayerToServer(mcRPGPlayer);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("feedback_cancel_active");
        QuestInstance quest = QuestTestHelper.startedQuestWithPlayer(def, mcRPGPlayer.getUUID());

        server.getPluginManager().callEvent(new QuestCancelEvent(quest));

        assertNotNull(playerMock.nextMessage());
    }

    @DisplayName("Cancel notification is suppressed when quest is already expired (expire already handled it)")
    @Test
    void onQuestCancel_questExpired_doesNotDuplicateNotification(McRPGPlayer mcRPGPlayer) {
        PlayerMock playerMock = addPlayerToServer(mcRPGPlayer);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("feedback_cancel_expired");
        QuestInstance quest = spy(QuestTestHelper.startedQuestWithPlayer(def, mcRPGPlayer.getUUID()));
        // Drain QuestStartEvent notification fired during quest construction
        drainMessages(playerMock);
        doReturn(true).when(quest).isExpired();

        server.getPluginManager().callEvent(new QuestCancelEvent(quest));

        assertNull(playerMock.nextMessage());
    }

    @DisplayName("Notifications are not sent for players not in scope")
    @Test
    void onQuestStart_playerNotInScope_doesNotReceiveMessage(McRPGPlayer mcRPGPlayer) {
        PlayerMock playerMock = addPlayerToServer(mcRPGPlayer);

        QuestDefinition def = QuestTestHelper.singlePhaseQuest("feedback_out_of_scope");
        // Create quest WITHOUT the player in scope; no start event fires for a non-started quest
        QuestInstance quest = QuestTestHelper.newQuestInstance(def);

        server.getPluginManager().callEvent(new QuestStartEvent(quest, def));

        assertNull(playerMock.nextMessage());
    }

    /** Clears any messages sent during player join (e.g. MockBukkit ServerMock.addPlayer side-effects). */
    private void drainMessages(@NotNull PlayerMock playerMock) {
        //noinspection StatementWithEmptyBody
        while (playerMock.nextMessage() != null) { /* drain */ }
    }
}
