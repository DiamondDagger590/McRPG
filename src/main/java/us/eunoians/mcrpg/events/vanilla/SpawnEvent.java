package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;

public class SpawnEvent implements Listener {

  @EventHandler
  public void spawnEvent(CreatureSpawnEvent e) {
    //Disabled Worlds
    if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
         McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(e.getEntity().getWorld().getName())) {
      return;
    }
    if((e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER) && McRPG.getInstance().getConfig().contains("Configuration.ModifySpawnExp.MobsFromSpawner")) {
      Methods.setMetadata(e.getEntity(), "ExpModifier", McRPG.getInstance().getConfig().getDouble("Configuration.ModifySpawnExp.MobsFromSpawner"));
    }
    if((e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG
    || e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) && McRPG.getInstance().getConfig().contains("Configuration.ModifySpawnExp.MobsFromEggs")) {
      Methods.setMetadata(e.getEntity(), "ExpModifier", McRPG.getInstance().getConfig().getDouble("Configuration.ModifySpawnExp.MobsFromEggs"));
    }
  }
}
