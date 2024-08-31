package us.eunoians.mcrpg.gui.slot.home;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.HomeGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutSelectionGui;

import java.util.List;
import java.util.Set;

/**
 * This slot is used in the {@link HomeGui} to open a new {@link LoadoutSelectionGui} when clicked.
 */
public class HomeLoadoutSlot extends Slot {

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        if (corePlayer instanceof McRPGPlayer mcRPGPlayer && mcRPGPlayer.getAsBukkitPlayer().isPresent()) {
            LoadoutSelectionGui loadoutSelectionGui = new LoadoutSelectionGui(mcRPGPlayer);
            McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer, loadoutSelectionGui);
            mcRPGPlayer.getAsBukkitPlayer().get().openInventory(loadoutSelectionGui.getInventory());
        }
        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(Material.COMPASS);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<red>Loadouts"));
        itemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to edit your loadouts.")));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(HomeGui.class);
    }
}
