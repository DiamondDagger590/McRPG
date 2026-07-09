package us.eunoians.mcrpg.combat;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents an active combat session for a single entity (the "session owner"). A session
 * tracks every opponent the owner has interacted with as a {@link CombatParticipant} and
 * maintains timing state for both session-level and per-participant inactivity timeouts.
 * <p>
 * Participants are stored in two collections:
 * <ul>
 *   <li><b>Player participants</b> — stored in an unbounded {@link HashMap} keyed by UUID.
 *       There is no cap on the number of player opponents tracked.</li>
 *   <li><b>Mob participants</b> — stored in a bounded FIFO {@link LinkedList}. When the
 *       queue exceeds {@code maxMobParticipants}, the oldest mob is evicted and returned
 *       from {@link #addParticipant(CombatParticipant)} so the caller can fire the
 *       appropriate removal event.</li>
 * </ul>
 * <p>
 * The {@link CombatType} (PVP vs PVE) is derived dynamically from the participant roster
 * rather than stored — a session is PVP if at least one player participant is present,
 * PVE otherwise. This means the combat type can change mid-session as participants are
 * added or removed.
 * <p>
 * Sessions are created, queried, and ended through the {@link CombatTrackerManager}, which
 * owns the session map and fires the corresponding lifecycle events
 * ({@link us.eunoians.mcrpg.event.combat.CombatSessionStartEvent},
 * {@link us.eunoians.mcrpg.event.combat.CombatSessionEndEvent}, etc.). This class is a
 * plain data container with no direct Bukkit event interaction.
 *
 * @see CombatTrackerManager
 * @see CombatParticipant
 * @see CombatType
 */
public class CombatSession {

    private final UUID entityUUID;
    private final Map<UUID, CombatParticipant> playerParticipants;
    private final LinkedList<CombatParticipant> mobParticipants;
    private final int maxMobParticipants;
    private final long startTimeMillis;
    private final long timeoutMillis;
    private long lastActivityMillis;

    /**
     * Constructs a new {@link CombatSession}.
     *
     * @param entityUUID         The UUID of the entity that owns this session.
     * @param maxMobParticipants The maximum number of mob participants before FIFO eviction.
     * @param timeoutMillis      The inactivity timeout in milliseconds.
     */
    public CombatSession(@NotNull UUID entityUUID, int maxMobParticipants, long timeoutMillis) {
        this.entityUUID = entityUUID;
        this.playerParticipants = new HashMap<>();
        this.mobParticipants = new LinkedList<>();
        this.maxMobParticipants = maxMobParticipants;
        long nowMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        this.startTimeMillis = nowMillis;
        this.lastActivityMillis = nowMillis;
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Gets the UUID of the entity that owns this session.
     *
     * @return The UUID of the session owner.
     */
    @NotNull
    public UUID getEntityUUID() {
        return entityUUID;
    }

    /**
     * Derives the current {@link CombatType} from the participant roster.
     * Returns {@link CombatType#PVP} if the roster contains at least one player,
     * {@link CombatType#PVE} otherwise.
     *
     * @return The derived {@link CombatType}.
     */
    @NotNull
    public CombatType getCombatType() {
        return playerParticipants.isEmpty() ? CombatType.PVE : CombatType.PVP;
    }

    /**
     * Gets an unmodifiable view of all participants (both players and mobs).
     *
     * @return An unmodifiable {@link Collection} of all {@link CombatParticipant}s.
     */
    @NotNull
    public Collection<CombatParticipant> getParticipants() {
        List<CombatParticipant> allParticipants = new ArrayList<>(playerParticipants.size() + mobParticipants.size());
        allParticipants.addAll(playerParticipants.values());
        allParticipants.addAll(mobParticipants);
        return Collections.unmodifiableList(allParticipants);
    }

    /**
     * Gets an unmodifiable view of player participants.
     *
     * @return An unmodifiable {@link Collection} of player {@link CombatParticipant}s.
     */
    @NotNull
    public Collection<CombatParticipant> getPlayerParticipants() {
        return Collections.unmodifiableCollection(playerParticipants.values());
    }

    /**
     * Gets an unmodifiable view of mob participants.
     *
     * @return An unmodifiable {@link Collection} of mob {@link CombatParticipant}s.
     */
    @NotNull
    public Collection<CombatParticipant> getMobParticipants() {
        return Collections.unmodifiableCollection(mobParticipants);
    }

    /**
     * Gets a specific participant by UUID.
     *
     * @param participantUUID The UUID of the participant to find.
     * @return An {@link Optional} containing the participant, or empty if not found.
     */
    @NotNull
    public Optional<CombatParticipant> getParticipant(@NotNull UUID participantUUID) {
        CombatParticipant playerParticipant = playerParticipants.get(participantUUID);
        if (playerParticipant != null) {
            return Optional.of(playerParticipant);
        }
        return mobParticipants.stream()
                .filter(participant -> participant.getUUID().equals(participantUUID))
                .findFirst();
    }

    /**
     * Adds a participant to the roster. Player participants go into the unlimited player map.
     * Mob participants go into the FIFO queue — if the queue is full, the oldest mob is evicted
     * and returned.
     *
     * @param participant The participant to add.
     * @return An {@link Optional} containing the evicted {@link CombatParticipant} if a mob was
     *         evicted from the FIFO queue, or empty if no eviction occurred.
     */
    @NotNull
    public Optional<CombatParticipant> addParticipant(@NotNull CombatParticipant participant) {
        if (participant.getParticipantType() == ParticipantType.PLAYER) {
            playerParticipants.put(participant.getUUID(), participant);
            return Optional.empty();
        }

        Optional<CombatParticipant> evictedParticipant = Optional.empty();
        if (!mobParticipants.isEmpty() && mobParticipants.size() >= maxMobParticipants) {
            evictedParticipant = Optional.of(mobParticipants.removeFirst());
        }
        mobParticipants.addLast(participant);
        return evictedParticipant;
    }

    /**
     * Removes a participant from the roster by UUID.
     *
     * @param participantUUID The UUID of the participant to remove.
     * @return An {@link Optional} containing the removed participant, or empty if not found.
     */
    @NotNull
    public Optional<CombatParticipant> removeParticipant(@NotNull UUID participantUUID) {
        CombatParticipant removedPlayer = playerParticipants.remove(participantUUID);
        if (removedPlayer != null) {
            return Optional.of(removedPlayer);
        }
        Iterator<CombatParticipant> mobIterator = mobParticipants.iterator();
        while (mobIterator.hasNext()) {
            CombatParticipant mobParticipant = mobIterator.next();
            if (mobParticipant.getUUID().equals(participantUUID)) {
                mobIterator.remove();
                return Optional.of(mobParticipant);
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if the roster contains a participant with the given UUID.
     *
     * @param participantUUID The UUID to check.
     * @return {@code true} if the participant is in the roster.
     */
    public boolean hasParticipant(@NotNull UUID participantUUID) {
        if (playerParticipants.containsKey(participantUUID)) {
            return true;
        }
        return mobParticipants.stream().anyMatch(participant -> participant.getUUID().equals(participantUUID));
    }

    /**
     * Records combat activity on this session, resetting the inactivity timeout.
     */
    public void recordActivity() {
        this.lastActivityMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
    }

    /**
     * Records an interaction with a specific participant, updating both the session's
     * last activity timestamp and the participant's last interaction timestamp.
     *
     * @param participantUUID The UUID of the participant involved in the interaction.
     */
    public void recordParticipantInteraction(@NotNull UUID participantUUID) {
        long nowMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        this.lastActivityMillis = nowMillis;
        getParticipant(participantUUID).ifPresent(participant -> participant.setLastInteractionMillis(nowMillis));
    }

    /**
     * Checks whether this session has exceeded its inactivity timeout.
     *
     * @return {@code true} if the session has been inactive longer than its timeout.
     */
    public boolean isTimedOut() {
        long currentTimeMillis = McRPG.getInstance().getTimeProvider().now().toEpochMilli();
        return (currentTimeMillis - lastActivityMillis) >= timeoutMillis;
    }

    /**
     * Finds all participants whose per-participant inactivity timer has expired. Used by
     * {@link us.eunoians.mcrpg.combat.task.CombatSessionTimeoutTask} to identify participants
     * that should be removed from the roster.
     *
     * @return A {@link List} of participants that have timed out.
     */
    @NotNull
    public List<CombatParticipant> getTimedOutParticipants() {
        List<CombatParticipant> timedOutParticipants = new ArrayList<>();
        for (CombatParticipant participant : getParticipants()) {
            if (participant.isTimedOut(timeoutMillis)) {
                timedOutParticipants.add(participant);
            }
        }
        return timedOutParticipants;
    }

    /**
     * Gets the epoch milliseconds when this session started.
     *
     * @return The session start timestamp.
     */
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    /**
     * Gets the epoch milliseconds of the most recent combat activity.
     *
     * @return The last activity timestamp.
     */
    public long getLastActivityMillis() {
        return lastActivityMillis;
    }

    /**
     * Computes the session duration in milliseconds, based on the current time.
     *
     * @return The duration in milliseconds.
     */
    public long getDurationMillis() {
        return McRPG.getInstance().getTimeProvider().now().toEpochMilli() - startTimeMillis;
    }

    /**
     * Gets the configured timeout for this session in milliseconds.
     *
     * @return The timeout in milliseconds.
     */
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    /**
     * Checks whether the participant roster is empty.
     *
     * @return {@code true} if no participants remain in the roster.
     */
    public boolean isEmpty() {
        return playerParticipants.isEmpty() && mobParticipants.isEmpty();
    }
}
