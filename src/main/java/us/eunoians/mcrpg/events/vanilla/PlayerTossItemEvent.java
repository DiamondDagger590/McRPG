package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.woodcutting.DemetersShrine;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.events.mcrpg.McRPGExpGain;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.Calendar;
import java.util.UUID;

public class PlayerTossItemEvent implements Listener {

  @EventHandler
  public void tossItem(PlayerDropItemEvent e){
    Player p = e.getPlayer();
    McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
    if(UnlockedAbilities.DEMETERS_SHRINE.isEnabled() && mp.doesPlayerHaveAbilityInLoadout(UnlockedAbilities.DEMETERS_SHRINE)
    && mp.getBaseAbility(UnlockedAbilities.DEMETERS_SHRINE).isToggled()){
      FileConfiguration woodcuttingConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.WOODCUTTING_CONFIG);
      DemetersShrine demetersShrine = (DemetersShrine) mp.getBaseAbility(UnlockedAbilities.DEMETERS_SHRINE);
      String key = "DemetersShrineConfig.Tier" + Methods.convertToNumeral(demetersShrine.getCurrentTier()) + ".";
      if(woodcuttingConfig.getStringList(key + "SacrificialItems").contains(e.getItemDrop().getItemStack().getType().toString())) {
        Item item = e.getItemDrop();
        Bukkit.getScheduler().runTaskLater(McRPG.getInstance(), () -> {
          if(mp.getCooldown(UnlockedAbilities.DEMETERS_SHRINE) != -1){
            p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.DemetersShrine.StillOnCooldown")));
            return;
          }
          if(item.isValid()){
            Location loc = item.getLocation();
            if(loc.getBlock().getType() == Material.WATER){

              if(item.getLocation().add(1, 0, 0).getBlock().getType() == Material.GOLD_BLOCK && item.getLocation().add(-1, 0, 0).getBlock().getType() == Material.GOLD_BLOCK
              && item.getLocation().add(0, 0, 1).getBlock().getType() == Material.GOLD_BLOCK && item.getLocation().add(0, 0, -1).getBlock().getType() == Material.GOLD_BLOCK){
                item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);
                if(item.getItemStack().getAmount() == 0){
                  item.remove();
                }
                loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 5);
                loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 5, 5);
                int duration = woodcuttingConfig.getInt(key + "Duration");
                double multiplier = woodcuttingConfig.getDouble(key + "ExpBoost");
                int cooldown = woodcuttingConfig.getInt(key + "Cooldown");
                McRPGExpGain.addDemetersShrineEffect(e.getPlayer().getUniqueId(), multiplier, duration);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, cooldown);
                p.sendMessage(Methods.color(p, McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.DemetersShrine.Activated")
                .replace("%Multiplier%", Double.toString(multiplier)).replace("%Duration%", Integer.toString(duration/60))));
                mp.getActiveAbilities().add(UnlockedAbilities.DEMETERS_SHRINE);
                mp.addAbilityOnCooldown(UnlockedAbilities.DEMETERS_SHRINE, cal.getTimeInMillis());
              }
            }
          }
        }, 5 * 20);
      }
    }
  }
}
