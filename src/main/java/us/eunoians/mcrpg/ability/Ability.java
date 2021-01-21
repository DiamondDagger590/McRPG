package us.eunoians.mcrpg.ability;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.annotation.AbilityIdentifier;
import us.eunoians.mcrpg.api.AbilityHolder;

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
     * Gets the {@link NamespacedKey} that this {@link Ability} belongs to
     *
     * @return The {@link NamespacedKey} that this {@link Ability} belongs to
     */
    @NotNull
    public NamespacedKey getSkill();

    /**
     * Gets the {@link AbilityHolder} that owns this {@link Ability}
     *
     * @return THe {@link AbilityHolder} that owns this {@link Ability}
     */
    @NotNull
    public AbilityHolder getAbilityHolder();

    /**
     * @param activator    The {@link AbilityHolder} that is activating this {@link Ability}
     * @param optionalData Any objects that should be passed in. It is up to the implementation of the
     *                     ability to sanitize this input but this is here as there is no way to allow a
     *                     generic activation method without providing access for all types of ability
     *                     activation.
     */
    public void activate(AbilityHolder activator, Object... optionalData);

    /**
     * Get the {@link NamespacedKey} for a specified {@link Ability}.
     *
     * @param clazz the class of the ability implementation
     * @return the {@link NamespacedKey} for the ability.
     */
    static NamespacedKey getId(Class<? extends BaseAbility> clazz) {
        if (clazz.getAnnotation(AbilityIdentifier.class) == null) {
            throw new IllegalArgumentException(clazz.getName() + " does not have the ability identifier annotation!");
        }

        return new NamespacedKey(McRPG.getInstance(), clazz.getAnnotation(AbilityIdentifier.class).id());
    }
}
