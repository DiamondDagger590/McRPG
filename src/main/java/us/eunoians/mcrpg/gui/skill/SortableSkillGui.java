package us.eunoians.mcrpg.gui.skill;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.gui.common.FillerItemGui;

/**
 * This gui is used to sort {@link us.eunoians.mcrpg.skill.Skill}s in a {@link com.diamonddagger590.mccore.gui.Gui} by
 * providing a {@link SkillSortType} that can be toggled through to do different kinds of sorting.
 */
public interface SortableSkillGui extends FillerItemGui {

    /**
     * Sets the sort node to the provided one.
     *
     * @param skillSortNode The new sort node to use for this gui.
     */
    void setSkillSortNode(@NotNull LinkedNode<SkillSortType> skillSortNode);

    /**
     * Progresses the {@link #getSkillSortNode()} ()} to the next node.
     */
    void progressToNextSortNode();

    /**
     * Get the sort node for this gui.
     *
     * @return The sort node for this gui.
     */
    @NotNull
    LinkedNode<SkillSortType> getSkillSortNode();
}
