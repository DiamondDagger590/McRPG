package us.eunoians.mcrpg.quest.board.generation;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.BoardMetadata;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.util.List;

/**
 * Encapsulates the logic of assembling the pool of eligible quest definitions for
 * board generation.
 * <p>
 * In Phase 1, draws exclusively from {@link QuestDefinitionRegistry} (hand-crafted quests
 * with {@code board-metadata}). Phase 2 will additionally draw from a template engine.
 */
public class QuestPool {

    private final QuestDefinitionRegistry definitionRegistry;

    public QuestPool(@NotNull QuestDefinitionRegistry definitionRegistry) {
        this.definitionRegistry = definitionRegistry;
    }

    /**
     * Gets definitions eligible for a specific rolled rarity.
     *
     * @param rolledRarity the rarity to filter by
     * @return the list of eligible definition keys
     */
    @NotNull
    public List<NamespacedKey> getEligibleDefinitions(@NotNull NamespacedKey rolledRarity) {
        return definitionRegistry.getAll().stream()
                .filter(def -> def.getBoardMetadata()
                        .filter(meta -> meta.boardEligible()
                                && meta.supportedRarities().contains(rolledRarity))
                        .isPresent())
                .map(QuestDefinition::getQuestKey)
                .toList();
    }

    /**
     * Gets all board-eligible definition keys regardless of rarity or cooldown state.
     * Used for backfill when not enough quests match a specific rarity.
     *
     * @return the list of eligible definition keys
     */
    @NotNull
    public List<NamespacedKey> getAllBoardEligibleDefinitions() {
        return definitionRegistry.getAll().stream()
                .filter(def -> def.getBoardMetadata()
                        .map(BoardMetadata::boardEligible)
                        .orElse(false))
                .map(QuestDefinition::getQuestKey)
                .toList();
    }
}
