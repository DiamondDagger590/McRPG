package us.eunoians.mcrpg.ability;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.SkillType;

/**
 * The generic base interface for all abilities to inherit from.
 *
 * @author DiamondDagger590
 */
public interface Ability {

    /**
     * If an ability has been modified and needs saving in some sort of manner, this method will return
     * true, indicating that it should be processed and stored to update the database.
     *
     * @return True if the ability has some dirty data in it that needs stored
     */
    public boolean isDirty();

    /**
     * Sets if this ability has dirty data that needs stored or not
     *
     * @param dirty True if the ability should be marked as dirty for storage
     */
    public void setDirty(boolean dirty);

    /**
     * Gets the {@link AbilityType} enum that represents this ability
     *
     * @return The {@link AbilityType} enum that represents this ability
     */
    @NotNull
    public AbilityType getAbilityType();

    /**
     * Gets the {@link SkillType} that this {@link Ability} belongs to
     *
     * @return The {@link SkillType} that this {@link Ability} belongs to
     */
    @NotNull
    public SkillType getSkill();

    /**
     * @param activator The {@link LivingEntity} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     *                     activation.
     */
    public void activate(LivingEntity activator, Object... optionalData);
}
