package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
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
 * Fired immediately before a batch of quest rewards is granted to an online player. This is the
 * single interception point third-party plugins use to modify, add, remove, or veto quest rewards
 * (e.g. an economy plugin doubling weekend XP, or a plugin blocking rewards in a specific world).
 * <p>
 * The reward list returned by {@link #getRewards()} is <b>mutable</b>: listeners may add or remove
 * entries and the changes are honored by the granter. Cancelling the event skips the entire batch;
 * no reward in it is granted and (on the pending-reward login path) the underlying rows are retained
 * for a later retry.
 * <p>
 * This event fires on all three grant pathways — see {@link RewardGrantContext}.
 */
public class QuestRewardGrantEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestInstance questInstance;
    private final NamespacedKey questKey;
    private final UUID playerUUID;
    private final List<QuestRewardType> rewards;
    private final RewardGrantContext context;
    private boolean cancelled;

    /**
     * Creates a new reward-grant event.
     *
     * @param questInstance the quest instance the rewards belong to, or {@code null} if the grant is
     *                      not tied to a live instance (e.g. some pending-reward grants)
     * @param questKey      the key of the quest the rewards belong to
     * @param playerUUID    the UUID of the player receiving the rewards
     * @param rewards       the mutable list of rewards about to be granted
     * @param context       the grant pathway this batch originates from
     */
    public QuestRewardGrantEvent(@Nullable QuestInstance questInstance, @NotNull NamespacedKey questKey,
                                 @NotNull UUID playerUUID, @NotNull List<QuestRewardType> rewards,
                                 @NotNull RewardGrantContext context) {
        this.questInstance = questInstance;
        this.questKey = questKey;
        this.playerUUID = playerUUID;
        this.rewards = rewards;
        this.context = context;
    }

    /**
     * Gets the quest instance the rewards belong to, if the grant is tied to a live instance.
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
     * Gets the UUID of the player receiving the rewards.
     *
     * @return the player UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Gets the mutable list of rewards about to be granted. Listeners may add or remove entries, and
     * the granter grants exactly the list's final contents. As with any Bukkit event, listeners run in
     * priority order and each sees the mutations made by earlier listeners; do not cache the list
     * across listeners and assume it is unchanged. The same list instance is returned to every
     * listener.
     *
     * @return the mutable reward list
     */
    @NotNull
    public List<QuestRewardType> getRewards() {
        return rewards;
    }

    /**
     * Gets the grant pathway this batch originates from.
     *
     * @return the grant context
     */
    @NotNull
    public RewardGrantContext getContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
