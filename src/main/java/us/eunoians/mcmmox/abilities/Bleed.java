package us.eunoians.mcmmox.abilities;

import lombok.Getter;
import org.bukkit.entity.Player;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.types.DefaultAbilities;

import java.util.ArrayList;
import java.util.UUID;

public class Bleed extends BaseAbility {

  @Getter
  private ArrayList<UUID> targeted;

  public Bleed() {
    super(DefaultAbilities.BLEED, true);
    targeted = new ArrayList<>();
  }

  public boolean isPlayerTargeted(Player p){
    return targeted.contains(p.getUniqueId());
  }

  public void stopTargetingPlayer(Player p){
    targeted.remove(p.getUniqueId());
  }

  public void startTargetingPlayer(Player p){
    targeted.add(p.getUniqueId());
  }

  public boolean canTarget(){
    return targeted.size() == Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.SWORDS_CONFIG).getInt("BleedConfig.BleedCap");
  }
}