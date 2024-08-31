package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is a combination of a {@link PaginatedGui} and a {@link SortableAbilityGui}, providing
 * common shared code across guis that display a set of {@link Ability Abilities}.
 * <p>
 * This is useful as most guis displaying abilities will require pagination and some sort of sorting,
 * since there are a lot of them.
 * <p>
 * This gui also assumes that there will be a 'navigation bar' for pagination, however specific navigation
 * bar implementation is left to be implemented by individual guis.
 */
public abstract class PaginatedSortedAbilityGui extends PaginatedGui implements SortableAbilityGui {

    private final Map<AbilitySortType, List<Ability>> cachedSorts;
    private LinkedNode<AbilitySortType> sortTypeNode;

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;

    public PaginatedSortedAbilityGui(@NotNull McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.cachedSorts = new HashMap<>();
        this.sortTypeNode = AbilitySortType.getFirstSortType();
    }

    /**
     * Get the {@link McRPGPlayer} who is viewing their abilities.
     *
     * @return The {@link McRPGPlayer} who is viewing their abilities.
     */
    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        return mcRPGPlayer;
    }

    /**
     * Gets the {@link Player} who is viewing their abilities.
     *
     * @return The {@link Player} who is viewing their abilities.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets a {@link List} of {@link Ability Abilities}, sorted based on the current {@link #getAbilitySortNode()} ()}.
     *
     * @param page The page to get the list for.
     * @return A {@link List} of {@link Ability Abilities} sorted based on the current {@link #getAbilitySortNode()} ()}
     * to be displayed on the provided page.
     */
    @NotNull
    protected List<Ability> getSortedAbilitiesForPage(int page) {
        AbilitySortType sortType = this.sortTypeNode.getNodeValue();
        List<Ability> abilities;
        if (cachedSorts.containsKey(sortType)) {
            abilities = cachedSorts.get(sortType);
        } else {
            abilities = getUnsortedAbilities()
                    .stream()
                    .map(namespacedKey -> McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(namespacedKey)).toList();
            abilities = sortType.filter(mcRPGPlayer, abilities);
            abilities = abilities
                    .stream()
                    .sorted(sortType.getAbilityComparator())
                    .toList();
            cachedSorts.put(sortType, abilities);
        }

        // Get the abilities that need to be displayed on this page
        int startRange = ((page - 1) * getNavigationRowStartIndex());
        int endRange = Math.min(abilities.size(), page * getNavigationRowStartIndex());
        return abilities.subList(startRange, endRange);
    }

    @Override
    public void setAbilitySortNode(@NotNull LinkedNode<AbilitySortType> abilitySortNode) {
        this.sortTypeNode = abilitySortNode;
    }

    /**
     * Progresses the {@link #getAbilitySortNode()} to the next node. Any values found in
     * {@link #getSkippedSortTypes()} will be skipped.
     */
    @Override
    public void progressToNextSortNode() {
        do {
            setAbilitySortNode(getAbilitySortNode().getNextNode());
        } while (getSkippedSortTypes().contains(getAbilitySortNode().getNodeValue()));
    }

    @Override
    public @NotNull LinkedNode<AbilitySortType> getAbilitySortNode() {
        return sortTypeNode;
    }

    /**
     * Gets the maximum page for this gui.
     * <p>
     * This calculates the maximum page by dividing {@link #getUnsortedAbilities()} by the {@link #getNavigationRowStartIndex()},
     * with a minimum value of 1.
     *
     * @return The maximum page for this gui.
     */
    @Override
    public int getMaximumPage() {
        return (int) Math.max(1, Math.ceil((double) getUnsortedAbilities().size() / getNavigationRowStartIndex()));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintNavigationBar(page);
        paintAbilities(page);
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
     * Gets a {@link Set} of {@link NamespacedKey}s that represents all {@link Ability Abilities} that can be possibly displayed
     * by this gui before sorting.
     *
     * @return A {@link Set} of {@link NamespacedKey}s that represents all {@link Ability Abilities} that can be possibly displayed
     * by this gui before sorting.
     */
    @NotNull
    public abstract Set<NamespacedKey> getUnsortedAbilities();

    /**
     * Paints the abilities for a given page.
     *
     * @param page The page to paint the abilities for.
     */
    protected abstract void paintAbilities(int page);

    /**
     * Paints the navigation bar for a given page.
     *
     * @param page The page to paint the navigation bar for.
     */
    protected abstract void paintNavigationBar(int page);

    /**
     * Gets a {@link Set} of {@link AbilitySortType}s that should not be used for sorting in this gui.
     * <p>
     * An empty set means no sort types should be skipped.
     *
     * @return A {@link Set} of {@link AbilitySortType}s that should not be used for sorting in this gui.
     */
    @NotNull
    protected Set<AbilitySortType> getSkippedSortTypes() {
        return new HashSet<>();
    }
}
