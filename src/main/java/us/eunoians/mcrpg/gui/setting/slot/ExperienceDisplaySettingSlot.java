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
import us.eunoians.mcrpg.setting.impl.ExperienceDisplaySetting;

/**
 * A {@link McRPGSettingSlot} that displays the {@link ExperienceDisplaySetting}.
 */
public class ExperienceDisplaySettingSlot extends McRPGSettingSlot<ExperienceDisplaySetting> {

    public ExperienceDisplaySettingSlot(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ExperienceDisplaySetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        var localizationManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.LOCALIZATION);
        switch (getSetting()) {
            case BOSS_BAR -> {
                ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_EXPERIENCE_DISPLAY_SETTING_BOSS_BAR_DISPLAY_ITEM));
                itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
                return itemBuilder;
            }
            case ACTION_BAR -> {
                ItemBuilder itemBuilder = ItemBuilder.from(localizationManager.getLocalizedSection(mcRPGPlayer, LocalizationKey.PLAYER_SETTINGS_GUI_EXPERIENCE_DISPLAY_SETTING_ACTION_BAR_DISPLAY_ITEM));
                itemBuilder.applyTagReplacements(localizationManager.getPaletteReplacements());
                return itemBuilder;
            }
            default -> {
                return ItemBuilder.from(new ItemStack(Material.AIR));
            }
        }
    }
}
