package us.eunoians.mcrpg.gui.slot.setting;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.KeepHandEmptySetting;

import java.util.List;

/**
 * A {@link PlayerSettingSlot} that displays {@link KeepHandEmptySetting}s.
 */
public class KeepHandEmptySettingSlot extends PlayerSettingSlot<KeepHandEmptySetting> {

    public KeepHandEmptySettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull KeepHandEmptySetting setting) {
        super(mcRPGPlayer, setting);
    }

    // TODO make this configurable before verum kills u
    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        switch (getSetting()) {
            case ENABLED -> {
                ItemStack itemStack = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Keep Hand Empty"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Status: <green>Enabled</green>."), miniMessage.deserialize("<gray>Prevents picked up items from going into your hand if it is empty."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this setting.")));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
            case DISABLED -> {
                ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Keep Hand Empty"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Status: <red>Disabled</red>."), miniMessage.deserialize("<gray>Lets picked up items go into your hand."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this setting.")));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
            default -> {
                return new ItemStack(Material.AIR);
            }
        }
    }
}
