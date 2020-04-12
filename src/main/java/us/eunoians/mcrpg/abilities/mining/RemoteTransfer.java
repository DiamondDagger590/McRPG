package us.eunoians.mcrpg.abilities.mining;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.abilities.BaseAbility;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.Methods;
import us.eunoians.mcrpg.api.util.RemoteTransferTracker;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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

  public RemoteTransfer(UUID uuid, boolean isToggled, int currentTier) {
    super(UnlockedAbilities.REMOTE_TRANSFER, isToggled, currentTier);

    itemsToSync = new HashMap<>();
    if (isUnlocked()) {
      updateBlocks();
    }

    if (RemoteTransferTracker.isTracked(uuid)) {
      linkedChestLocation = RemoteTransferTracker.getLocation(uuid);
    }

    File file = new File(McRPG.getInstance().getDataFolder(), File.separator + "remote_transfer_data" + File.separator + uuid.toString() + ".yml");
    FileConfiguration remoteTransferFile = YamlConfiguration.loadConfiguration(file);
    if(remoteTransferFile.contains("RemoteTransferBlocks")) {
      for(String s : remoteTransferFile.getConfigurationSection("RemoteTransferBlocks").getKeys(false)) {
        itemsToSync.put(Material.getMaterial(s), remoteTransferFile.getBoolean("RemoteTransferBlocks." + s));
      }
    }
  }

  public void updateBlocks() {
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

  public boolean isAbilityLinked() {
    return linkedChestLocation != null;
  }
}
