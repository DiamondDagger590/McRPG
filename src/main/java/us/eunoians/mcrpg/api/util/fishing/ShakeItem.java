package us.eunoians.mcrpg.api.util.fishing;

import lombok.Getter;
import org.bukkit.Material;

import java.util.List;

import static us.eunoians.mcrpg.api.util.fishing.FishingItemManager.getFishingLootConfig;


public class ShakeItem {

    @Getter
    private Material type;
    @Getter
    private String displayName;
    @Getter
    private List<String> lore;
    @Getter
    private int lowEndAmount;
    @Getter
    private int highEndAmount;
    @Getter
    private int exp;
    @Getter
    private double chance;
    @Getter
    private EnchantmentMeta enchantmentMeta;
    @Getter
    private PotionMeta potionMeta;

    public ShakeItem(String filePath){
        this.chance = getFishingLootConfig().getDouble(filePath + "Chance", 5.0);
        this.type = Material.getMaterial(getFishingLootConfig().getString(filePath + "Material", "AIR"));
        if(getFishingLootConfig().contains(filePath + "DisplayName")){
            this.displayName = getFishingLootConfig().getString(filePath + "DisplayName");
        }
        if(getFishingLootConfig().contains(filePath + "Lore")){
            this.lore = getFishingLootConfig().getStringList(filePath + "Lore");
        }
        String[] amountRange = getFishingLootConfig().getString(filePath + "Amount", "1").split("-");
        this.lowEndAmount = Integer.parseInt(amountRange[0]);
        this.highEndAmount = amountRange.length > 1 ? Integer.parseInt(amountRange[1]) : lowEndAmount;
        this.exp = getFishingLootConfig().getInt(filePath + "Exp", 100);
        if(getFishingLootConfig().contains(filePath + "EnchantmentMeta")){
            this.enchantmentMeta = new EnchantmentMeta(filePath + "EnchantmentMeta.");
        }
        if(getFishingLootConfig().contains(filePath + "PotionMeta")){
            this.potionMeta = new PotionMeta(filePath + "PotionMeta.");
        }
    }
}
