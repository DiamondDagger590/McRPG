package us.eunoians.mcrpg.gui.slot.home;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.HomeGui;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This slot is used in the {@link HomeGui} to open a settings gui when clicked.
 */
public class HomeSettingsSlot extends Slot {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;

    public HomeSettingsSlot(@NotNull McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
    }

    @Override
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        player.sendMessage(McRPG.getInstance().getMiniMessage().deserialize("<red>Settings are not currently supported. Please wait for a future release."));
        return true;
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        itemMeta.setOwningPlayer(player);
        itemMeta.displayName(miniMessage.deserialize("<red>Settings"));
        itemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to edit your McRPG settings.")));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(HomeGui.class);
    }
}
