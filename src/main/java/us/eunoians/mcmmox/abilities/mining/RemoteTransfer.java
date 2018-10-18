package us.eunoians.mcmmox.abilities.mining;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.HashMap;

public class RemoteTransfer extends BaseAbility {

  @Getter
  @Setter
  private Location linkedChestLocation;

  /**
   * Material and if it needs to sync or not
   */
  @Getter
  private HashMap<Material, Boolean> itemsToSync;

  public RemoteTransfer(){
    super(UnlockedAbilities.REMOTE_TRANSFER, true, false);
    itemsToSync = new HashMap<>();
    //TODO set a get range method in here and a way to scan in blocks from a setting. Also need to make a gui to toggle items on and off. Also need to save location to player and load it in
  }
}
