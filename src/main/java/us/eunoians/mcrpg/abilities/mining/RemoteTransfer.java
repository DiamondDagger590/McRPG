package us.eunoians.mcrpg.abilities.mining;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.types.UnlockedAbilities;

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
  }

  public void updateBlocks(){
	FileConfiguration miningConfig = McRPG.getInstance().getFileManager().getFile(FileManager.Files.MINING_CONFIG);
	for(String cat : miningConfig.getStringList("RemoteTransferConfig.Tier" + Methods.convertToNumeral(getCurrentTier()) + ".Categories")){
	  List<Material> blocksInCat = miningConfig.getStringList("RemoteTransferConfig.Categories." + cat).stream().map(Material::getMaterial).collect(Collectors.toList());
	  for(Material mat : blocksInCat){
	    if(!itemsToSync.containsKey(mat)){
		  itemsToSync.put(mat, true);
		}
	  }
	}
  }

  public boolean isAbilityLinked(){
    return linkedChestLocation != null;
  }
}
