package us.eunoians.mcrpg.events.vanilla;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.util.worldguard.EntryLimiterParser;
import us.eunoians.mcrpg.util.worldguard.WGRegion;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.util.HashMap;
import java.util.List;

public class MoveEvent implements Listener {

  @EventHandler (priority = EventPriority.HIGHEST)
  public void playerMove(PlayerMoveEvent e) {
    if (PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())) {
      e.setCancelled(true);
    }
    if(McRPG.getInstance().isWorldGuardEnabled()){
      McRPGPlayer player = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
      WGSupportManager supportManager = McRPG.getInstance().getWgSupportManager();
      World w = e.getTo().getWorld();
      if(!supportManager.isWorldTracker(w)){
        return;
      }
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      RegionManager manager = container.get(BukkitAdapter.adapt(w));
      assert manager != null;
      ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(e.getTo()));
      HashMap<String, WGRegion> regions = supportManager.getRegionManager().get(w);
      EntryLimiterParser entryLimiterParser = new EntryLimiterParser();
      for(ProtectedRegion region : set){
        if(regions.containsKey(region.getId())){
          List<String> entryExpressions = regions.get(region.getId()).getEnterExpressions();
          for(String expression : entryExpressions){
            if(entryLimiterParser.evaluateExpression(player, expression)){
              e.setCancelled(true);
              return;
            }
          }
        }
      }
    }
  }
}
