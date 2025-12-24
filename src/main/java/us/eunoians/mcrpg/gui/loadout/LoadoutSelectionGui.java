package us.eunoians.mcrpg.gui.loadout;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.gui.common.slot.McRPGPreviousGuiSlot;
import us.eunoians.mcrpg.gui.home.HomeGui;
import us.eunoians.mcrpg.gui.loadout.slot.LoadoutSelectionSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This gui displays all the player's {@link Loadout}s, where they can select individual loadouts
 * to edit.
 */
public class LoadoutSelectionGui extends McRPGPaginatedGui {

    private static final int NAVIGATION_ROW_START_INDEX = 9;
    private static final int PREVIOUS_GUI_SLOT_INDEX = NAVIGATION_ROW_START_INDEX;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;

    private final Player player;

    public LoadoutSelectionGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int page) {
        return Bukkit.createInventory(player, 18, RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION)
                .getLocalizedMessageAsComponent(getCreatingPlayer(), LocalizationKey.LOADOUT_SELECTION_GUI_TITLE));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintLoadouts(page);
        paintNavigationBar(page);
    }

    private void paintNavigationBar(int page) {
        // Paint the nav bar with filler glass
        Slot<McRPGPlayer> fillerItem = getFillerItemSlot();
        for (int i = 0; i < 9; i++) {
            setSlot(NAVIGATION_ROW_START_INDEX + i, fillerItem);
        }
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

    private void paintLoadouts(int page) {
        List<Loadout> loadouts = new ArrayList<>();
        LoadoutHolder loadoutHolder = getCreatingPlayer().asSkillHolder();
        for (int i = 1; i <= loadoutHolder.getMaxLoadoutAmount(); i++) {
            loadouts.add(loadoutHolder.getLoadout(i));
        }
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < loadouts.size()) {
                Loadout loadout = loadouts.get(i);
                LoadoutSelectionSlot loadoutSelectionSlot = new LoadoutSelectionSlot(getCreatingPlayer(), loadout);
                setSlot(i, loadoutSelectionSlot);
            }
            else {
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
                return LocalizationKey.LOADOUT_SELECTION_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM;
            }
        };
    }

    @Override
    public int getMaximumPage() {
        return Math.max(1, getCreatingPlayer().asSkillHolder().getMaxLoadoutAmount() / 9);
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }
}
