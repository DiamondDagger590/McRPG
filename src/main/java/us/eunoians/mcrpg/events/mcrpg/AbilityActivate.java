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
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.worldguard.ActionLimiterParser;
import us.eunoians.mcrpg.util.worldguard.WGRegion;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.util.HashMap;
import java.util.List;

public class AbilityActivate implements Listener {

  @EventHandler (priority = EventPriority.NORMAL)
 public void abilityActivateEvent(AbilityActivateEvent e){
   Skills skill = Skills.fromString(e.getAbility().getGenericAbility().getSkill());
   String abilityName = e.getAbility().getGenericAbility().getName().replace(" ", "").replace("_", "").toLowerCase();
   Player p = e.getMcRPGPlayer().getPlayer();
   if(McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getBoolean("UsePermsForAbility." + abilityName) && !(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".*")
	   || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".use.*")|| p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".unlock." + abilityName))){
	 e.setCancelled(true);
   }
   else if(McRPG.getInstance().isWorldGuardEnabled()){
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
     for(ProtectedRegion region : set){
       if(regions.containsKey(region.getId())){
         HashMap<String, List<String>> abilityExpressions = regions.get(region.getId()).getAbilityExpressions();
         if(abilityExpressions.containsKey("All")){
           List<String> expressions = abilityExpressions.get("All");
           for(String s : expressions){
             ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, e.getMcRPGPlayer());
             if(actionLimiterParser.evaluateExpression((UnlockedAbilities) e.getAbility().getGenericAbility())){
               e.setCancelled(true);
               return;
             }
           }
         }
         else if(abilityExpressions.containsKey(e.getAbility().getGenericAbility().getSkill())){
           List<String> expressions = abilityExpressions.get(e.getAbility().getGenericAbility().getSkill());
           if(evaluateExpressions(e, expressions)) return;
         }
         else if(abilityExpressions.containsKey(e.getAbility().getGenericAbility().getName())){
           List<String> expressions = abilityExpressions.get(e.getAbility().getGenericAbility().getName());
           if(evaluateExpressions(e, expressions)) return;
         }
       }
     }
   }
 }

  private boolean evaluateExpressions(AbilityActivateEvent e, List<String> expressions) {
    for(String s : expressions){
      ActionLimiterParser actionLimiterParser = new ActionLimiterParser(s, e.getMcRPGPlayer());
      if(actionLimiterParser.evaluateExpression()){
        e.setCancelled(true);
        return true;
      }
    }
    return false;
  }
}
