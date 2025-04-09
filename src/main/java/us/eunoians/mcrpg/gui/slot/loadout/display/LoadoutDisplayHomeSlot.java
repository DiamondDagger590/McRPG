package us.eunoians.mcrpg.gui.slot.loadout.display;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.display.LoadoutDisplayHomeGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;

import java.util.List;

/**
 * This slot allows for opening of the {@link LoadoutDisplayHomeGui} when clicked.
 */
public class LoadoutDisplayHomeSlot extends McRPGSlot {

    private Loadout loadout;

    public LoadoutDisplayHomeSlot(@NotNull Loadout loadout) {
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            LoadoutDisplayHomeGui loadoutDisplayHomeGui = new LoadoutDisplayHomeGui(mcRPGPlayer, loadout);
            McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer, loadoutDisplayHomeGui);
            player.openInventory(loadoutDisplayHomeGui.getInventory());
        });
        return true;
    }


    @NotNull
    @Override
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(Material.OAK_HANGING_SIGN);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<gold>Edit Loadout Display"));
        itemMeta.lore(List.of(miniMessage.deserialize("<gold>Click <gray>to change how this loadout is displayed.")));
        itemStack.setItemMeta(itemMeta);
        return ItemBuilder.from(itemStack);
    }
}
