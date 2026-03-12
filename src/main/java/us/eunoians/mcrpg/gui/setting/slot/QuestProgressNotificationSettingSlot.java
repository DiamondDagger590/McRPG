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
import us.eunoians.mcrpg.setting.impl.QuestProgressNotificationSetting;

/**
 * A {@link McRPGSettingSlot} that displays {@link QuestProgressNotificationSetting}s.
 */
public class QuestProgressNotificationSettingSlot extends McRPGSettingSlot<QuestProgressNotificationSetting> {

    public QuestProgressNotificationSettingSlot(@NotNull McRPGPlayer mcRPGPlayer,
                                                @NotNull QuestProgressNotificationSetting setting) {
        super(mcRPGPlayer, setting);
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        switch (getSetting()) {
            case ENABLED -> {
                return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_PROGRESS_NOTIFICATION_SETTING_ENABLED_DISPLAY_ITEM));
            }
            case DISABLED -> {
                return ItemBuilder.from(RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                        .manager(McRPGManagerKey.LOCALIZATION)
                        .getLocalizedSection(mcRPGPlayer, LocalizationKey.QUEST_PROGRESS_NOTIFICATION_SETTING_DISABLED_DISPLAY_ITEM));
            }
            default -> {
                return ItemBuilder.from(new ItemStack(Material.AIR));
            }
        }
    }
}
