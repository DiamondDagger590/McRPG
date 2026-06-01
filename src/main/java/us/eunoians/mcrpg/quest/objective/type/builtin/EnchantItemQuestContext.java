package us.eunoians.mcrpg.quest.objective.type.builtin;

import org.bukkit.event.enchantment.EnchantItemEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;

/**
 * Progress context wrapping an {@link EnchantItemEvent} for enchant item objectives.
 */
public class EnchantItemQuestContext extends QuestObjectiveProgressContext {

    private final EnchantItemEvent enchantItemEvent;

    public EnchantItemQuestContext(@NotNull EnchantItemEvent enchantItemEvent) {
        this.enchantItemEvent = enchantItemEvent;
    }

    /**
     * Gets the underlying enchant item event.
     *
     * @return the enchant item event
     */
    @NotNull
    public EnchantItemEvent getEnchantItemEvent() {
        return enchantItemEvent;
    }
}
