package us.eunoians.mcrpg.commands;

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
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.mining.RemoteTransfer;
import us.eunoians.mcrpg.api.events.mcrpg.mining.ChestLinkEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.FakeChestOpenEvent;
import us.eunoians.mcrpg.api.events.mcrpg.mining.PreChestLinkEvent;
import us.eunoians.mcrpg.api.exceptions.McRPGPlayerNotFoundException;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RemoteTransferTracker;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

public class McLink implements CommandExecutor, Listener {

  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(!(sender instanceof Player)) {
      sender.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Utility.OnlyPlayers")));
      return true;
    }
    else {
      Player p = (Player) sender;
      if(PlayerManager.isPlayerFrozen(p.getUniqueId())){
        return true;
      }
      //Disabled Worlds
      String world = p.getWorld().getName();
      if(McRPG.getInstance().getConfig().contains("Configuration.DisabledWorlds") &&
              McRPG.getInstance().getConfig().getStringList("Configuration.DisabledWorlds").contains(world)) {
        return true;
      }
      McRPGPlayer mp;
      try{
        mp = PlayerManager.getPlayer(p.getUniqueId());
      }
      catch(McRPGPlayerNotFoundException exception){
        return true;
      }
      Block target = p.getTargetBlock(null, 100);
      if(target.getType() != Material.CHEST) {
        p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.NotAChest")));
        return true;
      }
      else if(!mp.getAbilityLoadout().contains(UnlockedAbilities.REMOTE_TRANSFER) || !mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER).isToggled() || !UnlockedAbilities.REMOTE_TRANSFER.isEnabled()) {
        p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Utility.NotEnabledOrUnlocked")));
        return true;
      }
      else {
        FakeChestOpenEvent openEvent = new FakeChestOpenEvent(p, target.getLocation());
        Bukkit.getPluginManager().callEvent(openEvent);
        return true;
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void fakeBlockListener(FakeChestOpenEvent event) {
    McRPGPlayer mp;
    try{
      mp = PlayerManager.getPlayer(event.getPlayer().getUniqueId());
    }
    catch(McRPGPlayerNotFoundException exception){
      return;
    }
    Location loc = event.getClickedBlock().getLocation();
    Player p = event.getPlayer();
    PreChestLinkEvent preChestLinkEvent = new PreChestLinkEvent(mp, (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER), loc);
    preChestLinkEvent.setCancelled(event.isCancelled());
    Bukkit.getPluginManager().callEvent(preChestLinkEvent);
    if(preChestLinkEvent.isCancelled()) {
      p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + preChestLinkEvent.getErrorMessage()));
    }
    else {
      ChestLinkEvent chestLinkEvent = new ChestLinkEvent(mp, (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER), loc);
      Bukkit.getPluginManager().callEvent(chestLinkEvent);
      mp.setLinkedToRemoteTransfer(true);
      ((RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER)).setLinkedChestLocation(loc);
      RemoteTransferTracker.addLocation(p.getUniqueId(), loc);
      p.sendMessage(Methods.color(McRPG.getInstance().getPluginPrefix() + McRPG.getInstance().getLangFile().getString("Messages.Abilities.RemoteTransfer.Linked")));
    }
    event.setCancelled(true);
  }
}
