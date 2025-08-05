package us.eunoians.mcrpg.gui.setting.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.KeepHotbarSlotEmptySetting;

/**
 * A {@link McRPGSettingSlot} that displays {@link KeepHotbarSlotEmptySetting}s.
 */
public class KeepHotbarSlotEmptySettingSlot extends McRPGSettingSlot<KeepHotbarSlotEmptySetting> {

    public KeepHotbarSlotEmptySettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull KeepHotbarSlotEmptySetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        ItemBuilder itemBuilder;
        if (getSetting() == KeepHotbarSlotEmptySetting.DISABLED) {
            itemBuilder = ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_KEEP_HOTBAR_SLOT_EMPTY_SETTING_DISABLED_DISPLAY_ITEM));
        } else {
            int userSlot = getSetting().getSlot() + 1;
            itemBuilder = ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.LOCALIZATION)
                    .getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_KEEP_HOTBAR_SLOT_EMPTY_SETTING_ENABLED_DISPLAY_ITEM))
                    .setMaxStackSize(userSlot)
                    .setAmount(userSlot)
                    .addPlaceholder("slot", Integer.toString(userSlot));
        }
        return itemBuilder;
    }
}
