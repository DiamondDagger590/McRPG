package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.swords.Vampire;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McMMOPlayer;

public class VampireEvent extends AbilityActivateEvent {

  @Getter @Setter
  private int amountToHeal;

  public VampireEvent(McMMOPlayer user, Vampire vampire){
    super(vampire, user);
	int tier = vampire.getCurrentTier();
	this.amountToHeal = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("VampireConfig.Tier" + Methods.convertToNumeral(tier) + ".AmountToHeal");
	this.isCancelled = !vampire.isToggled();
  }
}
