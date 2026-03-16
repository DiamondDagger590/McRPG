package us.eunoians.mcrpg.external.mythicmobs;

import org.bukkit.NamespacedKey;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * {@link NamespacedKey} constants used to tag MythicMob entities spawned by the fishing skill.
 * <p>
 * These keys are written to an entity's {@link org.bukkit.persistence.PersistentDataContainer}
 * when the fishing skill spawns a mob via MythicMobs API, and are read by {@link MythicMobsListener}
 * to determine whether a MythicMob spawn/death event should be bridged into McRPG's event system.
 */
public final class FishingMobKeys {

    /**
     * Boolean key indicating that this entity was spawned by the McRPG fishing skill.
     */
    public static final NamespacedKey FISHING_MOB_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "fishing_mob");

    /**
     * String key containing the UUID of the player (angler) who triggered the mob spawn.
     */
    public static final NamespacedKey ANGLER_UUID_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "fishing_mob_angler");

    private FishingMobKeys() {
        // Utility class
    }
}
