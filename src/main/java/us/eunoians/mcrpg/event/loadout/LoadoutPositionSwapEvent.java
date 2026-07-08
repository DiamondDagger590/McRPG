package us.eunoians.mcrpg.event.loadout;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Fired when a player reorders abilities within their active loadout — specifically when
 * two combo-slot positions are swapped. Unlike {@link LoadoutAbilityChangeEvent}, no ability
 * enters or leaves the loadout; only the ordering changes.
 */
public class LoadoutPositionSwapEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID playerUUID;
    private final int fromComboSlot;
    private final int toComboSlot;

    /**
     * Creates a new loadout position swap event.
     *
     * @param playerUUID   the UUID of the player whose loadout positions were swapped
     * @param fromComboSlot the 1-indexed combo slot the ability was moved from
     * @param toComboSlot   the 1-indexed combo slot the ability was moved to
     */
    public LoadoutPositionSwapEvent(@NotNull UUID playerUUID, int fromComboSlot, int toComboSlot) {
        this.playerUUID = playerUUID;
        this.fromComboSlot = fromComboSlot;
        this.toComboSlot = toComboSlot;
    }

    /**
     * Gets the UUID of the player whose loadout was reordered.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the 1-indexed combo slot that the ability was moved from.
     *
     * @return the source combo slot
     */
    public int getFromComboSlot() {
        return fromComboSlot;
    }

    /**
     * Gets the 1-indexed combo slot that the ability was moved to.
     *
     * @return the target combo slot
     */
    public int getToComboSlot() {
        return toComboSlot;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
