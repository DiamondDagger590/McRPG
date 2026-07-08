package us.eunoians.mcrpg.quest.chain.condition.builtin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * A time-based {@link QuestChainStartCondition} that passes only when the current time is
 * at or after a configured {@link LocalDateTime} boundary in the configured timezone.
 * <p>
 * This condition is player-independent — the same boundary applies to all players. The
 * {@link Player} parameter is accepted to satisfy the interface contract but is not used
 * in evaluation.
 *
 * @param key      the condition type key (always {@link TimeGateChainConditionType#KEY})
 * @param after    the earliest local date-time at which this condition passes
 * @param timezone the timezone in which {@code after} is interpreted
 * @see TimeGateChainConditionType
 */
public record TimeGateCondition(
        @NotNull NamespacedKey key,
        @NotNull LocalDateTime after,
        @NotNull ZoneId timezone
) implements QuestChainStartCondition {

    /**
     * {@inheritDoc}
     *
     * @return the condition type key
     */
    @Override
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /**
     * Evaluates whether the given instant is at or after the configured time gate boundary.
     * <p>
     * Converts the provided {@code now} instant to a {@link ZonedDateTime} in the configured
     * {@link #timezone()}, then compares its local date-time against the {@link #after()}
     * boundary. Returns {@code true} if {@code now} is at or after the boundary.
     *
     * @param player the player being evaluated (not used by this condition)
     * @param now    the current instant, provided by the caller for testability
     * @return {@code true} if the current time is at or after the configured boundary
     */
    @Override
    public boolean evaluate(@NotNull Player player, @NotNull Instant now) {
        ZonedDateTime zonedNow = now.atZone(timezone);
        LocalDateTime localNow = zonedNow.toLocalDateTime();
        return !localNow.isBefore(after);
    }
}
