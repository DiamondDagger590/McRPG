package us.eunoians.mcrpg.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.player.McRPGPlayer;

/**
 * This class offers some basic construction for an {@link Ability} and should
 * be extended by all abilities
 *
 * @author DiamondDagger590
 */
public abstract class BaseAbility implements Ability {

    /**
     * The {@link McRPGPlayer} who this {@link Ability} belongs to
     */
    private final McRPGPlayer mcRPGPlayer;

    /**
     * A boolean representing if this {@link Ability} needs saving
     */
    protected boolean dirty;

    /**
     * @param mcRPGPlayer The {@link McRPGPlayer} that owns this {@link Ability}
     */
    public BaseAbility(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
    }

    /**
     * Gets the {@link McRPGPlayer} that this {@link Ability} belongs to.
     *
     * @return The {@link McRPGPlayer} that this {@link Ability} belongs to
     */
    public @NotNull McRPGPlayer getPlayer() {
        return this.mcRPGPlayer;
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
