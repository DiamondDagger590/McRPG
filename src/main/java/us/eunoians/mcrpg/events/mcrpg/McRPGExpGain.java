package us.eunoians.mcrpg.events.mcrpg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcrpg.McRPGPlayerExpGainEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.worldguard.ActionLimiterParser;
import us.eunoians.mcrpg.util.worldguard.WGRegion;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.util.HashMap;
import java.util.List;

public class McRPGExpGain implements Listener {

  @EventHandler(priority = EventPriority.NORMAL)
  public void expGain(McRPGPlayerExpGainEvent e) {
    Skills skill = e.getSkillGained().getType();
    Player p = e.getMcRPGPlayer().getPlayer();
    if(McRPG.getInstance().getConfig().getBoolean("Configuration.UseLevelPerms") && !(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".*")
      || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".level"))) {
      e.setCancelled(true);
    }
    else if(e.getSkillGained().getCurrentLevel() >= McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getInt("MaxLevel")) {
      e.setCancelled(true);
    }
    else if(McRPG.getInstance().isWorldGuardEnabled()) {
      WGSupportManager wgSupportManager = McRPG.getInstance().getWgSupportManager();
      if(!wgSupportManager.isWorldTracker(e.getMcRPGPlayer().getPlayer().getWorld())){
        return;
      }
      RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
      Location loc = e.getMcRPGPlayer().getPlayer().getLocation();
      RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));

      HashMap<String, WGRegion> regions = wgSupportManager.getRegionManager().get(loc.getWorld());
      assert manager != null;
      ApplicableRegionSet set = manager.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
      boolean useMultiplier = false;
      double lowestMultiplier = 0;
      boolean continueSeaching = true;
      for(ProtectedRegion region : set) {
        if(regions.containsKey(region.getId())) {
          double multiplier = regions.get(region.getId()).getExpMultiplier();
          if(useMultiplier && multiplier < lowestMultiplier){
            lowestMultiplier = multiplier;
          }
          else{
            lowestMultiplier = multiplier;
            useMultiplier = true;
          }
          if(!continueSeaching){
            continue;
          }
          HashMap<String, List<String>> expExpression = regions.get(region.getId()).getExpGainExpressions();
          if(expExpression.containsKey("All")){
            List<String> expressions = expExpression.get("All");
            for(String s : expressions){
              ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, e.getMcRPGPlayer());
              if(actionLimiterParser.evaluateExpression(e.getSkillGained().getType())){
                e.setCancelled(true);
                continueSeaching = false;
              }
            }
          }
          if(expExpression.containsKey(e.getSkillGained().getType().getName())){
            List<String> expressions = expExpression.get(e.getSkillGained().getType().getName());
            for(String s : expressions){
              ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, e.getMcRPGPlayer());
              if(actionLimiterParser.evaluateExpression()){
                e.setCancelled(true);
                continueSeaching = false;
              }
            }
          }
        }
      }
      e.setExpGained((int) (e.getExpGained() * lowestMultiplier));
    }
  }
}
