package us.eunoians.mcrpg.event.ability.herbalism;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.herbalism.InstantIrrigation;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This event is fired whenever {@link InstantIrrigation} activates, turning
 * a block into water.
 * <p>
 * If this event is cancelled, the block will not turn into water.
 */
public class InstantIrrigationActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability INSTANT_IRRIGATION = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(InstantIrrigation.INSTANT_IRRIGATION_KEY);

    private final Block block;
    private boolean cancelled = false;

    public InstantIrrigationActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Block block) {
        super(abilityHolder, INSTANT_IRRIGATION);
        this.block = block;
    }

    /**
     * Gets the {@link Block} being turned into water.
     *
     * @return The {@link Block} being turned into water.
     */
    @NotNull
    public Block getBlock() {
        return block;
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
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
