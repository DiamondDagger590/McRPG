package us.eunoians.mcrpg.listener.combat;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.ReloadableContentManager;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.CombatType;
import us.eunoians.mcrpg.combat.log.CombatLogMode;
import us.eunoians.mcrpg.combat.state.CombatStateSnapshot;
import us.eunoians.mcrpg.combat.stat.CombatSessionStatistics;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.display.hud.ActionBarHudDisplay;
import us.eunoians.mcrpg.display.hud.CenterContentPriority;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.event.combat.CombatSessionEndEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(McRPGPlayerExtension.class)
@DisplayName("OnCombatExitMessageListener")
class OnCombatExitMessageListenerTest extends McRPGBaseTest {

    private YamlDocument combatConfig;
    private DisplayManager displayManager;

    @BeforeEach
    void setUp() {
        FileManager fileManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);

        combatConfig = mock(YamlDocument.class);
        lenient().when(fileManager.getFile(FileType.COMBAT_CONFIG)).thenReturn(combatConfig);
        lenient().when(combatConfig.getBoolean(CombatConfigFile.DISPLAY_SHOW_COMBAT_EXIT_MESSAGE)).thenReturn(true);
        lenient().when(combatConfig.getInt(CombatConfigFile.DISPLAY_EXIT_MESSAGE_DURATION_TICKS)).thenReturn(60);

        YamlDocument hudConfig = mock(YamlDocument.class);
        lenient().when(hudConfig.getBoolean(any(Route.class), anyBoolean())).thenReturn(true);
        lenient().when(fileManager.getFile(FileType.HUD_CONFIG)).thenReturn(hudConfig);

        ReloadableContentManager reloadableContentManager = new ReloadableContentManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(reloadableContentManager);

        displayManager = new DisplayManager(mcRPG);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).register(displayManager);
    }

    private ReloadableContent<CombatLogMode> modeContent(String modeString) {
        lenient().when(combatConfig.getString(CombatConfigFile.COMBAT_LOG_MODE, "DISABLED")).thenReturn(modeString);
        return new ReloadableContent<>(combatConfig, CombatConfigFile.COMBAT_LOG_MODE,
                (yaml, route) -> CombatLogMode.valueOf(yaml.getString(route, "DISABLED")));
    }

    private CombatSessionEndEvent endEvent(McRPGPlayer mcRPGPlayer, CombatSessionEndReason reason, CombatType finalType) {
        return new CombatSessionEndEvent(mcRPGPlayer.getUUID(), reason, List.of(), finalType, 1000L,
                new CombatSessionStatistics().snapshot(), new CombatStateSnapshot(Map.of(), Map.of()));
    }

    private boolean hudHasExitMessage(McRPGPlayer mcRPGPlayer) {
        ActionBarHudDisplay hud = displayManager.getOrCreateActionBarHud(mcRPGPlayer);
        return hud.getSlot(CenterContentPriority.COMBAT_EXIT_FEEDBACK).isPresent();
    }

    @Test
    @DisplayName("sends an exit message when reason is TIMEOUT and mode would punish")
    void sendsExitMessage_onTimeout(McRPGPlayer mcRPGPlayer) {
        PlayerMock player = addPlayerToServer(mcRPGPlayer);
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("PLAYERS"));

        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.TIMEOUT, CombatType.PVP));

        assertTrue(hudHasExitMessage(mcRPGPlayer));
    }

    @Test
    @DisplayName("sends an exit message when reason is ALL_PARTICIPANTS_GONE")
    void sendsExitMessage_onAllParticipantsGone(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("PLAYERS"));

        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.ALL_PARTICIPANTS_GONE, CombatType.PVP));

        assertTrue(hudHasExitMessage(mcRPGPlayer));
    }

    @Test
    @DisplayName("does not send when reason is LOGOUT")
    void doesNotSend_onLogout(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("PLAYERS"));

        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.LOGOUT, CombatType.PVP));

        assertFalse(hudHasExitMessage(mcRPGPlayer));
    }

    @Test
    @DisplayName("does not send when reason is DEATH")
    void doesNotSend_onDeath(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("PLAYERS"));

        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.DEATH, CombatType.PVP));

        assertFalse(hudHasExitMessage(mcRPGPlayer));
    }

    @Test
    @DisplayName("does not send when reason is PLUGIN")
    void doesNotSend_onPlugin(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("PLAYERS"));

        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.PLUGIN, CombatType.PVP));

        assertFalse(hudHasExitMessage(mcRPGPlayer));
    }

    @Test
    @DisplayName("does not send when showExitMessage is false")
    void doesNotSend_whenDisplayFlagDisabled(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        when(combatConfig.getBoolean(CombatConfigFile.DISPLAY_SHOW_COMBAT_EXIT_MESSAGE)).thenReturn(false);
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("PLAYERS"));

        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.TIMEOUT, CombatType.PVP));

        assertFalse(hudHasExitMessage(mcRPGPlayer));
    }

    @Test
    @DisplayName("does not send when mode is DISABLED")
    void doesNotSend_whenModeDisabled(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("DISABLED"));

        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.TIMEOUT, CombatType.PVP));

        assertFalse(hudHasExitMessage(mcRPGPlayer));
    }

    @Test
    @DisplayName("does not send when mode is PLAYERS and the session's final combat type is PVE")
    void doesNotSend_whenPlayersModeAndPve(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("PLAYERS"));

        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.TIMEOUT, CombatType.PVE));

        assertFalse(hudHasExitMessage(mcRPGPlayer));
    }

    @Test
    @DisplayName("does not send when the player is not online")
    void doesNotSend_whenPlayerNotOnline(McRPGPlayer mcRPGPlayer) {
        // Deliberately not added to the server — Bukkit.getPlayer() will return null.
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("PLAYERS"));

        // Must not throw even though the player isn't online.
        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.TIMEOUT, CombatType.PVP));
    }

    @Test
    @DisplayName("does not send when McRPGPlayer is not loaded")
    void doesNotSend_whenMcRPGPlayerNotLoaded(McRPGPlayer mcRPGPlayer) {
        addPlayerToServer(mcRPGPlayer);
        RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER)
                .removePlayer(mcRPGPlayer.getUUID());
        OnCombatExitMessageListener listener = new OnCombatExitMessageListener(mcRPG, modeContent("PLAYERS"));

        // Must not throw even though the McRPGPlayer is no longer tracked by the player manager.
        listener.onCombatSessionEnd(endEvent(mcRPGPlayer, CombatSessionEndReason.TIMEOUT, CombatType.PVP));
    }
}
