package us.eunoians.mcrpg.exception.quest;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.impl.scope.QuestScope;

public class QuestScopeInvalidStateException extends RuntimeException {

    private final QuestScope questScope;

    public QuestScopeInvalidStateException(@NotNull QuestScope questScope) {
        this.questScope = questScope;
    }

    public QuestScopeInvalidStateException(@NotNull QuestScope questScope, @NotNull String message) {
        super(message);
        this.questScope = questScope;
    }

    @NotNull
    public QuestScope getQuestScope() {
        return questScope;
    }
}
