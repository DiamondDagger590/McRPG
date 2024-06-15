package us.eunoians.mcrpg.types;

import lombok.Getter;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import us.eunoians.mcrpg.McRPG;

import java.util.Arrays;

public enum BasePotionType{
	WATER("Water", true, "", PotionType.WATER, null),
	AWKWARD("Awkward", true, "", PotionType.AWKWARD, null),
	FAST_DIGGING("Haste", false, "217:192:67", null, PotionEffectType.HASTE),
	SLOW_DIGGING("Mining Fatigue", false, "74:66:23", null, PotionEffectType.MINING_FATIGUE),
	SPEED("Speed", true, "124:175:198", PotionType.SWIFTNESS, PotionEffectType.SPEED),
	SLOW("Slowness", true, "90:108:129", PotionType.SLOWNESS, PotionEffectType.SLOWNESS),
	LEAPING("Leaping", true, "34:255:76", PotionType.LEAPING, PotionEffectType.JUMP_BOOST),
	CONFUSION("Nausea", false, "85:29:74", null, PotionEffectType.NAUSEA),
	REGENERATION("Regeneration", true, "205:92:171", PotionType.REGENERATION, PotionEffectType.REGENERATION),
	WATER_BREATHING("Water Breathing", true, "46:82:153", PotionType.WATER_BREATHING, PotionEffectType.WATER_BREATHING),
	INVISIBILITY("Invisibility", true, "127:131:146", PotionType.INVISIBILITY, PotionEffectType.INVISIBILITY),
	BLINDNESS("Blindness", false, "31:31:35", null, PotionEffectType.BLINDNESS),
	NIGHT_VISION("Night Vision", true, "31:31:161", PotionType.NIGHT_VISION, PotionEffectType.NIGHT_VISION),
	HUNGER("Hunger", false, "88:118:83", null, PotionEffectType.HUNGER),
	SATURATION("Saturation", false, "248:36:35", null, PotionEffectType.SATURATION),
	HEAL("Instant Health", true, "248:36:35", PotionType.HEALING, PotionEffectType.INSTANT_HEALTH),
	HARM("Instant Damage", true, "67:10:9", PotionType.HARMING, PotionEffectType.INSTANT_DAMAGE),
	WITHER("Wither", false, "53:42:39", null, PotionEffectType.WITHER),
	HEALTH_BOOST("Health Boost", false, "248:125:35", null, PotionEffectType.HEALTH_BOOST),
	ABSORPTION("Absorption", false, "37:82:165", null, PotionEffectType.ABSORPTION),
	GLOWING("Glowing", false, "148:160:97", null, PotionEffectType.GLOWING),
	LUCK("Luck", false, "51:153:0", null, PotionEffectType.LUCK),
	UNLUCK("Unluck", false, "18:98:73", null, PotionEffectType.UNLUCK),
	DOLPHINS_GRACE("Dolphins Grace", false, "136:163:190", null, PotionEffectType.DOLPHINS_GRACE),
	INCREASE_DAMAGE("Strength", true, "147:36:35", PotionType.STRENGTH, PotionEffectType.STRENGTH),
	WEAKNESS("Weakness", true, "72:77:72", PotionType.WEAKNESS, PotionEffectType.WEAKNESS),
	RESISTANCE("Resistance", false, "153:69:58", null, PotionEffectType.RESISTANCE),
	FIRE_RESISTANCE("Fire Resistance", true, "228:154:58", PotionType.FIRE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE),
	LEVITATION("Levitation", false, "206:255:255", null, PotionEffectType.LEVITATION),
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
		return McRPG.getInstance().getLangFile().getString("Potions.PotionNames." + this.name.replace(" ", ""), "");
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
	
	@Override
	public String toString(){
		return "[" + (effectType != null ? effectType.toString() : "null") + "-" + customColour + "-" + (potionType != null ? potionType.toString() : "null") + "]";
	}
}
