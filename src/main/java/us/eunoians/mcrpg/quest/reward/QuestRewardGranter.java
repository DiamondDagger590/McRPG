package us.eunoians.mcrpg.quest.reward;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.quest.QuestRewardGrantEvent;
import us.eunoians.mcrpg.event.quest.QuestRewardGrantedEvent;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Central collaborator that grants a batch of {@link QuestRewardType} rewards to an online player,
 * firing the third-party interception events around the grant. Every quest reward-grant pathway
 * (inline completion, distribution, pending-reward login) routes through this class so the
 * {@link QuestRewardGrantEvent} / {@link QuestRewardGrantedEvent} fire uniformly and per-reward
 * failures are isolated in exactly one place.
 */
public final class QuestRewardGranter {

    private final McRPG plugin;

    /**
     * Creates a new granter.
     *
     * @param plugin the plugin instance, used for logging grant failures
     */
    public QuestRewardGranter(@NotNull McRPG plugin) {
        this.plugin = plugin;
    }

    /**
     * Grants a batch of rewards to an online player, firing the cancellable
     * {@link QuestRewardGrantEvent} before the batch and the informational
     * {@link QuestRewardGrantedEvent} after. If the pre-event is cancelled, nothing is granted and an
     * empty list is returned. Listener mutations to the reward list are honored. Each individual
     * {@code reward.grant(player)} is wrapped in its own try/catch so one throwing reward type cannot
     * abort the rest of the batch.
     *
     * @param player     the online player receiving the rewards
     * @param rewards    the rewards to grant; a defensive copy is passed to the event
     * @param questKey   the key of the quest the rewards belong to
     * @param instance   the quest instance the rewards belong to, or {@code null} if not tied to a
     *                   live instance
     * @param context    the grant pathway this batch originates from
     * @return the list of rewards that were actually granted (empty if the event was cancelled)
     */
    @NotNull
    public List<QuestRewardType> grantToOnlinePlayer(@NotNull Player player, @NotNull List<QuestRewardType> rewards,
                                                     @NotNull NamespacedKey questKey, @Nullable QuestInstance instance,
                                                     @NotNull RewardGrantContext context) {
        if (rewards.isEmpty()) {
            return List.of();
        }
        List<QuestRewardType> mutableRewards = new ArrayList<>(rewards);
        QuestRewardGrantEvent grantEvent = new QuestRewardGrantEvent(instance, questKey, player.getUniqueId(),
                mutableRewards, context);
        Bukkit.getPluginManager().callEvent(grantEvent);
        if (grantEvent.isCancelled()) {
            return List.of();
        }

        List<QuestRewardType> granted = new ArrayList<>();
        for (QuestRewardType reward : grantEvent.getRewards()) {
            try {
                reward.grant(player);
                granted.add(reward);
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to grant reward '" + reward.getKey()
                        + "' for quest " + questKey + " to player " + player.getUniqueId()
                        + " (context " + context + "); other rewards are unaffected.", e);
            }
        }

        if (!granted.isEmpty()) {
            Bukkit.getPluginManager().callEvent(new QuestRewardGrantedEvent(instance, questKey,
                    player.getUniqueId(), granted, context));
        }
        return granted;
    }
}
