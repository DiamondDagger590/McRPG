package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.OnAttackComponent;
import us.eunoians.mcrpg.ability.component.activatable.TargetablePlayerComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Set;

/**
 * Contains all the {@link us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent} that
 * can activate Bleed.
 */
public class BleedComponents {

    public static final BleedOnAttackComponent BLEED_ON_ATTACK_COMPONENT = new BleedOnAttackComponent();
    public static final BleedOnTargetPlayerComponent BLEED_ON_TARGET_PLAYER_COMPONENT = new BleedOnTargetPlayerComponent();

    private static class BleedOnAttackComponent implements OnAttackComponent {

        private static final Set<Material> SWORDS = Set.of(Material.WOODEN_SWORD, Material.GOLDEN_SWORD,
                Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD);

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

            if(damager instanceof LivingEntity livingEntity) {
                EntityEquipment entityEquipment = livingEntity.getEquipment();

                if(entityEquipment == null) {
                    return false;
                }
                return SWORDS.contains(entityEquipment.getItemInMainHand().getType());
            }
            else {
                return false;
            }
        }
    }

    private static class BleedOnTargetPlayerComponent implements TargetablePlayerComponent {

        @Override
        public boolean affectAllies() {
            return true;
        }

        @Override
        public boolean affectEnemies() {
            return true;
        }

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            if (event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent
                    && entityDamageByEntityEvent.getDamager() instanceof Player damager
                    && entityDamageByEntityEvent.getEntity() instanceof Player damaged) {
                return doesAffect(damager, damaged);
            }
            return true;
        }
    }
}
