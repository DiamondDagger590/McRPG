package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.skills.Swords;
import us.eunoians.mcmmox.types.GainReason;
import us.eunoians.mcmmox.types.Skills;

public class VanillaDamageEvent implements Listener {

  @EventHandler
  public void DamageEvent(EntityDamageByEntityEvent e) {
    FileConfiguration config;
    if (e.getDamager() instanceof Player) {
      Player damager = (Player) e.getDamager();
      McMMOPlayer mp = PlayerManager.getPlayer(damager.getUniqueId());
      if (damager.getItemInHand() == null) {
        //UNARMED
      }
      else {
        Material weapon = damager.getItemInHand().getType();
        if (weapon.name().contains("SWORD")) {
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
          mp.getSkill(Skills.SWORDS).giveExp(baseExp, GainReason.DAMAGE);

          return;
          //TODO award player swords exp
        }
      }
    }
    else {
      return;
    }
  }
}
