package us.eunoians.mcrpg.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * A base event for all custom McRPG events so that way we can always tell an event comes from McRPG
 *
 * @author DiamondDagger590
 */
public class McRPGEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
