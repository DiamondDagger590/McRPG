package us.eunoians.mcrpg.stat;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * Enum of keys for the built-in McRPG player stats.
 * <p>
 * The actual {@link PlayerStat} objects are created in
 * {@link us.eunoians.mcrpg.expansion.McRPGExpansion} where plugin context
 * (config access) is available. This enum provides stable compile-time keys
 * safe for use in switch statements and annotation values.
 */
public enum McRPGPlayerStat {

    /** Key for the Health stat. */
    HEALTH("health"),
    /** Key for the Mana stat. */
    MANA("mana");

    private final NamespacedKey key;

    @SuppressWarnings("deprecation")
    McRPGPlayerStat(@NotNull String keyString) {
        this.key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), keyString);
    }

    /**
     * @return The namespaced key for this built-in player stat.
     */
    @NotNull
    public NamespacedKey getKey() {
        return key;
    }
}
