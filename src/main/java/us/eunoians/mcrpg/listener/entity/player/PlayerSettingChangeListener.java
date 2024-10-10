package us.eunoians.mcrpg.listener.entity.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.setting.PlayerSettingChangeEvent;

/**
 * This listener handles any callbacks needed for whenever a players
 * {@link us.eunoians.mcrpg.setting.PlayerSetting} changes.
 */
public class PlayerSettingChangeListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void handleSettingChange(@NotNull PlayerSettingChangeEvent event) {
        event.getNewSetting().onSettingChange(event.getMcRPGPlayer(), event.getOldSetting());
    }

}
