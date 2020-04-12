package us.eunoians.mcrpg.abilities.swords;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.DefaultAbilities;

import java.util.ArrayList;
import java.util.UUID;

public class Bleed extends BaseAbility {

  @Getter
  private ArrayList<UUID> targeted;

  @Getter
  @Setter
  private double bonusChance = 0.0;

  public Bleed(boolean isToggled) {
    super(DefaultAbilities.BLEED, isToggled, 0, true);
    targeted = new ArrayList<>();
  }

  /**
   *
   * @param p Player to test for
   * @return true if the player is being targeted by this Bleed instance
   */
  public boolean isPlayerTargeted(Player p){
    return targeted.contains(p.getUniqueId());
  }

  /**
   *
   * @param p Player to stop tracking
   */
  public void stopTargetingPlayer(Player p){
    targeted.remove(p.getUniqueId());
  }

  /**
   *
   * @param p Player to start tracking
   */
  public void startTargetingPlayer(Player p){
    targeted.add(p.getUniqueId());
  }

  /**
   *
   * @return true if the amount of targets for bleed has nit been reached
   */
  public boolean canTarget(){
    return targeted.size() == McRPG.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("BleedConfig.BleedCap");
  }
}