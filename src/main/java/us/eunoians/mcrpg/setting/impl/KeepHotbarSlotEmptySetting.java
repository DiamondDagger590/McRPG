package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.setting.KeepHotbarSlotEmptySettingSlot;
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
 * This setting allows a player to dedicate a specific row on their
 * hotbar to prevent items from going into it. This allows the user
 * to create a designated slot for Unarmed abilities.
 */
public enum KeepHotbarSlotEmptySetting implements McRPGSetting, DenySlotSetting {

    /**
     * This setting means no slot is blocked.
     */
    DISABLED(-1),
    /**
     * This setting means the first hotbar slot is prevented from having items go into it.
     */
    SLOT_ONE(0),
    /**
     * This setting means the second hotbar slot is prevented from having items go into it.
     */
    SLOT_TWO(1),
    /**
     * This setting means the third hotbar slot is prevented from having items go into it.
     */
    SLOT_THREE(2),
    /**
     * This setting means the fourth hotbar slot is prevented from having items go into it.
     */
    SLOT_FOUR(3),
    /**
     * This setting means the fifth hotbar slot is prevented from having items go into it.
     */
    SLOT_FIVE(4),
    /**
     * This setting means the sixth hotbar slot is prevented from having items go into it.
     */
    SLOT_SIX(5),
    /**
     * This setting means the seventh hotbar slot is prevented from having items go into it.
     */
    SLOT_SEVEN(6),
    /**
     * This setting means the eighth hotbar slot is prevented from having items go into it.
     */
    SLOT_EIGHT(7),
    /**
     * This setting means the ninth hotbar slot is prevented from having items go into it.
     */
    SLOT_NINE(8),
    ;

    private static final LinkedNode<KeepHotbarSlotEmptySetting> FIRST_SETTING = new LinkedNode<>(DISABLED);
    private static final Map<KeepHotbarSlotEmptySetting, LinkedNode<KeepHotbarSlotEmptySetting>> SETTINGS = new HashMap<>();
    public static final NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "keep-hotbar-slot-empty-setting");

    static {
        SETTINGS.put(FIRST_SETTING.getNodeValue(), FIRST_SETTING);
        LinkedNode<KeepHotbarSlotEmptySetting> prev = FIRST_SETTING;
        for (KeepHotbarSlotEmptySetting setting : values()) {
            if (setting != FIRST_SETTING.getNodeValue()) {
                LinkedNode<KeepHotbarSlotEmptySetting> next = new LinkedNode<>(setting);
                prev.setNext(next);
                prev = next;
                SETTINGS.put(setting, prev);
            }
        }
        prev.setNext(FIRST_SETTING);
    }

    private final int slot;

    KeepHotbarSlotEmptySetting(int slot) {
        this.slot = slot;
    }

    /**
     * Gets the hotbar slot to prevent items from going into.
     * @return The hotbar slot to prevent items from going into. This will be a number from {@code -1} to {@code 8},
     * where {@code -1} represents {@link #DISABLED} while the other numbers will be the slot id.
     */
    public int getSlot() {
        return slot;
    }

    @NotNull
    @Override
    public List<Integer> getDeniedSlots(@NotNull McRPGPlayer player) {
        return slot == -1 ? List.of() : List.of(slot);
    }

    @NotNull
    @Override
    public NamespacedKey getSettingKey() {
        return SETTING_KEY;
    }

    @NotNull
    @Override
    public LinkedNode<KeepHotbarSlotEmptySetting> getFirstSetting() {
        return FIRST_SETTING;
    }

    @NotNull
    @Override
    public LinkedNode<KeepHotbarSlotEmptySetting> getNextSetting() {
        return SETTINGS.get(this).getNextNode();
    }

    @NotNull
    @Override
    public KeepHotbarSlotEmptySettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new KeepHotbarSlotEmptySettingSlot(player, this);
    }

    @Override
    public void onSettingChange(@NotNull McRPGPlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        // No-op
    }

    @NotNull
    @Override
    public Optional<KeepHotbarSlotEmptySetting> fromString(@NotNull String setting) {
        return Arrays.stream(values()).filter(keepHotbarSlotEmptySetting -> keepHotbarSlotEmptySetting.toString().equalsIgnoreCase(setting)).findFirst();
    }
}
