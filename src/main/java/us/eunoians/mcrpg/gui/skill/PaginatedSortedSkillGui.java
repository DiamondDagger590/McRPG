package us.eunoians.mcrpg.gui.skill;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is a combination of a {@link PaginatedGui} and a {@link SortableSkillGui}, providing
 * common shared code across guis that display a set of {@link Skill}s.
 * <p>
 * This is useful as most guis displaying skills will require pagination and some sort of sorting,
 * since there are a lot of them.
 * <p>
 * This gui also assumes that there will be a 'navigation bar' for pagination, however specific navigation
 * bar implementation is left to be implemented by individual guis.
 */
public abstract class PaginatedSortedSkillGui extends McRPGPaginatedGui implements SortableSkillGui {

    private final Map<SkillSortType, List<Skill>> cachedSorts;
    private LinkedNode<SkillSortType> sortTypeNode;

    private final Player player;

    public PaginatedSortedSkillGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.cachedSorts = new HashMap<>();
        this.sortTypeNode = SkillSortType.getFirstSortType();
    }

    public PaginatedSortedSkillGui(@NotNull McRPGPlayer mcRPGPlayer, int page) {
        super(mcRPGPlayer, page);
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.cachedSorts = new HashMap<>();
        this.sortTypeNode = SkillSortType.getFirstSortType();
    }

    /**
     * Gets the {@link Player} who is viewing their skills.
     *
     * @return The {@link Player} who is viewing their skills.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets a {@link List} of {@link Skill}s, sorted based on the current {@link #getSkillSortNode()}.
     *
     * @param page The page to get the list for.
     * @return A {@link List} of {@link Skill}s sorted based on the current {@link #getSkillSortNode()}
     * to be displayed on the provided page.
     */
    @NotNull
    protected List<Skill> getSortedSkillsForPage(int page) {
        SkillSortType sortType = this.sortTypeNode.getNodeValue();
        List<Skill> skills;
        if (cachedSorts.containsKey(sortType)) {
            skills = cachedSorts.get(sortType);
        } else {
            skills = getUnsortedSkills()
                    .stream()
                    .map(namespacedKey -> McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.SKILL).getRegisteredSkill(namespacedKey)).toList();
            skills = sortType.filter(getCreatingPlayer(), skills);
            skills = skills
                    .stream()
                    .sorted(sortType.getSkillComparator(getCreatingPlayer()))
                    .toList();
            cachedSorts.put(sortType, skills);
        }

        // Get the abilities that need to be displayed on this page
        int startRange = ((page - 1) * getNavigationRowStartIndex());
        int endRange = Math.min(skills.size(), page * getNavigationRowStartIndex());
        return skills.subList(startRange, endRange);
    }

    @Override
    public void setSkillSortNode(@NotNull LinkedNode<SkillSortType> skillSortNode) {
        this.sortTypeNode = skillSortNode;
    }

    /**
     * Progresses the {@link #getSkillSortNode()} to the next node. Any values found in
     * {@link #getSkippedSortTypes()} will be skipped.
     */
    @Override
    public void progressToNextSortNode() {
        do {
            setSkillSortNode(getSkillSortNode().getNextNode());
        } while (getSkippedSortTypes().contains(getSkillSortNode().getNodeValue()));
    }

    @Override
    public @NotNull LinkedNode<SkillSortType> getSkillSortNode() {
        return sortTypeNode;
    }

    /**
     * Gets the maximum page for this gui.
     * <p>
     * This calculates the maximum page by dividing {@link #getUnsortedSkills()} by the {@link #getNavigationRowStartIndex()},
     * with a minimum value of 1.
     *
     * @return The maximum page for this gui.
     */
    @Override
    public int getMaximumPage() {
        return (int) Math.max(1, Math.ceil((double) getUnsortedSkills().size() / getNavigationRowStartIndex()));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintNavigationBar(page);
        paintSkills(page);
    }

    /**
     * Gets the inventory index for the navigation row to start.
     * <p>
     * If an inventory is size {@code 54}, then to have a one row navigation bar, this
     * should return {@code 45} as an example.
     * <p>
     * This value is also used to determine the amount of pages to be displayed.
     *
     * @return The inventory index for the navigation row to start.
     */
    public abstract int getNavigationRowStartIndex();

    /**
     * Gets a {@link Set} of {@link NamespacedKey}s that represents all {@link Skill}s that can be possibly displayed
     * by this gui before sorting.
     *
     * @return A {@link Set} of {@link NamespacedKey}s that represents all {@link Skill}s that can be possibly displayed
     * by this gui before sorting.
     */
    @NotNull
    public abstract Set<NamespacedKey> getUnsortedSkills();

    /**
     * Paints the skills for a given page.
     *
     * @param page The page to paint the skills for.
     */
    protected abstract void paintSkills(int page);

    /**
     * Paints the navigation bar for a given page.
     *
     * @param page The page to paint the navigation bar for.
     */
    protected abstract void paintNavigationBar(int page);

    /**
     * Gets a {@link Set} of {@link SkillSortType}s that should not be used for sorting in this gui.
     * <p>
     * An empty set means no sort types should be skipped.
     *
     * @return A {@link Set} of {@link SkillSortType}s that should not be used for sorting in this gui.
     */
    @NotNull
    protected Set<SkillSortType> getSkippedSortTypes() {
        return new HashSet<>();
    }
}
