package us.eunoians.mcrpg.quest.reward;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a reward that has been queued for an offline player. When the player
 * next logs in, the reward is reconstructed from its serialized configuration and
 * granted. Pending rewards expire after a configurable duration.
 */
public class PendingReward {

    private final UUID id;
    private final UUID playerUUID;
    private final NamespacedKey rewardTypeKey;
    private final Map<String, Object> serializedConfig;
    private final NamespacedKey questKey;
    private final long createdAt;
    private final long expiresAt;

    /**
     * Creates a new pending reward.
     *
     * @param id               unique identifier for this pending reward
     * @param playerUUID       the UUID of the player who should receive the reward
     * @param rewardTypeKey    the key of the reward type in the registry
     * @param serializedConfig the serialized reward configuration
     * @param questKey         the quest definition key (for audit purposes)
     * @param createdAt        the timestamp when this reward was queued (epoch millis)
     * @param expiresAt        the timestamp when this reward expires (epoch millis)
     */
    public PendingReward(@NotNull UUID id,
                         @NotNull UUID playerUUID,
                         @NotNull NamespacedKey rewardTypeKey,
                         @NotNull Map<String, Object> serializedConfig,
                         @NotNull NamespacedKey questKey,
                         long createdAt,
                         long expiresAt) {
        this.id = id;
        this.playerUUID = playerUUID;
        this.rewardTypeKey = rewardTypeKey;
        this.serializedConfig = Map.copyOf(serializedConfig);
        this.questKey = questKey;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    /**
     * Gets the unique identifier for this pending reward.
     *
     * @return the pending reward ID
     */
    @NotNull
    public UUID getId() {
        return id;
    }

    /**
     * Gets the UUID of the player who should receive this reward.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the key of the reward type that should be used to grant this reward.
     *
     * @return the reward type key
     */
    @NotNull
    public NamespacedKey getRewardTypeKey() {
        return rewardTypeKey;
    }

    /**
     * Gets the serialized reward configuration that can be used to reconstruct
     * the configured reward type via {@link QuestRewardType#parseConfig}.
     *
     * @return an immutable map of the serialized config
     */
    @NotNull
    public Map<String, Object> getSerializedConfig() {
        return serializedConfig;
    }

    /**
     * Gets the quest definition key this reward originated from.
     *
     * @return the quest key
     */
    @NotNull
    public NamespacedKey getQuestKey() {
        return questKey;
    }

    /**
     * Gets the timestamp when this reward was queued.
     *
     * @return the creation timestamp in epoch milliseconds
     */
    public long getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the timestamp when this reward expires and should no longer be granted.
     *
     * @return the expiration timestamp in epoch milliseconds
     */
    public long getExpiresAt() {
        return expiresAt;
    }
}
