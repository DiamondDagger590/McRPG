package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.quest.PreQuestStartEvent;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.TutorialQuestSource;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.DisableTutorialSetting;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link TutorialPreQuestStartListener} — verifies each gating condition
 * (config toggle, bypass permission, player setting) correctly cancels or allows
 * {@link PreQuestStartEvent} for tutorial-sourced quests.
 */
public class TutorialPreQuestStartListenerTest extends McRPGBaseTest {

    private static final String BYPASS_PERMISSION = "mcrpg.tutorial.bypass";

    private QuestDefinition definition;
    private McRPGPlayerManager mockPlayerManager;

    @BeforeEach
    public void setup() {
        HandlerList.unregisterAll(mcRPG);
        server.getPluginManager().clearEvents();
        definition = QuestTestHelper.singlePhaseQuest("tutorial_pre_start_test_quest");
        mockPlayerManager = mock(McRPGPlayerManager.class);
        when(mockPlayerManager.getPlayer(any(UUID.class))).thenReturn(Optional.empty());
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(mockPlayerManager);

        // Ensure the file manager returns a non-null config with tutorial enabled = true
        // so that listener tests can test the bypass and setting gates independently.
        var fileManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        YamlDocument mockConfig = mock(YamlDocument.class);
        when(fileManager.getFile(FileType.MAIN_CONFIG)).thenReturn(mockConfig);
        when(mockConfig.getBoolean(MainConfigFile.TUTORIAL_ENABLED)).thenReturn(true);

        server.getPluginManager().registerEvents(new TutorialPreQuestStartListener(mcRPG), mcRPG);
    }

    @AfterEach
    public void tearDown() {
        HandlerList.unregisterAll(mcRPG);
    }

    @Test
    @DisplayName("Given a non-tutorial quest source, when PreQuestStartEvent fires, then event is not cancelled")
    public void onPreQuestStart_notCancelled_whenSourceIsNotTutorial() {
        PlayerMock playerMock = server.addPlayer();
        PreQuestStartEvent event = new PreQuestStartEvent(definition, playerMock, new ManualQuestSource());

        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given tutorial source and player has bypass permission, when PreQuestStartEvent fires, then event is cancelled")
    public void onPreQuestStart_cancelled_whenPlayerHasBypassPermission() {
        PlayerMock playerMock = server.addPlayer();
        playerMock.addAttachment(mcRPG, BYPASS_PERMISSION, true);
        PreQuestStartEvent event = new PreQuestStartEvent(definition, playerMock, new TutorialQuestSource());

        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("Given tutorial source and player DisableTutorialSetting is DISABLED, when PreQuestStartEvent fires, then event is cancelled")
    public void onPreQuestStart_cancelled_whenPlayerSettingIsDisabled() {
        PlayerMock playerMock = server.addPlayer();
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mockPlayerManager.getPlayer(playerMock.getUniqueId())).thenReturn(Optional.of(mcRPGPlayer));
        doReturn(Optional.of(DisableTutorialSetting.DISABLED))
                .when(mcRPGPlayer).getPlayerSetting(DisableTutorialSetting.SETTING_KEY);
        PreQuestStartEvent event = new PreQuestStartEvent(definition, playerMock, new TutorialQuestSource());

        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled());
    }

    @Test
    @DisplayName("Given tutorial source and player DisableTutorialSetting is ENABLED with no bypass, when PreQuestStartEvent fires, then event is not cancelled")
    public void onPreQuestStart_notCancelled_whenSettingEnabledAndNoBypass() {
        PlayerMock playerMock = server.addPlayer();
        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mockPlayerManager.getPlayer(playerMock.getUniqueId())).thenReturn(Optional.of(mcRPGPlayer));
        doReturn(Optional.of(DisableTutorialSetting.ENABLED))
                .when(mcRPGPlayer).getPlayerSetting(DisableTutorialSetting.SETTING_KEY);
        PreQuestStartEvent event = new PreQuestStartEvent(definition, playerMock, new TutorialQuestSource());

        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled());
    }

    @Test
    @DisplayName("Given tutorial source and player has no McRPGPlayer loaded, when PreQuestStartEvent fires, then event is not cancelled")
    public void onPreQuestStart_notCancelled_whenPlayerNotLoaded() {
        PlayerMock playerMock = server.addPlayer();
        // mockPlayerManager returns Optional.empty() by default for any UUID
        PreQuestStartEvent event = new PreQuestStartEvent(definition, playerMock, new TutorialQuestSource());

        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled());
    }
}
