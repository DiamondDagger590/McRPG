package us.eunoians.mcrpg.quest.chain;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Controls how many times a quest chain can be completed. Only {@code ONCE} is functionally
 * enforced in the current implementation; the other modes are parsed from YAML and stored
 * but treated identically to {@code ONCE} until repeat-mode backlog work lands.
 */
public enum QuestChainRepeatMode {

    ONCE,
    UNLIMITED,
    COOLDOWN,
    LIMITED,
    COOLDOWN_LIMITED;

    /**
     * Parses a repeat mode from a YAML string value, case-insensitively.
     * Hyphens are converted to underscores for matching.
     *
     * @param value the YAML string (e.g., "once", "cooldown-limited")
     * @return the parsed mode, or empty if unrecognized
     */
    @NotNull
    public static Optional<QuestChainRepeatMode> fromString(@NotNull String value) {
        String normalized = value.trim().toUpperCase().replace('-', '_');
        for (QuestChainRepeatMode mode : values()) {
            if (mode.name().equals(normalized)) {
                return Optional.of(mode);
            }
        }
        return Optional.empty();
    }
}
