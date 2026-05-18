package us.eunoians.mcrpg.gui.setting.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.impl.RequireEmptyOffhandSetting;

/**
 * @deprecated The ready-state activation model was removed when the mana-based combo activation
 *             system was introduced. This slot corresponds to {@link RequireEmptyOffhandSetting},
 *             which no longer affects gameplay. Scheduled for removal in a future release.
 */
@Deprecated(forRemoval = true)
public class RequireEmptyOffhandSettingSlot extends McRPGSettingSlot<RequireEmptyOffhandSetting> {

    public RequireEmptyOffhandSettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull RequireEmptyOffhandSetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        var localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        switch (getSetting()) {
            case ENABLED -> {
                ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_REQUIRE_EMPTY_OFFHAND_SETTING_SLOT_ENABLED_DISPLAY_ITEM));
                itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
                return itemBuilder;
            }
            case DISABLED -> {
                ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_REQUIRE_EMPTY_OFFHAND_SETTING_SLOT_DISABLED_DISPLAY_ITEM));
                itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
                return itemBuilder;
            }
            default -> {
                return ItemBuilder.from(new ItemStack(Material.AIR));
            }
        }
    }
}
