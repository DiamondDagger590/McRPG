package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.slot.QuestProgressNotificationSettingSlot;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A player setting that controls whether the player receives chat notifications
 * when they cross configured percentage thresholds on quest objective progress.
 * <p>
 * Defaults to {@link #ENABLED}. Players can toggle this off in the settings GUI.
 */
public enum QuestProgressNotificationSetting implements McRPGSetting {

    /**
     * The player will receive a chat notification when they reach a configured
     * progress threshold on a quest objective.
     */
    ENABLED,

    /**
     * The player will not receive any quest objective progress notifications.
     */
    DISABLED,
    ;

    private static final LinkedNode<QuestProgressNotificationSetting> FIRST_SETTING = new LinkedNode<>(ENABLED);
    private static final Map<QuestProgressNotificationSetting, LinkedNode<QuestProgressNotificationSetting>> SETTINGS = new HashMap<>();
    public static final NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "quest-progress-notification-setting");

    static {
        SETTINGS.put(FIRST_SETTING.getNodeValue(), FIRST_SETTING);
        LinkedNode<QuestProgressNotificationSetting> prev = FIRST_SETTING;
        for (QuestProgressNotificationSetting setting : values()) {
            if (setting != FIRST_SETTING.getNodeValue()) {
                LinkedNode<QuestProgressNotificationSetting> next = new LinkedNode<>(setting);
                prev.setNext(next);
                prev = next;
                SETTINGS.put(setting, prev);
            }
        }
        prev.setNext(FIRST_SETTING);
    }

    @NotNull
    @Override
    public NamespacedKey getSettingKey() {
        return SETTING_KEY;
    }

    @NotNull
    @Override
    public LinkedNode<QuestProgressNotificationSetting> getFirstSetting() {
        return FIRST_SETTING;
    }

    @NotNull
    @Override
    public LinkedNode<QuestProgressNotificationSetting> getNextSetting() {
        return SETTINGS.get(this).getNextNode();
    }

    @NotNull
    @Override
    public QuestProgressNotificationSettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new QuestProgressNotificationSettingSlot(player, this);
    }

    @Override
    public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        // No-op
    }

    @NotNull
    @Override
    public Optional<QuestProgressNotificationSetting> fromString(@NotNull String setting) {
        return Arrays.stream(values())
                .filter(s -> s.toString().equalsIgnoreCase(setting))
                .findFirst();
    }

    /**
     * Returns {@code true} if notifications are currently enabled.
     *
     * @return {@code true} when this is {@link #ENABLED}
     */
    public boolean isEnabled() {
        return this == ENABLED;
    }
}
