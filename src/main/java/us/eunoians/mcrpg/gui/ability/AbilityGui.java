package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.AbilitySlot;

import java.util.List;
import java.util.Set;

/**
 * This gui is the main gui for players to view all their abilities from.
 */
public class AbilityGui extends PaginatedSortedAbilityGui  {

    private static final Slot FILLER_GLASS_SLOT;
    private static final int NAVIGATION_ROW_START_INDEX = 45;
    private static final int PREVIOUS_PAGE_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 2;
    private static final int SORT_SLOT_INDEX = NAVIGATION_ROW_START_INDEX + 4;
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

    public AbilityGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
    }

    @NotNull
    @Override
    protected Inventory getInventoryForPage(int i) {
        return Bukkit.createInventory(getPlayer(), 54, McRPG.getInstance().getMiniMessage().deserialize("<gold>Skills Menu"));
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
        for (int i = 0; i < 9; i++) {
            setSlot(NAVIGATION_ROW_START_INDEX + i, FILLER_GLASS_SLOT);
        }
        // Set the sort slot
        setSlot(SORT_SLOT_INDEX, getAbilitySortNode().getNodeValue().getSlot());
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
    protected void paintAbilities(int page) {
        List<Ability> sortedAbilities = getSortedAbilitiesForPage(page);
        for (int i = 0; i < NAVIGATION_ROW_START_INDEX; i++) {
            if (i < sortedAbilities.size()) {
                setSlot(i, new AbilitySlot(getMcRPGPlayer(), sortedAbilities.get(i)));
            } else {
                removeSlot(i);
            }
        }
    }

    @Override
    public @NotNull Set<NamespacedKey> getUnsortedAbilities() {
        return getMcRPGPlayer().asSkillHolder().getAvailableAbilities();
    }

    @Override
    public int getNavigationRowStartIndex() {
        return NAVIGATION_ROW_START_INDEX;
    }
}
