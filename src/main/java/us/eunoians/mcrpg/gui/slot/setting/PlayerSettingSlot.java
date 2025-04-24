package us.eunoians.mcrpg.gui.slot.setting;

import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.PlayerSettingGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Optional;
import java.util.Set;

/**
 * A slot that displays a {@link PlayerSetting} and allows a player to click through all the different values
 * for the given setting.
 *
 * @param <T> The {@link PlayerSetting} represented by this slot.
 */
public abstract class PlayerSettingSlot<T extends PlayerSetting> extends McRPGSlot {

    protected final McRPGPlayer mcRPGPlayer;
    protected final Player player;
    private final T setting;

    public PlayerSettingSlot(@NotNull final McRPGPlayer mcRPGPlayer, @NotNull final T setting) {
        this.mcRPGPlayer = mcRPGPlayer;
        Optional<Player> playerOptional = mcRPGPlayer.getAsBukkitPlayer();
        if (playerOptional.isEmpty()) {
            throw new CorePlayerOfflineException(mcRPGPlayer);
        }
        this.player = playerOptional.get();
        this.setting = setting;
    }

    @Override
    public boolean onClick(@NotNull McRPGPlayer mcRPGPlayer, @NotNull ClickType clickType) {
        var guiOptional = mcRPGPlayer.getPlugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GUI).getOpenedGui(mcRPGPlayer);
        guiOptional.ifPresent(gui -> {
            mcRPGPlayer.setPlayerSetting(setting.getNextSetting().getNodeValue());
            gui.refreshGUI();
        });
        return true;
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(PlayerSettingGui.class);
    }

    /**
     * Gets the {@link PlayerSetting} represented by this slot.
     *
     * @return The {@link PlayerSetting} represented by this slot.
     */
    @NotNull
    public T getSetting() {
        return setting;
    }
}
