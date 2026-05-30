package us.eunoians.mcrpg.event.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;

/**
 * Fired when a chain advances from one step to the next after a quest within that
 * step is completed.
 * <p>
 * Both the step that was just completed and the step that will now become active are
 * provided. External plugins can listen to this event to respond to mid-chain progress.
 */
public class QuestChainStepAdvanceEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestChainDefinition chainDefinition;
    private final Player player;
    private final QuestChainStep completedStep;
    private final QuestChainStep nextStep;

    /**
     * Creates a new step-advance event.
     *
     * @param chainDefinition the definition of the chain being advanced
     * @param player          the player whose chain is advancing
     * @param completedStep   the step that was just completed
     * @param nextStep        the step that is now active
     */
    public QuestChainStepAdvanceEvent(@NotNull QuestChainDefinition chainDefinition,
                                      @NotNull Player player,
                                      @NotNull QuestChainStep completedStep,
                                      @NotNull QuestChainStep nextStep) {
        this.chainDefinition = chainDefinition;
        this.player = player;
        this.completedStep = completedStep;
        this.nextStep = nextStep;
    }

    /**
     * Gets the definition of the chain being advanced.
     *
     * @return the chain definition
     */
    @NotNull
    public QuestChainDefinition getChainDefinition() {
        return chainDefinition;
    }

    /**
     * Gets the player whose chain is advancing.
     *
     * @return the player
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the UUID of the player whose chain is advancing.
     * Convenience accessor for cases where the UUID is needed without the live {@link Player} reference.
     *
     * @return the player UUID
     */
    @NotNull
    public java.util.UUID getPlayerUUID() {
        return player.getUniqueId();
    }

    /**
     * Gets the step that was just completed.
     *
     * @return the completed step
     */
    @NotNull
    public QuestChainStep getCompletedStep() {
        return completedStep;
    }

    /**
     * Gets the step that is now active.
     *
     * @return the next step
     */
    @NotNull
    public QuestChainStep getNextStep() {
        return nextStep;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
