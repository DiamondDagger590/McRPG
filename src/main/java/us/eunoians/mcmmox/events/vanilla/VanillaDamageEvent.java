package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.Bleed;
import us.eunoians.mcmmox.api.events.mcmmo.BleedEvent;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.skills.Skill;
import us.eunoians.mcmmox.types.DefaultAbilities;
import us.eunoians.mcmmox.types.GainReason;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.util.Parser;

import java.util.Random;

public class VanillaDamageEvent implements Listener {

  @EventHandler
  public void damageEvent(EntityDamageByEntityEvent e) {
    FileConfiguration config;
    if (e.getDamager() instanceof Player) {
      Player damager = (Player) e.getDamager();
      McMMOPlayer mp = PlayerManager.getPlayer(damager.getUniqueId());
      if (damager.getItemInHand() == null) {
        //UNARMED
      }
      else {
        Material weapon = damager.getItemInHand().getType();
          if(weapon.name().contains("SWORD")){
            Skill playersSkill = mp.getSkill(Skills.SWORDS);
            if(!Skills.SWORDS.isEnabled()){
              return;
            }
            if(Skills.SWORDS.getEnabledAbilities().contains("Bleed")){
              if(playersSkill.getAbility(DefaultAbilities.BLEED).isToggled()){
                Bleed bleed = (Bleed) playersSkill.getAbility(DefaultAbilities.BLEED);
                Parser parser = DefaultAbilities.BLEED.getActivationEquation();
                if(e.getEntity() instanceof Player){
                  Player damagedPlayer = (Player) e.getEntity();
                  McMMOPlayer dmged = PlayerManager.getPlayer(damagedPlayer.getUniqueId());
                  if(!dmged.isHasBleedImmunity() && bleed.canTarget()){
                    parser.setVariable("swords_level", playersSkill.getCurrentLevel());
                    parser.setVariable("power_level", mp.getPowerLevel());
                    int chance = (int) parser.getValue() * 1000;
                    Random rand = new Random();
                    int val = rand.nextInt(100000);
                    if(chance >= val){
                      BleedEvent event = new BleedEvent(mp, e.getEntity(), bleed);
                      Bukkit.getPluginManager().callEvent(event);
                    }
                  }
                }
                else{
                  parser.setVariable("swords_level", playersSkill.getCurrentLevel());
                  parser.setVariable("power_level", mp.getPowerLevel());
                  Random rand = new Random();
                  int chance = (int) parser.getValue() * 1000;
                  int val = rand.nextInt(100000);
                  if(chance >= val){
                    BleedEvent event = new BleedEvent(mp, e.getEntity(), bleed);
                    Bukkit.getPluginManager().callEvent(event);
                  }
                }
              }
            }
            config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG);
            double multiplier = config.getDouble("MaterialBonus." + weapon);
            int baseExp = 0;
            if(!config.contains("ExpAwardedPerMob." + e.getEntity().toString())){
              baseExp = config.getInt("ExpAwardedPerMob.OTHER");
            }
            else{
              baseExp = config.getInt("ExpAwardedPerMob." + e.getEntity().toString());
            }
            double dmg = e.getDamage();
            int expAwarded = (int) (dmg * baseExp * multiplier);
            mp.getSkill(Skills.SWORDS).giveExp(expAwarded, GainReason.DAMAGE);
            return;
          }
        }
      }
    else {
      return;
    }
  }
}
