package us.eunoians.mcrpg.skill.impl.swords;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.skill.component.OnAttackLevelableComponent;

/**
 * A collection of all {@link us.eunoians.mcrpg.skill.component.EventLevelableComponent}s used for the
 * {@link Swords} skill.
 */
public class SwordsComponents {

    public static final SwordsLevelOnAttackComponent SWORDS_LEVEL_ON_ATTACK_COMPONENT = new SwordsLevelOnAttackComponent();

    public static class SwordsLevelOnAttackComponent implements OnAttackLevelableComponent {

        @Override
        public int calculateExperienceToGive(@NotNull SkillHolder skillHolder, @NotNull Event event) {
            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event; //Safe cast since can only be called after checks are done
            Entity damager = entityDamageByEntityEvent.getDamager();
            Entity damaged = entityDamageByEntityEvent.getEntity();
            double damage = entityDamageByEntityEvent.getFinalDamage();

            if (damager instanceof LivingEntity livingDamager && livingDamager.getEquipment() != null && damaged instanceof LivingEntity livingDamaged) {
                ItemStack heldItem = livingDamager.getEquipment().getItemInMainHand();

                double expToAward = damage * 1000;
                if (heldItem.getType().equals(Material.GOLDEN_SWORD)) {
                    expToAward *= 1.5;
                }

                return (int) expToAward;
            }

            return 0;
        }

        @Override
        public boolean affectsEntity(@NotNull Entity entity) {
            return entity instanceof LivingEntity;
        }

        @Override
        public boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {

            if (OnAttackLevelableComponent.super.shouldGiveExperience(skillHolder, event)) {
                EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) event; //Safe cast due to super call
                Entity damager = entityDamageByEntityEvent.getDamager();
                Entity damaged = entityDamageByEntityEvent.getEntity();

                if (damager instanceof LivingEntity livingDamager && livingDamager.getEquipment() != null && damaged instanceof LivingEntity livingDamaged) {
                    ItemStack heldItem = livingDamager.getEquipment().getItemInMainHand();
                    if (heldItem.getType().name().contains("_SWORD")) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
