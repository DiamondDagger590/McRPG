package us.eunoians.mcrpg.gui.loadout;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.combo.ComboPattern;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.AbilitySortType;
import us.eunoians.mcrpg.gui.ability.PaginatedSortedAbilityGui;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.loadout.slot.ActiveAbilityComboSlot;
import us.eunoians.mcrpg.gui.loadout.slot.ComboZoneFillerSlot;
import us.eunoians.mcrpg.gui.loadout.slot.LoadoutAbilitySlot;
import us.eunoians.mcrpg.gui.loadout.slot.display.LoadoutDisplayHomeSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The primary edit-loadout GUI for a single {@link Loadout}.
 * <p>
 * Layout (36 slots / 4 rows):
 * <pre>
 *  Row 1 (slots  0- 8): Combo row — light-blue filler (with legend lore) + active slots at 2, 4, 6
 *  Row 2 (slots  9-17): Passive ability grid (paginated, 18 slots across rows 2-3)
 *  Row 3 (slots 18-26): Passive ability grid (cont.)
 *  Row 4 (slots 27-35): Navigation bar
 * </pre>
 * <p>
 * The combo row's light-blue filler panes double as the combo info legend — hovering any
 * filler slot explains the combo system. Slots beyond the player's passive capacity are left
 * empty (air).
 *
 * <h2>Interaction model</h2>
 * <ul>
 *   <li>Clicking a combo slot opens {@link LoadoutAbilitySelectGui} in
 *       {@link LoadoutAbilitySelectGui.SelectionMode#ACTIVE} mode.</li>
 *   <li>Pressing number keys 1–3 while hovering a combo slot reorders that ability to the
 *       target combo position, swapping with the existing occupant when needed.</li>
 *   <li>Clicking a passive slot opens {@link LoadoutAbilitySelectGui} in
 *       {@link LoadoutAbilitySelectGui.SelectionMode#PASSIVE} mode.</li>
 * </ul>
 */
public class LoadoutGui extends PaginatedSortedAbilityGui {

    /** Number of passive ability slots per page (rows 2-3). */
    private static final int PASSIVE_GRID_SIZE = 18;
    /** Inventory index where the passive grid starts (row 2, slot 9). */
    private static final int PASSIVE_GRID_START = 9;

    /** Inventory index where the navigation bar starts (row 4). */
    private static final int NAV_BAR_START = 27;
    private static final int BACK_SLOT_INDEX = NAV_BAR_START;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAV_BAR_START + 2;
    private static final int SORT_SLOT_INDEX = NAV_BAR_START + 4;
    private static final int NEXT_PAGE_SLOT_INDEX = NAV_BAR_START + 6;
    private static final int LOADOUT_DISPLAY_EDIT_SLOT = NAV_BAR_START + 8;

    /** Inventory slots for the three active combo ability slots (row 1: slots 2, 4, 6). */
    private static final int COMBO_ACTIVE_SLOT_1 = 2;
    private static final int COMBO_ACTIVE_SLOT_2 = 4;
    private static final int COMBO_ACTIVE_SLOT_3 = 6;

    private final Loadout loadout;

    public LoadoutGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Loadout loadout) {
        super(mcRPGPlayer);
        this.loadout = loadout;
        setAbilitySortNode(AbilitySortType.getLoadoutOrderNode());
    }

    @NotNull
    public Loadout getLoadout() {
        return loadout;
    }

    @Override
    public int getNavigationRowStartIndex() {
        return PASSIVE_GRID_SIZE;
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        String loadoutName = loadout.getDisplay().getDisplayName().orElse(Integer.toString(loadout.getLoadoutSlot()));
        return Bukkit.createInventory(getPlayer(), 36, RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.LOADOUT_GUI_TITLE, Map.of("loadout-name", loadoutName)));
    }

    @Override
    public @NotNull List<NamespacedKey> getUnsortedAbilities() {
        var abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);
        return loadout.getOrderedAbilities().stream()
                .filter(key -> !abilityRegistry.registered(key) || !(abilityRegistry.getRegisteredAbility(key) instanceof us.eunoians.mcrpg.ability.combo.ComboActivatable))
                .toList();
    }

    @Override
    protected void paintAbilities(int page) {
        List<Ability> sortedPassives = getSortedAbilitiesForPage(page);
        int passivesInLoadout = getUnsortedAbilities().size();
        int activeCount = loadout.getOrderedActiveAbilities().size();
        int maxPassiveCapacity = loadout.getAbilities().size() + loadout.getRemainingLoadoutSize() - activeCount;
        int passiveDifference = passivesInLoadout - sortedPassives.size();
        int totalPassiveSlotsToShow = Math.max(sortedPassives.size(), maxPassiveCapacity - passiveDifference);

        for (int i = 0; i < PASSIVE_GRID_SIZE; i++) {
            int inventorySlot = PASSIVE_GRID_START + i;
            if (i < sortedPassives.size()) {
                setSlot(inventorySlot, new LoadoutAbilitySlot(loadout, sortedPassives.get(i)));
            } else if (i < totalPassiveSlotsToShow) {
                setSlot(inventorySlot, new LoadoutAbilitySlot(loadout));
            } else {
                removeSlot(inventorySlot);
            }
        }
    }

    @Override
    protected void paintNavigationBar(int page) {
        paintComboRow();
        paintNavBar(page);
    }

    /**
     * Paints row 1 (slots 0-8) as the combo row.
     * <p>
     * Light-blue filler panes (which carry the combo legend as hover lore) fill all 9 slots,
     * then the three active ability slots are placed at positions 2, 4, 6 — symmetrically
     * centred with 2 fillers on each edge and 1 between each active.
     */
    private void paintComboRow() {
        var comboFiller = new ComboZoneFillerSlot();
        for (int i = 0; i < 9; i++) {
            setSlot(i, comboFiller);
        }
        setComboActiveSlots();
    }

    /**
     * Places the three {@link ActiveAbilityComboSlot}s at their fixed inventory positions.
     */
    private void setComboActiveSlots() {
        List<NamespacedKey> activeAbilities = loadout.getOrderedActiveAbilities();
        var abilityRegistry = McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY);

        for (ComboPattern pattern : ComboPattern.allPatterns()) {
            int inventorySlot = comboPatternToInventorySlot(pattern);
            int patternIndex = pattern.getSlotIndex() - 1;
            if (patternIndex < activeAbilities.size()) {
                Ability ability = abilityRegistry.getRegisteredAbility(activeAbilities.get(patternIndex));
                setSlot(inventorySlot, new ActiveAbilityComboSlot(loadout, pattern, ability));
            } else {
                setSlot(inventorySlot, new ActiveAbilityComboSlot(loadout, pattern));
            }
        }
    }

    /**
     * Paints the navigation bar (row 4, slots 27-35).
     *
     * @param page The current page number.
     */
    private void paintNavBar(int page) {
        Slot<McRPGPlayer> fillerSlot = getFillerItemSlot();
        for (int i = 0; i < 9; i++) {
            setSlot(NAV_BAR_START + i, fillerSlot);
        }
        setSlot(BACK_SLOT_INDEX, getPreviousGuiSlot());
        setSlot(SORT_SLOT_INDEX, getAbilitySortNode().getNodeValue().getSlot());
        if (page > 1) {
            setSlot(PREVIOUS_PAGE_SLOT_INDEX, getPreviousPageSlot());
        }
        if (page < getMaximumPage()) {
            setSlot(NEXT_PAGE_SLOT_INDEX, getNextPageSlot());
        }
        setSlot(LOADOUT_DISPLAY_EDIT_SLOT, new LoadoutDisplayHomeSlot(getLoadout()));
    }

    /**
     * Handles number key presses (keys 1–3) on the three active combo slots.
     *
     * @param event The inventory click event.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onNumberKeyOnComboSlot(@NotNull InventoryClickEvent event) {
        if (event.getClick() != ClickType.NUMBER_KEY) {
            return;
        }
        if (!event.getView().getTopInventory().equals(inventory)) {
            return;
        }
        int rawSlot = event.getRawSlot();
        if (rawSlot != COMBO_ACTIVE_SLOT_1 && rawSlot != COMBO_ACTIVE_SLOT_2 && rawSlot != COMBO_ACTIVE_SLOT_3) {
            return;
        }
        event.setCancelled(true);

        int hotbarButton = event.getHotbarButton();
        int targetComboSlot = hotbarButton + 1;
        if (targetComboSlot < 1 || targetComboSlot > 3) {
            return;
        }
        int fromComboSlot = inventorySlotToComboSlot(rawSlot);
        if (fromComboSlot == -1 || fromComboSlot == targetComboSlot) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        var mcRPGPlayerOpt = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        loadout.swapActivePositions(fromComboSlot, targetComboSlot);
        var guiOpt = McRPG.getInstance().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.GUI)
                .getOpenedGui(mcRPGPlayer);
        guiOpt.ifPresent(gui -> gui.refreshGUI());
    }

    @NotNull
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                if (mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
                    LoadoutSelectionGui loadoutSelectionGui = new LoadoutSelectionGui(mcRPGPlayer);
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    player.openInventory(loadoutSelectionGui.getInventory());
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutSelectionGui);
                }
                return true;
            }

            @NotNull
            @Override
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.LOADOUT_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
            }
        };
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
                AbilitySortType.INNATE_ABILITIES,
                AbilitySortType.UNLOCKED_ABILITIES,
                AbilitySortType.UPGRADEABLE_ABILITIES,
                AbilitySortType.ACTIVE_ABILITIES
        );
    }

    /**
     * Maps a {@link ComboPattern} to its fixed inventory slot in the combo row.
     *
     * @param pattern The combo pattern.
     * @return The inventory slot index for that pattern's active ability slot.
     */
    private int comboPatternToInventorySlot(@NotNull ComboPattern pattern) {
        return switch (pattern) {
            case SLOT_1 -> COMBO_ACTIVE_SLOT_1;
            case SLOT_2 -> COMBO_ACTIVE_SLOT_2;
            case SLOT_3 -> COMBO_ACTIVE_SLOT_3;
        };
    }

    /**
     * Maps a raw inventory slot index to a 1-indexed combo slot number.
     *
     * @param inventorySlot The raw inventory slot.
     * @return The 1-indexed combo slot (1, 2, or 3), or {@code -1} if the slot is not a combo slot.
     */
    private int inventorySlotToComboSlot(int inventorySlot) {
        if (inventorySlot == COMBO_ACTIVE_SLOT_1) return 1;
        if (inventorySlot == COMBO_ACTIVE_SLOT_2) return 2;
        if (inventorySlot == COMBO_ACTIVE_SLOT_3) return 3;
        return -1;
    }
}
