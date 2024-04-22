package us.eunoians.mcrpg.quest;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.api.event.quest.QuestCompleteEvent;
import us.eunoians.mcrpg.api.event.quest.QuestStartEvent;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.exception.quest.QuestMissingObjectiveException;
import us.eunoians.mcrpg.quest.objective.QuestObjective;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Quest {

    private final UUID uuid;
    private final String configKey;
    private final Set<QuestObjective> questObjectives;
    private final Set<UUID> questHolders;
    private boolean started = false;
    private boolean completed = false;

    public Quest(@NotNull String configKey) {
        this.uuid = UUID.randomUUID();
        this.configKey = configKey;
        this.questObjectives = new HashSet<>();
        this.questHolders = new HashSet<>();
    }

    public Quest(@NotNull UUID uuid, @NotNull String configKey, @NotNull Set<QuestObjective> questObjectives, @NotNull Set<UUID> questHolders) {
        this.uuid = uuid;
        this.configKey = configKey;
        this.questObjectives = questObjectives;
        this.questHolders = questHolders;
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public String getConfigKey() {
        return configKey;
    }

    public void addQuestObjective(@NotNull QuestObjective... questObjective) {
        questObjectives.addAll(List.of(questObjective));
    }

    @NotNull
    public Set<QuestObjective> getQuestObjectives() {
        return ImmutableSet.copyOf(questObjectives);
    }

    public void addQuestHolder(@NotNull QuestHolder questHolder) {
        questHolders.add(questHolder.getUUID());
    }

    public void addQuestHolder(@NotNull UUID uuid) {
        questHolders.add(uuid);
    }

    public boolean doesQuestHaveHolder(@NotNull QuestHolder questHolder) {
        return doesQuestHaveHolder(questHolder.getUUID());
    }

    public boolean doesQuestHaveHolder(@NotNull UUID uuid) {
        return questHolders.contains(uuid);
    }

    @NotNull
    public Set<UUID> getQuestHolders() {
        return ImmutableSet.copyOf(questHolders);
    }

    public void removeQuestHolder(@NotNull QuestHolder questHolder) {
        removeQuestHolder(questHolder.getUUID());
    }

    public void removeQuestHolder(@NotNull UUID uuid) {
        questHolders.remove(uuid);
    }

    public double getQuestProgress() {
        double progress = 0;
        for (QuestObjective questObjective : questObjectives) {
            progress += questObjective.getObjectiveProgress();
        }
        return progress/questObjectives.size();
    }

    public boolean isCompleted() {
        if (!completed) {
            for (QuestObjective questObjective : questObjectives) {
                if (!questObjective.isObjectiveCompleted()) {
                    return false;
                }
            }
            completeQuest();
        }
        return completed;
    }

    public void onObjectiveComplete(@NotNull QuestObjective questObjective) {
        // Validate that this objective belongs to this quest
        if (!questObjectives.contains(questObjective)) {
            throw new QuestMissingObjectiveException(this, questObjective, String.format("Quest objective was marked as complete but doesn't belong to quest with UUID %s", uuid));
        }
        // If all objectives are finished, then the quest is complete
        for (QuestObjective objective : questObjectives) {
            if (!objective.isObjectiveCompleted()) {
                return;
            }
        }
        completeQuest();
    }

    public void startQuest() {
        QuestStartEvent questStartEvent = new QuestStartEvent(this);
        Bukkit.getPluginManager().callEvent(questStartEvent);
        startListeningForProgression();
        started = true;
    }

    public void completeQuest() {
        QuestCompleteEvent questCompleteEvent = new QuestCompleteEvent(this);
        Bukkit.getPluginManager().callEvent(questCompleteEvent);
        stopListeningForProgression();
        completed = true;
    }

    public void startListeningForProgression() {
        questObjectives.forEach(QuestObjective::startListeningForProgression);
    }

    public void stopListeningForProgression() {
        questObjectives.forEach(QuestObjective::stopListeningForProgression);
    }
}
