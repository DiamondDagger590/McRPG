package us.eunoians.mcrpg.gui.home.slot;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.home.HomeGui;
import us.eunoians.mcrpg.gui.setting.PlayerSettingGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.Set;

/**
 * This slot is used in the {@link HomeGui} to open a settings gui when clicked.
 */
public class HomeSettingsSlot implements McRPGSlot {

    private final McRPGPlayer mcRPGPlayer;
    private final Player player;

    public HomeSettingsSlot(@NotNull McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        PlayerSettingGui playerSettingGui = new PlayerSettingGui(mcRPGPlayer);
        McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).trackPlayerGui(mcRPGPlayer, playerSettingGui);
        player.openInventory(playerSettingGui.getInventory());
        return true;
    }

    @NotNull
    @Override
    public ItemBuilder getItem(@NotNull McRPGPlayer mcRPGPlayer) {
        return ItemBuilder.from(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION).getLocalizedSection(mcRPGPlayer, LocalizationKey.HOME_GUI_SETTINGS_SLOT_DISPLAY_ITEM));
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(HomeGui.class);
    }
}
