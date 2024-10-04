package us.eunoians.mcrpg.exception.expansion;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;
import us.eunoians.mcrpg.expansion.handler.ContentPackProcessor;

/**
 * This exception is thrown whenever a {@link McRPGContentPack} is fails its registration likely due to
 * a corresponding {@link ContentPackProcessor} not being registered.
 */
public class ContentPackFailedProcessingException extends RuntimeException {

    private final McRPGContentPack<? extends McRPGContent> contentPack;

    public ContentPackFailedProcessingException(@NotNull McRPGContentPack<? extends McRPGContent> contentPack) {
        this.contentPack = contentPack;
    }

    /**
     * Gets the {@link McRPGContentPack} that failed its registration.
     *
     * @return The {@link McRPGContentPack} that failed its registration.
     */
    @NotNull
    public McRPGContentPack<? extends McRPGContent> getContentPack() {
        return contentPack;
    }

    @Override
    public String getMessage() {
        return String.format("A Content Pack from the %s expansion was unable to be registered. A likely cause is a developer forgot to register a ContentHandler for this content pack.", contentPack.getContentExpansion().getExpansionKey());
    }
}
