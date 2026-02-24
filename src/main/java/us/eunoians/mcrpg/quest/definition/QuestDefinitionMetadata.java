package us.eunoians.mcrpg.quest.definition;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Extensible metadata that can be attached to a {@link QuestDefinition}.
 * <p>
 * Each metadata implementation is identified by a unique {@link NamespacedKey} and stored in
 * the definition's metadata map. This avoids constructor parameter explosion as new metadata
 * types are added across phases (board metadata, template metadata, NPC metadata, etc.).
 * <p>
 * Implementations must support serialization for persistence and round-trip fidelity.
 */
public interface QuestDefinitionMetadata {

    /**
     * Gets the unique key identifying this metadata type.
     *
     * @return the metadata key
     */
    @NotNull
    NamespacedKey getMetadataKey();

    /**
     * Serializes this metadata to a map suitable for SQL persistence.
     *
     * @return a serializable map of key-value pairs
     */
    @NotNull
    Map<String, Object> serialize();
}
