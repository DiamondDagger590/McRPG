package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.ability.impl.CooldownableAbility;
import us.eunoians.mcrpg.event.event.ability.AbilityPutOnCooldownEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.util.LunarUtils;

/**
 * This listener automatically starts the cooldown expire timer whenever
 * an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} gets put on cooldown.
 */
public class OnAbilityPutOnCooldownListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAbilityPutOnCooldown(AbilityPutOnCooldownEvent event) {
        AbilityHolder abilityHolder = event.getAbilityHolder();
        CooldownableAbility cooldownableAbility = event.getAbility();
        long cooldown = event.getCooldown();
        abilityHolder.startCooldownExpireNotificationTimer(cooldownableAbility, cooldown);
        LunarUtils.displayCooldown(abilityHolder.getUUID(), cooldownableAbility.getGuiItem(abilityHolder), cooldownableAbility.getAbilityKey().getKey(), cooldown);
    }
}
