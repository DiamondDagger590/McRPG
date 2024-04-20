package us.eunoians.mcrpg.quest;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.QuestObjective;

import java.util.Set;
import java.util.UUID;

public class Quest {

    private UUID uuid;
    private String configKey;
    private Set<QuestObjective> questObjectives;

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    @NotNull
    public String getConfigKey() {
        return configKey;
    }

    public void addQuestObjective(@NotNull QuestObjective questObjective) {
        questObjectives.add(questObjective);
    }

    @NotNull
    public Set<QuestObjective> getQuestObjectives() {
        return ImmutableSet.copyOf(questObjectives);
    }

    public double getQuestProgress() {
        double progress = questObjectives.stream().map(QuestObjective::getObjectiveProgress).reduce(Double::sum).get();
        return progress/questObjectives.size();
    }

    public boolean isCompleted() {
        for (QuestObjective questObjective : questObjectives) {
            if (!questObjective.isObjectiveCompleted()) {
                return false;
            }
        }
        return true;
    }

    public void onObjectiveComplete(@NotNull QuestObjective questObjective) {
        if (!questObjectives.contains(questObjective)) {

        }
    }
}
