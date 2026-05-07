package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.OnAttackComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Set;

/**
 * A list of general activation components shared across different Swords abilities.
 */
public class SwordsComponents {

    public static final HoldingSwordActivateComponent HOLDING_SWORD_ACTIVATE_COMPONENT = new HoldingSwordActivateComponent();

    private static final Set<Material> SWORDS = Set.of(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.COPPER_SWORD,
            Material.DIAMOND_SWORD, Material.GOLDEN_SWORD, Material.NETHERITE_SWORD);

    private static class HoldingSwordActivateComponent implements OnAttackComponent {

        @Override
        public boolean affectsEntity(@NotNull Entity entity) {
            return entity instanceof LivingEntity;
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {

            if (!OnAttackComponent.super.shouldActivate(abilityHolder, event)) {
                return false;
            }

            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event; //safe to assume after super call
            Entity damager = entityDamageByEntityEvent.getDamager();

            if (damager instanceof LivingEntity livingEntity) {
                EntityEquipment entityEquipment = livingEntity.getEquipment();
                // If the entity isnt holding anything
                if (entityEquipment == null) {
                    return false;
                }
                return SWORDS.contains(entityEquipment.getItemInMainHand().getType());
            } else {
                return false;
            }
        }
    }
}
