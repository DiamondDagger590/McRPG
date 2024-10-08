package us.eunoians.mcrpg.setting;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.gui.slot.setting.ExperienceDisplaySettingSlot;
import us.eunoians.mcrpg.gui.slot.setting.PlayerSettingSlot;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ExperienceDisplaySetting implements PlayerSetting {

    BOSS_BAR,
    ACTION_BAR,
    ;

    private static final LinkedNode<ExperienceDisplaySetting> FIRST_SETTING = new LinkedNode<>(BOSS_BAR);
    private static final Map<ExperienceDisplaySetting, LinkedNode<ExperienceDisplaySetting>> SETTINGS = new HashMap<>();
    private static final NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "experience-display-setting");

    static {
        SETTINGS.put(FIRST_SETTING.getNodeValue(), FIRST_SETTING);
        LinkedNode<ExperienceDisplaySetting> prev = FIRST_SETTING;
        for (ExperienceDisplaySetting setting : values()) {
            if (setting != FIRST_SETTING.getNodeValue()) {
                LinkedNode<ExperienceDisplaySetting> next = new LinkedNode<>(setting);
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
    public LinkedNode<ExperienceDisplaySetting> getFirstSetting() {
        return FIRST_SETTING;
    }

    @NotNull
    @Override
    public LinkedNode<ExperienceDisplaySetting> getNextSetting() {
        return SETTINGS.get(this).getNextNode();
    }

    @NotNull
    @Override
    public PlayerSettingSlot<? extends PlayerSetting> getSettingSlot(@NotNull McRPGPlayer mcRPGPlayer) {
        return new ExperienceDisplaySettingSlot(mcRPGPlayer, this);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
