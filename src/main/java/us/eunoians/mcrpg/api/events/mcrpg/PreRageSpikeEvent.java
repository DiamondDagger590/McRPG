package us.eunoians.mcrpg.api.events.mcrpg;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.swords.RageSpike;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.players.McRPGPlayer;

public class PreRageSpikeEvent extends AbilityActivateEvent {


  @Getter @Setter
  private int cooldown;
  @Getter @Setter
  private int damage;
  @Getter @Setter
  private int chargeTime;
  public PreRageSpikeEvent(McRPGPlayer user, RageSpike rageSpike){
    super(rageSpike, user);
    int tier = rageSpike.getCurrentTier();
	this.isCancelled = !rageSpike.isToggled();
	this.cooldown = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("RageSpikeConfig.Tier" + Methods.convertToNumeral(tier) + ".Cooldown");
	this.damage = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("RageSpikeConfig.Tier" + Methods.convertToNumeral(tier) + ".Damage");
	this.chargeTime = McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("RageSpikeConfig.Tier" + Methods.convertToNumeral(tier) + ".ChargeTime");
  }
}
