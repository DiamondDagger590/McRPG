package us.eunoians.mcrpg.gui.slot.loadout.display;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.display.LoadoutDisplayItemInputGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;

/**
 * This slot will open the {@link LoadoutDisplayItemInputGui} whenever clicked to allow
 * users to input an {@link ItemStack} to edit the {@link us.eunoians.mcrpg.loadout.LoadoutDisplay}.
 */
public class LoadoutDisplayItemSlot implements McRPGSlot {

    private final Loadout loadout;

    public LoadoutDisplayItemSlot(@NotNull Loadout loadout) {
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            player.closeInventory();
            LoadoutDisplayItemInputGui loadoutDisplayItemInputGui = new LoadoutDisplayItemInputGui(mcRPGPlayer, loadout);
            mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutDisplayItemInputGui);
            player.openInventory(loadoutDisplayItemInputGui.getInventory());
        });
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = loadout.getDisplay().getDisplayItem();
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<gold>Loadout Display Item"));
        itemMeta.lore(List.of(miniMessage.deserialize("<gold>Click <gray>to change what item is used to display the loadout.")));
        itemStack.setItemMeta(itemMeta);
        return ItemBuilder.from(itemStack);
    }
}
