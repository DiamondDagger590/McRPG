package us.eunoians.mcrpg.skill.experience.context;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.util.McRPGMethods;

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
    private final String displayName;

    McRPGGainReason(@NotNull String displayName) {
        this.key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), name().toLowerCase());
        this.displayName = displayName;
    }

    @Override
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return displayName;
    }
}
