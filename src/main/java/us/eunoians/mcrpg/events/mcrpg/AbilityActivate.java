package us.eunoians.mcrpg.events.mcrpg;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.blood.BloodManager;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.worldguard.ActionLimiterParser;
import us.eunoians.mcrpg.util.worldguard.WGRegion;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AbilityActivate implements Listener{
  
  static Map<UUID, BukkitTask> antiCheatTasks = new HashMap<>();
  
  @EventHandler(priority = EventPriority.NORMAL)
  public void abilityActivateEvent(AbilityActivateEvent e){
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getMcRPGPlayer().getPlayer().getWorld().getName())) {
      e.setCancelled(true);
      return;
    }
    Skills skill = e.getAbility().getGenericAbility().getSkill();
    String abilityName = e.getAbility().getGenericAbility().getName().replace(" ", "").replace("_", "").toLowerCase();
    Player p = e.getMcRPGPlayer().getPlayer();
    if(McRPG.getInstance().getFileManager().getFile(FileManager.Files.getSkillFile(skill)).getBoolean("UsePermsForAbility." + abilityName) && !(p.hasPermission("mcrpg.*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".use.*") || p.hasPermission("mcrpg." + skill.getName().toLowerCase() + ".unlock." + abilityName))){
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
    else if(BloodManager.getInstance().isPlayerUnderCurse(e.getMcRPGPlayer().getUuid())){
      e.setCancelled(true);
    }
  }
  
  @EventHandler(priority = EventPriority.MONITOR)
  public void setIgnore(AbilityActivateEvent e){
    if(McRPG.getInstance().isNcpEnabled()){
      UUID uuid = e.getMcRPGPlayer().getUuid();
      NCPExemptionManager.exemptPermanently(uuid);
      if(antiCheatTasks.containsKey(uuid)){
        antiCheatTasks.remove(uuid).cancel();
      }
      BukkitTask bukkitTask = new BukkitRunnable(){
        @Override
        public void run(){
          NCPExemptionManager.unexempt(uuid);
          antiCheatTasks.remove(uuid).cancel();
        }
      }.runTaskLater(McRPG.getInstance(), 5 * 20);
      antiCheatTasks.put(uuid, bukkitTask);
    }
  }
  
  private boolean evaluateExpressions(AbilityActivateEvent e, List<String> expressions){
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
