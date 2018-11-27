package us.eunoians.mcmmox.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.api.util.RemoteTransferTracker;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.types.UnlockedAbilities;

public class McUnlink implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] strings){
    if(sender instanceof Player){
	  McMMOPlayer mp = PlayerManager.getPlayer(((Player) sender).getUniqueId());
	  if(!mp.isLinkedToRemoteTransfer()){
		mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.IsNotLinked")));
		return true;
	  }
	  mp.setLinkedToRemoteTransfer(false);
	  ((RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER)).setLinkedChestLocation(null);
	  RemoteTransferTracker.removeLocation(mp.getUuid());
	  mp.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.Unlinked")));
	}
	return false;
  }
}
