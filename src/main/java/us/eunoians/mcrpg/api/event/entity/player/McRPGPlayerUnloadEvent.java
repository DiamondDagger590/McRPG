package us.eunoians.mcrpg.api.event.entity.player;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * This event is fired whenever an {@link McRPGPlayer} is unloaded.
 */
public class McRPGPlayerUnloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final McRPGPlayer mcRPGPlayer;

    public McRPGPlayerUnloadEvent(@NotNull McRPGPlayer player) {
        this.mcRPGPlayer = player;
    }

    /**
     * Gets the {@link McRPGPlayer} that is unloaded.
     *
     * @return The {@link McRPGPlayer} that is unloaded.
     */
    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
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
