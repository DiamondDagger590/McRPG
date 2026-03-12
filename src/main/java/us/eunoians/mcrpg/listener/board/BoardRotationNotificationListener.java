package us.eunoians.mcrpg.listener.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.board.BoardRotationEvent;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;

/**
 * Broadcasts a notification to all online players whenever the quest board rotates.
 * <p>
 * {@link BoardRotationEvent} fires asynchronously from the database executor thread.
 * The message broadcast is therefore scheduled back to the main thread using
 * {@link org.bukkit.scheduler.BukkitScheduler#runTask}.
 */
public class BoardRotationNotificationListener implements Listener {

    /**
     * Schedules a main-thread task that sends a localized board-rotated notification
     * to every online player.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBoardRotation(@NotNull BoardRotationEvent event) {
        McRPG plugin = McRPG.getInstance();
        Bukkit.getScheduler().runTask(plugin, () -> {
            McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION);
            McRPGPlayerManager playerManager = RegistryAccess.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.PLAYER);

            for (var onlinePlayer : Bukkit.getOnlinePlayers()) {
                Optional<McRPGPlayer> mcRPGPlayerOpt = playerManager.getPlayer(onlinePlayer.getUniqueId());
                if (mcRPGPlayerOpt.isPresent()) {
                    McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
                    Component message = localizationManager.getLocalizedMessageAsComponent(
                            mcRPGPlayer, LocalizationKey.QUEST_BOARD_ROTATED_NOTIFICATION);
                    onlinePlayer.sendMessage(message);
                } else {
                    // Fallback for players not yet tracked — use server locale
                    Component message = localizationManager.getLocalizedMessageAsComponent(
                            LocalizationKey.QUEST_BOARD_ROTATED_NOTIFICATION);
                    onlinePlayer.sendMessage(message);
                }
            }
        });
    }
}
