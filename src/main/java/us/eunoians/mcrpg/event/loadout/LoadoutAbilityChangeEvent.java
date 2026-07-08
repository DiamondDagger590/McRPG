package us.eunoians.mcrpg.event.loadout;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Fired whenever a player's loadout is modified — an ability is equipped, unequipped,
 * or swapped. The {@link ChangeReason} enum indicates what kind of modification occurred.
 * <p>
 * Field availability per reason:
 * <ul>
 *     <li>{@link ChangeReason#EQUIP} — {@link #getNewAbility()} present, {@link #getPreviousAbility()} empty</li>
 *     <li>{@link ChangeReason#UNEQUIP} — {@link #getPreviousAbility()} present, {@link #getNewAbility()} empty</li>
 *     <li>{@link ChangeReason#SWAP} — both present</li>
 * </ul>
 */
public class LoadoutAbilityChangeEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UUID playerUUID;
    private final ChangeReason reason;
    @Nullable
    private final NamespacedKey previousAbility;
    @Nullable
    private final NamespacedKey newAbility;
    private final int loadoutSlot;

    /**
     * Creates a new loadout ability change event.
     *
     * @param playerUUID      the UUID of the player whose loadout changed
     * @param reason          the kind of change that occurred
     * @param previousAbility the ability that was removed or replaced, or {@code null} for {@link ChangeReason#EQUIP}
     * @param newAbility      the ability that was added or placed, or {@code null} for {@link ChangeReason#UNEQUIP}
     * @param loadoutSlot     the loadout slot index affected
     */
    public LoadoutAbilityChangeEvent(@NotNull UUID playerUUID,
                                     @NotNull ChangeReason reason,
                                     @Nullable NamespacedKey previousAbility,
                                     @Nullable NamespacedKey newAbility,
                                     int loadoutSlot) {
        this.playerUUID = playerUUID;
        this.reason = reason;
        this.previousAbility = previousAbility;
        this.newAbility = newAbility;
        this.loadoutSlot = loadoutSlot;
    }

    /**
     * Gets the UUID of the player whose loadout changed.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the kind of loadout change that occurred.
     *
     * @return the change reason
     */
    @NotNull
    public ChangeReason getReason() {
        return reason;
    }

    /**
     * Gets the ability that was removed or replaced. Present for {@link ChangeReason#UNEQUIP}
     * and {@link ChangeReason#SWAP}; empty for {@link ChangeReason#EQUIP}.
     *
     * @return the previous ability key, or empty if this was a fresh equip
     */
    @NotNull
    public Optional<NamespacedKey> getPreviousAbility() {
        return Optional.ofNullable(previousAbility);
    }

    /**
     * Gets the ability that was added or placed into the slot. Present for
     * {@link ChangeReason#EQUIP} and {@link ChangeReason#SWAP}; empty for
     * {@link ChangeReason#UNEQUIP}.
     *
     * @return the new ability key, or empty if this was a removal
     */
    @NotNull
    public Optional<NamespacedKey> getNewAbility() {
        return Optional.ofNullable(newAbility);
    }

    /**
     * Gets the loadout slot index affected by this change.
     *
     * @return the loadout slot index
     */
    public int getLoadoutSlot() {
        return loadoutSlot;
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

    /**
     * Describes the kind of loadout modification that occurred.
     */
    public enum ChangeReason {
        /** A new ability was added to the loadout. */
        EQUIP,
        /** An existing ability was removed from the loadout. */
        UNEQUIP,
        /** An existing ability was replaced by a different ability. */
        SWAP
    }
}
