package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.slot.DisableTutorialSettingSlot;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Player setting controlling whether the tutorial quest chain is active.
 * Default is {@link #ENABLED}. Toggling to {@link #DISABLED} triggers a
 * confirmation GUI that, on confirm, abandons the active tutorial chain
 * and prevents future tutorial quest starts for this player.
 * <p>
 * Once disabled and confirmed, toggling back to {@link #ENABLED} does NOT
 * restart the chain or grant missed rewards — the chain remains
 * {@link us.eunoians.mcrpg.quest.chain.QuestChainState#ABANDONED}.
 * Only an admin {@code /mcrpg quest chain reset} can restore the tutorial.
 */
public enum DisableTutorialSetting implements McRPGSetting {

    /**
     * Tutorial is active. The player's tutorial chain auto-starts and progresses normally.
     */
    ENABLED,

    /**
     * Tutorial is disabled. No tutorial quests will start for this player.
     * This is a one-way door from the player GUI — only admin reset can reverse it.
     */
    DISABLED;

    private static final LinkedNode<DisableTutorialSetting> FIRST_SETTING = new LinkedNode<>(ENABLED);
    private static final Map<DisableTutorialSetting, LinkedNode<DisableTutorialSetting>> SETTINGS = new HashMap<>();
    public static final NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "disable-tutorial-setting");

    static {
        SETTINGS.put(FIRST_SETTING.getNodeValue(), FIRST_SETTING);
        LinkedNode<DisableTutorialSetting> prev = FIRST_SETTING;
        for (DisableTutorialSetting setting : values()) {
            if (setting != FIRST_SETTING.getNodeValue()) {
                LinkedNode<DisableTutorialSetting> next = new LinkedNode<>(setting);
                prev.setNext(next);
                prev = next;
                SETTINGS.put(setting, prev);
            }
        }
        prev.setNext(FIRST_SETTING);
    }

    /**
     * Returns the unique key identifying this setting.
     *
     * @return {@code mcrpg:disable-tutorial-setting}
     */
    @NotNull
    @Override
    public NamespacedKey getSettingKey() {
        return SETTING_KEY;
    }

    /**
     * Returns the first (default) setting node in the linked cycle.
     *
     * @return the {@link #ENABLED} node
     */
    @NotNull
    @Override
    public LinkedNode<DisableTutorialSetting> getFirstSetting() {
        return FIRST_SETTING;
    }

    /**
     * Returns the next setting in the linked cycle from this value.
     *
     * @return the next {@link DisableTutorialSetting} node
     */
    @NotNull
    @Override
    public LinkedNode<DisableTutorialSetting> getNextSetting() {
        return SETTINGS.get(this).getNextNode();
    }

    /**
     * Returns the GUI slot for this setting, bound to the given player.
     *
     * @param player the player the slot is created for
     * @return a new {@link DisableTutorialSettingSlot}
     */
    @NotNull
    @Override
    public DisableTutorialSettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new DisableTutorialSettingSlot(player, this);
    }

    /**
     * No-op — confirmation GUI handles the actual chain abandonment.
     * This callback fires after the setting value is persisted.
     *
     * @param player     the player whose setting changed
     * @param oldSetting the previous setting value, if any
     */
    @Override
    public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        // No-op — the DisableTutorialConfirmSlot performs chain abandonment directly.
    }

    /**
     * Parses a setting value from its string representation.
     *
     * @param setting the string to parse
     * @return the matching enum value, or empty if not recognized
     */
    @NotNull
    @Override
    public Optional<DisableTutorialSetting> fromString(@NotNull String setting) {
        return Arrays.stream(values())
                .filter(s -> s.toString().equalsIgnoreCase(setting))
                .findFirst();
    }

    /**
     * Returns {@code true} when tutorials are disabled for this player.
     *
     * @return {@code true} if this is {@link #DISABLED}
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }
}
