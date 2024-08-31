package us.eunoians.mcrpg.api.event.ability.mining;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is activated whenever a player activates {@link RemoteTransfer} to teleport
 * items into a linked chest.
 */
public class RemoteTransferActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability REMOTE_TRANSFER = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY);
    private final Location remoteTransferDestination;
    private boolean cancelled = false;

    public RemoteTransferActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Location remoteTransferDestination) {
        super(abilityHolder, REMOTE_TRANSFER);
        this.remoteTransferDestination = remoteTransferDestination;
    }

    @NotNull
    @Override
    public RemoteTransfer getAbility() {
        return (RemoteTransfer) super.getAbility();
    }

    /**
     * Gets the location of the chest items are being teleported to.
     *
     * @return The location of the chest items are being teleported to.
     */
    @NotNull
    public Location getRemoteTransferDestination() {
        return remoteTransferDestination;
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
