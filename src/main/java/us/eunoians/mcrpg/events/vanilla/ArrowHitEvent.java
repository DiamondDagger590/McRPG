package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ArrowHitEvent implements Listener {

    @EventHandler
    public void hitEvent(ProjectileHitEvent e){
        if(e.getEntity() instanceof Arrow){
            if(ShootEvent.getArrowTasks().containsKey(e.getEntity().getUniqueId())){
                ShootEvent.getArrowTasks().remove(e.getEntity().getUniqueId()).cancel();
            }
        }
    }
}
