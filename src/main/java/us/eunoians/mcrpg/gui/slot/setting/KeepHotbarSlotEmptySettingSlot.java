package us.eunoians.mcrpg.gui.slot.setting;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.KeepHotbarSlotEmptySetting;

import java.util.List;

/**
 * A {@link PlayerSettingSlot} that displays {@link KeepHotbarSlotEmptySetting}s.
 */
public class KeepHotbarSlotEmptySettingSlot extends PlayerSettingSlot<KeepHotbarSlotEmptySetting> {

    public KeepHotbarSlotEmptySettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull KeepHotbarSlotEmptySetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        if (getSetting() == KeepHotbarSlotEmptySetting.DISABLED) {
            ItemStack itemStack = new ItemStack(Material.RED_SHULKER_BOX);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(miniMessage.deserialize("<gold>Keep Hotbar Slot Empty"));
            itemMeta.lore(List.of(miniMessage.deserialize("<gray>Status: <red>Disabled</red>."), miniMessage.deserialize("<gray>"), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to enable this setting.")));
            itemStack.setItemMeta(itemMeta);
            return itemStack;
        }
        ItemStack itemStack = new ItemStack(Material.GREEN_SHULKER_BOX);
        int userSlot = getSetting().getSlot() + 1;
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setMaxStackSize(userSlot);
        itemMeta.displayName(miniMessage.deserialize("<gold>Keep Hotbar Slot Empty"));
        itemMeta.lore(List.of(miniMessage.deserialize("<gray>Empty Slot: <gold>" + userSlot + "</gold>."), miniMessage.deserialize("<gray>Prevents picked up items going into the designated slot."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this setting.")));
        itemStack.setItemMeta(itemMeta);
        itemStack.setAmount(userSlot);
        return itemStack;
    }
}
