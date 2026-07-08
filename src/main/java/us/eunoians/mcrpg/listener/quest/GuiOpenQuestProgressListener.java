package us.eunoians.mcrpg.listener.quest;

import com.diamonddagger590.mccore.event.gui.CoreGuiOpenEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.quest.objective.type.builtin.GuiOpenQuestContext;

/**
 * Listens for {@link CoreGuiOpenEvent} and drives quest objective progress for GUI open objectives.
 */
public class GuiOpenQuestProgressListener implements QuestProgressListener {

    private final QuestManager questManager;

    /**
     * Constructs a new {@link GuiOpenQuestProgressListener}.
     *
     * @param questManager the {@link QuestManager} used to drive quest progress
     */
    public GuiOpenQuestProgressListener(@NotNull QuestManager questManager) {
        this.questManager = questManager;
    }

    /**
     * Handles {@link CoreGuiOpenEvent} to progress any active GUI open quest objectives
     * for the player that opened a GUI.
     *
     * @param event the {@link CoreGuiOpenEvent} that fired
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGuiOpen(CoreGuiOpenEvent event) {
        progressQuests(questManager, event.getPlayerUUID(), new GuiOpenQuestContext(event));
    }
}
