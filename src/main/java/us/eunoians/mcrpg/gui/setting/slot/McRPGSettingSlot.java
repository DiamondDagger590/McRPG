package us.eunoians.mcrpg.gui.setting.slot;

import com.diamonddagger590.mccore.gui.slot.setting.PlayerSettingSlot;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.PlayerSettingGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;

import java.util.Set;

public class McRPGSettingSlot<T extends PlayerSetting> extends PlayerSettingSlot<T, McRPGPlayer> implements McRPGSlot {

    public McRPGSettingSlot(@NotNull McRPGPlayer corePlayer, @NotNull T setting) {
        super(corePlayer, setting);
    }

    @Override
    public Set<Class<?>> getValidGuiTypes() {
        return Set.of(PlayerSettingGui.class);
    }
}
