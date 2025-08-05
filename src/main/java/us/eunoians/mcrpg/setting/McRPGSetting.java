package us.eunoians.mcrpg.setting;

import com.diamonddagger590.mccore.setting.PlayerSetting;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.expansion.content.McRPGContent;
import us.eunoians.mcrpg.gui.setting.slot.McRPGSettingSlot;

import java.util.Optional;

/**
 * This setting represents a setting that belongs to the {@link McRPGExpansion}.
 */
public interface McRPGSetting extends PlayerSetting, McRPGContent {

    @NotNull
    default Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    /**
     * Gets the {@link McRPGSettingSlot} used to display this slot to players and
     * allow them to cycle through the setting's options.
     *
     * @param player The player to create the slot for.
     * @return The {@link McRPGSettingSlot} used to display this slot to players and
     * allow them to cycle through the setting's options.
     */
    @NotNull
    McRPGSettingSlot<? extends McRPGSetting> getSettingSlot(@NotNull McRPGPlayer player);
}
