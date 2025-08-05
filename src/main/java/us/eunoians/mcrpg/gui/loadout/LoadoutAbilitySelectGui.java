package us.eunoians.mcrpg.gui.loadout;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.type.ActiveAbility;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.AbilitySortType;
import us.eunoians.mcrpg.gui.ability.PaginatedSortedAbilityGui;
import us.eunoians.mcrpg.gui.loadout.slot.LoadoutSelectAbilitySlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.filter.core.McRPGChainPlayerContextFilter;
import us.eunoians.mcrpg.util.filter.key.AbilityKeyInLoadoutFilter;
import us.eunoians.mcrpg.util.filter.key.AbilityKeyUnlockedFilter;

import java.util.List;
import java.util.Set;

/**
 * This gui is used when a player is trying to select an {@link Ability} to go into
 * their {@link Loadout}.
 * <p>
 * Abilities in this gui are automatically filtered out if the ability can't be added to the selected slot. Since
 * a loadout can only contain one {@link ActiveAbility} per {@link us.eunoians.mcrpg.skill.Skill},
 * if a player is trying to replace an active ability, all other active abilities for that skill are filtered out and not shown as options.
 */
public class LoadoutAbilitySelectGui extends PaginatedSortedAbilityGui {

    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;
    private static final McRPGChainPlayerContextFilter<NamespacedKey> ABILITY_KEY_FILTER = new McRPGChainPlayerContextFilter<>(new AbilityKeyUnlockedFilter(), new AbilityKeyInLoadoutFilter());

    private final Loadout loadout;
    @Nullable
    private final NamespacedKey oldAbilityKey;

    public LoadoutAbilitySelectGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        super(mcRPGPlayer);
        this.loadout = loadout;
        this.oldAbilityKey = null;
    }

    public LoadoutAbilitySelectGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull NamespacedKey oldAbilityKey) {
        super(mcRPGPlayer);
        this.loadout = loadout;
        this.oldAbilityKey = oldAbilityKey;
    }

    @Override
    public int getNavigationRowStartIndex() {
        return NAVIGATION_ROW_START_INDEX;
    }

    @Override
    public @NotNull Set<NamespacedKey> getUnsortedAbilities() {
        return Set.copyOf(ABILITY_KEY_FILTER.filter(getCreatingPlayer(), getCreatingPlayer().asSkillHolder().getAvailableAbilities()));
    }

    @Override
    protected void paintAbilities(int page) {
        List<Ability> sortedAbilities = getSortedAbilitiesForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < sortedAbilities.size()) {
                if (oldAbilityKey != null) {
                    setSlot(i, new LoadoutSelectAbilitySlot(getCreatingPlayer(), loadout, sortedAbilities.get(i), oldAbilityKey));
                } else {
                    setSlot(i, new LoadoutSelectAbilitySlot(getCreatingPlayer(), loadout, sortedAbilities.get(i)));
                }
            } else {
                removeSlot(i);
            }
        }
    }

    @Override
    protected void paintNavigationBar(int page) {
        // Paint the nav bar with filler glass
        Slot<McRPGPlayer> fillerSlot = getFillerItemSlot();
        for (int i = 0; i < 9; i++) {
            setSlot(NAVIGATION_ROW_START_INDEX + i, fillerSlot);
        }
        // Set the sort slot
        setSlot(SORT_SLOT_INDEX, getAbilitySortNode().getNodeValue().getSlot());
        // If the page is not the first page, then we need to put a previous arrow button
        if (page > 1) {
            setSlot(PREVIOUS_PAGE_SLOT_INDEX, getPreviousPageSlot());
        }
        // If the page is not the max page, then we need to put a next arrow button
        if (page < getMaximumPage()) {
            setSlot(NEXT_PAGE_SLOT_INDEX, getNextPageSlot());
        }
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(getPlayer(), 54, RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.LOADOUT_ABILITY_SELECT_GUI_TITLE));
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    @Override
    protected @NotNull Set<AbilitySortType> getSkippedSortTypes() {
        return Set.of(AbilitySortType.UPGRADEABLE_ABILITIES, AbilitySortType.INNATE_ABILITIES);
    }
}
