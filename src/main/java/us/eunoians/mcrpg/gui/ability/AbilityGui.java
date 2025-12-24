package us.eunoians.mcrpg.gui.ability;

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
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.slot.AbilitySlot;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.home.HomeGui;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Set;

/**
 * This gui is the main gui for players to view all their abilities from.
 */
public class AbilityGui extends PaginatedSortedAbilityGui {

    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_GUI_SLOT_INDEX = NAVIGATION_ROW_START_INDEX;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;

    public AbilityGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int i) {
        return Bukkit.createInventory(getPlayer(), 54,
                RegistryAccess.registryAccess()
                        .registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.ABILITY_GUI_TITLE));
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
        setSlot(PREVIOUS_GUI_SLOT_INDEX, getPreviousGuiSlot());
    }

    @Override
    protected void paintAbilities(int page) {
        List<Ability> sortedAbilities = getSortedAbilitiesForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < sortedAbilities.size()) {
                setSlot(i, new AbilitySlot(getCreatingPlayer(), sortedAbilities.get(i)));
            } else {
                removeSlot(i);
            }
        }
    }

    @NotNull
    public McRPGPreviousGuiSlot getPreviousGuiSlot() {
        return new McRPGPreviousGuiSlot() {
            @Override
            public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
                if (mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
                    HomeGui homeGui = new HomeGui(mcRPGPlayer);;
                    Player player = mcRPGPlayer.getAsBukkitPlayer().get();
                    player.openInventory(homeGui.getInventory());
                    McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, homeGui);
                }
                return true;
            }

            @NotNull
            @Override
            public Route getSpecificDisplayItemRoute() {
                return LocalizationKey.ABILITY_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
            }
        };
    }

    @Override
    @NotNull
    public Set<NamespacedKey> getUnsortedAbilities() {
        return getCreatingPlayer().asSkillHolder().getAvailableAbilities();
    }

    @Override
    public int getNavigationRowStartIndex() {
        return NAVIGATION_ROW_START_INDEX;
    }
}
