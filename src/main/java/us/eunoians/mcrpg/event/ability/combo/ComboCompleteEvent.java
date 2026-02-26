package us.eunoians.mcrpg.event.ability.combo;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired by {@link us.eunoians.mcrpg.ability.combo.ComboTracker} when a player successfully
 * completes a 3-click combo sequence that matches one of the known {@link us.eunoians.mcrpg.ability.combo.ComboPattern}s.
 * <p>
 * Cancelling this event prevents ability activation but still resets the combo state.
 */
public class ComboCompleteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final int slotIndex;
    private boolean cancelled = false;

    /**
     * @param player    The player who completed the combo.
     * @param slotIndex The 1-based slot index (1–3) whose pattern was matched.
     */
    public ComboCompleteEvent(@NotNull Player player, int slotIndex) {
        this.player = player;
        this.slotIndex = slotIndex;
    }

    /**
     * Returns the player who completed the combo.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the 1-based active slot index whose pattern was completed (1, 2, or 3).
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
