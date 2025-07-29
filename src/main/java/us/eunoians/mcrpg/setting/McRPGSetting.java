package us.eunoians.mcrpg.setting;

import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.gui.slot.setting.McRPGSettingSlot;

import java.util.Optional;

/**
 * This setting represents a setting that belongs to the {@link McRPGExpansion}.
 */
public interface McRPGSetting extends PlayerSetting, McRPGContent {

    @NotNull
    default Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    @NotNull
    McRPGSettingSlot<? extends McRPGSetting> getSettingSlot(@NotNull McRPGPlayer player);
}
