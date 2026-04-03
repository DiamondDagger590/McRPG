package us.eunoians.mcrpg.event.item;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.entity.player.McRPGPlayerEvent;

/**
 * Fired when a player attempts to consume a skill book.
 * <p>
 * This event is fired <b>before</b> the ability is unlocked. If cancelled,
 * the item is not consumed and no {@link us.eunoians.mcrpg.event.ability.AbilityUnlockEvent}
 * is fired.
 * <p>
 * Third-party plugins can listen to this event to:
 * <ul>
 *   <li>Gate consumption behind additional conditions (location, level, currency)</li>
 *   <li>Log or track skill book usage</li>
 *   <li>Inspect the item being consumed (read-only clone)</li>
 * </ul>
 */
public class SkillBookConsumeEvent extends McRPGPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final NamespacedKey abilityKey;
    private final ItemStack itemStack;
    private boolean cancelled;

    /**
     * Creates a new skill book consume event.
     *
     * @param mcRPGPlayer the player consuming the skill book
     * @param abilityKey  the {@link NamespacedKey} of the ability being unlocked
     * @param itemStack   the skill book item being consumed
     */
    public SkillBookConsumeEvent(@NotNull McRPGPlayer mcRPGPlayer,
                                 @NotNull NamespacedKey abilityKey,
                                 @NotNull ItemStack itemStack) {
        super(mcRPGPlayer);
        this.abilityKey = abilityKey;
        this.itemStack = itemStack;
        this.cancelled = false;
    }

    /**
     * Gets the ability key that this skill book would unlock.
     *
     * @return the ability {@link NamespacedKey}
     */
    @NotNull
    public NamespacedKey getAbilityKey() {
        return abilityKey;
    }

    /**
     * Gets a defensive copy of the skill book item stack being consumed.
     * <p>
     * Returns a clone so that event listeners cannot mutate the actual item
     * in the player's inventory. The real item is removed by the consumption
     * listener after this event completes uncancelled.
     *
     * @return a clone of the skill book item stack
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
