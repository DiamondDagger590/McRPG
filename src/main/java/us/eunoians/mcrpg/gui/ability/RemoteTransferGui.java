package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.ClosableGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.LinkedNode;
import com.diamonddagger590.mccore.util.comparator.ChainComparator;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.mining.RemoteTransfer;
import us.eunoians.mcrpg.ability.impl.mining.remotetransfer.RemoteTransferCategory;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.FillerItemGui;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.gui.ability.slot.remotetransfer.RemoteTransferToggleAllSlot;
import us.eunoians.mcrpg.gui.ability.slot.remotetransfer.RemoteTransferToggleSlot;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.filter.core.McRPGPlayerContextFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This gui is used to let players toggle the allow state for a given material for their {@link RemoteTransfer} ability.
 */
public class RemoteTransferGui extends McRPGPaginatedGui implements ClosableGui<McRPGPlayer>, FillerItemGui {

    private final Comparator<RemoteTransferToggleSlot> ALPHABETICAL_CATEGORY = Comparator.comparing(slot -> slot.getRemoteTransferCategory().getName(getCreatingPlayer()));
    // TODO this might need revisited (also shouldn't be tied to material, we need to support custom item data as well :<)
    private final Comparator<RemoteTransferToggleSlot> ALPHABETICAL_MATERIAL = new ChainComparator<>(ALPHABETICAL_CATEGORY,
            Comparator.comparing(slot -> slot.getItem(getCreatingPlayer()).getType().toString()));

    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_GUI_SLOT_INDEX = NAVIGATION_ROW_START_INDEX;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;
    private static final int TOGGLE_ALL_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 8;

    private final Player player;
    private final List<RemoteTransferToggleSlot> remoteTransferToggleSlots = new ArrayList<>(); // This means it won't auto refresh until they open again but oh well
    private final Map<RemoteTransferSortOption, List<RemoteTransferToggleSlot>> cachedSort = new HashMap<>();
    private LinkedNode<RemoteTransferSortOption> sortOption;

    public RemoteTransferGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.sortOption = RemoteTransferSortOption.FIRST_SORT_TYPE;
        // Create all the slots we will need
        for (RemoteTransferCategory remoteTransferCategory : RemoteTransfer.getRemoteTransferCategories()) {
            for (CustomItemWrapper customItemWrapper : remoteTransferCategory.getCategoryItems()) {
                remoteTransferToggleSlots.add(new RemoteTransferToggleSlot(mcRPGPlayer, customItemWrapper, remoteTransferCategory));
            }
        }
    }

    @NotNull
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
         return new McRPGPreviousGuiSlot() {

            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                if (mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
                    AbilityAttributeEditGui abilityAttributeEditGui = new AbilityAttributeEditGui(mcRPGPlayer, McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY));
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    player.closeInventory();
                    player.openInventory(abilityAttributeEditGui.getInventory());
                }
                return true;
            }
        };
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int i) {
        return Bukkit.createInventory(player, 54, RegistryAccess.registryAccess().registry(McRPGRegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION).getLocalizedMessage(getCreatingPlayer(), LocalizationKey.REMOTE_TRANSFER_GUI_TITLE));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintNavigationBar(page, paintItems(page));
    }

    /**
     * Paints the items on the inventory for the given page.
     *
     * @param page The page to paint the items for.
     * @return {@code true} if any of the items are disallowed
     */
    private boolean paintItems(int page) {
        List<RemoteTransferToggleSlot> slots = getItemSlots(page);
        boolean enableButton = false;
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < slots.size()) {
                RemoteTransferToggleSlot slot = slots.get(i);
                setSlot(i, slot);
                if (slot.isItemDisallowed()) {
                    enableButton = true;
                }
            } else {
                removeSlot(i);
            }
        }
        return enableButton;
    }

    /**
     * Paints the navigation bar on the inventory for the given page.
     *
     * @param page         The page to paint the navigation bar for.
     * @param enableButton If {@code true}, sets the {@link RemoteTransferToggleAllSlot} to be in "enable" mode.
     */
    private void paintNavigationBar(int page, boolean enableButton) {
        Slot<McRPGPlayer> fillerItem = getFillerItemSlot();
        // Paint the nav bar with filler glass
        for (int i = 0; i < 9; i++) {
            setSlot(NAVIGATION_ROW_START_INDEX + i, fillerItem);
        }
        // Set the back button
        setSlot(PREVIOUS_GUI_SLOT_INDEX, getPreviousGuiSlot());
        // Set the sort slot
        setSlot(SORT_SLOT_INDEX, sortOption.getNodeValue().getSortSlot());
        // If the page is not the first page, then we need to put a previous arrow button
        if (page > 1) {
            setSlot(PREVIOUS_PAGE_SLOT_INDEX, getPreviousPageSlot());
        }
        // If the page is not the max page, then we need to put a next arrow button
        if (page < getMaximumPage()) {
            setSlot(NEXT_PAGE_SLOT_INDEX, getNextPageSlot());
        }
        // Set the toggle all slot
        setSlot(TOGGLE_ALL_SLOT_INDEX, new RemoteTransferToggleAllSlot(enableButton));
    }

    /**
     * Gets all the {@link RemoteTransferToggleSlot}s for the given page (sorted and filtered based on the existing sort type).
     *
     * @param page The page to get the slots for.
     * @return A {@link List} of all {@link RemoteTransferToggleSlot}s for the given page (sorted and filtered based on the existing sort type).
     */
    @NotNull
    private List<RemoteTransferToggleSlot> getItemSlots(int page) {
        List<RemoteTransferToggleSlot> slots = getAllCurrentItemSlots();

        // Get the abilities that need to be displayed on this page
        int startRange = ((page - 1) * NAVIGATION_ROW_START_INDEX);
        int endRange = Math.min(slots.size(), page * NAVIGATION_ROW_START_INDEX);
        return slots.subList(startRange, endRange);
    }

    /**
     * Gets all the {@link RemoteTransferToggleSlot}s for the current sort type, sorted and filtered.
     *
     * @return A {@link List} of all the {@link RemoteTransferToggleSlot}s for the current sort type, sorted and filtered.
     */
    public List<RemoteTransferToggleSlot> getAllCurrentItemSlots() {
        RemoteTransferSortOption sortOption = getSortOption().getNodeValue();
        List<RemoteTransferToggleSlot> slots = new ArrayList<>();
        if (cachedSort.containsKey(sortOption)) {
            slots = cachedSort.get(sortOption);
        } else {
            slots = sortOption.filter(getCreatingPlayer(), remoteTransferToggleSlots).stream().sorted(ALPHABETICAL_MATERIAL).collect(Collectors.toList());
            cachedSort.put(sortOption, slots);
        }
        return slots;
    }

    @Override
    public int getMaximumPage() {
        return (int) Math.max(1, Math.ceil((double) getAllCurrentItemSlots().size() / 45));
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    /**
     * Set the sort option for this gui.
     *
     * @param sortOption The new sort option for this gui.
     */
    void setSortOption(LinkedNode<RemoteTransferSortOption> sortOption) {
        this.sortOption = sortOption;
    }

    /**
     * Gets the node containing the current {@link RemoteTransferSortOption}.
     *
     * @return The node containing the current {@link RemoteTransferSortOption}.
     */
    @NotNull
    LinkedNode<RemoteTransferSortOption> getSortOption() {
        return sortOption;
    }

    @Override
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        Player bukkitPlayer = (Player) inventoryCloseEvent.getPlayer();
        var corePlayerOptional = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getPlayer(bukkitPlayer.getUniqueId());
        if (corePlayerOptional.isPresent()) {
            McRPGPlayer mcRPGPlayer = corePlayerOptional.get();
            AbilityAttributeEditGui abilityAttributeEditGui = new AbilityAttributeEditGui(mcRPGPlayer, McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.ABILITY).getRegisteredAbility(RemoteTransfer.REMOTE_TRANSFER_KEY));
            Bukkit.getScheduler().scheduleSyncDelayedTask(McRPG.getInstance(), () -> {
                McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, abilityAttributeEditGui);
                bukkitPlayer.openInventory(abilityAttributeEditGui.getInventory());
            }, 1L);
        }
    }

    /**
     * This class is a wrapper for {@link us.eunoians.mcrpg.ability.impl.mining.remotetransfer.RemoteTransferCategoryType}s to be used
     * as a filter for the remote transfer gui, as well as add an "All" filter that is a combination of all filters.
     */
    private static class RemoteTransferSortOption {

        public final static LinkedNode<RemoteTransferSortOption> FIRST_SORT_TYPE = new LinkedNode<>(new RemoteTransferSortOption());

        static {
            LinkedNode<RemoteTransferSortOption> prev = FIRST_SORT_TYPE;
            for (RemoteTransferCategory remoteTransferCategory : RemoteTransfer.getRemoteTransferCategories()) {
                LinkedNode<RemoteTransferSortOption> next = new LinkedNode<>(new RemoteTransferSortOption(remoteTransferCategory));
                prev.setNext(next);
                prev = next;
            }
            prev.setNext(FIRST_SORT_TYPE);
        }

        private final RemoteTransferCategory remoteTransferCategory;
        private final McRPGPlayerContextFilter<RemoteTransferToggleSlot> filter;

        public RemoteTransferSortOption() {
            this.remoteTransferCategory = null;
            this.filter = (corePlayer, collection) -> collection;
        }

        public RemoteTransferSortOption(@NotNull RemoteTransferCategory remoteTransferCategory) {
            this.remoteTransferCategory = remoteTransferCategory;
            this.filter = (corePlayer, collection) -> collection.stream()
                    .filter(remoteTransferToggleSlot -> remoteTransferToggleSlot.getRemoteTransferCategory().getCategoryKey().equalsIgnoreCase(remoteTransferCategory.getCategoryKey()))
                    .collect(Collectors.toList());
        }

        /**
         * Gets the name for this sort option.
         *
         * @return The name for this sort option.
         */
        @NotNull
        public String getName(@NotNull McRPGPlayer mcRPGPlayer) {
            return remoteTransferCategory.getName(mcRPGPlayer);
        }

        /**
         * Filters a {@link Collection} of {@link RemoteTransferToggleSlot}s based on the type of category represented by this option.
         *
         * @param mcRPGPlayer               The {@link McRPGPlayer} to filter for.
         * @param remoteTransferToggleSlots The {@link Collection} to filter.
         * @return A filtered {@link List} of {@link RemoteTransferToggleSlot}s.
         */
        @NotNull
        public List<RemoteTransferToggleSlot> filter(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Collection<RemoteTransferToggleSlot> remoteTransferToggleSlots) {
            return List.copyOf(filter.filter(mcRPGPlayer, remoteTransferToggleSlots));
        }

        /**
         * Gets a {@link Slot} for this sort option that will progress to the next sort option when clicked.
         *
         * @return A {@link Slot} for this sort option.
         */
        @NotNull
        public McRPGSlot getSortSlot() {
            return new McRPGSlot() {
                @Override
                public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                    var guiOptional = CorePlugin.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
                    guiOptional.ifPresent(gui -> {
                        if (gui instanceof RemoteTransferGui remoteTransferGui) {
                            mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
                                remoteTransferGui.setPage(1);
                                // Ignore empty categories
                                do {
                                    remoteTransferGui.setSortOption(remoteTransferGui.getSortOption().getNextNode());
                                } while (remoteTransferGui.getAllCurrentItemSlots().isEmpty());
                                remoteTransferGui.refreshGUI();
                            });
                        }
                    });
                    return true;
                }

                @NotNull
                @Override
                public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
                    return remoteTransferCategory.getItemBuilder(mcRPGPlayer);
                }

                @NotNull
                @Override
                public Set<Class<?>> getValidGuiTypes() {
                    return Set.of(RemoteTransferGui.class);
                }
            };
        }
    }
}
