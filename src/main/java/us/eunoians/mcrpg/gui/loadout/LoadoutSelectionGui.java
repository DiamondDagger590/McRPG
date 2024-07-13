package us.eunoians.mcrpg.gui.loadout;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.CoreGui;
import com.diamonddagger590.mccore.gui.component.ClickableGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.LoadoutHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.loadout.Loadout;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoadoutSelectionGui extends CoreGui implements ClickableGui {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;

    public LoadoutSelectionGui(@NotNull McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        setupGUI();
    }

    @Override
    @EventHandler(priority = EventPriority.LOWEST)
    public void handleClick(@NotNull InventoryClickEvent inventoryClickEvent) {
        Inventory inventory = inventoryClickEvent.getClickedInventory();
        if (inventoryClickEvent.getWhoClicked() instanceof Player player
                && inventory != null
                && canProcessEvent(player, inventory)) {
            inventoryClickEvent.setCancelled(true);

            int clickedSlot = inventoryClickEvent.getSlot();
            LoadoutHolder loadoutHolder = mcRPGPlayer.asSkillHolder();

            // If they clicked on the upper part of the inventory
            if (clickedSlot < inventory.getSize() - 10) {
                // If they clicked on a valid loadout
                if (loadoutHolder.hasLoadout(clickedSlot + 1)) {
                    ClickType clickType = inventoryClickEvent.getClick();
                    if (clickType == ClickType.LEFT) {
                        player.performCommand("loadout edit " + (clickedSlot + 1));
                    }
                    else if(clickType == ClickType.RIGHT){
                        player.performCommand("loadout set " + (clickedSlot + 1));
                    }
                }
            }
            // They clicked on the bottom
            else {

            }
        }
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, McRPG.getInstance());
    }

    @Override
    public void unregisterListeners() {
        InventoryClickEvent.getHandlerList().unregister(this);
    }

    private void setupGUI() {
        List<Loadout> loadouts = new ArrayList<>();
        LoadoutHolder loadoutHolder = mcRPGPlayer.asSkillHolder();
        for (int i = 1; i <= getMaxLoadoutAmount(); i++) {
            loadouts.add(loadoutHolder.getLoadout(i));
        }
        int loadoutSize = loadouts.size();
        inventory = Bukkit.createInventory(player, Math.clamp(9 - loadoutSize % 9 + loadoutSize, 18, 54), McRPG.getInstance().getMiniMessage().deserialize("<gold>Select your loadout"));
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        for (int i = 0; i < loadoutSize; i++) {
            Loadout loadout = loadouts.get(i);
            ItemStack itemStack = new ItemStack(Material.CHERRY_SIGN);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(miniMessage.deserialize(String.format("<gray>Loadout <gold>%d</gold></gray>", i + 1)));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Left click to edit this loadout."));
            if (i + 1 == loadoutHolder.getCurrentLoadoutSlot()) {
                lore.add(miniMessage.deserialize(""));
                lore.add(miniMessage.deserialize("<gray>This is your currently selected loadout."));
                itemMeta.addEnchant(Enchantment.POWER, 1, true);
                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            else {
                lore.add(miniMessage.deserialize("<gray>Right click to set this loadout as active."));
            }
            itemMeta.lore(lore);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(i, itemStack);
        }
    }

    private int getMaxLoadoutAmount() {
        return McRPG.getInstance().getFileManager().getFile(FileType.MAIN_CONFIG).getInt(MainConfigFile.MAX_LOADOUT_AMOUNT);
    }
}
