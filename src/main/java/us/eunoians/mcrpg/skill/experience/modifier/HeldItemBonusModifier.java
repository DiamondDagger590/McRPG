package us.eunoians.mcrpg.skill.experience.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.skill.impl.type.HeldItemBonusSkill;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;
import us.eunoians.mcrpg.skill.experience.context.SkillExperienceContext;

import java.util.Objects;

/**
 * This modifier hooks into {@link HeldItemBonusSkill}s and allows for providing an experience gain modifier
 * based on what items are being held.
 */
public final class HeldItemBonusModifier extends ExperienceModifier {

    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey(McRPG.getInstance(), "material-bonus-modifier");

    @Override
    public NamespacedKey getModifierKey() {
        return MODIFIER_KEY;
    }

    @Override
    public boolean canProcessContext(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext) {
        return skillExperienceContext instanceof EntityDamageContext damageContext
                && damageContext.getEvent().getDamager() instanceof LivingEntity livingEntity
                && livingEntity.getEquipment() != null
                && damageContext.getSkill() instanceof HeldItemBonusSkill;
    }

    @Override
    public double getModifier(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext, int experienceToCalculateOn) {
        EntityDamageContext damageContext = (EntityDamageContext) skillExperienceContext;
        HeldItemBonusSkill heldItemBonusSkill = (HeldItemBonusSkill) damageContext.getSkill();
        LivingEntity attacker = (LivingEntity) damageContext.getEvent().getDamager();
        // This should already be normalized in the skill implementation as it needs to be normalized per held item
        return heldItemBonusSkill.getHeldItemBonus(Objects.requireNonNull(attacker.getEquipment()).getItemInMainHand(), attacker.getEquipment().getItemInOffHand());
    }
}
