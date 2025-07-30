package us.eunoians.mcrpg.gui.loadout;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.slot.Slot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.common.McRPGPaginatedGui;
import us.eunoians.mcrpg.gui.slot.loadout.LoadoutSelectionSlot;
import us.eunoians.mcrpg.loadout.Loadout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This gui displays all the player's {@link Loadout}s, where they can select individual loadouts
 * to edit.
 */
public class LoadoutSelectionGui extends McRPGPaginatedGui {

    private static final int NAVIGATION_ROW_START_INDEX = 9;
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
        return Bukkit.createInventory(player, 18, McRPG.getInstance().getMiniMessage().deserialize("<gold>Viewing loadouts"));
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
