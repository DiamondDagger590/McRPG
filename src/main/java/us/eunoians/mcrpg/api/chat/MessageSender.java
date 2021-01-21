package us.eunoians.mcrpg.api.chat;

import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.Methods;

/**
 * This class is used to send messages to {@link Player}s depending on various settings and sending it
 * through mediums such as chat or action bars.
 *
 * @author DiamodnDagger590
 */
public class MessageSender {

    /**
     * Sends a message to a player either through a normal chat message or things such as action bars.
     * <p>
     * This method also handles chat coloring and dealing with placeholders offered from things such as {@link me.clip.placeholderapi.PlaceholderAPI}.
     *
     * @param player    The {@link Player} being sent the message
     * @param message   The {@link String} message to be sent. This method handles coloring and placeholders so it doesn't need to be handled beforehand
     * @param forceChat If {@code true}, then the message will be sent through chat irregadless of the player's personal settings.
     */
    public void sendMessage(Player player, String message, boolean forceChat) {
        //TODO allow for future methods of sending messages
        if (true || forceChat) {
            player.sendMessage(Methods.color(player, McRPG.getInstance().getPluginPrefix() + message));
        }
    }
}
