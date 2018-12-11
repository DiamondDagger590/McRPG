package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.swords.DeeperWound;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class DeeperWoundEvent extends AbilityActivateEvent{

  @Getter @Setter
  private int durationBoost;

  public DeeperWoundEvent(McRPGPlayer user, DeeperWound deeperWound){
	super(deeperWound, user);
	int tier = deeperWound.getCurrentTier();
	this.durationBoost = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("DeeperWoundConfig.Tier" + Methods.convertToNumeral(tier) + ".DurationBoost");
	this.isCancelled = !deeperWound.isToggled();
  }
}
