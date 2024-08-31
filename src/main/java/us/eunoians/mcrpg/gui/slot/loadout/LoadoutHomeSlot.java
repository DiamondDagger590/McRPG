package us.eunoians.mcrpg.gui.slot.loadout;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.loadout.LoadoutGui;
import us.eunoians.mcrpg.gui.loadout.LoadoutSelectionGui;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This slot is used to go back to the {@link LoadoutSelectionGui} from the {@link LoadoutGui}.
 */
public class LoadoutHomeSlot extends Slot {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;

    public LoadoutHomeSlot(@NotNull McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        LoadoutSelectionGui loadoutSelectionGui = new LoadoutSelectionGui(mcRPGPlayer);
        McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer, loadoutSelectionGui);
        player.openInventory(loadoutSelectionGui.getInventory());
        return true;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(LoadoutGui.class);
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack previousGuiItem = new ItemStack(Material.BARRIER);
        ItemMeta previousGuiItemMeta = previousGuiItem.getItemMeta();
        previousGuiItemMeta.displayName(miniMessage.deserialize("<red>Return to loadout selection</red>"));
        previousGuiItemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to return to the loadout selection screen.</gray>.")));
        previousGuiItem.setItemMeta(previousGuiItemMeta);
        return previousGuiItem;
    }
}
