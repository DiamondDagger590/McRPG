package us.eunoians.mcrpg.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.swords.SerratedStrikes;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McMMOPlayer;

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
	this.isCancelled = !serratedStrikes.isToggled();
	this.cooldown = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("SerratedStrikesConfig.Tier" + Methods.convertToNumeral(tier) + ".Cooldown");
	this.duration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("SerratedStrikesConfig.Tier" + Methods.convertToNumeral(tier) + ".Duration");
	this.activationRateBoost = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getDouble("SerratedStrikesConfig.Tier" + Methods.convertToNumeral(tier) + ".ActivationBoost");
  }
}
