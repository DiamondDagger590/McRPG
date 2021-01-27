package us.eunoians.mcrpg.ability;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcrpg.McRPG;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This interface represents an {@link Ability} that requires some sort of activation
 * action to be done before activation can be performed.
 *
 * @author DiamondDagger590
 */
public interface ReadyableAbility extends Ability {

    Map<UUID, BukkitTask> readyTasks = new HashMap<>();

    /**
     * Checks to see if this ability is currently in a ready status
     *
     * @return {@code true} if this ability is currently in a ready status
     */
    public boolean isReady();

    /**
     * Sets if this ability is currently in a ready status or not
     *
     * @param ready If this ability should be in a ready state or note
     */
    public void setReady(boolean ready);

    /**
     * Handles parsing an {@link Event} to see if this ability should enter "ready" status.
     * <p>
     *
     * @param event The {@link Event} that needs to be parsed
     * @return {@code true} if the {@link ReadyableAbility} should enter "ready" status from this method call
     */
    public boolean handleReadyAttempt(Event event);

    /**
     * This method should only be called by {@link us.eunoians.mcrpg.ability.listener.ReadyableAbilityCheckListener} which
     * handles parsing events to set the {@link us.eunoians.mcrpg.api.AbilityHolder} into
     * a ready state.
     *
     * Actually setting {@link ReadyableAbility#isReady()} is automatically called by {@link us.eunoians.mcrpg.api.manager.ReadyTaskManager}
     */
    public default void startReady(int readySeconds) {
        if(readyTasks.containsKey(getAbilityHolder().getUniqueId())) {
            readyTasks.remove(getAbilityHolder().getUniqueId()).cancel();
        }
        McRPG.getInstance().getReadyTaskManager().startReadyTask(getAbilityHolder(), this, readySeconds);
    }

    /**
     * Gets a {@link Set} of all {@link Material}s that can activate this {@link ReadyableAbility}
     *
     * @return A {@link Set} of all {@link Material}s that can activate this {@link ReadyableAbility}
     */
    public Set<Material> getActivatableMaterials();

    /**
     * Checks to see if this {@link ReadyableAbility} can be set to a ready status by interacting with a block.
     * <p>
     * If this returns {@code false}, then {@link #isValidReadyableBlock(Block)} will not be called.
     *
     * @return {@code true} if this ability can be ready'd from interacting with a {@link Block}
     */
    public boolean readyFromBlock();

    /**
     * Checks to see if interacting with the provided {@link Block} can trigger a ready status
     * for this ability.
     * <p>
     * This method will not be called unless {@link #readyFromBlock()} returns {@code true}
     *
     * @param block The {@link Block} to check for ready status
     * @return {@code true} if the provided {@link Block} is valid for this ability to enter ready status
     */
    default boolean isValidReadyableBlock(Block block) {
        return true;
    }

    /**
     * Checks to see if this {@link ReadyableAbility} can be set to a ready status by interacting with an entity.
     * <p>
     * If this returns {@code false}, then {@link #isValidReadyableEntity(Entity)}  will not be called.
     *
     * @return {@code true} if this ability can be ready'd from interacting with a {@link Entity}
     */
    public boolean readyFromEntity();

    /**
     * Checks to see if interacting with the provided {@link Entity} can trigger a ready status
     * for this ability.
     * <p>
     * This method will not be called unless {@link #readyFromEntity()} returns {@code true}
     *
     * @param entity The {@link Entity} to be checked
     * @return {@code true} if the {@link Entity} is valid for triggering ready status
     */
    default boolean isValidReadyableEntity(Entity entity) {
        return true;
    }

    /**
     * Gets the amount of seconds that the "ready" status should last for this ability
     *
     * @return The amount of seconds that the "ready" status should last for this ability
     */
    public int getReadyDurationSeconds();
}
