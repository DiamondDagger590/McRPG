package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.swords.RageSpike;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class PreRageSpikeEvent extends AbilityActivateEvent {


  @Getter @Setter
  private int cooldown;
  @Getter @Setter
  private int damage;
  @Getter @Setter
  private int chargeTime;
  public PreRageSpikeEvent(McMMOPlayer user, RageSpike rageSpike){
    super(rageSpike, user);
    int tier = rageSpike.getCurrentTier();
	this.isCancelled = !rageSpike.isToggled();
	this.cooldown = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("RageSpikeConfig.Tier" + Methods.convertToNumeral(tier) + ".Cooldown");
	this.damage = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("RageSpikeConfig.Tier" + Methods.convertToNumeral(tier) + ".Damage");
	this.chargeTime = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("RageSpikeConfig.Tier" + Methods.convertToNumeral(tier) + ".ChargeTime");
  }
}
