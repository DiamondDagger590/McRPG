package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.swords.TaintedBlade;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class TaintedBladeEvent extends AbilityActivateEvent {

  @Getter @Setter
  private int strengthDuration;

  @Getter @Setter
  private int resistanceDuration;

  @Getter @Setter
  private int hungerDuration;

  @Getter @Setter
  private int cooldown;

  public TaintedBladeEvent(McRPGPlayer user, TaintedBlade taintedBlade){
    super(taintedBlade, user);
	int tier = taintedBlade.getCurrentTier();
	this.isCancelled = !taintedBlade.isToggled();
	this.cooldown = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("TaintedBladeConfig.Tier" + Methods.convertToNumeral(tier) + ".Cooldown");
	this.strengthDuration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("TaintedBladeConfig.Tier" + Methods.convertToNumeral(tier) + ".StrengthDuration");
	this.resistanceDuration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("TaintedBladeConfig.Tier" + Methods.convertToNumeral(tier) + ".ResistanceDuration");
	this.hungerDuration = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("TaintedBladeConfig.Tier" + Methods.convertToNumeral(tier) + ".HungerDuration");
  }
}
