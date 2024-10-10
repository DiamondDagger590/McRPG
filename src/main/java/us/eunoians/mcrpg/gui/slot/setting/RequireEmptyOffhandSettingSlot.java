package us.eunoians.mcrpg.gui.slot.setting;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.RequireEmptyOffhandSetting;

import java.util.List;

public class RequireEmptyOffhandSettingSlot extends PlayerSettingSlot<RequireEmptyOffhandSetting> {

    public RequireEmptyOffhandSettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull RequireEmptyOffhandSetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        switch (getSetting()) {
            case ENABLED -> {
                ItemStack itemStack = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Require Empty Offhand"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Status: <green>Enabled</green>."), miniMessage.deserialize("<gray>Abilities can not be readied if you have an item in your offhand."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this setting.")));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
            case DISABLED -> {
                ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Require Empty Offhand"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Status: <red>Disabled</red>."), miniMessage.deserialize("<gray>Abilities can be readied even with an item in your offhand."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this setting.")));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
            default -> {
                return new ItemStack(Material.AIR);
            }
        }    }
}
