package us.eunoians.mcrpg.exception.quest;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuestNotActiveException extends RuntimeException {

    private final UUID questUUID;

    public QuestNotActiveException(@NotNull UUID questUUID) {
        this.questUUID = questUUID;
    }

    public QuestNotActiveException(@NotNull UUID questUUID, @NotNull String message) {
        super(message);
        this.questUUID = questUUID;
    }

    @NotNull
    public UUID getQuest() {
        return questUUID;
    }

}
