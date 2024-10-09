package us.eunoians.mcrpg.event.content;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;

/**
 * This event is thrown whenever a {@link McRPGContentPack} is processed and the content
 * is now available for use.
 */
public class ContentPackRegisteredEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final McRPGContentPack<? extends McRPGContent> contentPack;

    public ContentPackRegisteredEvent(@NotNull McRPGContentPack<? extends McRPGContent> contentPack) {
        this.contentPack = contentPack;
    }

    /**
     * Gets the {@link McRPGContentPack} that was registered.
     *
     * @return The {@link McRPGContentPack} that was registered.
     */
    @NotNull
    public McRPGContentPack<? extends McRPGContent> getContentPack() {
        return contentPack;
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
