package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.slot.DisableBonusExperienceConsumptionSettingSlot;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A player setting that allows players to disable the consumption of their
 * rested and boosted experience. When enabled, players will not consume
 * their bonus experience pools but also won't receive the bonus experience multiplier.
 */
public enum DisableBonusExperienceConsumptionSetting implements McRPGSetting {

    /**
     * This setting will prevent rested and boosted experience from being consumed.
     * Players will not receive bonus experience while this is enabled.
     */
    ENABLED,
    /**
     * This setting allows rested and boosted experience to be consumed normally,
     * providing bonus experience multipliers.
     */
    DISABLED,
    ;

    private static final LinkedNode<DisableBonusExperienceConsumptionSetting> FIRST_SETTING = new LinkedNode<>(DISABLED);
    private static final Map<DisableBonusExperienceConsumptionSetting, LinkedNode<DisableBonusExperienceConsumptionSetting>> SETTINGS = new HashMap<>();
    public static final NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "disable-bonus-experience-consumption-setting");

    static {
        SETTINGS.put(FIRST_SETTING.getNodeValue(), FIRST_SETTING);
        LinkedNode<DisableBonusExperienceConsumptionSetting> prev = FIRST_SETTING;
        for (DisableBonusExperienceConsumptionSetting setting : values()) {
            if (setting != FIRST_SETTING.getNodeValue()) {
                LinkedNode<DisableBonusExperienceConsumptionSetting> next = new LinkedNode<>(setting);
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
    public LinkedNode<DisableBonusExperienceConsumptionSetting> getFirstSetting() {
        return FIRST_SETTING;
    }

    @NotNull
    @Override
    public LinkedNode<DisableBonusExperienceConsumptionSetting> getNextSetting() {
        return SETTINGS.get(this).getNextNode();
    }

    @NotNull
    @Override
    public DisableBonusExperienceConsumptionSettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new DisableBonusExperienceConsumptionSettingSlot(player, this);
    }

    @Override
    public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        // No-op
    }

    @NotNull
    @Override
    public Optional<DisableBonusExperienceConsumptionSetting> fromString(@NotNull String setting) {
        return Arrays.stream(values()).filter(disableBonusExperienceConsumptionSetting -> disableBonusExperienceConsumptionSetting.toString().equalsIgnoreCase(setting)).findFirst();
    }
}
