package us.eunoians.mcrpg.exception;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.unlock.UnlockConditionType;

import java.util.Optional;

/**
 * Thrown by {@link UnlockConditionType#parseConfig} when a config section is malformed —
 * missing required keys, invalid values, or mutually-exclusive options both set.
 * <p>
 * Caught by {@link us.eunoians.mcrpg.ability.unlock.UnlockConditionManager#parseSection},
 * which logs the offending ability + condition id and skips that single entry rather than
 * aborting startup.
 */
public class UnlockConditionParseException extends RuntimeException {

    private final NamespacedKey conditionTypeKey;

    public UnlockConditionParseException(@NotNull String message) {
        super(message);
        this.conditionTypeKey = null;
    }

    public UnlockConditionParseException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
        this.conditionTypeKey = null;
    }

    public UnlockConditionParseException(@NotNull NamespacedKey conditionTypeKey, @NotNull String message) {
        super(message);
        this.conditionTypeKey = conditionTypeKey;
    }

    public UnlockConditionParseException(@NotNull NamespacedKey conditionTypeKey, @NotNull String message,
                                         @NotNull Throwable cause) {
        super(message, cause);
        this.conditionTypeKey = conditionTypeKey;
    }

    /**
     * The condition type key that failed to parse. Empty when the exception was thrown
     * without condition context (e.g. from a generic caller).
     *
     * @return the condition type key, or empty
     */
    @NotNull
    public Optional<NamespacedKey> getConditionTypeKey() {
        return Optional.ofNullable(conditionTypeKey);
    }
}
