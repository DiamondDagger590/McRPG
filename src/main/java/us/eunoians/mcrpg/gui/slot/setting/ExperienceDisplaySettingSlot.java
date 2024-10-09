package us.eunoians.mcrpg.gui.slot.setting;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.ExperienceDisplaySetting;

import java.util.List;

/**
 * A {@link PlayerSettingSlot} that displays the {@link ExperienceDisplaySetting}.
 */
public class ExperienceDisplaySettingSlot extends PlayerSettingSlot<ExperienceDisplaySetting> {

    public ExperienceDisplaySettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ExperienceDisplaySetting setting) {
        super(mcRPGPlayer, setting);
    }

    // TODO make this configurable before verum kills u
    @NotNull
    @Override
    public ItemStack getItem() {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        switch (getSetting()) {
            case BOSS_BAR -> {
                ItemStack itemStack = new ItemStack(Material.DRAGON_HEAD);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Boss Bar"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Displays gained experience through a boss bar."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this display setting.")));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
            case ACTION_BAR -> {
                ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Action Bar"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Displays gained experience through an action bar."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this display setting.")));
                itemStack.setItemMeta(itemMeta);
                return itemStack;
            }
            default -> {
                return new ItemStack(Material.AIR);
            }
        }
    }
}
