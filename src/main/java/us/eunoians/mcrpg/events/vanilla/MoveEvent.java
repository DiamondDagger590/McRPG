package us.eunoians.mcrpg.events.vanilla;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.fitness.RunnersDiet;
import us.eunoians.mcrpg.abilities.woodcutting.NymphsVitality;
import us.eunoians.mcrpg.api.events.mcrpg.fitness.RunnersDietEvent;
import us.eunoians.mcrpg.api.events.mcrpg.woodcutting.NymphsVitalityEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.worldguard.EntryLimiterParser;
import us.eunoians.mcrpg.util.worldguard.WGRegion;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.util.HashMap;
import java.util.List;

public class MoveEvent implements Listener{

  @EventHandler(priority = EventPriority.HIGHEST)
  public void playerMove(PlayerMoveEvent e){
    if(PlayerManager.isPlayerFrozen(e.getPlayer().getUniqueId())){
      e.setCancelled(true);
      return;
    }
    McRPGPlayer player = PlayerManager.getPlayer(e.getPlayer().getUniqueId());
    if(player == null || player.getAbilityLoadout() == null){
      return;
    }
    if(player.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.NYMPHS_VITALITY) &&
            UnlockedAbilities.NYMPHS_VITALITY.isEnabled() && player.getBaseAbility(UnlockedAbilities.NYMPHS_VITALITY).isToggled()){
      Biome biome = e.getPlayer().getLocation().getBlock().getBiome();
      FileConfiguration woodCuttingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.WOODCUTTING_CONFIG);
      if(woodCuttingConfig.getStringList("NymphsVitalityConfig.Biomes").contains(biome.name())){
        NymphsVitality nymphsVitality = (NymphsVitality) player.getBaseAbility(UnlockedAbilities.NYMPHS_VITALITY);
        int minHunger = woodCuttingConfig.getInt("NymphsVitalityConfig.Tier" + Methods.convertToNumeral(nymphsVitality.getCurrentTier()) + ".MinimumHunger");
        Player p = e.getPlayer();
        if(p.getFoodLevel() < minHunger){
          NymphsVitalityEvent nymphsVitalityEvent = new NymphsVitalityEvent(player, nymphsVitality, p.getFoodLevel() + 1, p.getFoodLevel());
          Bukkit.getPluginManager().callEvent(nymphsVitalityEvent);
          if(!nymphsVitalityEvent.isCancelled()){
            p.setFoodLevel(nymphsVitalityEvent.getNewHunger());
          }
        }
      }
    }
    if(UnlockedAbilities.RUNNERS_DIET.isEnabled() && player.getAbilityLoadout().contains(UnlockedAbilities.RUNNERS_DIET)
            && player.getBaseAbility(UnlockedAbilities.RUNNERS_DIET).isToggled()){
      FileConfiguration fitnessConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.FITNESS_CONFIG);
      RunnersDiet runnersDiet = (RunnersDiet) player.getBaseAbility(UnlockedAbilities.RUNNERS_DIET);
      int minHunger = fitnessConfig.getInt("RunnersDietConfig.Tier" + Methods.convertToNumeral(runnersDiet.getCurrentTier())
              + ".MinHunger");
      Player p = e.getPlayer();
      if(p.isSprinting() && p.getFoodLevel() < minHunger){
        RunnersDietEvent runnersDietEvent = new RunnersDietEvent(player, runnersDiet);
        Bukkit.getPluginManager().callEvent(runnersDietEvent);
        if(!runnersDietEvent.isCancelled()){
          p.setFoodLevel(p.getFoodLevel() + 1);
        }
      }
    }
    if(McRPG.getInstance().isWorldGuardEnabled()){
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
