package us.eunoians.mcrpg.types;

import lombok.Getter;

public enum BasePotionType{
	WATER("Water", true, ""),
	AWKWARD("Awkward", true, ""),
	FAST_DIGGING("Haste", false, "240:226:77"),
	SLOW_DIGGING("Mining Fatigue", false, "53:54:79"),
	SPEED("Speed", true, ""),
	SLOW("Slowness", true, ""),
	JUMP("Jump", true, ""),
	CONFUSION("Nausea", false, "56:92:28"),
	REGENERATION("Regeneration", true, ""),
	WATER_BREATHING("Water Breathing", true, ""),
	INVISIBILITY("Invisibility", true, ""),
	BLINDNESS("Blindness", false, "86:80:102"),
	NIGHT_VISION("Night Vision", true, ""),
	HUNGER("Hunger", false, "81:128:6"),
	SATURATION("Saturation", false, "199:112:50"),
	HEAL("Instant Health", true, ""),
	HARM("Instant Damage", true, ""),
	WITHER("Wither", false, "23:22:21"),
	HEALTH_BOOST("Health Boost", false, "219:59:99"),
	ABSORPTION("Absorption", false, "240:235:77"),
	GLOWING("Glowing", false, "217:49:232"),
	LUCK("Luck", false, "18:222:42"),
	UNLUCK("Unluck", false, "138:191:4"),
	DOLPHINS_GRACE("Dolphins Grace", false, "108:197:224"),
	INCREASE_DAMAGE("Strength", true, ""),
	WEAKNESS("Weakness", true, ""),
	RESISTANCE("Resistance", false, "138:175:186"),
	FIRE_RESISTANCE("Fire Resistance", true, ""),
	LEVITATION("Levitation", true, ""),
	SLOW_FALLING("Slow Falling", true, ""),
	POISON("Poison", true, "");


	@Getter
	private boolean isVanilla;

	@Getter
	private String name;

	@Getter
	private String customColour;

	BasePotionType(String name, boolean isVanilla, String customColour){
		this.isVanilla = isVanilla;
		this.name = name;
		this.customColour = customColour;
	}
}
