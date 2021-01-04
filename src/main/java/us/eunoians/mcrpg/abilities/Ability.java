package us.eunoians.mcrpg.abilities;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.player.McRPGPlayer;
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
     * Gets the {@link McRPGPlayer} that this {@link Ability} belongs to.
     *
     * @return The {@link McRPGPlayer} that this {@link Ability} belongs to
     */
    @NotNull
    public McRPGPlayer getPlayer();

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
     * Attempts to handle the {@link Event} and activate the ability based on the event
     *
     * @param event The {@link Event} to handle
     */
    public void handleEvent(Event event);

    /**
     * Checks to see if the provided {@link Event} is valid for handling
     *
     * @param event The {@link Event} to validate
     * @return True if the event can be passed for testing
     */
    public boolean isValidEvent(Event event);
}
