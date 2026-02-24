package us.eunoians.mcrpg.quest.board.template;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when the {@link QuestTemplateEngine} fails to generate a concrete
 * {@link us.eunoians.mcrpg.quest.definition.QuestDefinition} from a
 * {@link QuestTemplate}. Exposes the template key, rarity key, and the
 * specific element that caused the failure so that third-party expansion
 * developers can diagnose registration or configuration issues.
 */
public class QuestGenerationException extends RuntimeException {

    private final NamespacedKey templateKey;
    private final NamespacedKey rarityKey;
    private final NamespacedKey failedElementKey;

    /**
     * @param message          human-readable description of the failure
     * @param templateKey      the key of the template being generated
     * @param rarityKey        the rarity that was rolled for this generation attempt
     * @param failedElementKey the key of the element that caused the failure (e.g. an
     *                         unregistered objective type or reward type key), or
     *                         {@code null} if the failure is not tied to a specific element
     */
    public QuestGenerationException(@NotNull String message,
                                    @NotNull NamespacedKey templateKey,
                                    @NotNull NamespacedKey rarityKey,
                                    @Nullable NamespacedKey failedElementKey) {
        super(message);
        this.templateKey = templateKey;
        this.rarityKey = rarityKey;
        this.failedElementKey = failedElementKey;
    }

    /**
     * @param message          human-readable description of the failure
     * @param cause            the underlying exception
     * @param templateKey      the key of the template being generated
     * @param rarityKey        the rarity that was rolled for this generation attempt
     * @param failedElementKey the key of the element that caused the failure, or
     *                         {@code null} if the failure is not tied to a specific element
     */
    public QuestGenerationException(@NotNull String message,
                                    @NotNull Throwable cause,
                                    @NotNull NamespacedKey templateKey,
                                    @NotNull NamespacedKey rarityKey,
                                    @Nullable NamespacedKey failedElementKey) {
        super(message, cause);
        this.templateKey = templateKey;
        this.rarityKey = rarityKey;
        this.failedElementKey = failedElementKey;
    }

    /**
     * Returns the key of the template that was being generated when the failure occurred.
     *
     * @return the template key
     */
    @NotNull
    public NamespacedKey getTemplateKey() {
        return templateKey;
    }

    /**
     * Returns the rarity that was rolled for this generation attempt.
     *
     * @return the rarity key
     */
    @NotNull
    public NamespacedKey getRarityKey() {
        return rarityKey;
    }

    /**
     * Returns the key of the specific element that caused the failure, such as an
     * unregistered objective type or reward type. May be {@code null} when the failure
     * is not attributable to a single element (e.g. expression evaluation errors).
     *
     * @return the failed element key, or {@code null}
     */
    @Nullable
    public NamespacedKey getFailedElementKey() {
        return failedElementKey;
    }
}
