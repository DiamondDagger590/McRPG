package us.eunoians.mcrpg.types;

import lombok.Getter;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Arrays;

public enum BasePotionType{
	WATER("Water", true, "", PotionType.WATER, null),
	AWKWARD("Awkward", true, "", PotionType.AWKWARD, null),
	FAST_DIGGING("Haste", false, "240:226:77", PotionType.UNCRAFTABLE, PotionEffectType.FAST_DIGGING),
	SLOW_DIGGING("Mining Fatigue", false, "53:54:79", PotionType.UNCRAFTABLE, PotionEffectType.SLOW_DIGGING),
	SPEED("Speed", true, "", PotionType.SPEED, PotionEffectType.SPEED),
	SLOW("Slowness", true, "", PotionType.SLOWNESS, PotionEffectType.SLOW),
	JUMP("Jump", true, "", PotionType.JUMP, PotionEffectType.JUMP),
	CONFUSION("Nausea", false, "56:92:28", PotionType.UNCRAFTABLE, PotionEffectType.CONFUSION),
	REGENERATION("Regeneration", true, "", PotionType.REGEN, PotionEffectType.REGENERATION),
	WATER_BREATHING("Water Breathing", true, "", PotionType.WATER_BREATHING, PotionEffectType.WATER_BREATHING),
	INVISIBILITY("Invisibility", true, "", PotionType.INVISIBILITY, PotionEffectType.INVISIBILITY),
	BLINDNESS("Blindness", false, "86:80:102", PotionType.UNCRAFTABLE, PotionEffectType.BLINDNESS),
	NIGHT_VISION("Night Vision", true, "", PotionType.NIGHT_VISION, PotionEffectType.NIGHT_VISION),
	HUNGER("Hunger", false, "81:128:6", PotionType.UNCRAFTABLE, PotionEffectType.HUNGER),
	SATURATION("Saturation", false, "199:112:50", PotionType.UNCRAFTABLE, PotionEffectType.SATURATION),
	HEAL("Instant Health", true, "", PotionType.INSTANT_HEAL, PotionEffectType.HEAL),
	HARM("Instant Damage", true, "", PotionType.INSTANT_DAMAGE, PotionEffectType.HARM),
	WITHER("Wither", false, "23:22:21", PotionType.UNCRAFTABLE, PotionEffectType.WITHER),
	HEALTH_BOOST("Health Boost", false, "219:59:99", PotionType.UNCRAFTABLE, PotionEffectType.HEALTH_BOOST),
	ABSORPTION("Absorption", false, "240:235:77", PotionType.UNCRAFTABLE, PotionEffectType.ABSORPTION),
	GLOWING("Glowing", false, "217:49:232", PotionType.UNCRAFTABLE, PotionEffectType.GLOWING),
	LUCK("Luck", false, "18:222:42", PotionType.UNCRAFTABLE, PotionEffectType.LUCK),
	UNLUCK("Unluck", false, "138:191:4", PotionType.UNCRAFTABLE, PotionEffectType.UNLUCK),
	DOLPHINS_GRACE("Dolphins Grace", false, "108:197:224", PotionType.UNCRAFTABLE, PotionEffectType.DOLPHINS_GRACE),
	INCREASE_DAMAGE("Strength", true, "", PotionType.UNCRAFTABLE, PotionEffectType.INCREASE_DAMAGE),
	WEAKNESS("Weakness", true, "", PotionType.WEAKNESS, PotionEffectType.WEAKNESS),
	RESISTANCE("Resistance", false, "138:175:186", PotionType.UNCRAFTABLE, PotionEffectType.DAMAGE_RESISTANCE),
	FIRE_RESISTANCE("Fire Resistance", true, "", PotionType.FIRE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE),
	LEVITATION("Levitation", false, "", PotionType.UNCRAFTABLE, PotionEffectType.LEVITATION),
	SLOW_FALLING("Slow Falling", true, "", PotionType.SLOW_FALLING, PotionEffectType.SLOW_FALLING),
	POISON("Poison", true, "", PotionType.POISON, PotionEffectType.POISON);


	@Getter
	private boolean isVanilla;

	@Getter
	private String name;

	@Getter
	private String customColour;

	@Getter
	private PotionType potionType;

	@Getter
	private PotionEffectType effectType;

	BasePotionType(String name, boolean isVanilla, String customColour, PotionType potionType, PotionEffectType effectType){
		this.isVanilla = isVanilla;
		this.name = name;
		this.customColour = customColour;
		this.potionType = potionType;
		this.effectType = effectType;
	}

	public static BasePotionType getFromPotionType(PotionType type){
		return Arrays.stream(values()).filter(p -> p.getPotionType() == type).findFirst().orElse(BasePotionType.AWKWARD);
	}

	public static BasePotionType getFromPotionEffect(PotionEffectType type){
		return Arrays.stream(values()).filter(p -> p.getEffectType().equals(type)).findFirst().orElse(BasePotionType.AWKWARD);
	}
}
