package us.eunoians.mcmmox.events.mcmmo;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.unarmed.StickyFingers;
import us.eunoians.mcmmox.abilities.unarmed.TighterGrip;
import us.eunoians.mcmmox.api.events.mcmmo.DisarmEvent;
import us.eunoians.mcmmox.api.events.mcmmo.StickyFingersEvent;
import us.eunoians.mcmmox.api.events.mcmmo.TighterGripEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.types.DefaultAbilities;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;
import us.eunoians.mcmmox.util.Parser;

import java.util.Random;

public class DisarmHandler implements Listener {

  @EventHandler
  public void disarmHandler(DisarmEvent e){
	McMMOPlayer target = e.getTarget();
	StickyFingers stickyFingers = (StickyFingers) target.getBaseAbility(DefaultAbilities.STICKY_FINGERS);
	if(DefaultAbilities.STICKY_FINGERS.isEnabled() && stickyFingers.isToggled()){
	  Parser parser = DefaultAbilities.STICKY_FINGERS.getActivationEquation();
	  parser.setVariable("swords_level", target.getSkill(Skills.UNARMED).getCurrentLevel());
	  parser.setVariable("power_level", target.getPowerLevel());
	  double bonus = 0.0;
	  if(UnlockedAbilities.TIGHTER_GRIP.isEnabled() && target.getAbilityLoadout().contains(UnlockedAbilities.TIGHTER_GRIP) && target.getBaseAbility(UnlockedAbilities.TIGHTER_GRIP).isToggled()){
		TighterGrip tighterGrip = (TighterGrip) target.getBaseAbility(UnlockedAbilities.TIGHTER_GRIP);
		bonus = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.UNARMED_CONFIG).getDouble("TighterGripConfig.Tier"
			+ Methods.convertToNumeral(tighterGrip.getCurrentTier()) + ".GripBoost");
		TighterGripEvent tighterGripEvent = new TighterGripEvent(target, tighterGrip, bonus);
		if(tighterGripEvent.isCancelled()){
		  bonus = 0.0;
		}
		else{
		  bonus = tighterGripEvent.getGripBonus();
		}
	  }
	  int chance = (int) (parser.getValue() + bonus) * 1000;
	  Random rand = new Random();
	  int val = rand.nextInt(100000);
	  if(chance >= val){
		StickyFingersEvent stickyFingersEvent = new StickyFingersEvent(target, stickyFingers);
		Bukkit.getPluginManager().callEvent(stickyFingersEvent);
		if(!stickyFingersEvent.isCancelled()){
		  Player targ = target.getPlayer();
		  e.setCancelled(true);
		  targ.getLocation().getWorld().playSound(targ.getLocation(), Sound.ENTITY_SLIME_ATTACK, 10, 1);
		  targ.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix()
		  + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.StickyFingers.Resisted")));
		}
	  }
	}
  }
}
