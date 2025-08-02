package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.setting.PlayerSetting;
import com.diamonddagger590.mccore.util.LinkedNode;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.slot.setting.LocaleSettingSlot;
import us.eunoians.mcrpg.localization.NativeLocale;
import us.eunoians.mcrpg.setting.McRPGSetting;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This setting allows players to change what localization is shown to them.
 * <p>
 * The default order of localization goes (on best effort basis): locale setting -> client locale -> server default -> english.
 * If any localization is missing a string, then it goes to the next locale in the chain until it eventually defaults to english.
 */
public enum LocaleSetting implements McRPGSetting {

    CLIENT_LOCALE(),
    SERVER_LOCALE(),
    ENGLISH(NativeLocale.ENGLISH),
    ;

    private static final LinkedNode<LocaleSetting> FIRST_SETTING = new LinkedNode<>(CLIENT_LOCALE);
    private static final Map<LocaleSetting, LinkedNode<LocaleSetting>> SETTINGS = new HashMap<>();
    public static final NamespacedKey SETTING_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "locale-setting");

    static {
        SETTINGS.put(FIRST_SETTING.getNodeValue(), FIRST_SETTING);
        LinkedNode<LocaleSetting> prev = FIRST_SETTING;
        for (LocaleSetting localeSetting : LocaleSetting.values()) {
            if (localeSetting != FIRST_SETTING.getNodeValue()) {
                LinkedNode<LocaleSetting> next = new LinkedNode<>(localeSetting);
                prev.setNext(next);
                prev = next;
                SETTINGS.put(localeSetting, prev);
            }
        }
        prev.setNext(FIRST_SETTING);
    }

    private final NativeLocale locale;

    LocaleSetting() {
        this.locale = null;
    }

    LocaleSetting(@NotNull NativeLocale locale) {
        this.locale = locale;
    }

    /**
     * Gets the {@link NativeLocale} represented by this setting.
     * <p>
     * Some settings do not represent a specific locale, only ones that
     * map to a {@link NativeLocale}.
     *
     * @return An {@link Optional} containing the {@link NativeLocale} represented by this setting. The optional
     * will be empty if the setting does not represent a specific locale.
     */
    @NotNull
    public Optional<NativeLocale> getNativeLocale() {
        return Optional.ofNullable(locale);
    }

    @NotNull
    @Override
    public NamespacedKey getSettingKey() {
        return SETTING_KEY;
    }

    @NotNull
    @Override
    public LinkedNode<? extends PlayerSetting> getFirstSetting() {
        return FIRST_SETTING;
    }

    @NotNull
    @Override
    public LinkedNode<? extends PlayerSetting> getNextSetting() {
        return SETTINGS.get(this).getNextNode();
    }

    @NotNull
    @Override
    public LocaleSettingSlot getSettingSlot(@NotNull McRPGPlayer player) {
        return new LocaleSettingSlot(player, this);
    }

    @Override
    public void onSettingChange(@NotNull CorePlayer player, @NotNull Optional<PlayerSetting> oldSetting) {
        // No callback needed
    }

    @NotNull
    @Override
    public Optional<? extends PlayerSetting> fromString(@NotNull String setting) {
        return Arrays.stream(values()).filter(localeSetting -> localeSetting.toString().equalsIgnoreCase(setting)).findFirst();
    }
}
