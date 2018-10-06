package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Setter;
import lombok.Getter;
import org.bukkit.entity.Entity;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.DeeperWound;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class DeeperWoundEvent extends AbilityActivateEvent{

  @Getter @Setter
  private int durationBoost;

  public DeeperWoundEvent(McMMOPlayer user, Entity target, DeeperWound deeperWound){
	super(deeperWound, user);
	int tier = deeperWound.getCurrentTier();
	this.durationBoost = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("DeeperWoundConfig.Tier" + Methods.convertToNumeral(tier) + ".DurationBoost");
	this.isCancelled = deeperWound.isToggled();
  }
}
