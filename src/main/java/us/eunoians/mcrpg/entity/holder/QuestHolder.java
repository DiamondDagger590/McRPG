package us.eunoians.mcrpg.entity.holder;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.Quest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class QuestHolder {

    private final UUID uuid;
    private final Set<UUID> activeQuests;

    public QuestHolder(UUID uuid) {
        this.uuid = uuid;
        this.activeQuests = new HashSet<>();
    }

    public QuestHolder(UUID uuid, Set<UUID> activeQuests) {
        this.uuid = uuid;
        this.activeQuests = activeQuests;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isQuestActive(@NotNull Quest quest) {
        return isQuestActive(quest.getUUID());
    }
    public boolean isQuestActive(@NotNull UUID uuid) {
        return activeQuests.contains(uuid);
    }
}
