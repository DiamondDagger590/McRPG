package us.eunoians.mcrpg.event.entity.player;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * This event is fired whenever an {@link McRPGPlayer} is unloaded.
 */
public class McRPGPlayerUnloadEvent extends McRPGPlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public McRPGPlayerUnloadEvent(@NotNull McRPGPlayer player) {
        super(player);
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
