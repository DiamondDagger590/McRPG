package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.event.gui.CoreGuiOpenEvent;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

import java.util.Optional;
import java.util.UUID;

/**
 * Progress context wrapping a {@link CoreGuiOpenEvent}. Carries the optional GUI key
 * and the UUID of the player who opened the GUI.
 */
public class GuiOpenQuestContext extends QuestObjectiveProgressContext {

    private final CoreGuiOpenEvent coreGuiOpenEvent;

    /**
     * Creates a context from the given GUI open event.
     *
     * @param coreGuiOpenEvent the event that triggered this context
     */
    public GuiOpenQuestContext(@NotNull CoreGuiOpenEvent coreGuiOpenEvent) {
        this.coreGuiOpenEvent = coreGuiOpenEvent;
    }

    /**
     * Gets the namespaced key of the GUI that was opened, if present.
     *
     * @return an optional containing the GUI key, or empty if the GUI has no key
     */
    @NotNull
    public Optional<NamespacedKey> getGuiKey() {
        return coreGuiOpenEvent.getGuiKey();
    }

    /**
     * Gets the UUID of the player who opened the GUI.
     *
     * @return the player's UUID
     */
    @NotNull
    public UUID getPlayerUUID() {
        return coreGuiOpenEvent.getPlayerUUID();
    }
}
