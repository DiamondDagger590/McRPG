package us.eunoians.mcrpg.ability.impl;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockDropItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface DropMultiplierAbility extends Ability {

    Map<Location, Integer> getMultiplierMap();

    default boolean isBlockMultiplied(@NotNull Block block) {
        return isBlockMultiplied(block.getLocation());
    }

    default boolean isBlockMultiplied(@NotNull Location location) {
        return getMultiplierMap().containsKey(location);
    }

    default int getMultiplier(@NotNull Block block) {
        return getMultiplier(block.getLocation());
    }

    default int getMultiplier(@NotNull Location location) {
        return getMultiplierMap().getOrDefault(location, 1);
    }

    default void addMultiplier(@NotNull Block block, int multiplier) {
        addMultiplier(block.getLocation(), multiplier);
    }

    default void addMultiplier(@NotNull Location location, int multiplier) {
        getMultiplierMap().put(location, multiplier);
    }

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
