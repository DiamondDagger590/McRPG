package us.eunoians.mcrpg.listener.entity.player;

import com.diamonddagger590.mccore.event.setting.setting.PlayerSettingChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * This listener handles any callbacks needed for whenever a players
 * {@link com.diamonddagger590.mccore.setting.PlayerSetting} changes.
 */
public class PlayerSettingChangeListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleSettingChange(@NotNull PlayerSettingChangeEvent event) {
        event.getNewSetting().onSettingChange(event.getCorePlayer(), event.getOldSetting());
    }

}
