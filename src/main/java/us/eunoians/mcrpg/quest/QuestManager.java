package us.eunoians.mcrpg.quest;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class QuestManager {

    private Map<UUID, Quest> activeQuests;

    public QuestManager() {
        activeQuests = new HashMap<>();
    }

    @NotNull
    public Set<Quest> getActiveQuests() {
        return ImmutableSet.copyOf(activeQuests.values());
    }

    public boolean isQuestActive(@NotNull UUID uuid) {
        return activeQuests.containsKey(uuid);
    }

    @NotNull
    public Optional<Quest> getActiveQuest(@NotNull UUID uuid) {
        return Optional.ofNullable(activeQuests.get(uuid));
    }

    public void addActiveQuest(@NotNull UUID uuid, @NotNull Quest quest) {
    }
}
