package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.RewardGrantContext;

import java.util.List;
import java.util.UUID;

/**
 * Fired immediately after a batch of quest rewards has been granted to an online player. This is a
 * non-cancellable, informational event intended for audit/logging listeners. The reward list is the
 * set that was actually granted successfully (after any {@link QuestRewardGrantEvent} mutation and
 * after per-reward failures were skipped), and is immutable.
 * <p>
 * This event fires on all three grant pathways — see {@link RewardGrantContext}.
 * <p>
 * <b>Fires only when at least one reward was granted.</b> If the pre-grant
 * {@link QuestRewardGrantEvent} was cancelled, or every reward in the batch threw during
 * {@code grant}, no {@code QuestRewardGrantedEvent} fires. Audit listeners that need to observe
 * every attempted batch (including fully-failed ones) should also listen for
 * {@link QuestRewardGrantEvent}, which fires for every non-empty batch before granting.
 */
public class QuestRewardGrantedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestInstance questInstance;
    private final NamespacedKey questKey;
    private final UUID playerUUID;
    private final List<QuestRewardType> grantedRewards;
    private final RewardGrantContext context;

    /**
     * Creates a new reward-granted event.
     *
     * @param questInstance  the quest instance the rewards belong to, or {@code null} if not tied to
     *                       a live instance
     * @param questKey       the key of the quest the rewards belong to
     * @param playerUUID     the UUID of the player who received the rewards
     * @param grantedRewards the immutable list of rewards that were actually granted
     * @param context        the grant pathway this batch originated from
     */
    public QuestRewardGrantedEvent(@Nullable QuestInstance questInstance, @NotNull NamespacedKey questKey,
                                   @NotNull UUID playerUUID, @NotNull List<QuestRewardType> grantedRewards,
                                   @NotNull RewardGrantContext context) {
        this.questInstance = questInstance;
        this.questKey = questKey;
        this.playerUUID = playerUUID;
        this.grantedRewards = List.copyOf(grantedRewards);
        this.context = context;
    }

    /**
     * Gets the quest instance the rewards belong to, if the grant was tied to a live instance.
     *
     * @return the quest instance, or {@code null} if not tied to a live instance
     */
    @Nullable
    public QuestInstance getQuestInstance() {
        return questInstance;
    }

    /**
     * Gets the key of the quest the rewards belong to.
     *
     * @return the quest key
     */
    @NotNull
    public NamespacedKey getQuestKey() {
        return questKey;
    }

    /**
     * Gets the UUID of the player who received the rewards.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the immutable list of rewards that were actually granted.
     *
     * @return the granted rewards
     */
    @NotNull
    public List<QuestRewardType> getGrantedRewards() {
        return grantedRewards;
    }

    /**
     * Gets the grant pathway this batch originated from.
     *
     * @return the grant context
     */
    @NotNull
    public RewardGrantContext getContext() {
        return context;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return the handler list
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the static handler list for this event, used by Bukkit for registration.
     *
     * @return the handler list
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
