package us.eunoians.mcrpg.api.event.ability.woodcutting;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.woodcutting.DryadsGift;
import us.eunoians.mcrpg.api.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is fired whenever {@link DryadsGift} activates.
 */
public class DryadsGiftActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability DRYADS_GIFT = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(DryadsGift.DRYADS_GIFT_KEY);
    private int experienceToDrop;
    private boolean cancelled = false;

    public DryadsGiftActivateEvent(@NotNull AbilityHolder abilityHolder, int experienceToDrop) {
        super(abilityHolder, DRYADS_GIFT);
        this.experienceToDrop = Math.max(1, experienceToDrop);
    }

    @NotNull
    @Override
    public DryadsGift getAbility() {
        return (DryadsGift) super.getAbility();
    }

    /**
     * Gets the amount of experience to drop.
     *
     * @return The amount of experience to drop.
     */
    public int getExperienceToDrop() {
        return experienceToDrop;
    }

    /**
     * Sets the amount experience to drop.
     *
     * @param experienceToDrop The new amount of experience to drop.
     */
    public void setDropMultiplier(int experienceToDrop) {
        this.experienceToDrop = Math.max(1, experienceToDrop);
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
