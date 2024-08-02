package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.DropMultiplierAbility;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public class OnBlockDropItemListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void handleDropItem(BlockDropItemEvent blockDropItemEvent) {
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
        abilityRegistry.getAllAbilities().stream()
                .map(abilityRegistry::getRegisteredAbility)
                .filter(ability -> ability instanceof DropMultiplierAbility)
                .map(ability -> (DropMultiplierAbility) ability)
                .forEach(dropMultiplierAbility -> dropMultiplierAbility.processDropEvent(blockDropItemEvent));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleDropMultiplierEvent(BlockDropItemEvent blockDropItemEvent) {
        // Handle remote transfer (cuz she special)
        AbilityRegistry abilityRegistry = McRPG.getInstance().getAbilityRegistry();
        RemoteTransfer remoteTransfer = (RemoteTransfer) abilityRegistry.getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
        var abilityHolderOptional = McRPG.getInstance().getEntityManager().getAbilityHolder(blockDropItemEvent.getPlayer().getUniqueId());
        if (abilityHolderOptional.isPresent()) {
            AbilityHolder abilityHolder = abilityHolderOptional.get();
            remoteTransfer.activateAbility(abilityHolder, blockDropItemEvent);
        }
    }
}
