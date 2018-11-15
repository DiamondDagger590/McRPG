package us.eunoians.mcmmox.util.mcmmo;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcmmox.Mcmmox;

/**
 * This code is not mine. It is from the original McMMO allowed for use by their license.
 * All credit goes to the original authors as I have only changed a little to suit my needs
 */

public class MobHealthDisplayUpdaterTask extends BukkitRunnable {
  private LivingEntity target;
  private String oldName;
  private boolean oldNameVisible;

  public MobHealthDisplayUpdaterTask(LivingEntity target) {
	if (target.isValid()) {
	  this.target = target;
	  this.oldName = target.getMetadata(Mcmmox.getInstance().getCustomNameKey()).get(0).asString();
	  this.oldNameVisible = target.getMetadata(Mcmmox.getInstance().getCustomVisibleKey()).get(0).asBoolean();
	}
  }

  @Override
  public void run() {
	if (target != null && target.isValid()) {
	  target.setCustomNameVisible(oldNameVisible);
	  target.setCustomName(oldName);
	  target.removeMetadata(Mcmmox.getInstance().getCustomNameKey(), Mcmmox.getInstance());
	  target.removeMetadata(Mcmmox.getInstance().getCustomVisibleKey(), Mcmmox.getInstance());
	}
  }
}