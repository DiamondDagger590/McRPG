package us.eunoians.mcmmox.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.api.events.mcmmo.ChestLinkEvent;
import us.eunoians.mcmmox.api.events.mcmmo.FakeChestOpenEvent;
import us.eunoians.mcmmox.api.events.mcmmo.PreChestLinkEvent;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.types.UnlockedAbilities;

public class McLink implements CommandExecutor, Listener {

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(!(sender instanceof Player)){
      sender.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Utility.OnlyPlayers")));
      return true;
	}
	else{
	  Player p = (Player) sender;
	  McMMOPlayer mp = PlayerManager.getPlayer(p.getUniqueId());
	  Block target = p.getTargetBlock(null, 100);
	  if(target.getType() != Material.CHEST){
	    p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.NotAChest")));
	  	return true;
	  }
	  else if(!mp.getAbilityLoadout().contains(UnlockedAbilities.REMOTE_TRANSFER) || !mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER).isToggled() || !UnlockedAbilities.REMOTE_TRANSFER.isEnabled()){
	    p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Utility.NotEnabledOrUnlocked")));
	    return true;
	  }
	  else{
		FakeChestOpenEvent openEvent = new FakeChestOpenEvent(p, target.getLocation());
		Bukkit.getPluginManager().callEvent(openEvent);
		return true;
	  }
	}
  }

  @EventHandler (priority = EventPriority.HIGHEST)
  public void fakeBlockListener(FakeChestOpenEvent event){
    McMMOPlayer mp = PlayerManager.getPlayer(event.getPlayer().getUniqueId());
    Location loc = event.getClickedBlock().getLocation();
    Player p = event.getPlayer();
	PreChestLinkEvent preChestLinkEvent = new PreChestLinkEvent(mp, (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER), loc);
	preChestLinkEvent.setCancelled(event.isCancelled());
	Bukkit.getPluginManager().callEvent(preChestLinkEvent);
	if(preChestLinkEvent.isCancelled()){
	  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + preChestLinkEvent.getErrorMessage()));
	}
	else{
	  ChestLinkEvent chestLinkEvent = new ChestLinkEvent(mp, (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER), loc);
	  Bukkit.getPluginManager().callEvent(chestLinkEvent);
	  mp.setLinkedToRemoteTransfer(true);
	  mp.setRemoteTransferLocation(loc);
	  p.sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() + Mcmmox.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.Linked")));
	}
	event.setCancelled(true);
  }
}
