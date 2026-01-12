package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.PlayerSettingGui;
import us.eunoians.mcrpg.gui.setting.slot.LocaleSettingSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * A marker interface for locale-related player settings.
 * <p>
 * This interface is implemented by both {@link LocaleSetting} (for CLIENT_LOCALE and SERVER_LOCALE)
 * and {@link SpecificLocaleSetting} (for specific locale codes like "en", "fr").
 * <p>
 * It provides common functionality such as the setting key and GUI refresh behavior.
 */
public interface LocalePlayerSetting extends McRPGSetting {

    /**
     * The setting key used to store locale settings for players.
     */
    NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "locale-setting");

    @NotNull
    @Override
    default NamespacedKey getSettingKey() {
        return SETTING_KEY;
    }

    @NotNull
    @Override
    LocaleSettingSlot getSettingSlot(@NotNull McRPGPlayer player);

    /**
     * Called when the locale setting changes. Refreshes the player's settings GUI
     * if they have one open so the new locale is immediately reflected.
     *
     * @param player     The player whose setting changed.
     * @param oldSetting The previous setting value.
     */
    @Override
    default void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        refreshPlayerSettingGui(player);
    }

    /**
     * Refreshes the player's settings GUI if they have one open.
     * This is called when the locale setting changes to update the GUI with the new language.
     *
     * @param player The player whose GUI should be refreshed.
     */
    static void refreshPlayerSettingGui(@NotNull CorePlayer player) {
        if (!(player instanceof McRPGPlayer mcRPGPlayer)) {
            return;
        }

        Optional<Player> bukkitPlayerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (bukkitPlayerOptional.isEmpty()) {
            return;
        }

        Player bukkitPlayer = bukkitPlayerOptional.get();

        // Check if the player has a GUI open
        Optional<Gui<McRPGPlayer>> guiOptional = McRPG.getInstance()
                .registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.GUI)
                .getOpenedGui(mcRPGPlayer);

        if (guiOptional.isPresent() && guiOptional.get() instanceof PlayerSettingGui) {
            // Create a new settings GUI and open it
            PlayerSettingGui newGui = new PlayerSettingGui(mcRPGPlayer);
            bukkitPlayer.openInventory(newGui.getInventory());
            McRPG.getInstance()
                    .registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI)
                    .trackPlayerGui(mcRPGPlayer, newGui);
        }
    }
}
