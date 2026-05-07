package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.slot.RequireEmptyOffhandSettingSlot;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This setting previously allowed players to require an empty offhand in order to ready their abilities.
 *
 * @deprecated The ready-state activation model was removed in the mana ability system Phase 2.
 *             This setting no longer affects gameplay because abilities are activated exclusively
 *             via click-combo sequences. Scheduled for removal in a future release.
 */
@Deprecated(forRemoval = true)
public enum RequireEmptyOffhandSetting implements McRPGSetting {

    /**
     * This setting requires the player have an empty offhand in order to activate abilities.
     */
    ENABLED,
    /**
     * This setting allows players to ready abilities even if they don't have an empty offhand.
     */
    DISABLED,
    ;

    private static final LinkedNode<RequireEmptyOffhandSetting> FIRST_SETTING = new LinkedNode<>(ENABLED);
    private static final Map<RequireEmptyOffhandSetting, LinkedNode<RequireEmptyOffhandSetting>> SETTINGS = new HashMap<>();
    public static final NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "require-empty-offhand-setting");

    static {
        SETTINGS.put(FIRST_SETTING.getNodeValue(), FIRST_SETTING);
        LinkedNode<RequireEmptyOffhandSetting> prev = FIRST_SETTING;
        for (RequireEmptyOffhandSetting setting : values()) {
            if (setting != FIRST_SETTING.getNodeValue()) {
                LinkedNode<RequireEmptyOffhandSetting> next = new LinkedNode<>(setting);
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
    public LinkedNode<RequireEmptyOffhandSetting> getFirstSetting() {
        return FIRST_SETTING;
    }

    @NotNull
    @Override
    public LinkedNode<RequireEmptyOffhandSetting> getNextSetting() {
        return SETTINGS.get(this).getNextNode();
    }

    @NotNull
    @Override
    public RequireEmptyOffhandSettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new RequireEmptyOffhandSettingSlot(player, this);
    }

    @Override
    public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        // No-op
    }

    @NotNull
    @Override
    public Optional<RequireEmptyOffhandSetting> fromString(@NotNull String setting) {
        return Arrays.stream(values()).filter(requireEmptyOffhandSetting -> requireEmptyOffhandSetting.toString().equalsIgnoreCase(setting)).findFirst();
    }
}
