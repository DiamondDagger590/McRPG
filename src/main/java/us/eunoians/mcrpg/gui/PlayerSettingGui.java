package us.eunoians.mcrpg.gui;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.PaginatedGui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.PlayerSetting;

import java.util.List;
import java.util.Optional;

/**
 * This gui is used to display all {@link PlayerSetting}s to a player.
 */
public class PlayerSettingGui extends PaginatedGui {

    private static final Slot FILLER_GLASS_SLOT;
    private static final int SETTING_DISPLAY_SIZE = 18;
    private static final int NAVIGATION_ROW_START_INDEX = SETTING_DISPLAY_SIZE;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int NEXT_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 6;

    // Create static slots
    static {
        // Create filler glass
        ItemStack fillerGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerGlassMeta = fillerGlass.getItemMeta();
        fillerGlassMeta.setDisplayName(" ");
        fillerGlass.setItemMeta(fillerGlassMeta);
        FILLER_GLASS_SLOT = new Slot() {

            @Override
            public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
                return true;
            }

            @NotNull
            @Override
            public ItemStack getItem() {
                return fillerGlass;
            }
        };
    }

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;
    private final McRPG plugin;

    public PlayerSettingGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull McRPG mcRPG) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.plugin = mcRPG;
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int i) {
        return Bukkit.createInventory(getPlayer(), 27, McRPG.getInstance().getMiniMessage().deserialize("<gold>Editing Settings"));
    }

    @Override
    protected void paintInventoryForPage(@NotNull Inventory inventory, int page) {
        paintNavigationBar(page);
        paintSettings(page);
    }

    /**
     * Paints the settings for a given page.
     *
     * @param page The page to paint the settings for.
     */
    private void paintSettings(int page) {
        List<PlayerSetting> settings = getSettingsForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < settings.size()) {
                setSlot(i, settings.get(i).getSettingSlot(mcRPGPlayer));
            } else {
                removeSlot(i);
            }
        }
    }

    /**
     * Paints the navigation bar for a given page.
     *
     * @param page The page to paint.
     */
    private void paintNavigationBar(int page) {
        // Paint the nav bar with filler glass
        for (int i = 0; i < 9; i++) {
            setSlot(NAVIGATION_ROW_START_INDEX + i, FILLER_GLASS_SLOT);
        }
        // If the page is not the first page, then we need to put a previous arrow button
        if (page > 1) {
            setSlot(PREVIOUS_PAGE_SLOT_INDEX, PREVIOUS_PAGE_SLOT);
        }
        // If the page is not the max page, then we need to put a next arrow button
        if (page < getMaximumPage()) {
            setSlot(NEXT_PAGE_SLOT_INDEX, NEXT_PAGE_SLOT);
        }
    }

    @Override
    public int getMaximumPage() {
        return (int) Math.max(1, Math.ceil((double) getSettings().size() / getNavigationRowStartIndex()));
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
     * Gets a {@link List} of all {@link PlayerSetting}s to be displayed.
     *
     * @return A {@link List} of all {@link PlayerSetting}s to be displayed.
     */
    @NotNull
    public List<PlayerSetting> getSettings() {
        return mcRPGPlayer.getPlayerSettings()
                .stream()
                .toList();
    }

    /**
     * Gets a partial {@link List} of {@link PlayerSetting}s, containing only the ones that
     * should be displayed for the provided page.
     *
     * @param page The page to get the partial list for.
     * @return A partial {@link List} of {@link PlayerSetting}s, containing only the ones
     * that should be displayed for the provided page.
     */
    @NotNull
    public List<PlayerSetting> getSettingsForPage(int page) {
        // Get the abilities that need to be displayed on this page
        List<PlayerSetting> settings = getSettings();
        int startRange = ((page - 1) * getNavigationRowStartIndex());
        int endRange = Math.min(settings.size(), page * getNavigationRowStartIndex());
        return settings.subList(startRange, endRange);
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
    public int getNavigationRowStartIndex() {
        return NAVIGATION_ROW_START_INDEX;
    }

    /**
     * Gets the {@link Player} who is viewing their settings.
     *
     * @return The {@link Player} who is viewing their settings.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }
}
