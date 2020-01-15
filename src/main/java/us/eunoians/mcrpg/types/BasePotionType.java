package us.eunoians.mcrpg.types;

import lombok.Getter;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Arrays;

public enum BasePotionType{
	WATER("Water", true, "", PotionType.WATER, null),
	AWKWARD("Awkward", true, "", PotionType.AWKWARD, null),
	FAST_DIGGING("Haste", false, "217:192:67", PotionType.UNCRAFTABLE, PotionEffectType.FAST_DIGGING),
	SLOW_DIGGING("Mining Fatigue", false, "74:66:23", PotionType.UNCRAFTABLE, PotionEffectType.SLOW_DIGGING),
	SPEED("Speed", true, "124:175:198", PotionType.SPEED, PotionEffectType.SPEED),
	SLOW("Slowness", true, "90:108:129", PotionType.SLOWNESS, PotionEffectType.SLOW),
	JUMP("Jump", true, "34:255:76", PotionType.JUMP, PotionEffectType.JUMP),
	CONFUSION("Nausea", false, "85:29:74", PotionType.UNCRAFTABLE, PotionEffectType.CONFUSION),
	REGENERATION("Regeneration", true, "205:92:171", PotionType.REGEN, PotionEffectType.REGENERATION),
	WATER_BREATHING("Water Breathing", true, "46:82:153", PotionType.WATER_BREATHING, PotionEffectType.WATER_BREATHING),
	INVISIBILITY("Invisibility", true, "127:131:146", PotionType.INVISIBILITY, PotionEffectType.INVISIBILITY),
	BLINDNESS("Blindness", false, "31:31:35", PotionType.UNCRAFTABLE, PotionEffectType.BLINDNESS),
	NIGHT_VISION("Night Vision", true, "31:31:161", PotionType.NIGHT_VISION, PotionEffectType.NIGHT_VISION),
	HUNGER("Hunger", false, "88:118:83", PotionType.UNCRAFTABLE, PotionEffectType.HUNGER),
	SATURATION("Saturation", false, "248:36:35", PotionType.UNCRAFTABLE, PotionEffectType.SATURATION),
	HEAL("Instant Health", true, "248:36:35", PotionType.INSTANT_HEAL, PotionEffectType.HEAL),
	HARM("Instant Damage", true, "67:10:9", PotionType.INSTANT_DAMAGE, PotionEffectType.HARM),
	WITHER("Wither", false, "53:42:39", PotionType.UNCRAFTABLE, PotionEffectType.WITHER),
	HEALTH_BOOST("Health Boost", false, "248:125:35", PotionType.UNCRAFTABLE, PotionEffectType.HEALTH_BOOST),
	ABSORPTION("Absorption", false, "37:82:165", PotionType.UNCRAFTABLE, PotionEffectType.ABSORPTION),
	GLOWING("Glowing", false, "148:160:97", PotionType.UNCRAFTABLE, PotionEffectType.GLOWING),
	LUCK("Luck", false, "51:153:0", PotionType.UNCRAFTABLE, PotionEffectType.LUCK),
	UNLUCK("Unluck", false, "18:98:73", PotionType.UNCRAFTABLE, PotionEffectType.UNLUCK),
	DOLPHINS_GRACE("Dolphins Grace", false, "136:163:190", PotionType.UNCRAFTABLE, PotionEffectType.DOLPHINS_GRACE),
	INCREASE_DAMAGE("Strength", true, "147:36:35", PotionType.UNCRAFTABLE, PotionEffectType.INCREASE_DAMAGE),
	WEAKNESS("Weakness", true, "72:77:72", PotionType.WEAKNESS, PotionEffectType.WEAKNESS),
	RESISTANCE("Resistance", false, "153:69:58", PotionType.UNCRAFTABLE, PotionEffectType.DAMAGE_RESISTANCE),
	FIRE_RESISTANCE("Fire Resistance", true, "228:154:58", PotionType.FIRE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE),
	LEVITATION("Levitation", false, "206:255:255", PotionType.UNCRAFTABLE, PotionEffectType.LEVITATION),
	SLOW_FALLING("Slow Falling", true, "255:239:209", PotionType.SLOW_FALLING, PotionEffectType.SLOW_FALLING),
	POISON("Poison", true, "78:147:49", PotionType.POISON, PotionEffectType.POISON);


	@Getter
	private boolean isVanilla;

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

	public String getDisplayName(){
		return this.name;
	}

	public String getName(){
		return effectType != null ? effectType.getName() : (potionType == PotionType.WATER ? "WATER" : "AWKWARD");
	}

	public static BasePotionType getFromPotionType(PotionType type){
		if(type == PotionType.WATER){
			return WATER;
		}
		return Arrays.stream(values()).filter(p -> p.getPotionType() == type).findFirst().orElse(BasePotionType.AWKWARD);
	}

	public static BasePotionType getFromPotionEffect(PotionEffectType type){
		return Arrays.stream(values()).filter(p -> p.getEffectType() != null).filter(p -> p.getEffectType().equals(type)).findFirst().orElse(BasePotionType.AWKWARD);
	}
}
