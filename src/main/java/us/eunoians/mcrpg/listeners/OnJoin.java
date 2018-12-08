package us.eunoians.mcrpg.listeners;

import lombok.var;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.eunoians.mcrpg.McRPG;

public class OnJoin implements Listener {
  private McRPG mcRPG;

  public OnJoin(McRPG mcRPG) {
    this.mcRPG = mcRPG;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onJoin(PlayerJoinEvent event) {
    Bukkit.getScheduler().runTaskAsynchronously(mcRPG, () -> {
      var newVersionAvailable = mcRPG.getPluginUpdater().needsUpdate();
      var version = mcRPG.getPluginUpdater().getVersion();
      var downloadURL = mcRPG.getPluginUpdater().getDownloadURL();
      if (!newVersionAvailable)
        return;
      String v = version.replaceAll("[a-zA-z: ]", "");
      if (event.getPlayer().isOp()) {
        try {
          if (Class.forName("org.spigotmc.SpigotConfig") != null) {
            BaseComponent[] textComponent = new ComponentBuilder("[")
                    .color(ChatColor.GOLD)
                    .append("McRPG")
                    .color(ChatColor.GREEN)
                    .append("]")
                    .color(ChatColor.GOLD)
                    .append(" A new update is available: ")
                    .color(ChatColor.AQUA)
                    .append(v)
                    .color(ChatColor.YELLOW)
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadURL))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click here to update").create()))
                    .create();
            event.getPlayer().spigot().sendMessage(textComponent);
          }
        } catch (ClassNotFoundException e) {
          String msg = org.bukkit.ChatColor.translateAlternateColorCodes('&', "&6[&aMcMMOX&6] &bA new update is available: &e" + v);
          event.getPlayer().sendRawMessage(msg);
        }
      }
    });
  }
}
