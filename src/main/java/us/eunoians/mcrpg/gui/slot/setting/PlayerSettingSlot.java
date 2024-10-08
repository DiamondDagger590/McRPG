package us.eunoians.mcrpg.gui.slot.setting;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.exception.CorePlayerOfflineException;
import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.gui.slot.Slot;
import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.PlayerSettingGui;
import us.eunoians.mcrpg.setting.PlayerSetting;

import java.util.Optional;
import java.util.Set;

public abstract class PlayerSettingSlot<T extends PlayerSetting> extends Slot {

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
    public boolean onClick(@NotNull CorePlayer corePlayer, @NotNull ClickType clickType) {
        var guiOptional = CorePlugin.getInstance().getGuiTracker().getOpenedGui(corePlayer);
        guiOptional.ifPresent(gui -> {
            mcRPGPlayer.setPlayerSetting(setting.getNextSetting().getNodeValue());
            gui.refreshGUI();
        });
        return true;
    }

    @Override
    public Set<Class<? extends Gui>> getValidGuiTypes() {
        return Set.of(PlayerSettingGui.class);
    }

    @NotNull
    public T getSetting() {
        return setting;
    }
}
