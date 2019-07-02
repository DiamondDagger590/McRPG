package us.eunoians.mcrpg.api.util.fishing;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public class FishingResult {

    @Getter
    private ItemStack itemStack;
    @Getter
    private int vanillaExp;
    @Getter
    private int mcrpgExp;

    public FishingResult(ItemStack itemStack, int vanillaExp, int mcrpgExp){
        this.itemStack = itemStack;
        this.vanillaExp = vanillaExp;
        this.mcrpgExp = mcrpgExp;
    }
}
