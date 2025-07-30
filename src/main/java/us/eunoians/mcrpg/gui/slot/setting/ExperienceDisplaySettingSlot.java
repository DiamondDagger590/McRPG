package us.eunoians.mcrpg.gui.slot.setting;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.setting.impl.ExperienceDisplaySetting;

import java.util.List;

/**
 * A {@link McRPGSettingSlot} that displays the {@link ExperienceDisplaySetting}.
 */
public class ExperienceDisplaySettingSlot extends McRPGSettingSlot<ExperienceDisplaySetting> {

    public ExperienceDisplaySettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ExperienceDisplaySetting setting) {
        super(mcRPGPlayer, setting);
    }

    // TODO make this configurable before verum kills u
    @NotNull
    @Override
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        switch (getSetting()) {
            case BOSS_BAR -> {
                ItemStack itemStack = new ItemStack(Material.DRAGON_HEAD);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Experience Display"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Displays gained experience through a boss bar."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this display setting.")));
                itemStack.setItemMeta(itemMeta);
                return ItemBuilder.from(itemStack);
            }
            case ACTION_BAR -> {
                ItemStack itemStack = new ItemStack(Material.BLAZE_ROD);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Experience Display"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Displays gained experience through an action bar."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this display setting.")));
                itemStack.setItemMeta(itemMeta);
                return ItemBuilder.from(itemStack);
            }
            default -> {
                return ItemBuilder.from(new ItemStack(Material.AIR));
            }
        }
    }
}
