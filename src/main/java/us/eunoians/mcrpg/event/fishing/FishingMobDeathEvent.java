package us.eunoians.mcrpg.event.fishing;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a MythicMob that was spawned by the McRPG fishing skill dies.
 * <p>
 * This event bridges the MythicMobs death lifecycle into McRPG's domain,
 * allowing other McRPG systems (or external plugins) to award experience,
 * track kills, or perform post-death logic.
 * <p>
 * The killer may be {@code null} if the mob was killed by environmental damage,
 * despawned, or otherwise died without a player attacker.
 */
public class FishingMobDeathEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Entity mob;
    private final Player killer;
    private final String mythicMobType;

    /**
     * Creates a new {@link FishingMobDeathEvent}.
     *
     * @param mob           The MythicMob entity that died
     * @param killer        The player who killed the mob, or {@code null} if no player killer
     * @param mythicMobType The internal MythicMobs mob type identifier
     */
    public FishingMobDeathEvent(@NotNull Entity mob, @Nullable Player killer,
                                @NotNull String mythicMobType) {
        this.mob = mob;
        this.killer = killer;
        this.mythicMobType = mythicMobType;
    }

    /**
     * Gets the MythicMob entity that died.
     *
     * @return The dead {@link Entity}
     */
    @NotNull
    public Entity getMob() {
        return mob;
    }

    /**
     * Gets the player who killed the mob, if any.
     *
     * @return The killer {@link Player}, or {@code null} if the mob was not killed by a player
     */
    @Nullable
    public Player getKiller() {
        return killer;
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
