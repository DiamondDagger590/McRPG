package us.eunoians.mcrpg.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RemoteTransferTracker;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class McUnlink implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
    if(sender instanceof Player) {
      if(PlayerManager.isPlayerFrozen(((Player) sender).getUniqueId())){
        return true;
      }
      //Disabled Worlds
      String world = ((Player) sender).getWorld().getName();
      if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
              McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world)) {
        return true;
      }

      McRPGPlayer mp = PlayerManager.getPlayer(((Player) sender).getUniqueId());
      if(!mp.isLinkedToRemoteTransfer()) {
        mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.IsNotLinked")));
        return true;
      }
      mp.setLinkedToRemoteTransfer(false);
      ((RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER)).setLinkedChestLocation(null);
      RemoteTransferTracker.removeLocation(mp.getUuid());
      mp.getPlayer().sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.Unlinked")));
    }
    return false;
  }
}
