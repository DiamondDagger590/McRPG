package us.eunoians.mcrpg.event.fishing;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a MythicMob spawns as part of the McRPG fishing skill.
 * <p>
 * This event bridges the MythicMobs spawn lifecycle into McRPG's domain,
 * allowing other McRPG systems (or external plugins) to react to or cancel
 * a fishing mob spawn.
 * <p>
 * The event is fired <em>after</em> the MythicMob has been spawned by MythicMobs.
 * Cancelling this event will remove the spawned entity from the world.
 */
public class FishingMobSpawnEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player angler;
    private final Entity mob;
    private final String mythicMobType;
    private final Location spawnLocation;
    private boolean cancelled = false;

    /**
     * Creates a new {@link FishingMobSpawnEvent}.
     *
     * @param angler        The player whose fishing action triggered the mob spawn
     * @param mob           The entity that was spawned by MythicMobs
     * @param mythicMobType The internal MythicMobs mob type identifier
     * @param spawnLocation The location where the mob was spawned
     */
    public FishingMobSpawnEvent(@NotNull Player angler, @NotNull Entity mob,
                                @NotNull String mythicMobType, @NotNull Location spawnLocation) {
        this.angler = angler;
        this.mob = mob;
        this.mythicMobType = mythicMobType;
        this.spawnLocation = spawnLocation;
    }

    /**
     * Gets the player whose fishing action triggered this mob spawn.
     *
     * @return The angler {@link Player}
     */
    @NotNull
    public Player getAngler() {
        return angler;
    }

    /**
     * Gets the entity that was spawned by MythicMobs.
     *
     * @return The spawned {@link Entity}
     */
    @NotNull
    public Entity getMob() {
        return mob;
    }

    /**
     * Gets the internal MythicMobs mob type identifier (e.g. {@code "FishingZombie"}).
     *
     * @return The MythicMobs mob type string
     */
    @NotNull
    public String getMythicMobType() {
        return mythicMobType;
    }

    /**
     * Gets the location where the mob was spawned.
     *
     * @return The spawn {@link Location}
     */
    @NotNull
    public Location getSpawnLocation() {
        return spawnLocation;
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
