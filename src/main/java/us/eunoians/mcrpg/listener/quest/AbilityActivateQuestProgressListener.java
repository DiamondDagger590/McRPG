package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.AbilityActivateQuestContext;

/**
 * Listens for {@link AbilityActivateEvent} and drives quest objective progress for ability activate objectives.
 */
public class AbilityActivateQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Constructs a new {@link AbilityActivateQuestProgressListener}.
     *
     * @param questManager the {@link QuestManager} used to drive quest progress
     */
    public AbilityActivateQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles {@link AbilityActivateEvent} to progress any active ability activation quest objectives
     * for the ability holder that activated an ability.
     *
     * @param event the {@link AbilityActivateEvent} that fired
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbilityActivate(AbilityActivateEvent event) {
        progressQuests(questManager, event.getAbilityHolder().getUUID(), new AbilityActivateQuestContext(event));
    }
}
