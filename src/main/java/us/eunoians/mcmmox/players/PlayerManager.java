package us.eunoians.mcmmox.players;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.skills.Swords;
import us.eunoians.mcmmox.types.Skills;
import us.eunoians.mcmmox.types.UnlockedAbilities;
import us.eunoians.mcmmox.util.Parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {

  //Players who are currently logged on
  private static HashMap<UUID, McMMOPlayer> players = new HashMap<>();
  private static ArrayList<UUID> playersFrozen = new ArrayList<UUID>();
  private static Plugin plugin = Mcmmox.getInstance();

  public static void addMcMMOPlayer(Player player, boolean freeze) {
    if(players.containsKey(player.getUniqueId())){
      return;
    }
    UUID uuid = player.getUniqueId();
    if (freeze) {
      playersFrozen.add(uuid);
    }
    BukkitTask task = new BukkitRunnable() {
      public void run() {
        McMMOPlayer mp = new McMMOPlayer(uuid);
        players.put(uuid, mp);
        playersFrozen.remove(uuid);
      }
    }.runTaskAsynchronously(plugin);
  }

  public static boolean isPlayerFrozen(UUID uuid) {
    return playersFrozen.contains(uuid);
  }

  public static McMMOPlayer getPlayer(UUID uuid) {
    return players.get(uuid);
  }


}
