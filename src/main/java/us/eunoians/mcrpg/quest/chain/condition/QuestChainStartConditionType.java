package us.eunoians.mcrpg.quest.chain.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.quest.chain.QuestChainStartCondition;

/**
 * Factory interface for creating {@link QuestChainStartCondition} instances from YAML configuration.
 * Each implementation is keyed by a unique {@link NamespacedKey} and registered in the
 * {@link QuestChainStartConditionTypeRegistry}.
 * <p>
 * Third-party plugins implement this interface to define custom condition types that gate
 * chain starts based on arbitrary criteria (time, permission, economy balance, etc.).
 * Implementations are registered via
 * {@link us.eunoians.mcrpg.expansion.content.QuestChainStartConditionContentPack}.
 * <p>
 * Extends {@link McRPGContent} so that condition types can be distributed via the
 * {@link us.eunoians.mcrpg.expansion.ContentExpansion} system.
 *
 * @see QuestChainStartCondition
 * @see QuestChainStartConditionTypeRegistry
 */
public interface QuestChainStartConditionType extends McRPGContent {

    /**
     * Returns the unique key identifying this condition type in YAML configuration files.
     *
     * @return the condition type key (e.g., {@code mcrpg:time_gate})
     */
    @NotNull
    NamespacedKey getKey();

    /**
     * Parses a {@link QuestChainStartCondition} from the given YAML configuration section.
     * <p>
     * Implementations should validate all required fields and throw
     * {@link IllegalArgumentException} if the section is malformed.
     *
     * @param config the YAML section containing the condition configuration
     * @return the parsed condition instance
     * @throws IllegalArgumentException if the configuration section is invalid or missing
     *                                  required fields
     */
    @NotNull
    QuestChainStartCondition parse(@NotNull Section config);
}
