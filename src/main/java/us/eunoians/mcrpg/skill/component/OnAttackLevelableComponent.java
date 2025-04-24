package us.eunoians.mcrpg.skill.component;

import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * An {@link EventLevelableComponent} that awards experience whenever a {@link SkillHolder}
 * is the attacker for an {@link EntityDamageByEntityEvent}.
 */
public interface OnAttackLevelableComponent extends EventLevelableComponent {

    /**
     * Checks to see if this ability component can affect the provided
     * {@link Entity}.
     *
     * @param entity The {@link Entity} to check
     * @return {@code true} if the provided {@link Entity} is affected by
     * this ability component
     */
    boolean affectsEntity(@NotNull Entity entity);

    @Override
    default boolean shouldGiveExperience(@NotNull SkillHolder skillHolder, @NotNull Event event) {
        if (event instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
            Entity damager = entityDamageByEntityEvent.getDamager();
            Entity damaged = entityDamageByEntityEvent.getEntity();
            double damage = entityDamageByEntityEvent.getDamage();
            return damager.getUniqueId().equals(skillHolder.getUUID()) && affectsEntity(damaged);
        }
        return false;
    }

    /**
     * Gets the amount of experience to give per damage dealt to the attacked {@link Entity}.
     *
     * @param skillHolder    The {@link SkillHolder} to calculate base experience for.
     * @param attackedEntity The {@link Entity} that was attacked.
     * @return The amount of experience to give per damage dealt to the attacked {@link Entity}.
     */
    int getBaseExperienceForEntity(@NotNull SkillHolder skillHolder, @NotNull Entity attackedEntity);

    /**
     * Gets the amount of damage to award experience for.
     *
     * @param entityDamageByEntityEvent The {@link EntityDamageByEntityEvent} to use for awarding experience.
     * @return The amount of damage to award experience for.
     */
    default double getDamageToAwardExperienceFor(@NotNull EntityDamageByEntityEvent entityDamageByEntityEvent) {
        return Math.min(entityDamageByEntityEvent.getFinalDamage(), McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getDouble(MainConfigFile.MAX_DAMAGE_CAP_TO_AWARD_EXPERIENCE));
    }
}
