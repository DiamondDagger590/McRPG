package us.eunoians.mcrpg.external.mythicmobs;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * A hook for containing all code related to
 * <a href="https://mythiccraft.io/index.php?pages/official-mythicmobs/">MythicMobs</a>
 * that this plugin needs to support it.
 * <p>
 * MythicMobs is used by the fishing skill to spawn custom mobs when a player catches a fish.
 * McRPG registers a custom drop type ({@code mcrpg_skillbook}) via {@link MythicMobsListener}
 * and bridges MythicMobs spawn/death events into McRPG's own event system.
 */
public class MythicMobsHook extends PluginHook<McRPG> {

    public MythicMobsHook(@NotNull McRPG plugin) {
        super(plugin);
    }
}
