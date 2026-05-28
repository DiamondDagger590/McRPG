package us.eunoians.mcrpg.event.quest;

import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;

/**
 * Fired when a quest instance is cancelled (manually abandoned or due to expiration).
 * <p>
 * Use {@link #isExpiration()} to distinguish between a player-initiated cancellation
 * and a time-based expiration. Chain listeners use this flag to apply the correct
 * state transition (ABANDONED for cancel, step's {@code on-quest-expire} policy for
 * expiration).
 */
public class QuestCancelEvent extends QuestEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    @Nullable
    private final QuestDefinition questDefinition;
    private final boolean expiration;

    /**
     * Creates a new quest cancel event.
     *
     * @param questInstance   the quest instance that was cancelled
     * @param questDefinition the definition the quest was created from
     * @param expiration      {@code true} if the cancellation was caused by the quest's
     *                        expiry time being reached; {@code false} for manual abandon
     */
    public QuestCancelEvent(@NotNull QuestInstance questInstance,
                            @NotNull QuestDefinition questDefinition,
                            boolean expiration) {
        super(questInstance);
        this.questDefinition = questDefinition;
        this.expiration = expiration;
    }

    /**
     * Creates a non-expiration cancel event (manual abandon).
     *
     * @param questInstance   the quest instance that was cancelled
     * @param questDefinition the definition the quest was created from
     * @deprecated use {@link #QuestCancelEvent(QuestInstance, QuestDefinition, boolean)} to
     *             explicitly state whether the cancellation is due to expiration
     */
    @Deprecated
    public QuestCancelEvent(@NotNull QuestInstance questInstance,
                            @NotNull QuestDefinition questDefinition) {
        this(questInstance, questDefinition, false);
    }

    /**
     * Creates a cancel event without a quest definition reference. Provided for
     * backward compatibility with tests and external callers that do not have access
     * to the quest definition. {@link #getQuestDefinition()} will throw in this case —
     * prefer using {@link #QuestCancelEvent(QuestInstance, QuestDefinition, boolean)}.
     *
     * @param questInstance the quest instance that was cancelled
     * @deprecated use {@link #QuestCancelEvent(QuestInstance, QuestDefinition, boolean)}
     */
    @Deprecated
    public QuestCancelEvent(@NotNull QuestInstance questInstance) {
        super(questInstance);
        this.questDefinition = null;
        this.expiration = false;
    }

    /**
     * Gets the definition the cancelled quest was created from, or {@code null} if this
     * event was created via the deprecated single-argument constructor.
     *
     * @return the quest definition, or {@code null}
     */
    @Nullable
    public QuestDefinition getQuestDefinition() {
        return questDefinition;
    }

    /**
     * Returns whether this cancellation was triggered by the quest's expiry timer
     * (as opposed to a manual player abandon).
     *
     * @return {@code true} if expired, {@code false} if manually cancelled
     */
    public boolean isExpiration() {
        return expiration;
    }

    /**
     * Gets the key of the quest definition that was cancelled.
     *
     * @return the quest definition key
     */
    @NotNull
    public NamespacedKey getQuestDefinitionKey() {
        return questDefinition.getQuestKey();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
