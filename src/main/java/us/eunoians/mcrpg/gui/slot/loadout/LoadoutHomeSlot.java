package us.eunoians.mcrpg.gui.slot.loadout;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.registry.RegistryKey;
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
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This slot is used to go back to the {@link LoadoutSelectionGui} from the {@link LoadoutGui}.
 */
public class LoadoutHomeSlot implements McRPGSlot {

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
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        LoadoutSelectionGui loadoutSelectionGui = new LoadoutSelectionGui(mcRPGPlayer);
        mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, loadoutSelectionGui);
        player.openInventory(loadoutSelectionGui.getInventory());
        return true;
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(LoadoutGui.class);
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack previousGuiItem = new ItemStack(Material.BARRIER);
        ItemMeta previousGuiItemMeta = previousGuiItem.getItemMeta();
        previousGuiItemMeta.displayName(miniMessage.deserialize("<red>Return to loadout selection</red>"));
        previousGuiItemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to return to the loadout selection screen.</gray>.")));
        previousGuiItem.setItemMeta(previousGuiItemMeta);
        return ItemBuilder.from(previousGuiItem);
    }
}
