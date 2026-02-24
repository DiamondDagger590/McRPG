package us.eunoians.mcrpg.quest.board.template;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when {@link GeneratedQuestDefinitionSerializer} fails to deserialize a
 * JSON snapshot back into a {@link us.eunoians.mcrpg.quest.definition.QuestDefinition}.
 * Exposes the quest key (if parseable) and a description of the element that failed,
 * helping third-party expansion developers identify unregistered types or corrupted data.
 */
public class QuestDeserializationException extends RuntimeException {

    private final String questKey;
    private final String failedElement;

    /**
     * @param message        human-readable description of the failure
     * @param questKey       the {@code quest_key} value from the JSON, or {@code null} if parsing
     *                       failed before that field could be read
     * @param failedElement  a description of the element that could not be deserialized
     *                       (e.g. {@code "objective type mcrpg:block_break"})
     */
    public QuestDeserializationException(@NotNull String message,
                                         @Nullable String questKey,
                                         @NotNull String failedElement) {
        super(message);
        this.questKey = questKey;
        this.failedElement = failedElement;
    }

    /**
     * @param message        human-readable description of the failure
     * @param cause          the underlying exception
     * @param questKey       the {@code quest_key} value from the JSON, or {@code null} if parsing
     *                       failed before that field could be read
     * @param failedElement  a description of the element that could not be deserialized
     */
    public QuestDeserializationException(@NotNull String message,
                                         @NotNull Throwable cause,
                                         @Nullable String questKey,
                                         @NotNull String failedElement) {
        super(message, cause);
        this.questKey = questKey;
        this.failedElement = failedElement;
    }

    /**
     * Returns the {@code quest_key} from the JSON being deserialized, if it was
     * successfully parsed before the error occurred.
     *
     * @return the quest key string, or {@code null} if unavailable
     */
    @Nullable
    public String getQuestKey() {
        return questKey;
    }

    /**
     * Returns a human-readable description of the element that caused the
     * deserialization failure (e.g. {@code "objective type mcrpg:block_break"}).
     *
     * @return the failed element description
     */
    @NotNull
    public String getFailedElement() {
        return failedElement;
    }
}
