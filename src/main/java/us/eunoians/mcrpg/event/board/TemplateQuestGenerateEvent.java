package us.eunoians.mcrpg.event.board;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;

/**
 * Fired after the template engine generates a concrete {@link QuestDefinition} from
 * a {@link QuestTemplate} but before the definition is registered and used for an
 * offering. Third-party plugins can inspect the generated definition and the template
 * it originated from, or cancel the generation entirely (causing the slot to fall
 * back to another offering source or remain empty).
 * <p>
 * This event is {@link Cancellable} -- cancelling it discards the generated definition.
 * The definition itself is not replaceable because the serialization context
 * (resolved variables, objective configs) is coupled to the original generation.
 */
public class TemplateQuestGenerateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final QuestTemplate template;
    private final NamespacedKey rarityKey;
    private final QuestDefinition generatedDefinition;
    private boolean cancelled;

    /**
     * Creates a new template quest generate event.
     *
     * @param template            the template that was used for generation
     * @param rarityKey           the rarity that was rolled for this generation
     * @param generatedDefinition the generated quest definition
     */
    public TemplateQuestGenerateEvent(@NotNull QuestTemplate template,
                                      @NotNull NamespacedKey rarityKey,
                                      @NotNull QuestDefinition generatedDefinition) {
        this.template = template;
        this.rarityKey = rarityKey;
        this.generatedDefinition = generatedDefinition;
    }

    /**
     * Gets the template that was used to generate this quest definition.
     *
     * @return the quest template
     */
    @NotNull
    public QuestTemplate getTemplate() {
        return template;
    }

    /**
     * Gets the rarity that was rolled for this generation attempt.
     *
     * @return the rarity key
     */
    @NotNull
    public NamespacedKey getRarityKey() {
        return rarityKey;
    }

    /**
     * Gets the generated quest definition. This is read-only; the definition cannot
     * be replaced because serialization context is tied to the original generation.
     *
     * @return the generated quest definition
     */
    @NotNull
    public QuestDefinition getGeneratedDefinition() {
        return generatedDefinition;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
