package us.eunoians.mcrpg.types;

import lombok.Getter;
import org.bukkit.potion.PotionType;

public enum BasePotionType{
	WATER("Water", true, "", PotionType.WATER),
	AWKWARD("Awkward", true, "", PotionType.AWKWARD),
	FAST_DIGGING("Haste", false, "240:226:77", PotionType.UNCRAFTABLE),
	SLOW_DIGGING("Mining Fatigue", false, "53:54:79", PotionType.UNCRAFTABLE),
	SPEED("Speed", true, "", PotionType.SPEED),
	SLOW("Slowness", true, "", PotionType.SLOWNESS),
	JUMP("Jump", true, "", PotionType.JUMP),
	CONFUSION("Nausea", false, "56:92:28", PotionType.UNCRAFTABLE),
	REGENERATION("Regeneration", true, "", PotionType.REGEN),
	WATER_BREATHING("Water Breathing", true, "", PotionType.WATER_BREATHING),
	INVISIBILITY("Invisibility", true, "", PotionType.INVISIBILITY),
	BLINDNESS("Blindness", false, "86:80:102", PotionType.UNCRAFTABLE),
	NIGHT_VISION("Night Vision", true, "", PotionType.NIGHT_VISION),
	HUNGER("Hunger", false, "81:128:6", PotionType.UNCRAFTABLE),
	SATURATION("Saturation", false, "199:112:50", PotionType.UNCRAFTABLE),
	HEAL("Instant Health", true, "", PotionType.INSTANT_HEAL),
	HARM("Instant Damage", true, "", PotionType.INSTANT_DAMAGE),
	WITHER("Wither", false, "23:22:21", PotionType.UNCRAFTABLE),
	HEALTH_BOOST("Health Boost", false, "219:59:99", PotionType.UNCRAFTABLE),
	ABSORPTION("Absorption", false, "240:235:77", PotionType.UNCRAFTABLE),
	GLOWING("Glowing", false, "217:49:232", PotionType.UNCRAFTABLE),
	LUCK("Luck", false, "18:222:42", PotionType.UNCRAFTABLE),
	UNLUCK("Unluck", false, "138:191:4", PotionType.UNCRAFTABLE),
	DOLPHINS_GRACE("Dolphins Grace", false, "108:197:224", PotionType.UNCRAFTABLE),
	INCREASE_DAMAGE("Strength", true, "", PotionType.UNCRAFTABLE),
	WEAKNESS("Weakness", true, "", PotionType.WEAKNESS),
	RESISTANCE("Resistance", false, "138:175:186", PotionType.UNCRAFTABLE),
	FIRE_RESISTANCE("Fire Resistance", true, "", PotionType.FIRE_RESISTANCE),
	LEVITATION("Levitation", false, "", PotionType.UNCRAFTABLE),
	SLOW_FALLING("Slow Falling", true, "", PotionType.SLOW_FALLING),
	POISON("Poison", true, "", PotionType.POISON);


	@Getter
	private boolean isVanilla;

	@Getter
	private String name;

	@Getter
	private String customColour;

	@Getter
	private PotionType potionType;

	BasePotionType(String name, boolean isVanilla, String customColour, PotionType potionType){
		this.isVanilla = isVanilla;
		this.name = name;
		this.customColour = customColour;
		this.potionType = potionType;
	}
}
