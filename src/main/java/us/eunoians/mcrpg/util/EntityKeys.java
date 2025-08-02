package us.eunoians.mcrpg.util;

import org.bukkit.NamespacedKey;
import us.eunoians.mcrpg.McRPG;

/**
 * A collection of all {@link NamespacedKey}s that can be given to {@link org.bukkit.entity.Entity Entities}
 * through McRPG.
 */
public final class EntityKeys {

    public static final NamespacedKey SPAWN_REASON_EXPERIENCE_MODIFIER_KEY = new NamespacedKey(McRPG.getInstance(), "spawn_reason_experience_modifier");

}
