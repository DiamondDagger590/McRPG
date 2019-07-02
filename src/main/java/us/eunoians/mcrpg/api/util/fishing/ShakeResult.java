package us.eunoians.mcrpg.api.util.fishing;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public class ShakeResult {

    @Getter
    private ItemStack itemStack;
    @Getter
    private int exp;

    public ShakeResult(ItemStack itemStack, int exp){
        this.itemStack = itemStack;
        this.exp = exp;
    }
}
