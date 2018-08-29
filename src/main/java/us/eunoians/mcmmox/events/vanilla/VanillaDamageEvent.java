package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.eunoians.mcmmox.skills.Swords;

public class VanillaDamageEvent implements Listener {

    @EventHandler
    public void DamageEvent(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player damager = (Player) e.getDamager();
            if(damager.getItemInHand() == null){
                //UNARMED
            }
            else{
                Material weapon = damager.getItemInHand().getType();
                if(weapon.name().contains("SWORD")){
                    double multiplier = Swords.getWeaponBonus(weapon);
                    int baseExp = Swords.getBaseExpAwarded(e.getEntityType());
                    double dmg = e.getDamage();
                    int expAwarded = (int) (dmg * baseExp * multiplier);
                    //TODO award player swords exp
                }
            }
        }
        else{
            return;
        }
    }
}
