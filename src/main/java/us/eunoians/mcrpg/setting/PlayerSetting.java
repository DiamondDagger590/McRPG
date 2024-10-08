package us.eunoians.mcrpg.setting;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.gui.slot.setting.PlayerSettingSlot;

public interface PlayerSetting extends McRPGContent {

    @NotNull
    NamespacedKey getSettingKey();

    @NotNull
    LinkedNode<? extends PlayerSetting> getFirstSetting();

    @NotNull
    LinkedNode<? extends PlayerSetting> getNextSetting();

    @NotNull
    PlayerSettingSlot<? extends PlayerSetting> getSettingSlot(@NotNull McRPGPlayer player);
}
