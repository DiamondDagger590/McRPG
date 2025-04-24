package us.eunoians.mcrpg.listener.entity;

import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;

import static us.eunoians.mcrpg.util.EntityKeys.SPAWN_REASON_EXPERIENCE_MODIFIER_KEY;

/**
 * This listener handles modifying the experience gained when attacking an {@link org.bukkit.entity.Entity} that
 * spawned with a specific {@link org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason}.
 */
public class EntitySpawnListener implements Listener {

    private static final Map<CreatureSpawnEvent.SpawnReason, Route> SPAWN_REASON_ROUTE_MAP = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (!SPAWN_REASON_ROUTE_MAP.containsKey(reason)) {
            SPAWN_REASON_ROUTE_MAP.put(reason, Route.addTo(MainConfigFile.MODIFY_MOB_SPAWN_EXPERIENCE_CONFIGURATION, reason.toString()));
        }
        double experienceModifier = McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MAIN_CONFIG).getDouble(SPAWN_REASON_ROUTE_MAP.get(reason), 1.0);
        // We make a base assumption that the multiplier from this is always 1.0, so if it isn't anything different, then there's no reason to store it.
        if (experienceModifier != 1.0) {
            event.getEntity().getPersistentDataContainer().set(SPAWN_REASON_EXPERIENCE_MODIFIER_KEY, PersistentDataType.DOUBLE, experienceModifier);
        }
    }
}
