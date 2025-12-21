package us.eunoians.mcrpg.event.ability.herbalism;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.herbalism.TooManyPlants;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This ability is fired whenever a player activates {@link TooManyPlants}.
 */
public class TooManyPlantsActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability TO_MANY_PLANTS = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(TooManyPlants.TOO_MANY_PLANTS_KEY);
    private int dropMultiplier;
    private boolean cancelled = false;

    public TooManyPlantsActivateEvent(@NotNull AbilityHolder abilityHolder, int dropMultiplier) {
        super(abilityHolder, TO_MANY_PLANTS);
        this.dropMultiplier = Math.max(1, dropMultiplier);
    }

    @NotNull
    @Override
    public TooManyPlants getAbility() {
        return (TooManyPlants) super.getAbility();
    }

    /**
     * The amount to multiply block drops by.
     * @return The amount to multiply block drops by.
     */
    public int getDropMultiplier() {
        return dropMultiplier;
    }

    /**
     * Sets the multiplier for block drops.
     * @param dropMultiplier The new multiplier for block drops (has to be at least 1).
     */
    public void setDropMultiplier(int dropMultiplier) {
        this.dropMultiplier = Math.max(1, dropMultiplier);
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
