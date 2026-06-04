package us.eunoians.mcrpg.event.quest;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.source.QuestSource;

import java.util.UUID;

/**
 * Fired when a quest instance is started (transitions to {@code IN_PROGRESS} and
 * the first phase's stages are activated).
 */
public class QuestStartEvent extends QuestEvent {

    private final QuestDefinition questDefinition;
    private final QuestSource questSource;
    private final UUID starterUUID;

    /**
     * Creates a new quest start event.
     *
     * @param questInstance   the quest instance that was started
     * @param questDefinition the definition the quest was created from
     * @param questSource     the source that originated this quest start
     * @param starterUUID     the UUID of the player who initiated the quest start, or {@code null} if system-initiated
     */
    public QuestStartEvent(@NotNull QuestInstance questInstance,
                           @NotNull QuestDefinition questDefinition,
                           @NotNull QuestSource questSource,
                           @Nullable UUID starterUUID) {
        super(questInstance);
        this.questDefinition = questDefinition;
        this.questSource = questSource;
        this.starterUUID = starterUUID;
    }

    /**
     * Creates a new quest start event without a starter UUID (system-initiated).
     *
     * @param questInstance   the quest instance that was started
     * @param questDefinition the definition the quest was created from
     * @param questSource     the source that originated this quest start
     */
    public QuestStartEvent(@NotNull QuestInstance questInstance,
                           @NotNull QuestDefinition questDefinition,
                           @NotNull QuestSource questSource) {
        this(questInstance, questDefinition, questSource, null);
    }

    /**
     * Gets the definition the quest was created from.
     *
     * @return the quest definition
     */
    @NotNull
    public QuestDefinition getQuestDefinition() {
        return questDefinition;
    }

    /**
     * Gets the source that originated this quest start.
     *
     * @return the quest source
     */
    @NotNull
    public QuestSource getQuestSource() {
        return questSource;
    }

    /**
     * Gets the UUID of the player who initiated the quest start. May be {@code null}
     * for system-initiated or test-driven starts where no specific player is responsible.
     *
     * @return the starter's UUID, or {@code null} if system-initiated
     */
    @Nullable
    public UUID getStarterUUID() {
        return starterUUID;
    }
}
