package us.eunoians.mcrpg.event.display;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.display.hud.ActionBarCenterContent;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.entity.player.McRPGPlayerEvent;

/**
 * Fired when a caller sets an action bar center-content slot on a player's
 * {@code ActionBarHudDisplay}, before the slot map is mutated.
 * <p>
 * Third-party listeners can:
 * <ul>
 *     <li>{@link #setCancelled(boolean) Cancel} the set to veto the change
 *         entirely.</li>
 *     <li>{@link #setNewContent(ActionBarCenterContent) Replace} the new
 *         content with a wrapped / translated / decorated version.</li>
 * </ul>
 * McRPG's own call sites do not listen to this event — the cancellable
 * contract exists purely for third-party integration.
 */
public class ActionBarSlotSetEvent extends McRPGPlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int priority;
    private final ActionBarCenterContent previousContent;
    private ActionBarCenterContent newContent;
    private boolean cancelled;

    /**
     * @param mcRPGPlayer     The player whose HUD is being updated.
     * @param priority        The slot priority being written.
     * @param previousContent The content that currently occupies the slot, or
     *                        {@code null} if the slot is empty.
     * @param newContent      The content the caller is trying to write.
     */
    public ActionBarSlotSetEvent(@NotNull McRPGPlayer mcRPGPlayer,
                                 int priority,
                                 @Nullable ActionBarCenterContent previousContent,
                                 @NotNull ActionBarCenterContent newContent) {
        super(mcRPGPlayer);
        this.priority = priority;
        this.previousContent = previousContent;
        this.newContent = newContent;
    }

    /**
     * @return The slot priority being written.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @return The content currently occupying the slot, or {@code null} if the
     * slot is empty.
     */
    @Nullable
    public ActionBarCenterContent getPreviousContent() {
        return previousContent;
    }

    /**
     * @return The content the caller is writing to the slot, after any prior
     * listener modifications.
     */
    @NotNull
    public ActionBarCenterContent getNewContent() {
        return newContent;
    }

    /**
     * Replaces the content that will be written if the event is not cancelled.
     *
     * @param newContent The new content to write.
     */
    public void setNewContent(@NotNull ActionBarCenterContent newContent) {
        this.newContent = newContent;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
