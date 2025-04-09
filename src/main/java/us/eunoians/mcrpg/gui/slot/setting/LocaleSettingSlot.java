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
import us.eunoians.mcrpg.localization.NativeLocale;
import us.eunoians.mcrpg.setting.impl.LocaleSetting;

import java.util.List;

/**
 * A {@link PlayerSettingSlot} that displays {@link LocaleSetting}s.
 */
public class LocaleSettingSlot extends PlayerSettingSlot<LocaleSetting> {

    public LocaleSettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull LocaleSetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@Nullable McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        switch (getSetting()) {
            case CLIENT_LOCALE -> {
                ItemStack itemStack = new ItemStack(Material.DIRT);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Client Language"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Tries to use your client language for McRPG where possible."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this display setting.")));
                itemStack.setItemMeta(itemMeta);
                return ItemBuilder.from(itemStack);
            }
            case SERVER_LOCALE -> {
                ItemStack itemStack = new ItemStack(Material.COMPASS);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.displayName(miniMessage.deserialize("<gold>Server Language"));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Tries to use the server default language for McRPG where possible."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this display setting.")));
                itemStack.setItemMeta(itemMeta);
                return ItemBuilder.from(itemStack);
            }
            default -> {
                ItemStack itemStack = new ItemStack(Material.OAK_HANGING_SIGN);
                ItemMeta itemMeta = itemStack.getItemMeta();
                NativeLocale nativeLocale = getSetting().getNativeLocale().orElse(NativeLocale.ENGLISH);
                itemMeta.displayName(miniMessage.deserialize("<gold>" + nativeLocale.getLocaleName()));
                itemMeta.lore(List.of(miniMessage.deserialize("<gray>Tries to use " + nativeLocale.getLocaleName() + " for McRPG where possible."), miniMessage.deserialize(""), miniMessage.deserialize("<gold>Click <gray>to change this display setting.")));
                itemStack.setItemMeta(itemMeta);
                return ItemBuilder.from(itemStack);
            }
        }
    }
}
