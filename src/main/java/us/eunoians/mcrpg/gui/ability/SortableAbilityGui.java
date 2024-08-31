package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.jetbrains.annotations.NotNull;

/**
 * This gui is used to sort {@link us.eunoians.mcrpg.ability.impl.Ability Abilities} in a {@link com.diamonddagger590.mccore.gui.Gui} by
 * providing a {@link AbilitySortType} that can be toggled through to do different kinds of sorting.
 */
public interface SortableAbilityGui {

    /**
     * Sets the sort node to the provided one.
     *
     * @param abilitySortNode The new sort node to use for this gui.
     */
    void setAbilitySortNode(@NotNull LinkedNode<AbilitySortType> abilitySortNode);

    /**
     * Progresses the {@link #getAbilitySortNode()} to the next node.
     */
    void progressToNextSortNode();

    /**
     * Get the sort node for this gui.
     *
     * @return The sort node for this gui.
     */
    @NotNull
    LinkedNode<AbilitySortType> getAbilitySortNode();
}
