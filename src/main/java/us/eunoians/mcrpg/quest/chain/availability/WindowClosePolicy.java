package us.eunoians.mcrpg.quest.chain.availability;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Defines what happens to active chain or quest instances when their
 * availability window closes.
 */
public enum WindowClosePolicy {

    /** Immediately expire/cancel all active instances. */
    EXPIRE_ACTIVE,
    /** Block new starts but allow active instances to complete naturally. */
    ALLOW_FINISH,
    /** Warn players, then expire after a configurable grace period. */
    EXPIRE_WITH_GRACE;

    /**
     * Parses a close policy from a YAML config string.
     *
     * @param value the config string (e.g., "expire-active", "allow-finish")
     * @return the parsed policy, or empty if unrecognized
     */
    @NotNull
    public static Optional<WindowClosePolicy> fromString(@NotNull String value) {
        try {
            return Optional.of(valueOf(value.toUpperCase().replace('-', '_')));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
