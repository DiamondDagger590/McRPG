package us.eunoians.mcrpg.ability.ready;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * The {@link ReadyData} is not tied to a specific ability, instead represents a possibly shared
 * ready state. An example would be two skills that use an axe as the ready tool, WoodCutting and Axes.
 * <p>
 * Since both use an axe to ready, they both need to share the same type of {@link ReadyData}. It is then up
 * to specific implementation (breaking a log or attacking an entity) to determine what ability is going to 'consume'
 * the ready status.
 */
public abstract class ReadyData {

    /**
     * Gets the message to send whenever an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} readies.
     *
     * @param player The player to get the message for
     * @return The message to send whenever an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} readies.
     */
    @NotNull
    public abstract Component getReadyMessage(@NotNull McRPGPlayer player);

    /**
     * Gets the message to send whenever an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} unreadies.
     *
     * @param player The player to get the message for.
     * @return The message to send whenever an {@link us.eunoians.mcrpg.entity.holder.AbilityHolder} unreadies.
     */
    @NotNull
    public abstract Component getUnreadyMessage(@NotNull McRPGPlayer player);
}
