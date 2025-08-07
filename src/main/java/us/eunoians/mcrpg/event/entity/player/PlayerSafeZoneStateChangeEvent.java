package us.eunoians.mcrpg.event.entity.player;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * This event is fired whenever a {@link McRPGPlayer} is no longer in a safe zone
 * or has just been marked as entering a safe zone.
 */
public class PlayerSafeZoneStateChangeEvent extends McRPGPlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private SafeZoneStateChangeType safeZoneStateChangeType;

    public PlayerSafeZoneStateChangeEvent(@NotNull McRPGPlayer mcRPGPlayer, @NotNull SafeZoneStateChangeType safeZoneStateChangeType) {
        super(mcRPGPlayer);
        this.safeZoneStateChangeType = safeZoneStateChangeType;
    }

    /**
     * Gets the type of change that is being represented by this event.
     *
     * @return The type of change that is being represented by this event.
     */
    @NotNull
    public SafeZoneStateChangeType getSafeZoneStateChangeType() {
        return safeZoneStateChangeType;
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

    /**
     * This enum is all the possible reasons that this event can be fired.
     */
    public enum SafeZoneStateChangeType {
        /**
         * A player was marked as not in a safe zone but is now marked as being in one.
         */
        ENTERED,
        /**
         * A player was marked as in a safe zone but is now marked as not being in one.
         */
        LEFT
    }
}
