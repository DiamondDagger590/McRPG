package us.eunoians.mcrpg.event.ability.guardian;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.guardian.TsunamiWall;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

/**
 * This event is fired whenever {@link TsunamiWall} activates.
 */
public class TsunamiWallActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final Ability TSUNAMI_WALL = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(TsunamiWall.TSUNAMI_WALL_KEY);

    private static final HandlerList HANDLERS = new HandlerList();

    private final Location wallCenter;
    private boolean cancelled = false;

    /**
     * Constructs a new {@link TsunamiWallActivateEvent}.
     *
     * @param abilityHolder The {@link AbilityHolder} activating the ability
     * @param wallCenter    The {@link Location} center of the tsunami wall
     */
    public TsunamiWallActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Location wallCenter) {
        super(abilityHolder, TSUNAMI_WALL);
        this.wallCenter = wallCenter;
    }

    @Override
    @NotNull
    public TsunamiWall getAbility() {
        return (TsunamiWall) super.getAbility();
    }

    /**
     * Gets the center {@link Location} of the tsunami wall.
     *
     * @return The center {@link Location} of the tsunami wall
     */
    @NotNull
    public Location getWallCenter() {
        return wallCenter;
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
