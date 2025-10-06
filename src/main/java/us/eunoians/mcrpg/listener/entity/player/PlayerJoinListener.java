package us.eunoians.mcrpg.listener.entity.player;

import com.google.common.io.BaseEncoding;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.task.player.McRPGPlayerLoadTask;

import java.util.UUID;

/**
 * Starts the {@link McRPGPlayerLoadTask} to load in the player
 */
public class PlayerJoinListener implements Listener {

    private static final UUID RESOURCE_PACK_UUID = UUID.randomUUID();

    @EventHandler(ignoreCancelled = true)
    public void handleJoin(@NotNull PlayerJoinEvent playerJoinEvent) {
        McRPG mcRPG = McRPG.getInstance();
        Player player = playerJoinEvent.getPlayer();
        McRPGPlayer mcRPGPlayer = new McRPGPlayer(player, mcRPG);
        new McRPGPlayerLoadTask(mcRPG, mcRPGPlayer).runTask();

        // TODO add some sort of compatibility wrapper around this
        player.addResourcePack(RESOURCE_PACK_UUID,
                "https://raw.githubusercontent.com/DiamondDagger590/McRPG-ResourcePack/main/mcprg-resource-pack.zip",
                BaseEncoding.base16().decode("60650836c414f856fa22f3c1095c81e9c36df55d".toUpperCase()), "This pack is required for ability icons", true);
    }

    @EventHandler
    public void handleResourcePack(@NotNull PlayerResourcePackStatusEvent playerResourcePackStatusEvent) {
        PlayerResourcePackStatusEvent.Status status = playerResourcePackStatusEvent.getStatus();
        playerResourcePackStatusEvent.getPlayer().sendMessage(Component.text(status.name()));
    }
}
