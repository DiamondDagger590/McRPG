package us.eunoians.mcmmox.api.events.mcmmo;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcmmox.Abilities.BaseAbility;
import us.eunoians.mcmmox.Abilities.Bleed;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.players.McMMOPlayer;

public class BleedEvent extends AbilityActivateEvent {

  @Getter
  private McMMOPlayer target;
  @Getter
  private McMMOPlayer user;
  @Getter @Setter
  private int damage;
  @Getter @Setter
  private int frequency;
  @Getter @Setter
  private int baseDuration;
  @Getter @Setter
  private boolean pierceArmour;
  @Getter
  private int minimumHealthAllowed;
  @Getter @Setter
  private boolean bleedImmunityEnabled;
  @Getter @Setter
  private int bleedImmunityDuration;


  public BleedEvent(BaseAbility ability, McMMOPlayer user, McMMOPlayer target, Bleed bleed){
	super(ability, user);
	FileConfiguration config = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG);
	isCancelled = bleed.isToggled();
	this.target = target;
	this.damage = config.getInt("BleedConfig.BaseDamage");
	this.minimumHealthAllowed = config.getInt("BleedConfig.MinimumHealthAllowed");
	this.frequency = config.getInt("BleedConfig.Frequency");
	this.baseDuration = config.getInt("BleedConfig.BaseDuration");
	this.pierceArmour = config.getBoolean("BleedConfig.BleedPierceArmour");
	this.bleedImmunityEnabled = config.getBoolean("BleedConfig.BleedImmunityEnabled");
	this.bleedImmunityDuration = config.getInt("BleedConfig.BleedImmunityDuration");
  }

}
