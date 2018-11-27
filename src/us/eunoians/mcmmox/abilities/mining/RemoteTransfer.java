package us.eunoians.mcmmox.abilities.mining;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.api.util.FileManager;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.types.UnlockedAbilities;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    /*TODO set a get range method in here and a way to scan in blocks from a setting. Also need to make a gui to toggle items on and off. Also need to save location to player and load it in
	Make sure to allow for config reloading in this*/
  }

  public void updateBlocks(){
	FileConfiguration miningConfig = Mcmmox.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
	for(String cat : miningConfig.getStringList("RemoteTransferConfig.Tier" + Methods.convertToNumeral(getCurrentTier()) + ".Categories")){
	  List<Material> blocksInCat = miningConfig.getStringList("RemoteTransferConfig.Categories." + cat).stream().map(Material::getMaterial).collect(Collectors.toList());
	  for(Material mat : blocksInCat){
	    if(!itemsToSync.containsKey(mat)){
		  itemsToSync.put(mat, true);
		}
	  }
	}
  }
}
