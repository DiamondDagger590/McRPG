package us.eunoians.mcrpg.skill.experience.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.skill.experience.context.EntityDamageContext;
import us.eunoians.mcrpg.skill.experience.context.SkillExperienceContext;
import us.eunoians.mcrpg.util.EntityKeys;

/**
 * This modifier handles modifying experience gained based on the {@link org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason}
 * of any given {@link Entity}.
 * <p>
 * The reason is attached in the {@link us.eunoians.mcrpg.listener.entity.EntitySpawnListener} along with
 * the modifier to be applied.
 */
public final class SpawnReasonModifier extends ExperienceModifier {

    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey(McRPG.getInstance(), "spawn-reason-modifier");

    @Override
    public NamespacedKey getModifierKey() {
        return MODIFIER_KEY;
    }

    @Override
    public boolean canProcessContext(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext) {
        return skillExperienceContext instanceof EntityDamageContext;
    }

    @Override
    public double getModifier(@NotNull SkillExperienceContext<? extends Event> skillExperienceContext) {
        EntityDamageContext damageContext = (EntityDamageContext) skillExperienceContext;
        Entity entity = damageContext.getEvent().getEntity();
        return entity.getPersistentDataContainer().has(EntityKeys.SPAWN_REASON_EXPERIENCE_MODIFIER_KEY) ?
                entity.getPersistentDataContainer().get(EntityKeys.SPAWN_REASON_EXPERIENCE_MODIFIER_KEY, PersistentDataType.DOUBLE) - 1 : 0.0;
    }
}
