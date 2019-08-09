package us.eunoians.mcrpg.api.util.brewing;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.eunoians.mcrpg.types.BasePotionType;

import static us.eunoians.mcrpg.api.util.brewing.PotionFactory.getBasePotionTypeFromItemStack;

public class BasePotion{

	@Getter
	private boolean isSplash;

	@Getter
	private boolean isLingering;

	@Getter
	private BasePotionType basePotionType;

	@Getter
	private ItemStack potionItem;

	BasePotion(ItemStack potion){
		this.basePotionType = getBasePotionTypeFromItemStack(potion);
		this.isSplash = potion.getType() == Material.SPLASH_POTION;
		this.isLingering = potion.getType() == Material.LINGERING_POTION;
		this.potionItem = potion;
	}


}
