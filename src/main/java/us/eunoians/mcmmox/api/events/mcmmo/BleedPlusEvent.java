package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.swords.BleedPlus;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class BleedPlusEvent extends AbilityActivateEvent {

  @Getter @Setter
  private int damageBoost;

  public BleedPlusEvent(McMMOPlayer user, BleedPlus bleedPlus){
    super(bleedPlus, user);
	int tier = bleedPlus.getCurrentTier();
	this.damageBoost = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("Bleed+Config.Tier" + Methods.convertToNumeral(tier) + ".DamageBoost");
	this.isCancelled = !bleedPlus.isToggled();
  }
}
