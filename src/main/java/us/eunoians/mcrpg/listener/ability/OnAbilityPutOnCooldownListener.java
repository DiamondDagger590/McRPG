package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.CooldownableAbility;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.AbilityPutOnCooldownEvent;
import us.eunoians.mcrpg.external.lunar.LunarUtils;

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
        var playerOptional = McRPG.getInstance().getPlayerManager().getPlayer(abilityHolder.getUUID());
        if (McRPG.getInstance().isLunarEnabled() && playerOptional.isPresent() && playerOptional.get() instanceof McRPGPlayer mcRPGPlayer) {
            LunarUtils.displayCooldown(abilityHolder.getUUID(), cooldownableAbility.getDisplayItemBuilder(mcRPGPlayer).asItemStack(), cooldownableAbility.getAbilityKey().getKey(), cooldown);
        }
    }
}
