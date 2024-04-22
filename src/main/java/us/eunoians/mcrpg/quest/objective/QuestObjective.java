package us.eunoians.mcrpg.quest.objective;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.api.event.quest.QuestObjectiveCompleteEvent;
import us.eunoians.mcrpg.api.event.quest.QuestObjectiveProgressEvent;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.exception.quest.QuestObjectiveCompleteException;
import us.eunoians.mcrpg.quest.Quest;

import java.util.List;

public abstract class QuestObjective implements Listener {

    private final Quest quest;
    private final int requiredProgression;

    protected int currentProgression;

    public QuestObjective(@NotNull Quest quest, int requiredProgression) {
        this.quest = quest;
        this.requiredProgression = requiredProgression;
        this.currentProgression = 0;
    }

    public QuestObjective(@NotNull Quest quest, int requiredProgression, int currentProgression) {
        this.quest = quest;
        this.requiredProgression = requiredProgression;
        this.currentProgression = currentProgression;
    }

    @NotNull
    public Quest getQuest() {
        return quest;
    }

    public int getRequiredProgression() {
        return requiredProgression;
    }

    public int getCurrentProgression() {
        return currentProgression;
    }

    public void progressObjective(int progress) {
        if (isObjectiveCompleted()) {
            throw new QuestObjectiveCompleteException(this, String.format("Quest objective for quest %s tried to have additional progress but is already completed.", quest.getUUID()));
        }
        QuestObjectiveProgressEvent questObjectiveProgressEvent = new QuestObjectiveProgressEvent(getQuest(), this, Math.min(progress, requiredProgression - currentProgression));
        Bukkit.getPluginManager().callEvent(questObjectiveProgressEvent);
        currentProgression += progress;
        if (currentProgression >= requiredProgression) {
            onComplete();
        }

    }

    public double getObjectiveProgress() {
        return (double) currentProgression / requiredProgression;
    }

    public boolean isObjectiveCompleted() {
        return currentProgression >= requiredProgression;
    }

    public abstract boolean canProcessEvent(@NotNull QuestHolder questHolder, @NotNull Event event);

    public abstract void processEvent(@NotNull QuestHolder questHolder, @NotNull Event event);

    protected void onComplete() {
        QuestObjectiveCompleteEvent questObjectiveCompleteEvent = new QuestObjectiveCompleteEvent(getQuest(), this);
        Bukkit.getPluginManager().callEvent(questObjectiveCompleteEvent);
        quest.onObjectiveComplete(this);
    }

    public abstract void startListeningForProgression();

    public abstract void stopListeningForProgression();

    public abstract Component getObjectiveTitle();

    public abstract List<Component> getObjectiveInfoText();
}
