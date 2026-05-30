package us.eunoians.mcrpg.quest.chain;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Controls how many times a quest chain can be completed.
 *
 * <p><strong>Current enforcement:</strong> Only {@code ONCE} is functionally enforced.
 * {@code UNLIMITED}, {@code COOLDOWN}, {@code LIMITED}, and {@code COOLDOWN_LIMITED} are
 * parsed from YAML and stored, but {@link QuestChainManager#tryStartChain} treats any
 * terminal-state player as ineligible to restart regardless of the configured mode — identical
 * behavior to {@code ONCE}. This means chains configured with non-{@code ONCE} modes will not
 * restart until the enforcement logic is implemented.
 *
 * <p><b>TODO:</b> Implement per-mode enforcement in {@link QuestChainManager#tryStartChain}:
 * {@code UNLIMITED} should always allow restart, {@code LIMITED} should check
 * {@link QuestChainDefinition#getMaxCompletions()} against the completion count,
 * {@code COOLDOWN} should check elapsed time against {@link QuestChainDefinition#getRepeatCooldown()},
 * and {@code COOLDOWN_LIMITED} should enforce both.
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
