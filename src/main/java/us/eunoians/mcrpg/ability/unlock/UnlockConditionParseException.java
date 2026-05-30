package us.eunoians.mcrpg.ability.unlock;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown by {@link UnlockConditionType#parseConfig} when a config section is malformed —
 * missing required keys, invalid values, or mutually-exclusive options both set.
 * <p>
 * Caught by {@link UnlockConditionManager#parseSection}, which logs the offending
 * ability + condition id and skips that single entry rather than aborting startup.
 */
public class UnlockConditionParseException extends RuntimeException {

    public UnlockConditionParseException(@NotNull String message) {
        super(message);
    }

    public UnlockConditionParseException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
