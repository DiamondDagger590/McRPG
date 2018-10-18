package us.eunoians.mcmmox.events.vanilla;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import us.eunoians.mcmmox.abilities.mining.RemoteTransfer;
import us.eunoians.mcmmox.players.McMMOPlayer;
import us.eunoians.mcmmox.players.PlayerManager;
import us.eunoians.mcmmox.types.UnlockedAbilities;

public class BreakEvent implements Listener {

  @EventHandler
  public void breakEvent(BlockBreakEvent event){
    if(!event.isCancelled()){
      Player p = event.getPlayer();
	  McMMOPlayer mp = PlayerManager.getPlayer(((Player) p).getUniqueId());
	  if(mp.getAbilityLoadout().contains(UnlockedAbilities.REMOTE_TRANSFER) && mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER).isToggled() && UnlockedAbilities.REMOTE_TRANSFER.isEnabled()){
		RemoteTransfer remoteTransfer = (RemoteTransfer) mp.getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);

	  }
	}
  }
}
