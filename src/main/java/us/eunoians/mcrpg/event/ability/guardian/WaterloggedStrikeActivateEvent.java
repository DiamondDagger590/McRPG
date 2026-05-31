package us.eunoians.mcrpg.event.ability.guardian;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.guardian.WaterloggedStrike;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This event is fired whenever {@link WaterloggedStrike} activates.
 */
public class WaterloggedStrikeActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final Ability WATERLOGGED_STRIKE = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(WaterloggedStrike.WATERLOGGED_STRIKE_KEY);

    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;

    /**
     * Constructs a new {@link WaterloggedStrikeActivateEvent}.
     *
     * @param abilityHolder The {@link AbilityHolder} activating the ability
     */
    public WaterloggedStrikeActivateEvent(@NotNull AbilityHolder abilityHolder) {
        super(abilityHolder, WATERLOGGED_STRIKE);
    }

    @Override
    @NotNull
    public WaterloggedStrike getAbility() {
        return (WaterloggedStrike) super.getAbility();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Required by Bukkit to locate the handler list for this event class.
     *
     * @return The {@link HandlerList} for this event
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
