package us.eunoians.mcrpg.quest.board.distribution;

/**
 * Controls how an individual reward within a split-mode distribution tier is handled.
 * Only meaningful when the tier's {@link RewardSplitMode} is {@code SPLIT_EVEN} or
 * {@code SPLIT_PROPORTIONAL}; ignored for {@code INDIVIDUAL} tiers.
 */
public enum PotBehavior {

    /**
     * Scale the reward amount by the split multiplier (default).
     * 1000 XP pot with 4 players → 250 XP each.
     * Non-scalable reward types fall back to {@code ALL} behavior with a logged warning.
     */
    SCALE,

    /**
     * Grant the full, unscaled reward to the top N contributors in the tier.
     * The number of recipients is controlled by {@code topCount} on the reward entry
     * (defaults to 1). If fewer players qualify than {@code topCount}, all qualifying
     * players receive it. Ties are broken by UUID natural ordering.
     */
    TOP_N,

    /**
     * Grant the full, unscaled reward to all qualifying players (ignoring split mode).
     * Useful for participation tokens or achievement flags that should not be divided.
     */
    ALL
}
