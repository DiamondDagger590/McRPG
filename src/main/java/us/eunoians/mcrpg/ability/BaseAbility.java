package us.eunoians.mcrpg.ability;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.creation.AbilityCreationData;

import java.util.List;

/**
 * This class offers some basic construction for an {@link Ability} and should
 * be extended by all abilities
 *
 * @author DiamondDagger590
 */
public abstract class BaseAbility implements Ability {

    /**
     * The {@link AbilityCreationData} that is used to create this {@link Ability}
     */
    private final AbilityCreationData abilityCreationData;

    /**
     * A boolean representing if this {@link Ability} needs saving
     */
    protected boolean dirty;

    /**
     * A {@link List} that contains all registered listeners for this {@link BaseAbility}.
     */
    private List<Listener> registeredListeners;

    /**
     * This assumes that the required extension of {@link AbilityCreationData}. Implementations of this will need
     * to sanitize the input.
     *
     * @param abilityCreationData The {@link AbilityCreationData} that is used to create this {@link Ability}
     */
    public BaseAbility(AbilityCreationData abilityCreationData) {
        this.abilityCreationData = abilityCreationData;
    }

    /**
     * Abstract method that can be used to create listeners for this specific ability.
     * Note: This should only return a {@link List} of {@link Listener} objects. These shouldn't be registered yet!
     * This will be done automatically.
     *
     * @return a list of listeners for this {@link Ability}
     */
    protected abstract List<Listener> createListeners();

    /**
     * Register the {@link Listener} objects for this {@link BaseAbility}.
     *
     * @param plugin the plugin that's creating the ability instance.
     */
    public void registerListeners (@NotNull Plugin plugin) {
        this.registeredListeners = createListeners();
        for (Listener listener : registeredListeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    /**
     * Unregister all {@link Listener} objects for this {@link BaseAbility}.
     */
    public void unregisterListeners () {
        for (Listener listener : registeredListeners) {
            HandlerList.unregisterAll(listener);
        }
    }

    /**
     * Gets the {@link AbilityCreationData} that creates this {@link Ability}.
     *
     * @return The {@link AbilityCreationData} that creates this {@link Ability}
     */
    public @NotNull AbilityCreationData getAbilityCreationData() {
        return this.abilityCreationData;
    }

    /**
     * If an ability has been modified and needs saving in some sort of manner, this method will return
     * true, indicating that it should be processed and stored to update the database.
     *
     * @return True if the ability has some dirty data in it that needs stored
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets if this ability has dirty data that needs stored or not
     *
     * @param dirty True if the ability should be marked as dirty for storage
     */
    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
