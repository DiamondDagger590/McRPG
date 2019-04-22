package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;

public class EntityDeathEvent implements Listener {

  @EventHandler
  public void deathEvent(org.bukkit.event.entity.EntityDeathEvent e){
    LivingEntity entity = e.getEntity();
    if(entity.getKiller() != null && entity.hasMetadata("GuardianExp")){
      int exp = entity.getMetadata("GuardianExp").get(0).asInt();
      Player p = entity.getKiller();
      McRPGPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
      mp.giveRedeemableExp(exp);
      p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Commands.Utility.ObtainedRedeemableExp")
      .replace("%Amount%", Integer.toString(exp))));
    }
  }
}
