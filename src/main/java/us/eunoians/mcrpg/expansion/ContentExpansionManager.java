package us.eunoians.mcrpg.expansion;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.event.content.ContentPackRegisteredEvent;
import us.eunoians.mcrpg.exception.expansion.ContentPackFailedProcessingException;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.expansion.content.McRPGContentPack;
import us.eunoians.mcrpg.expansion.handler.ContentPackProcessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is the main manager for all content expansion packs.
 * <p>
 * Any {@link ContentPackProcessor}s required to process specific types of {@link McRPGContentPack}s must
 * be registered here. Additionally, the handlers must be registered before the content gets registered, otherwise
 * the content won't actually be properly registered.
 */
public class ContentExpansionManager {

    private final McRPG mcRPG;
    private final Set<ContentPackProcessor> contentPackProcessors;
    private final Map<NamespacedKey, ContentExpansion> contentExpansions;

    public ContentExpansionManager(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        contentPackProcessors = new HashSet<>();
        contentExpansions = new HashMap<>();
    }

    /**
     * Registers the provided {@link ContentPackProcessor} to process registered {@link McRPGContentPack}s.
     *
     * @param contentPackProcessor The {@link ContentPackProcessor} to register.
     */
    public void registerContentHandler(@NotNull ContentPackProcessor contentPackProcessor) {
        contentPackProcessors.add(contentPackProcessor);
    }

    /**
     * Registers the provided {@link ContentExpansion} and attempts to process all the provided {@link McRPGContentPack}s
     * provided by the content expansion.
     * <p>
     * If there is not a {@link ContentPackProcessor} registered that can process the provided content pack, then the pack will not be properly registered.
     *
     * @param contentExpansion The {@link ContentExpansion} to be registered.
     */
    public void registerContentExpansion(@NotNull ContentExpansion contentExpansion) {
        contentExpansions.put(contentExpansion.getExpansionKey(), contentExpansion);
        contentExpansion.getExpansionContent().forEach(this::processContent);
    }

    /**
     * Checks to see if there is a registered {@link ContentExpansion} that belongs to the provided {@link NamespacedKey}.
     *
     * @param namespacedKey The {@link NamespacedKey} to check.
     * @return {@code true} if the provided {@link NamespacedKey} has a registered {@link ContentExpansion}.
     */
    public boolean hasContentExpansion(@NotNull NamespacedKey namespacedKey) {
        return contentExpansions.containsKey(namespacedKey);
    }

    /**
     * Gets an {@link Optional} containing the {@link ContentExpansion} belonging to the provided {@link NamespacedKey}.
     *
     * @param namespacedKey The {@link NamespacedKey} to get the registered {@link ContentExpansion} for.
     * @return An {@link Optional} containing the {@link ContentExpansion} belonging to the provided {@link NamespacedKey},
     * or empty if there is none.
     */
    @NotNull
    public Optional<ContentExpansion> getContentExpansion(@NotNull NamespacedKey namespacedKey) {
        return Optional.ofNullable(contentExpansions.get(namespacedKey));
    }

    /**
     * Attempts to process the provided {@link McRPGContentPack} using all the registered {@link ContentPackProcessor}s.
     *
     * If the pack is properly processed, a {@link ContentPackRegisteredEvent} will be fired. Otherwise, an {@link ContentPackFailedProcessingException}
     * will be thrown.
     * @param content The {@link McRPGContentPack} to process.
     * @throws ContentPackFailedProcessingException If the {@link McRPGContentPack} was unable to be processed.
     */
    private void processContent(@NotNull McRPGContentPack<? extends McRPGContent> content) {
        final AtomicBoolean processed = new AtomicBoolean(false);
        contentPackProcessors.forEach(mcRPGContentContentPackProcessor -> {
            if (mcRPGContentContentPackProcessor.processContentPack(mcRPG, content) && !processed.get()) {
                processed.set(true);
            }
        });
        if (processed.get()) {
            Bukkit.getPluginManager().callEvent(new ContentPackRegisteredEvent(content));
        } else {
            throw new ContentPackFailedProcessingException(content);

        }
    }
}
