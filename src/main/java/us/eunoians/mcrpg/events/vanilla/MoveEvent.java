package us.eunoians.mcrpg.events.vanilla;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

public class MoveEvent implements Listener {

  @EventHandler (priority = EventPriority.HIGHEST)
  public void playerMove(PlayerMoveEvent e) {
    if (PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())) {
      e.setCancelled(true);
    }
    if(McRPG.getInstance().isWorldGuardEnabled()){
      WGSupportManager supportManager = McRPG.getInstance().getWgSupportManager();
      if(!supportManager.isWorldTracker(e.getTo().getWorld())){
        return;
      }
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionManager manager = container.get(BukkitAdapter.adapt(e.getTo().getWorld()));
      assert manager != null;
      ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(e.getTo()));
      for(ProtectedRegion region : set){

      }
    }
  }
}
