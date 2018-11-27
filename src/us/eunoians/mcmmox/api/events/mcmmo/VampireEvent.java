package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.swords.Vampire;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class VampireEvent extends AbilityActivateEvent {

  @Getter @Setter
  private int amountToHeal;

  public VampireEvent(McMMOPlayer user, Vampire vampire){
    super(vampire, user);
	int tier = vampire.getCurrentTier();
	this.amountToHeal = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("VampireConfig.Tier" + Methods.convertToNumeral(tier) + ".AmountToHeal");
	this.isCancelled = !vampire.isToggled();
  }
}
