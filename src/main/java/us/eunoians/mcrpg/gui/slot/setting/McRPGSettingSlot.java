package us.eunoians.mcrpg.gui.slot.setting;

import com.diamonddagger590.mccore.gui.slot.setting.PlayerSettingSlot;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.PlayerSettingGui;

import java.util.Set;

public class McRPGSettingSlot<T extends PlayerSetting> extends PlayerSettingSlot<T, McRPGPlayer> {

    public McRPGSettingSlot(@NotNull McRPGPlayer corePlayer, @NotNull T setting) {
        super(corePlayer, setting);
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(PlayerSettingGui.class);
    }
}
