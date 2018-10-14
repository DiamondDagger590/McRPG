package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.swords.TaintedBlade;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class TaintedBladeEvent extends AbilityActivateEvent {

  @Getter @Setter
  private int strengthDuration;

  @Getter @Setter
  private int resistanceDuration;

  @Getter @Setter
  private int hungerDuration;

  @Getter @Setter
  private int cooldown;
  public TaintedBladeEvent(McMMOPlayer user, TaintedBlade taintedBlade){
    super(taintedBlade, user);
	int tier = taintedBlade.getCurrentTier();
	this.isCancelled = taintedBlade.isToggled();
	this.cooldown = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("TaintedBladeConfig.Tier" + Methods.convertToNumeral(tier) + ".Cooldown");
	this.strengthDuration = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("TaintedBladeConfig.Tier" + Methods.convertToNumeral(tier) + ".StrengthDuration");
	this.resistanceDuration = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("TaintedBladeConfig.Tier" + Methods.convertToNumeral(tier) + ".ResistanceDuration");
	this.hungerDuration = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("TaintedBladeConfig.Tier" + Methods.convertToNumeral(tier) + ".HungerDuration");
  }
}
