package us.eunoians.mcrpg.quest.objective;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.event.quest.QuestObjectiveCompleteEvent;
import us.eunoians.mcrpg.event.event.quest.QuestObjectiveProgressEvent;
import us.eunoians.mcrpg.entity.holder.QuestHolder;
import us.eunoians.mcrpg.exception.quest.QuestObjectiveCompleteException;
import us.eunoians.mcrpg.quest.Quest;

import java.util.List;

/**
 * A {@link Quest} will have multiple objectives, which are individual goals of which all have to be completed
 * in order for the quest itself to be complete.
 * <p>
 * This class provides the basic behavior for quest objectives. Implementation behavior can be seen in examples
 * like {@link BlockBreakQuestObjective} which is an objective that gets progressed by breaking blocks.
 * <p>
 * Whenever a quest is started or resumed, all objectives will have their listeners registered. When either the objective
 * is completed, or the entire quest is completed/abandoned, an objective will have its listeners unregistered.
 */
public abstract class QuestObjective implements Listener {

    private final Quest quest;
    private final int requiredProgression;
    private int currentProgression;

    public QuestObjective(@NotNull Quest quest, int requiredProgression) {
        this.quest = quest;
        this.requiredProgression = requiredProgression;
        this.currentProgression = 0;
    }

    public QuestObjective(@NotNull Quest quest, int requiredProgression, int currentProgression) {
        this.quest = quest;
        this.requiredProgression = requiredProgression;
        this.currentProgression = Math.min(requiredProgression, currentProgression);
    }

    /**
     * Gets the {@link Quest} that this objective belongs to.
     *
     * @return The {@link Quest} that this objective belongs to.
     */
    @NotNull
    public Quest getQuest() {
        return quest;
    }

    /**
     * Gets the progression required for this objective to be completed.
     *
     * @return The progression required for this objective to be completed.
     */
    public int getRequiredProgression() {
        return requiredProgression;
    }

    /**
     * Gets the current progression towards this objective being completed
     *
     * @return The current progression towards this objective being completed
     */
    public int getCurrentProgression() {
        return currentProgression;
    }

    /**
     * Progresses the objective by the provided amount and calls a {@link QuestObjectiveProgressEvent}.
     * <p>
     * If the provided progress amount exceeds the remaining amount needed for the objective to be complete,
     * then only that remaining amount is added instead.
     *
     * @param progress The amount to progress the objective by
     * @throws QuestObjectiveCompleteException if {@link #isObjectiveCompleted()} returns {@code true}.
     */
    public void progressObjective(int progress) throws QuestObjectiveCompleteException {
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

    /**
     * Gets the progress of this objective represented as a double between {@code 0.0} and {@code 1.0}.
     *
     * @return A double between {@code 0.0} and {@code 1.0} representing the progress of this objective.
     */
    public double getObjectiveProgress() {
        return (double) currentProgression / requiredProgression;
    }

    /**
     * Checks to see if this objective is completed.
     *
     * @return {@code true} if this objective is completed.
     */
    public boolean isObjectiveCompleted() {
        return currentProgression >= requiredProgression;
    }

    /**
     * Checks to see if this objective can process the provided {@link Event}. If it can,
     * {@link #processEvent(QuestHolder, Event)} will be called after.
     *
     * @param questHolder The {@link QuestHolder} involved in whatever the {@link Event} is
     * @param event       The {@link Event} to check
     * @return {@code true} if the provided {@link Event} can be processed.
     */
    public abstract boolean canProcessEvent(@NotNull QuestHolder questHolder, @NotNull Event event);

    /**
     * Processes the provided {@link Event} to award progression to this objective.
     *
     * @param questHolder The {@link QuestHolder} involved in whatever the {@link Event} is
     * @param event       The {@link Event} to process.
     */
    public abstract void processEvent(@NotNull QuestHolder questHolder, @NotNull Event event);

    /**
     * Called whenever this objective is completed, calling a {@link QuestObjectiveCompleteEvent} and calls {@link Quest#onObjectiveComplete(QuestObjective)}
     * to notify the {@link Quest} that one of its objectives are done.
     */
    protected void onComplete() {
        QuestObjectiveCompleteEvent questObjectiveCompleteEvent = new QuestObjectiveCompleteEvent(getQuest(), this);
        Bukkit.getPluginManager().callEvent(questObjectiveCompleteEvent);
        quest.onObjectiveComplete(this);
    }

    /**
     * Registers any {@link Listener}s needed for this objective.
     */
    public abstract void startListeningForProgression();

    /**
     * Unregisters any {@link Listener}s used by this objective.
     */
    public abstract void stopListeningForProgression();

    /**
     * Gets a display title for this objective.
     *
     * @return A {@link Component} used as a display title for this objective.
     */
    public abstract Component getObjectiveTitle();

    /**
     * Gets a {@link List} of {@link Component}s that provides more information about this specific objective.
     *
     * @return A {@link List} of {@link Component}s that provides more information about this specific objective.
     */
    public abstract List<Component> getObjectiveInfoText();
}
