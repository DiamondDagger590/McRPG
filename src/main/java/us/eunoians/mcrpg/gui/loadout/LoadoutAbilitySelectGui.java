package us.eunoians.mcrpg.gui.loadout;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.loadout.slot.LoadoutSelectAbilitySlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.filter.core.McRPGChainPlayerContextFilter;
import us.eunoians.mcrpg.util.filter.key.AbilityKeyComboActivatableFilter;
import us.eunoians.mcrpg.util.filter.key.AbilityKeyInLoadoutFilter;
import us.eunoians.mcrpg.util.filter.key.AbilityKeyUnlockedFilter;

import java.util.List;
import java.util.Set;

/**
 * This gui is used when a player is trying to select an {@link Ability} to go into
 * their {@link Loadout}.
 * <p>
 * Abilities in this gui are automatically filtered by the {@link SelectionMode}:
 * <ul>
 *   <li>{@link SelectionMode#ACTIVE} — shows only {@link us.eunoians.mcrpg.ability.combo.ComboActivatable}
 *       abilities, used when filling or replacing an active combo slot.</li>
 *   <li>{@link SelectionMode#PASSIVE} — shows only non-{@link us.eunoians.mcrpg.ability.combo.ComboActivatable}
 *       abilities, used when filling or replacing a passive slot.</li>
 * </ul>
 * Abilities already in the loadout (and not being replaced) are always excluded.
 */
public class LoadoutAbilitySelectGui extends PaginatedSortedAbilityGui {

    /**
     * Determines which kind of abilities this selection GUI presents.
     */
    public enum SelectionMode {
        /**
         * Show only {@link us.eunoians.mcrpg.ability.combo.ComboActivatable} abilities.
         * Used when the player is selecting an ability for a combo slot.
         */
        ACTIVE,
        /**
         * Show only non-{@link us.eunoians.mcrpg.ability.combo.ComboActivatable} abilities.
         * Used when the player is selecting an ability for a passive slot.
         */
        PASSIVE
    }

    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;

    private final Loadout loadout;
    @Nullable
    private final NamespacedKey oldAbilityKey;
    private final SelectionMode selectionMode;
    private final McRPGChainPlayerContextFilter<NamespacedKey> abilityKeyFilter;

    /**
     * Constructs a selection GUI for adding a new ability to an empty loadout slot.
     *
     * @param mcRPGPlayer   The player editing their loadout.
     * @param loadout       The loadout being edited.
     * @param selectionMode Whether to show active or passive abilities.
     */
    public LoadoutAbilitySelectGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull SelectionMode selectionMode) {
        super(mcRPGPlayer);
        this.loadout = loadout;
        this.oldAbilityKey = null;
        this.selectionMode = selectionMode;
        this.abilityKeyFilter = buildFilter(loadout, null, selectionMode);
    }

    /**
     * Constructs a selection GUI for replacing an existing ability in the loadout.
     *
     * @param mcRPGPlayer   The player editing their loadout.
     * @param loadout       The loadout being edited.
     * @param oldAbilityKey The key of the ability being replaced.
     * @param selectionMode Whether to show active or passive abilities.
     */
    public LoadoutAbilitySelectGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout, @NotNull NamespacedKey oldAbilityKey, @NotNull SelectionMode selectionMode) {
        super(mcRPGPlayer);
        this.loadout = loadout;
        this.oldAbilityKey = oldAbilityKey;
        this.selectionMode = selectionMode;
        this.abilityKeyFilter = buildFilter(loadout, oldAbilityKey, selectionMode);
    }

    @Override
    public int getNavigationRowStartIndex() {
        return NAVIGATION_ROW_START_INDEX;
    }

    @Override
    public @NotNull List<NamespacedKey> getUnsortedAbilities() {
        return List.copyOf(abilityKeyFilter.filter(getCreatingPlayer(), getCreatingPlayer().asSkillHolder().getAvailableAbilities()));
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
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                if (mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
                    LoadoutGui loadoutGui = new LoadoutGui(mcRPGPlayer, loadout);
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    player.openInventory(loadoutGui.getInventory());
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutGui);
                }
                return true;
            }

            @NotNull
            @Override
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.LOADOUT_ABILITY_SELECT_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
            }
        };
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
        return Set.of(
                AbilitySortType.UPGRADEABLE_ABILITIES,
                AbilitySortType.INNATE_ABILITIES,
                AbilitySortType.LOADOUT_ORDER,
                AbilitySortType.PASSIVE_ABILITIES,
                AbilitySortType.ACTIVE_ABILITIES
        );
    }

    /**
     * Builds the filter chain for the given loadout, optional replaced ability, and selection mode.
     * <p>
     * The chain always starts with {@link AbilityKeyUnlockedFilter} (only unlockable abilities are
     * selectable), then applies a {@link AbilityKeyComboActivatableFilter} to restrict to the
     * correct ability type, and finally applies {@link AbilityKeyInLoadoutFilter} to exclude
     * abilities already occupying other slots.
     *
     * @param loadout       The loadout being edited.
     * @param oldAbilityKey The ability being replaced, or {@code null} for a fresh add.
     * @param mode          The selection mode (active or passive).
     * @return The composed filter chain.
     */
    @NotNull
    private static McRPGChainPlayerContextFilter<NamespacedKey> buildFilter(@NotNull Loadout loadout,
                                                                             @Nullable NamespacedKey oldAbilityKey,
                                                                             @NotNull SelectionMode mode) {
        return new McRPGChainPlayerContextFilter<>(
                new AbilityKeyUnlockedFilter(),
                new AbilityKeyComboActivatableFilter(mode == SelectionMode.ACTIVE),
                new AbilityKeyInLoadoutFilter(loadout, oldAbilityKey)
        );
    }
}
