package us.eunoians.mcrpg.ability.component.readyable;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Set;

public interface RightClickReadyComponent extends EventReadyableComponent {

    @NotNull
    Set<Material> getValidMaterialsForActivation();

    default boolean isMaterialValidForActivation(@NotNull Material material) {
        return getValidMaterialsForActivation().contains(material);
    }

    @Override
    default boolean shouldReady(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
        // TODO add region blocking
        if (abilityHolder.getReadiedAbility().isEmpty()) {
            if (event instanceof PlayerInteractEvent playerInteractEvent && playerInteractEvent.getPlayer().getUniqueId().equals(abilityHolder.getUUID())
                    && (playerInteractEvent.getAction() == Action.RIGHT_CLICK_AIR || playerInteractEvent.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                return isMaterialValidForActivation(playerInteractEvent.getMaterial());
            } else if (event instanceof PlayerInteractAtEntityEvent playerInteractAtEntityEvent && playerInteractAtEntityEvent.getPlayer().getUniqueId().equals(abilityHolder.getUUID())) {
                return isMaterialValidForActivation(playerInteractAtEntityEvent.getPlayer().getEquipment().getItemInMainHand().getType());
            }
        }
        return false;
    }
}
