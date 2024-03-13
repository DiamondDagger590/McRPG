package us.eunoians.mcrpg.gui;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.CoreGui;
import com.diamonddagger590.mccore.gui.component.StandardClickableGui;
import com.diamonddagger590.mccore.gui.function.GuiClickFunction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;

public class HomeGui extends CoreGui implements StandardClickableGui {

    private static final ItemStack FILLER_GLASS = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private static final ItemStack SETTINGS_BUTTON_ITEM = new ItemStack(Material.LEVER);
    private static final GuiClickFunction ON_SETTINGS_CLICK = ((corePlayer, gui, inventoryClickEvent) -> {

        inventoryClickEvent.setCancelled(true);
    });
    private static final ItemStack SKILLS_MENU_BUTTON_ITEM = new ItemStack(Material.REDSTONE);
    private static final GuiClickFunction ON_SKILLS_CLICK = ((corePlayer, gui, inventoryClickEvent) -> {
        inventoryClickEvent.setCancelled(true);
        Optional<Player> playerOptional = corePlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(corePlayer);
        }
        playerOptional.get().performCommand("skill");
    });

    static {
        ItemMeta meta = FILLER_GLASS.getItemMeta();
        meta.setDisplayName("");
        FILLER_GLASS.setItemMeta(meta);

        meta = SETTINGS_BUTTON_ITEM.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Settings");
        SETTINGS_BUTTON_ITEM.setItemMeta(meta);

        meta = SKILLS_MENU_BUTTON_ITEM.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Skills Menu");
        SKILLS_MENU_BUTTON_ITEM.setItemMeta(meta);
    }

    private final McRPGPlayer mcRPGPlayer;

    public HomeGui(@NotNull McRPGPlayer mcRPGPlayer) {
        super();
        this.mcRPGPlayer = mcRPGPlayer;
        this.guiFillerFunction = (inventory -> {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    inventory.setItem(i, FILLER_GLASS.clone());
                }
            }
        });

        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.inventory = Bukkit.createInventory(playerOptional.get(), 27, McRPG.getInstance().getMiniMessage().deserialize("<gold>McRPG Menu"));
        setupItems();
    }

    private void setupItems() {
        inventory.setItem(11, SETTINGS_BUTTON_ITEM);
        addGuiClickFunction(11, ON_SETTINGS_CLICK);
        inventory.setItem(14, SKILLS_MENU_BUTTON_ITEM);
        addGuiClickFunction(14, ON_SKILLS_CLICK);
        executeFillerFunction();
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    public McRPGPlayer getPlayer() {
        return mcRPGPlayer;
    }


    @Override
    public boolean cancelNonFunctionClicks() {
        return true;
    }

    @Override
    @EventHandler(priority = EventPriority.MONITOR)
    public void handleClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        StandardClickableGui.super.handleClick(inventoryClickEvent);
    }
}
