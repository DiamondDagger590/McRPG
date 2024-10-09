package us.eunoians.mcrpg.event.setting;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.entity.player.McRPGPlayerEvent;
import us.eunoians.mcrpg.setting.PlayerSetting;

import java.util.Optional;

public class PlayerSettingChangeEvent extends McRPGPlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    private final PlayerSetting oldSetting;
    private final PlayerSetting newSetting;

    public PlayerSettingChangeEvent(@NotNull McRPGPlayer mcRPGPlayer, @Nullable PlayerSetting oldSetting, @NotNull PlayerSetting newSetting) {
        super(mcRPGPlayer);
        this.oldSetting = oldSetting;
        this.newSetting = newSetting;
    }

    @NotNull
    public Optional<PlayerSetting> getOldSetting() {
        return Optional.ofNullable(oldSetting);
    }

    @NotNull
    public PlayerSetting getNewSetting() {
        return newSetting;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
