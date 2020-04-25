package us.eunoians.mcrpg.party;

import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;

public class AcceptedTeleportRequest{
  
  /**
   * The task that is updating the player about the status of their wait and will
   * teleport them afterwards
   */
  @Getter
  private BukkitTask waitTask;
  
  /**
   * The teleport request that was accepted
   */
  @Getter
  private TeleportRequest teleportRequest;
  
  public AcceptedTeleportRequest(BukkitTask waitTask, TeleportRequest teleportRequest){
    this.waitTask = waitTask;
    this.teleportRequest = teleportRequest;
  }
}
