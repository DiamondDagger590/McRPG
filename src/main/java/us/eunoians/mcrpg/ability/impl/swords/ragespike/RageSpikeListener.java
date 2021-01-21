package us.eunoians.mcrpg.ability.impl.swords.ragespike;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.api.AbilityHolder;

/**
 * This listener handles activation of {@link RageSpike}
 *
 * @author DiamondDagger590
 */
public class RageSpikeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleShift(PlayerToggleSneakEvent playerToggleSneakEvent){

        AbilityHolder abilityHolder = new AbilityHolder(playerToggleSneakEvent.getPlayer());

        NamespacedKey id = Ability.getId(RageSpike.class);

        if(abilityHolder.hasAbility(id)) {

            RageSpike rageSpike = (RageSpike) abilityHolder.getAbility(id);

            if (playerToggleSneakEvent.isSneaking()) {

                if (rageSpike.isReady()) {
                    rageSpike.activate(abilityHolder);
                }
            }
            else{

                //Attempt to cancel the charging task
                if(rageSpike.getChargingTask() != null){
                    rageSpike.cancelChargingTask();
                }
            }
        }
    }
}
