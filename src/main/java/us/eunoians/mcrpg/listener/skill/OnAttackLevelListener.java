package us.eunoians.mcrpg.listener.skill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class OnAttackLevelListener implements SkillListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleOnAttackAbilities(EntityDamageByEntityEvent entityDamageByEntityEvent) {
        levelSkill(entityDamageByEntityEvent.getDamager().getUniqueId(), entityDamageByEntityEvent);
    }
}
