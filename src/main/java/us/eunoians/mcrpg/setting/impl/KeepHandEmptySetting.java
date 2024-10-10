package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.setting.KeepHandEmptySettingSlot;
import us.eunoians.mcrpg.setting.DenySlotSetting;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.setting.PlayerSetting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A player setting that allows players to keep their held item slot empty
 * in order to be able to more reliably use Unarmed abilities.
 */
public enum KeepHandEmptySetting implements DenySlotSetting, McRPGSetting {

    /**
     * This setting will prevent items from going into the users held item slot.
     */
    ENABLED,
    /**
     * This setting won't prevent items from going into the users held item slot.
     */
    DISABLED,
    ;

    private static final LinkedNode<KeepHandEmptySetting> FIRST_SETTING = new LinkedNode<>(DISABLED);
    private static final Map<KeepHandEmptySetting, LinkedNode<KeepHandEmptySetting>> SETTINGS = new HashMap<>();
    public static final NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "keep-hand-empty-setting");

    static {
        SETTINGS.put(FIRST_SETTING.getNodeValue(), FIRST_SETTING);
        LinkedNode<KeepHandEmptySetting> prev = FIRST_SETTING;
        for (KeepHandEmptySetting setting : values()) {
            if (setting != FIRST_SETTING.getNodeValue()) {
                LinkedNode<KeepHandEmptySetting> next = new LinkedNode<>(setting);
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
    public LinkedNode<KeepHandEmptySetting> getFirstSetting() {
        return FIRST_SETTING;
    }

    @NotNull
    @Override
    public LinkedNode<KeepHandEmptySetting> getNextSetting() {
        return SETTINGS.get(this).getNextNode();
    }

    @NotNull
    @Override
    public KeepHandEmptySettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new KeepHandEmptySettingSlot(player, this);
    }

    @Override
    public void onSettingChange(@NotNull McRPGPlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        // No-op
    }

    @NotNull
    @Override
    public Optional<KeepHandEmptySetting> fromString(@NotNull String setting) {
        return Arrays.stream(values()).filter(keepHandEmptySetting -> keepHandEmptySetting.toString().equalsIgnoreCase(setting)).findFirst();
    }

    @NotNull
    @Override
    public List<Integer> getDeniedSlots(@NotNull McRPGPlayer player) {
        var playerOptional = player.getAsBukkitPlayer();
        if (playerOptional.isPresent() && this == ENABLED) {
            return List.of(playerOptional.get().getInventory().getHeldItemSlot());
        }
        return List.of();
    }
}
