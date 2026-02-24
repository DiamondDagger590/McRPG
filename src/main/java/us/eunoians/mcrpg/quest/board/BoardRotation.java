package us.eunoians.mcrpg.quest.board;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Tracks a single rotation epoch for a quest board.
 * <p>
 * References a {@link us.eunoians.mcrpg.quest.board.refresh.RefreshType} by key
 * rather than a fixed enum, enabling future event-driven types.
 */
public class BoardRotation {

    private final UUID rotationId;
    private final NamespacedKey boardKey;
    private final NamespacedKey refreshTypeKey;
    private final long rotationEpoch;
    private final long startedAt;
    private final long expiresAt;

    public BoardRotation(@NotNull UUID rotationId,
                         @NotNull NamespacedKey boardKey,
                         @NotNull NamespacedKey refreshTypeKey,
                         long rotationEpoch,
                         long startedAt,
                         long expiresAt) {
        this.rotationId = rotationId;
        this.boardKey = boardKey;
        this.refreshTypeKey = refreshTypeKey;
        this.rotationEpoch = rotationEpoch;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Returns the unique identifier for this rotation.
     *
     * @return the rotation UUID
     */
    @NotNull
    public UUID getRotationId() {
        return rotationId;
    }

    /**
     * Returns the board this rotation belongs to.
     *
     * @return the board key
     */
    @NotNull
    public NamespacedKey getBoardKey() {
        return boardKey;
    }

    /**
     * Returns the refresh type that triggered this rotation.
     *
     * @return the refresh type key
     */
    @NotNull
    public NamespacedKey getRefreshTypeKey() {
        return refreshTypeKey;
    }

    /**
     * Returns the epoch value for this rotation, whose semantics depend on the
     * refresh type (e.g. day-of-epoch for daily, week-of-epoch for weekly).
     *
     * @return the rotation epoch
     */
    public long getRotationEpoch() {
        return rotationEpoch;
    }

    /**
     * Returns the epoch millis when this rotation started.
     *
     * @return the start timestamp
     */
    public long getStartedAt() {
        return startedAt;
    }

    /**
     * Returns the epoch millis when this rotation expires.
     *
     * @return the expiration timestamp
     */
    public long getExpiresAt() {
        return expiresAt;
    }
}
