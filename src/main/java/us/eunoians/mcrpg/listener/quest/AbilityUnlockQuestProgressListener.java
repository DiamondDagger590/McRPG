package us.eunoians.mcrpg.listener.quest;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.ability.AbilityUnlockEvent;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.AbilityUnlockQuestContext;

/**
 * Listens for {@link AbilityUnlockEvent} and drives quest objective progress for ability unlock objectives.
 */
public class AbilityUnlockQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Constructs a new {@link AbilityUnlockQuestProgressListener}.
     *
     * @param questManager the {@link QuestManager} used to drive quest progress
     */
    public AbilityUnlockQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles {@link AbilityUnlockEvent} to progress any active ability unlock quest objectives
     * for the ability holder that unlocked an ability.
     *
     * @param event the {@link AbilityUnlockEvent} that fired
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAbilityUnlock(AbilityUnlockEvent event) {
        progressQuests(questManager, event.getAbilityHolder().getUUID(), new AbilityUnlockQuestContext(event));
    }
}
