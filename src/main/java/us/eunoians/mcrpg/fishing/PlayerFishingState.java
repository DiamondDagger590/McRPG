package us.eunoians.mcrpg.fishing;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Session-only fishing state for a single player. Tracks the accumulated
 * spawn chance, last hook location, and the set of currently active fishing
 * mob UUIDs.
 * <p>
 * This state is <strong>never persisted</strong> to the database. It is
 * discarded when the player logs out and optionally reset on world change.
 * <p>
 * Created lazily via {@link us.eunoians.mcrpg.entity.player.McRPGPlayer#getOrCreateFishingState(double)}
 * on the player's first catch of the session.
 */
public class PlayerFishingState {

    private double currentSpawnChance;
    private Location lastHookLocation;
    private final Set<UUID> activeMobUUIDs;

    /**
     * Creates a new player fishing state with the given initial spawn chance.
     *
     * @param initialChance the starting spawn chance, typically the {@code base-chance}
     *                      value from the fishing mob spawn configuration (0.0–1.0)
     */
    public PlayerFishingState(double initialChance) {
        this.currentSpawnChance = initialChance;
        this.lastHookLocation = null;
        this.activeMobUUIDs = new HashSet<>();
    }

    /**
     * Gets the player's current accumulated spawn chance.
     * <p>
     * This value increases when the player fishes in the same area and
     * decreases when they move to a new area.
     *
     * @return the current spawn chance, between {@code 0.0} and the configured {@code max-chance}
     */
    public double getCurrentSpawnChance() {
        return currentSpawnChance;
    }

    /**
     * Sets the player's current spawn chance.
     *
     * @param chance the new spawn chance value
     */
    public void setCurrentSpawnChance(double chance) {
        this.currentSpawnChance = chance;
    }

    /**
     * Gets the last known hook location for this player.
     * <p>
     * Used to determine whether consecutive casts are in the "same area"
     * (within {@code same-area-range} blocks). Empty if the player has not
     * yet cast or if the location was cleared on world change.
     *
     * @return the last hook location, or empty if no fishing has occurred yet
     */
    @NotNull
    public Optional<Location> getLastHookLocation() {
        return Optional.ofNullable(lastHookLocation);
    }

    /**
     * Sets the last known hook location.
     *
     * @param location the hook location to set
     */
    public void setLastHookLocation(@NotNull Location location) {
        this.lastHookLocation = location;
    }

    /**
     * Clears the last known hook location.
     */
    public void clearLastHookLocation() {
        this.lastHookLocation = null;
    }

    /**
     * Adds a mob UUID to the set of active fishing mobs owned by this player.
     *
     * @param mobUUID the entity UUID of the spawned fishing mob
     */
    public void addActiveMob(@NotNull UUID mobUUID) {
        activeMobUUIDs.add(mobUUID);
    }

    /**
     * Removes a mob UUID from the active set. Called when the mob dies or despawns.
     *
     * @param mobUUID the entity UUID of the mob to remove
     * @return {@code true} if the mob was in the active set and was removed
     */
    public boolean removeActiveMob(@NotNull UUID mobUUID) {
        return activeMobUUIDs.remove(mobUUID);
    }

    /**
     * Gets the number of currently active fishing mobs for this player.
     * <p>
     * Compared against {@code max-active-mobs-per-player} to determine
     * whether new spawns are allowed.
     *
     * @return the active mob count (zero or greater)
     */
    public int getActiveMobCount() {
        return activeMobUUIDs.size();
    }

    /**
     * Gets an unmodifiable view of the active fishing mob UUIDs.
     *
     * @return an unmodifiable set of active mob entity UUIDs
     */
    @NotNull
    public Set<UUID> getActiveMobUUIDs() {
        return Collections.unmodifiableSet(activeMobUUIDs);
    }
}
