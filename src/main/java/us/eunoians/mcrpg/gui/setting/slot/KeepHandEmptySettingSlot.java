package us.eunoians.mcrpg.gui.setting.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.KeepHandEmptySetting;

/**
 * A {@link McRPGSettingSlot} that displays {@link KeepHandEmptySetting}s.
 */
public class KeepHandEmptySettingSlot extends McRPGSettingSlot<KeepHandEmptySetting> {

    public KeepHandEmptySettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull KeepHandEmptySetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        switch (getSetting()) {
            case ENABLED -> {
                return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_KEEP_HAND_EMPTY_SETTING_ENABLED_DISPLAY_ITEM));
            }
            case DISABLED -> {
                return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_KEEP_HAND_EMPTY_SETTING_DISABLED_DISPLAY_ITEM));
            }
            default -> {
                return ItemBuilder.from(new ItemStack(Material.AIR));
            }
        }
    }
}
