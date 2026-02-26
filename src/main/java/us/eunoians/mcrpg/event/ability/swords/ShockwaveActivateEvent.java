package us.eunoians.mcrpg.event.ability.swords;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.Shockwave;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This event is fired whenever {@link Shockwave} activates.
 */
public class ShockwaveActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability SHOCKWAVE = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(Shockwave.SHOCKWAVE_KEY);

    private boolean cancelled = false;

    public ShockwaveActivateEvent(@NotNull AbilityHolder abilityHolder) {
        super(abilityHolder, SHOCKWAVE);
    }

    @Override
    @NotNull
    public Shockwave getAbility() {
        return (Shockwave) super.getAbility();
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
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
