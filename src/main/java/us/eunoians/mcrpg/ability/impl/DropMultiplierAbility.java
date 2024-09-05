package us.eunoians.mcrpg.ability.impl;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDropItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Drop Multiplier abilities will multiply the drops of a block by a specific amount when
 * they activate.
 */
public interface DropMultiplierAbility extends Ability {

    Map<Location, Integer> getMultiplierMap();

    /**
     * Checks to see if the provided {@link Block} will have its drops multiplied.
     *
     * @param block The {@link Block} to check for possible multiplication of drops.
     * @return {@code true} if the provided {@link Block} has a multiplier for its drops.
     */
    default boolean isBlockMultiplied(@NotNull Block block) {
        return isBlockMultiplied(block.getLocation());
    }

    /**
     * Checks to see if the provided {@link Location} will have its drops multiplied.
     *
     * @param location The {@link Location} to check for possible multiplication of drops.
     * @return {@code true} if the provided {@link Location} has a multiplier for its drops.
     */
    default boolean isBlockMultiplied(@NotNull Location location) {
        return getMultiplierMap().containsKey(location);
    }

    /**
     * Gets the multiplier for the provided {@link Block}'s drops.
     *
     * @param block The {@link Block} to get the multiplier for.
     * @return The multiplier for the provided {@link Block}'s drops. (Defaults to 1).
     */
    default int getMultiplier(@NotNull Block block) {
        return getMultiplier(block.getLocation());
    }

    /**
     * Gets the multiplier for the provided {@link Location}'s drops.
     *
     * @param location The {@link Location} to get the multiplier for.
     * @return The multiplier for the provided {@link Location}'s drops. (Defaults to 1).
     */
    default int getMultiplier(@NotNull Location location) {
        return getMultiplierMap().getOrDefault(location, 1);
    }

    /**
     * Sets the multiplier for the provided {@link Block}.
     *
     * @param block      The {@link Block} to multiply the drops of.
     * @param multiplier The multiplier for the {@link Block}'s drops.
     */
    default void addMultiplier(@NotNull Block block, int multiplier) {
        addMultiplier(block.getLocation(), multiplier);
    }

    /**
     * Sets the multiplier for the provided {@link Location}.
     *
     * @param location   The {@link Location} to multiply the drops of.
     * @param multiplier The multiplier for the {@link Location}'s drops.
     */
    default void addMultiplier(@NotNull Location location, int multiplier) {
        getMultiplierMap().put(location, multiplier);
    }

    /**
     * Processes the provided {@link Event} and if it is a {@link BlockDropItemEvent},
     * then it will multiply the item drops if there is a block that has an active multiplier
     * before removing the multiplier.
     *
     * @param event The {@link Event} to process.
     */
    default void processDropEvent(@NotNull Event event) {
        if (event instanceof BlockDropItemEvent blockDropItemEvent && isBlockMultiplied(blockDropItemEvent.getBlock())) {
            Location location = blockDropItemEvent.getBlock().getLocation();
            int multiplier = getMultiplier(location);
            for (Item item : blockDropItemEvent.getItems()) {
                item.getItemStack().setAmount(item.getItemStack().getAmount() * multiplier);
            }
            getMultiplierMap().remove(location);
        }
    }

}
