package us.eunoians.mcrpg.api.util.brewing;

import lombok.Getter;
import us.eunoians.mcrpg.types.BasePotionType;

public class BasePotion{

	@Getter
	private boolean isSplash;

	@Getter
	private boolean isLingering;

	@Getter
	private BasePotionType basePotionType;

	BasePotion(BasePotionType basePotionType, boolean isSplash, boolean isLingering){
		this.basePotionType = basePotionType;
		this.isSplash = isSplash;
		this.isLingering = isLingering;
	}



}
