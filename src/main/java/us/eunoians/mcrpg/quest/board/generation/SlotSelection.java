package us.eunoians.mcrpg.quest.board.generation;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.board.template.GeneratedQuestResult;

/**
 * Represents the outcome of the unified quest source selection for a single board slot.
 * Either a hand-crafted definition was chosen or a template-generated definition was produced.
 */
public sealed interface SlotSelection permits SlotSelection.HandCrafted, SlotSelection.TemplateGenerated {

    /**
     * A hand-crafted quest definition was selected from the {@link us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry}.
     *
     * @param definitionKey the key of the selected hand-crafted definition
     * @param rarityKey     the rarity that was rolled for this slot
     */
    record HandCrafted(@NotNull NamespacedKey definitionKey,
                       @NotNull NamespacedKey rarityKey) implements SlotSelection {}

    /**
     * A quest was generated from a template via the {@link us.eunoians.mcrpg.quest.board.template.QuestTemplateEngine}.
     *
     * @param result    the generated quest result containing the definition, template key, and serialized JSON
     * @param rarityKey the rarity that was rolled for this slot
     */
    record TemplateGenerated(@NotNull GeneratedQuestResult result,
                             @NotNull NamespacedKey rarityKey) implements SlotSelection {}
}
