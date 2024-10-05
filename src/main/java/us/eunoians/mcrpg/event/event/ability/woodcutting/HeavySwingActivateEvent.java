package us.eunoians.mcrpg.event.event.ability.woodcutting;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.woodcutting.HeavySwing;
import us.eunoians.mcrpg.event.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Set;

/**
 * This event is fired whenever {@link HeavySwing} activates.
 */
public class HeavySwingActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability HEAVY_SWING = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(HeavySwing.HEAVY_SWING_KEY);
    private Set<Location> toBreakLocations;
    private boolean cancelled = false;

    public HeavySwingActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Set<Location> toBreakLocations) {
        super(abilityHolder, HEAVY_SWING);
        this.toBreakLocations = toBreakLocations;
    }

    /**
     * Gets a mutable {@link Set} of {@link Location}s that contain all the blocks that will
     * be broken.
     * @return A mutable {@link Set} of {@link Location}s that contain all the blocks that will
     * be broken.
     */
    @NotNull
    public Set<Location> getToBreakLocations() {
        return toBreakLocations;
    }

    /**
     * Sets the {@link Set} of {@link Location}s that contain all the blocks that will be broken.
     * @param toBreakLocations The {@link Set} of {@link Location} that contain all the blocks that will be broken.
     */
    public void setToBreakLocations(@NotNull Set<Location> toBreakLocations) {
        this.toBreakLocations = toBreakLocations;
    }

    @NotNull
    @Override
    public HeavySwing getAbility() {
        return (HeavySwing) super.getAbility();
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
