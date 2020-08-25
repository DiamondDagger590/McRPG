package us.eunoians.mcrpg.events.mcrpg;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.unarmed.StickyFingers;
import us.eunoians.mcrpg.abilities.unarmed.TighterGrip;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.DisarmEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.StickyFingersEvent;
import us.eunoians.mcrpg.api.events.mcrpg.unarmed.TighterGripEvent;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.DefaultAbilities;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.types.UnlockedAbilities;
import us.eunoians.mcrpg.util.Parser;

import java.util.Random;

public class DisarmHandler implements Listener{

  @EventHandler(priority = EventPriority.HIGH)
  public void disarmHandler(DisarmEvent e){
    McRPGPlayer target = e.getTarget();
    StickyFingers stickyFingers = (StickyFingers) target.getBaseAbility(DefaultAbilities.STICKY_FINGERS);
    if(DefaultAbilities.STICKY_FINGERS.isEnabled() && stickyFingers.isToggled()){
      Parser parser = DefaultAbilities.STICKY_FINGERS.getActivationEquation();
      parser.setVariable("swords_level", target.getSkill(Skills.UNARMED).getCurrentLevel());
      parser.setVariable("power_level", target.getPowerLevel());
      double bonus = 0.0;
      if(UnlockedAbilities.TIGHTER_GRIP.isEnabled() && target.getAbilityLoadout().contains(UnlockedAbilities.TIGHTER_GRIP) && target.getBaseAbility(UnlockedAbilities.TIGHTER_GRIP).isToggled()){
        TighterGrip tighterGrip = (TighterGrip) target.getBaseAbility(UnlockedAbilities.TIGHTER_GRIP);
        bonus = McRPG.getInstance().getFileManager().getFile(FileManager.Files.UNARMED_CONFIG).getDouble("TighterGripConfig.Tier"
                + Methods.convertToNumeral(tighterGrip.getCurrentTier()) + ".GripBoost");
        TighterGripEvent tighterGripEvent = new TighterGripEvent(target, tighterGrip, bonus);
        if(tighterGripEvent.isCancelled()){
          bonus = 0.0;
        }
        else{
          bonus = tighterGripEvent.getGripBonus();
        }
      }
      int chance = (int) ((parser.getValue() + bonus) * 1000);
      Random rand = new Random();
      int val = rand.nextInt(100000);
      if(chance >= val){
        StickyFingersEvent stickyFingersEvent = new StickyFingersEvent(target, stickyFingers);
        Bukkit.getPluginManager().callEvent(stickyFingersEvent);
        if(!stickyFingersEvent.isCancelled()){
          Player targ = target.getPlayer();
          e.setCancelled(true);
          FileConfiguration soundFile = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SOUNDS_FILE);
          targ.getLocation().getWorld().playSound(targ.getLocation(), Sound.valueOf(soundFile.getString("Sounds.Unarmed.StickyFingers.Sound")),
            Float.parseFloat(soundFile.getString("Sounds.Unarmed.StickyFingers.Volume")), Float.parseFloat(soundFile.getString("Sounds.Unarmed.StickyFingers.Pitch")));
          targ.sendMessage(Methods.color(targ, McRPG.getInstance().getPluginPrefix()
                  + McRPG.getInstance().getLangFile().getString("Messages.Abilities.StickyFingers.Resisted")));
          return;
        }
      }
    }
    e.getTarget().getPlayer().setCanPickupItems(false);
    new BukkitRunnable(){
      @Override
      public void run(){
        e.getTarget().getPlayer().setCanPickupItems(true);
      }
    }.runTaskLater(McRPG.getInstance(), McRPG.getInstance().getFileManager().getFile(FileManager.Files.UNARMED_CONFIG)
            .getInt("DisarmConfig.CancelPickupDuration") * 20);
  }
}
