package us.eunoians.mcrpg.listener.board;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Broadcasts a notification to all online players whenever the quest board rotates.
 * <p>
 * Multiple refresh types (daily + weekly) can rotate the same board in the same cycle,
 * each firing a {@link BoardRotationEvent}. A per-board cooldown suppresses duplicates
 * so players see exactly one notification per rotation window.
 */
public class BoardRotationNotificationListener implements Listener {

    private static final long COOLDOWN_MS = 30_000L;

    /**
     * Timestamp of the last broadcast per board. Bounded by the number of boards;
     * entries are never removed but the map stays trivially small (one entry per board).
     */
    private final Map<NamespacedKey, Long> lastBroadcastMillis = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBoardRotation(@NotNull BoardRotationEvent event) {
        NamespacedKey boardKey = event.getBoard().getBoardKey();
        long now = System.currentTimeMillis();
        if (now - lastBroadcastMillis.getOrDefault(boardKey, 0L) < COOLDOWN_MS) {
            return;
        }
        lastBroadcastMillis.put(boardKey, now);

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
                    Component message = localizationManager.getLocalizedMessageAsComponent(
                            LocalizationKey.QUEST_BOARD_ROTATED_NOTIFICATION);
                    onlinePlayer.sendMessage(message);
                }
            }
        });
    }
}
