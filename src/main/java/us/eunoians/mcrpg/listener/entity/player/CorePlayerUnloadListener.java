package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.event.player.PlayerUnloadEvent;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Handles unloading the user's data now that they've been saved
 */
public class CorePlayerUnloadListener implements Listener {

    @EventHandler
    public void onPlayerUnload(PlayerUnloadEvent event) {
        CorePlayer corePlayer = event.getCorePlayer();
        McRPG mcRPG = McRPG.getInstance();
        if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
            mcRPGPlayer.asSkillHolder().cleanupHolder();
            mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.ENTITY).removeAbilityHolder(mcRPGPlayer.getUUID());
            mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).removePlayer(mcRPGPlayer.getUUID());
            mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DISPLAY).removeDisplay(mcRPGPlayer.getUUID());
        }

    }
}
