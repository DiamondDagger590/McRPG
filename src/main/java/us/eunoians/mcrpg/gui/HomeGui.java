package us.eunoians.mcrpg.gui;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.exception.gui.InventoryAlreadyExistsForGuiException;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.home.HomeAbilitiesSlot;
import us.eunoians.mcrpg.gui.slot.home.HomeLoadoutSlot;
import us.eunoians.mcrpg.gui.slot.home.HomeSettingsSlot;

import java.util.Optional;

/**
 * The main gui for players to interact with McRPG through
 */
public class HomeGui extends Gui {

    private static final Slot FILLER_GLASS_SLOT;
    private static final int SETTINGS_SLOT_INDEX = 10;
    private static final int ABILITIES_SLOT_INDEX = 13;
    private static final int LOADOUT_SLOT_INDEX = 16;

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;

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

    public HomeGui(@NotNull McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
    }

    @Override
    protected void buildInventory() {
        if (this.inventory != null) {
            throw new InventoryAlreadyExistsForGuiException(this);
        } else {
            this.inventory = Bukkit.createInventory(player, 27, McRPG.getInstance().getMiniMessage().deserialize("<gold>Home Gui"));
            paintInventory();
        }
    }

    @Override
    public void paintInventory() {
        for (int i = 0; i < inventory.getSize(); i++) {
            setSlot(i, FILLER_GLASS_SLOT);
        }
        // Set the main slots for this gui
        setSlot(SETTINGS_SLOT_INDEX, new HomeSettingsSlot(mcRPGPlayer));
        setSlot(ABILITIES_SLOT_INDEX, new HomeAbilitiesSlot());
        setSlot(LOADOUT_SLOT_INDEX, new HomeLoadoutSlot());
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
