package us.eunoians.mcrpg.types;

import lombok.Getter;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import us.eunoians.mcrpg.McRPG;

import java.util.Arrays;
import java.util.Locale;

public enum BasePotionType{
	WATER("Water", true, "", PotionType.WATER, null),
	AWKWARD("Awkward", true, "", PotionType.AWKWARD, null),
	HASTE("Haste", false, "217:192:67", null, PotionEffectType.HASTE),
	MINING_FATIGUE("Mining Fatigue", false, "74:66:23", null, PotionEffectType.MINING_FATIGUE),
	SPEED("Speed", true, "51:235:255", PotionType.SWIFTNESS, PotionEffectType.SPEED),
	SLOWNESS("Slowness", true, "139:175:224", PotionType.SLOWNESS, PotionEffectType.SLOWNESS),
	JUMP_BOOST("Leaping", true, "253:255:132", PotionType.LEAPING, PotionEffectType.JUMP_BOOST),
	NAUSEA("Nausea", false, "85:29:74", null, PotionEffectType.NAUSEA),
	REGENERATION("Regeneration", true, "205:92:171", PotionType.REGENERATION, PotionEffectType.REGENERATION),
	WATER_BREATHING("Water Breathing", true, "152:218:192", PotionType.WATER_BREATHING, PotionEffectType.WATER_BREATHING),
	INVISIBILITY("Invisibility", true, "246:246:246", PotionType.INVISIBILITY, PotionEffectType.INVISIBILITY),
	BLINDNESS("Blindness", false, "31:31:35", null, PotionEffectType.BLINDNESS),
	NIGHT_VISION("Night Vision", true, "194:255:102", PotionType.NIGHT_VISION, PotionEffectType.NIGHT_VISION),
	HUNGER("Hunger", false, "88:118:83", null, PotionEffectType.HUNGER),
	SATURATION("Saturation", false, "248:36:35", null, PotionEffectType.SATURATION),
	INSTANT_HEALTH("Instant Health", true, "248:36:35", PotionType.HEALING, PotionEffectType.INSTANT_HEALTH),
	INSTANT_DAMAGE("Instant Damage", true, "169:101:106", PotionType.HARMING, PotionEffectType.INSTANT_DAMAGE),
	WITHER("Wither", false, "115:97:86", null, PotionEffectType.WITHER),
	HEALTH_BOOST("Health Boost", false, "248:125:35", null, PotionEffectType.HEALTH_BOOST),
	ABSORPTION("Absorption", false, "37:82:165", null, PotionEffectType.ABSORPTION),
	GLOWING("Glowing", false, "148:160:97", null, PotionEffectType.GLOWING),
	LUCK("Luck", false, "89:193:6", null, PotionEffectType.LUCK),
	UNLUCK("Unluck", false, "18:98:73", null, PotionEffectType.UNLUCK),
	DOLPHINS_GRACE("Dolphins Grace", false, "136:163:190", null, PotionEffectType.DOLPHINS_GRACE),
	STRENGTH("Strength", true, "255:199:0", PotionType.STRENGTH, PotionEffectType.STRENGTH),
	WEAKNESS("Weakness", true, "72:77:72", PotionType.WEAKNESS, PotionEffectType.WEAKNESS),
	RESISTANCE("Resistance", false, "153:69:58", null, PotionEffectType.RESISTANCE),
	FIRE_RESISTANCE("Fire Resistance", true, "255:153:0", PotionType.FIRE_RESISTANCE, PotionEffectType.FIRE_RESISTANCE),
	LEVITATION("Levitation", false, "206:255:255", null, PotionEffectType.LEVITATION),
	SLOW_FALLING("Slow Falling", true, "243:207:185", PotionType.SLOW_FALLING, PotionEffectType.SLOW_FALLING),
	POISON("Poison", true, "135:163:99", PotionType.POISON, PotionEffectType.POISON),
	INFESTED("Infested", true, "107:119:107", PotionType.INFESTED, PotionEffectType.INFESTED),
	DARKNESS("Darkness", false, "47:44:61", null, PotionEffectType.DARKNESS),
	HERO_OF_THE_VILLAGE("Hero of the Village", false, "134:208:160", null, PotionEffectType.HERO_OF_THE_VILLAGE),
	OOZING("Oozing", true, "117:197:125", PotionType.OOZING, PotionEffectType.OOZING),
	WIND_CHARGED("Wind Charged", true, "145:155:197", PotionType.WIND_CHARGED, PotionEffectType.WIND_CHARGED),
	WEAVING("Weaving", true, "91:79:68", PotionType.WEAVING, PotionEffectType.WEAVING),
	;


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
		return effectType != null ? effectType.getKey().getKey().toUpperCase(Locale.ROOT) : (potionType == PotionType.WATER ? "WATER" : "AWKWARD");
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
