package us.eunoians.mcrpg.skill.experience.context;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Locale;

/**
 * Built-in {@link GainReason} implementations for McRPG.
 * <p>
 * Third-party plugins can define their own {@link GainReason} implementations
 * as enums or classes.
 */
public enum McRPGGainReason implements GainReason {

    /** XP from breaking a block (mining, woodcutting, herbalism). */
    BLOCK_BREAK("Block Break"),

    /** XP from dealing damage (swords, etc.). */
    ENTITY_DAMAGE("Entity Damage"),

    /** XP from redeeming redeemable experience. */
    REDEEM("Redeem"),

    /** XP granted via admin command. */
    COMMAND("Command"),

    /** Any other source (fallback). */
    OTHER("Other");

    private final NamespacedKey key;

    /**
     * A plain-English display name used for internal logging and admin-facing UIs (e.g.,
     * debug commands, console output). This is not player-facing chat text, so it does not
     * need to go through the localization system. If gain reasons are ever surfaced in
     * player-visible messages, those messages should use {@link us.eunoians.mcrpg.configuration.file.localization.LocalizationKey}
     * entries that reference the reason by its {@link #key} rather than displaying this field directly.
     */
    private final String displayName;

    @SuppressWarnings("deprecation") // NamespacedKey(String, String) — no Plugin instance available in enum constructor
    McRPGGainReason(@NotNull String displayName) {
        this.key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), name().toLowerCase(Locale.ROOT));
        this.displayName = displayName;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public String getDisplayName() {
        return displayName;
    }
}
