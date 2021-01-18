package us.eunoians.mcrpg.ability.impl.taming.gore;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.taming.GoreActivateEvent;
import us.eunoians.mcrpg.player.McRPGPlayer;
import us.eunoians.mcrpg.util.parser.Parser;

import java.util.concurrent.ThreadLocalRandom;

/**
 * This handles activation of {@link Gore}
 *
 * @author DiamondDagger590
 */
public class GoreListener implements Listener {

    /**
     * The {@link NamespacedKey} that maps to {@link Gore}
     */
    private final NamespacedKey GORE_KEY = McRPG.getNamespacedKey("gore");

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handleGore(EntityDamageByEntityEvent entityDamageByEntityEvent) {

        if (entityDamageByEntityEvent.getDamager() instanceof Wolf && ((Wolf) entityDamageByEntityEvent.getDamager()).isTamed()
                && entityDamageByEntityEvent.getEntity() instanceof LivingEntity) {

            Wolf wolf = (Wolf) entityDamageByEntityEvent.getDamager();
            LivingEntity livingEntity = (LivingEntity) entityDamageByEntityEvent.getEntity();

            if (wolf.getOwner() instanceof Player && ((Player) wolf.getOwner()).isOnline()) {

                Player owner = (Player) wolf.getOwner();
                AbilityHolder abilityHolder = new AbilityHolder(owner);

                if (abilityHolder.hasAbility(GORE_KEY)) {

                    Gore gore = (Gore) abilityHolder.getAbility(GORE_KEY);

                    Parser activationEquation = gore.getActivationEquation();
                    //this.getActivationEquation().setVariable("taming_level", taming.getCurrentLevel());

                    if (activationEquation.getValue() * 100000 >= ThreadLocalRandom.current().nextInt(100000)) {

                        GoreActivateEvent goreActivateEvent = new GoreActivateEvent((McRPGPlayer) abilityHolder, gore);
                        Bukkit.getPluginManager().callEvent(goreActivateEvent);

                        if (!goreActivateEvent.isCancelled()) {
                            gore.activate(abilityHolder);
                        }
                    }
                }
            }
        }
    }
}
