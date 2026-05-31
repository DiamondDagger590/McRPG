package us.eunoians.mcrpg.event.ability.guardian;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.guardian.Whirlpool;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This event is fired whenever {@link Whirlpool} activates.
 */
public class WhirlpoolActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final Ability WHIRLPOOL = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(Whirlpool.WHIRLPOOL_KEY);

    private static final HandlerList HANDLERS = new HandlerList();

    private final Location center;
    private boolean cancelled = false;

    /**
     * Constructs a new {@link WhirlpoolActivateEvent}.
     *
     * @param abilityHolder The {@link AbilityHolder} activating the ability
     * @param center        The {@link Location} center of the whirlpool
     */
    public WhirlpoolActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Location center) {
        super(abilityHolder, WHIRLPOOL);
        this.center = center;
    }

    @Override
    @NotNull
    public Whirlpool getAbility() {
        return (Whirlpool) super.getAbility();
    }

    /**
     * Gets the center {@link Location} of the whirlpool.
     *
     * @return The center {@link Location} of the whirlpool
     */
    @NotNull
    public Location getCenter() {
        return center;
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
