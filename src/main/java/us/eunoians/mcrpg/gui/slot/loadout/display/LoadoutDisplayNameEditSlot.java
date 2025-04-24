package us.eunoians.mcrpg.gui.slot.loadout.display;


import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.chat.LoadoutDisplayNameChatResponse;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.loadout.Loadout;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;

/**
 * Clicking this slot will start a {@link LoadoutDisplayNameChatResponse}, where when responded to,
 * the response will be saved as the new name for the {@link us.eunoians.mcrpg.loadout.LoadoutDisplay}.
 */
public class LoadoutDisplayNameEditSlot extends McRPGSlot {

    private final Loadout loadout;

    public LoadoutDisplayNameEditSlot(@NotNull Loadout loadout) {
        this.loadout = loadout;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        mcRPGPlayer.getAsBukkitPlayer().ifPresent(player -> {
            // Close inventory
            player.closeInventory();
            // Notify player to send a response for the new name of the loadout
            McRPG mcRPG = McRPG.getInstance();
            MiniMessage miniMessage = mcRPG.getMiniMessage();
            Audience audience = mcRPG.getAdventure().player(player);
            audience.sendMessage(miniMessage.deserialize("<gray>Please type in chat the name you want this loadout to be called, or type <gold>cancel</gold> to cancel:"));
            LoadoutDisplayNameChatResponse loadoutDisplayNameChatResponse = new LoadoutDisplayNameChatResponse(player.getUniqueId(), loadout);
            mcRPG.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.CHAT_RESPONSE).addPendingResponse(player.getUniqueId(), loadoutDisplayNameChatResponse);
        });
        return true;
    }


    @NotNull
    @Override
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        ItemStack itemStack = new ItemStack(Material.OAK_HANGING_SIGN);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(miniMessage.deserialize("<gold>Loadout Display Name"));
        itemMeta.lore(List.of(miniMessage.deserialize("<gold>Click <gray>to change what the loadout's name is.")));
        itemStack.setItemMeta(itemMeta);
        return ItemBuilder.from(itemStack);
    }
}
