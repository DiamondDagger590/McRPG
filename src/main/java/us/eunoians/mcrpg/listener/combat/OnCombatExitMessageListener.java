package us.eunoians.mcrpg.listener.combat;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.configuration.common.ReloadableBoolean;
import com.diamonddagger590.mccore.configuration.common.ReloadableInteger;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.combat.CombatSessionEndReason;
import us.eunoians.mcrpg.combat.log.CombatLogMode;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.CombatConfigFile;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.display.hud.ActionBarHudDisplay;
import us.eunoians.mcrpg.display.hud.CenterContentPriority;
import us.eunoians.mcrpg.display.hud.content.TimedCenterContent;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.combat.CombatSessionEndEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.Set;

/**
 * Sends a brief "no longer in combat" action bar message when a player's combat
 * session ends via timeout or all-participants-gone — but only when the server's
 * combat log configuration would have punished a logout during that session. This
 * tells the player "it is now safe to log out" without adding noise on servers where
 * combat logging has no consequences.
 * <p>
 * The combat log mode is read from the shared {@link ReloadableContent} owned by
 * {@link us.eunoians.mcrpg.combat.log.CombatLogManager} — both sites use a single
 * cached parse. The display flag and duration are {@link ReloadableBoolean} /
 * {@link ReloadableInteger} fields owned by this listener.
 */
public class OnCombatExitMessageListener implements Listener {

    private final McRPG mcRPG;
    private final ReloadableContent<CombatLogMode> mode;
    private final ReloadableBoolean showExitMessage;
    private final ReloadableInteger exitMessageDurationTicks;

    /**
     * Constructs a new {@link OnCombatExitMessageListener}. Creates and registers
     * the reloadable config fields with the
     * {@link com.diamonddagger590.mccore.configuration.ReloadableContentManager}.
     *
     * @param mcRPG The plugin instance for localization and display access.
     * @param mode  The shared reloadable combat log mode, owned by {@link us.eunoians.mcrpg.combat.log.CombatLogManager}.
     */
    public OnCombatExitMessageListener(@NotNull McRPG mcRPG,
                                       @NotNull ReloadableContent<CombatLogMode> mode) {
        this.mcRPG = mcRPG;
        this.mode = mode;

        var config = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.COMBAT_CONFIG);
        this.showExitMessage = new ReloadableBoolean(config, CombatConfigFile.DISPLAY_SHOW_COMBAT_EXIT_MESSAGE);
        this.exitMessageDurationTicks = new ReloadableInteger(config, CombatConfigFile.DISPLAY_EXIT_MESSAGE_DURATION_TICKS);

        mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(ManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(Set.of(showExitMessage, exitMessageDurationTicks));
    }

    /**
     * Handles a combat session end event by sending a conditional exit message.
     *
     * @param event The combat session end event.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCombatSessionEnd(@NotNull CombatSessionEndEvent event) {
        if (!shouldSendExitMessage(event)) {
            return;
        }

        Player player = Bukkit.getPlayer(event.getEntityUUID());
        if (player == null) {
            return;
        }

        McRPGPlayerManager playerManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER);
        Optional<McRPGPlayer> mcRPGPlayerOpt = playerManager.getPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }

        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        DisplayManager displayManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.DISPLAY);
        ActionBarHudDisplay hud = displayManager.getOrCreateActionBarHud(mcRPGPlayer);

        McRPGLocalizationManager localizationManager = mcRPG.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        Component exitMessage = localizationManager.getLocalizedMessageAsComponent(
                mcRPGPlayer, LocalizationKey.COMBAT_EXIT_MESSAGE);

        long expiryTick = Bukkit.getCurrentTick() + exitMessageDurationTicks.getContent();
        hud.setSlot(CenterContentPriority.COMBAT_EXIT_FEEDBACK, new TimedCenterContent(exitMessage, expiryTick));
    }

    /**
     * Determines whether the exit message should be sent for this session end event.
     * Reads from cached reloadable fields — no per-call config parsing.
     *
     * @param event The combat session end event.
     * @return {@code true} if the exit message should be sent.
     */
    private boolean shouldSendExitMessage(@NotNull CombatSessionEndEvent event) {
        CombatSessionEndReason reason = event.getReason();
        if (reason == CombatSessionEndReason.LOGOUT
                || reason == CombatSessionEndReason.DEATH
                || reason == CombatSessionEndReason.PLUGIN) {
            return false;
        }

        if (!showExitMessage.getContent()) {
            return false;
        }

        return mode.getContent().shouldPunish(event.getFinalCombatType());
    }
}
