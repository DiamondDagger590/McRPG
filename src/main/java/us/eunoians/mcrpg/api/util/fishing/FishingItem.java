package us.eunoians.mcrpg.api.util.fishing;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.types.UnlockedAbilities;

import java.util.List;
import java.util.Map;

public class FishingItem {

    @Getter private Material itemType;
    @Getter private double chance;
    @Getter private int lowEndAmount;
    @Getter private int highEndAmount;
    @Getter private int lowEndDurability;
    @Getter private int highEndDurability;
    @Getter private int lowEndVanillaExpAmount;
    @Getter private int highEndVanillaExpAmount;
    @Getter private int mcrpgExpValue;
    @Getter private String displayName;
    @Getter private List<String> lore;
    @Getter private EnchantmentMeta enchantmentMeta;
    @Getter private PotionMeta potionMeta;
    @Getter private Map<UnlockedAbilities, FishingItemDep> dependancies;

    public FishingItem(){

    }

    static FileConfiguration getFishingLootConfig(){
        return McRPG.getInstance().getFileManager().getFile(FileManager.Files.FISHING_LOOT);
    }
}
