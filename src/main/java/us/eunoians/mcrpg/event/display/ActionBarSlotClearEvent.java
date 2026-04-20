package us.eunoians.mcrpg.event.display;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.display.hud.ActionBarCenterContent;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.entity.player.McRPGPlayerEvent;

/**
 * Fired after an action bar center-content slot has been cleared — either via
 * an explicit {@code clearSlot} call or via eviction during the per-frame
 * resolve when the slot's content returns empty.
 * <p>
 * Informational only; listeners cannot cancel or mutate the removal. Useful
 * for third-party plugins that need to react to their own content being
 * reclaimed (analytics, cleanup of companion HUD elements, etc.).
 */
public class ActionBarSlotClearEvent extends McRPGPlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final int priority;
    private final ActionBarCenterContent content;

    /**
     * @param mcRPGPlayer The player whose HUD slot was cleared.
     * @param priority    The slot priority that was cleared.
     * @param content     The content that was previously in the slot.
     */
    public ActionBarSlotClearEvent(@NotNull McRPGPlayer mcRPGPlayer,
                                   int priority,
                                   @NotNull ActionBarCenterContent content) {
        super(mcRPGPlayer);
        this.priority = priority;
        this.content = content;
    }

    /**
     * @return The slot priority that was cleared.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @return The content that occupied the slot prior to being cleared.
     */
    @NotNull
    public ActionBarCenterContent getContent() {
        return content;
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
