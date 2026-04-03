package us.eunoians.mcrpg.external.mythicmobs;

import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.Optional;

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

    /**
     * Spawns a MythicMobs mob at the given location.
     *
     * @param mythicMobsId the MM internal type ID
     * @param location     the Bukkit location to spawn at
     * @param mobLevel     the MM mob level
     * @return the spawned entity, or empty if the type ID is not registered in MM
     */
    @NotNull
    public Optional<Entity> spawnMob(@NotNull String mythicMobsId,
                                      @NotNull Location location,
                                      double mobLevel) {
        Optional<MythicMob> mythicMob = MythicBukkit.inst().getMobManager().getMythicMob(mythicMobsId);
        if (mythicMob.isEmpty()) {
            getPlugin().getLogger().warning("MythicMob type '" + mythicMobsId
                    + "' not found in MythicMobs registry.");
            return Optional.empty();
        }

        ActiveMob activeMob = mythicMob.get().spawn(BukkitAdapter.adapt(location), mobLevel);
        return Optional.of(activeMob.getEntity().getBukkitEntity());
    }

    /**
     * Checks whether a MythicMobs mob type ID is registered.
     *
     * @param mythicMobsId the MM internal type ID
     * @return true if the type exists in the MM registry
     */
    public boolean isMobTypeRegistered(@NotNull String mythicMobsId) {
        return MythicBukkit.inst().getMobManager().getMythicMob(mythicMobsId).isPresent();
    }
}
