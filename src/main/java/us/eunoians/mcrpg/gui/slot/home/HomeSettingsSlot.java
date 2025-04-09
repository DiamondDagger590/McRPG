package us.eunoians.mcrpg.gui.slot.home;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.HomeGui;
import us.eunoians.mcrpg.gui.PlayerSettingGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This slot is used in the {@link HomeGui} to open a settings gui when clicked.
 */
public class HomeSettingsSlot extends McRPGSlot {

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
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        PlayerSettingGui playerSettingGui = new PlayerSettingGui(mcRPGPlayer, McRPG.getInstance());
        McRPG.getInstance().getGuiTracker().trackPlayerGui(mcRPGPlayer, playerSettingGui);
        player.openInventory(playerSettingGui.getInventory());
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        itemMeta.setOwningPlayer(player);
        itemMeta.displayName(miniMessage.deserialize("<red>Settings"));
        itemMeta.lore(List.of(miniMessage.deserialize("<gray>Click to edit your McRPG settings.")));
        itemStack.setItemMeta(itemMeta);
        return ItemBuilder.from(itemStack);
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(HomeGui.class);
    }
}
