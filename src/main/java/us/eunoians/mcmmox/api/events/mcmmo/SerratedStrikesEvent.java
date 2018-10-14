package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.swords.SerratedStrikes;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class SerratedStrikesEvent extends AbilityActivateEvent{

  @Getter @Setter
  private double activationRateBoost;

  @Getter @Setter
  private int duration;

  @Getter @Setter
  private int cooldown;

  public SerratedStrikesEvent(McMMOPlayer user, SerratedStrikes serratedStrikes){
    super(serratedStrikes, user);
	int tier = serratedStrikes.getCurrentTier();
	this.isCancelled = serratedStrikes.isToggled();
	this.cooldown = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("SerratedStrikesConfig.Tier" + Methods.convertToNumeral(tier) + ".Cooldown");
	this.duration = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("SerratedStrikesConfig.Tier" + Methods.convertToNumeral(tier) + ".Duration");
	this.activationRateBoost = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getDouble("SerratedStrikesConfig.Tier" + Methods.convertToNumeral(tier) + ".ActivationBoost");
  }
}
