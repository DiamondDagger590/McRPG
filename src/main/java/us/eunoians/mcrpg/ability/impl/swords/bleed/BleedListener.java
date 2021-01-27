package us.eunoians.mcrpg.ability.impl.swords.bleed;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.taming.GoreActivateEvent;
import us.eunoians.mcrpg.api.manager.BleedManager;
import us.eunoians.mcrpg.util.parser.Parser;

/**
 * This {@link Listener} handles activation of {@link Bleed}
 *
 * @author DiamondDagger590
 */
public class BleedListener implements Listener {

    private static final NamespacedKey BLEED_KEY = McRPG.getNamespacedKey("bleed");

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleGore(GoreActivateEvent goreActivateEvent){

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleDamage(EntityDamageByEntityEvent entityDamageByEntityEvent){

        if(entityDamageByEntityEvent.getDamager() instanceof LivingEntity && entityDamageByEntityEvent.getEntity() instanceof LivingEntity &&
                AbilityHolder.isAbilityHolder((LivingEntity) entityDamageByEntityEvent.getDamager())){

            LivingEntity damager = (LivingEntity) entityDamageByEntityEvent.getDamager();
            AbilityHolder abilityHolder = damager instanceof Player ? new AbilityHolder((Player) damager) : AbilityHolder.getFromEntity(damager);

            if(abilityHolder.hasAbility(BLEED_KEY)){

                Bleed bleed = (Bleed) abilityHolder.getAbility(BLEED_KEY);

                BleedManager bleedManager = McRPG.getInstance().getBleedManager();

                LivingEntity damagee = (LivingEntity) entityDamageByEntityEvent.getEntity();

                if (bleedManager.canInflictBleed(damagee.getUniqueId(), damagee.getUniqueId())
                        && damager.getEquipment() != null && damager.getEquipment().getItemInMainHand().getType().toString().endsWith("_SWORD")) {

                    Parser parser = bleed.getActivationEquation();

                    //TODO try activation odds
                    if(true) {
                        bleed.activate(abilityHolder, damagee);
                    }
                }
            }
        }
    }
}
