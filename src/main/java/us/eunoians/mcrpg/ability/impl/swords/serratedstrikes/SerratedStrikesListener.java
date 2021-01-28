package us.eunoians.mcrpg.ability.impl.swords.serratedstrikes;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * Handles activation of {@link SerratedStrikes}
 *
 * @author DiamondDagger590
 */
public class SerratedStrikesListener implements Listener {

    private static final NamespacedKey SERRATED_STRIKES_KEY = McRPG.getNamespacedKey("serrated_strikes");

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleActivate(EntityDamageByEntityEvent entityDamageByEntityEvent){
        if(entityDamageByEntityEvent.getDamager() instanceof LivingEntity && entityDamageByEntityEvent.getEntity() instanceof LivingEntity &&
                AbilityHolder.isAbilityHolder((LivingEntity) entityDamageByEntityEvent.getDamager())) {

            LivingEntity damager = (LivingEntity) entityDamageByEntityEvent.getDamager();
            AbilityHolder abilityHolder = damager instanceof Player ? new AbilityHolder((Player) damager) : AbilityHolder.getFromEntity(damager);

            if(abilityHolder instanceof McRPGPlayer ? abilityHolder.getAbilityFromLoadout(SERRATED_STRIKES_KEY) != null : abilityHolder.hasAbility(SERRATED_STRIKES_KEY)) {

                SerratedStrikes serratedStrikes = (SerratedStrikes) abilityHolder.getAbility(SERRATED_STRIKES_KEY);

                if (serratedStrikes.isToggled() && serratedStrikes.isReady()) {
                    serratedStrikes.activate(abilityHolder, entityDamageByEntityEvent);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleDeath(PlayerDeathEvent playerDeathEvent){
        if(playerDeathEvent.getEntity().hasMetadata(SerratedStrikes.METADATA_KEY)){
            playerDeathEvent.getEntity().removeMetadata(SerratedStrikes.METADATA_KEY, McRPG.getInstance());
        }
    }
}
