package us.eunoians.mcmmox.players;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.abilities.Bleed;
import us.eunoians.mcmmox.abilities.DeeperWound;
import us.eunoians.mcmmox.abilities.RageSpike;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.skills.Skill;
import us.eunoians.mcmmox.skills.Swords;
import us.eunoians.mcmmox.types.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class McMMOPlayer {

  /**
   * The UUID of the player
   */
  @Getter
  private UUID uuid;
  /**
   * The power level of a player. The total sum of all of the players skill levels
   */
  @Getter
  private int powerLevel;
  /**
   * The amount of ability points a player has for upgrading their abilities.
   */
  @Getter
  @Setter
  private int abilityPoints;
  /**
   * The array of skills for the player
   */
  private ArrayList<Skill> skills = new ArrayList<>();
  /**
   * The abilities a player has unlocked and has not yet accepted or denied. Whenever a player next opens the mcmmo main gui they should be forced to go through these
   */
  @Getter
  private ArrayList<UnlockedAbilities> pendingUnlockAbilities = new ArrayList<>();

  private HashMap<UnlockedAbilities, Long> abilitiesOnCooldown = new HashMap<>();

  @Getter
  @Setter
  private boolean hasBleedImmunity = false;

  @Getter
  private ArrayList<BaseAbility> abilityLoadout = new ArrayList<>();

  @Getter
  @Setter
  private DisplayType displayType = DisplayType.EXP_SCOREBOARD;

  @Getter
  @Setter
  private boolean isReadying = false;

  @Getter
  @Setter
  private PlayerReadyBit readyingAbilityBit = null;

  /**
   * The file configuration of the player that we get to edit.
   */
  //TODO Migrate these to a database manager instead of here. These are just here for white box testing
  private FileConfiguration playerData;
  private File playerFile;

  public McMMOPlayer(UUID uuid){
	this.uuid = uuid;
	this.playerFile = new File(Mcmmox.getInstance().getDataFolder(), File.separator + "PlayerData" + File.separator + uuid.toString() + ".yml");
	this.playerData = YamlConfiguration.loadConfiguration(playerFile);
	boolean isNew = false;
	if(!playerFile.exists()){
	  isNew = true;
	  try{
		playerFile.createNewFile();
	  }catch(IOException e){
		e.printStackTrace();
	  }
	}
	if(isNew){
	  for(Skills type : Skills.values()){
		playerData.set(type.getName() + ".Level", 0);
		playerData.set(type.getName() + ".CurrentExp", 0);
	  }
	  for(DefaultAbilities ability : DefaultAbilities.values()){
		playerData.set(ability.getSkill() + "." + ability.getName() + ".IsToggled", true);
	  }
	  for(UnlockedAbilities ability : UnlockedAbilities.values()){
		playerData.set(ability.getSkill() + "." + ability.getName() + ".Tier", 0);
		playerData.set(ability.getSkill() + "." + ability.getName() + ".IsToggled", true);
	  }
	  playerData.set("DisplayType", displayType.getName());
	  playerData.set("Cooldowns.placeholder", null);
	  playerData.set("AbilityPoints", 0);
	  playerData.set("PendingAbilitiesUnlocked.placeholder", null);
	  playerData.set("AbilityLoadout.placeholder", null);
	  try{
		playerData.save(playerFile);
	  }catch(IOException e){
		e.printStackTrace();
	  }
	}
	this.displayType = DisplayType.fromString(playerData.getString("DisplayType"));
	this.abilityPoints = playerData.getInt("AbilityPoints");
	ArrayList<UnlockedAbilities> list = new ArrayList<>();
	for(String string : playerData.getStringList("PendingAbilitiesUnlocked")){
	  UnlockedAbilities unlockedAbilities = UnlockedAbilities.fromString(string);
	  list.add(unlockedAbilities);
	}
	this.pendingUnlockAbilities = list;
	//Initialize swords
	Arrays.stream(Skills.values()).forEach(skill -> {
	  HashMap<GenericAbility, BaseAbility> abilityMap = new HashMap<>();
	  if(skill.equals(Skills.SWORDS)){
	    //Initialize bleed
		Bleed bleed = new Bleed();
		bleed.setToggled(playerData.getBoolean("Swords.Bleed.IsToggled"));
		//Initialize Deeper Wound
		DeeperWound deeperWound = new DeeperWound();
		deeperWound.setToggled(playerData.getBoolean("Swords.DeeperWound.IsToggled"));
		deeperWound.setCurrentTier(playerData.getInt("Swords.DeeperWound.Tier"));
		if(playerData.getInt("Swords.DeeperWound.Tier") != 0){
		  deeperWound.setUnlocked(true);
		}
		//Initialize Rage Spike
		RageSpike rageSpike = new RageSpike();
		rageSpike.setToggled(playerData.getBoolean("Swords.RageSpike.IsToggled"));
		rageSpike.setCurrentTier(playerData.getInt("Swords.RageSpike.Tier"));
		if(playerData.getInt("Swords.RageSpike.Tier") != 0){
		  rageSpike.setUnlocked(true);
		}
		abilityMap.put(DefaultAbilities.BLEED, bleed);
		abilityMap.put(UnlockedAbilities.DEEPER_WOUND, deeperWound);
		abilityMap.put(UnlockedAbilities.RAGE_SPIKE, rageSpike);
		//Create skill
		Swords swords = new Swords(playerData.getInt("Swords.Level"),
			playerData.getInt("Swords.CurrentExp"), abilityMap, this);
		skills.add(swords);
	  }
	});
	for(String s : playerData.getStringList("AbilityLoadout")){
	  //It has to be an unlocked ability since default ones cant be in the loadout
	  UnlockedAbilities ability = UnlockedAbilities.fromString(s);
	  abilityLoadout.add(getSkill(ability.getSkill()).getAbility(ability));
	}
	if(playerData.contains("Cooldowns")){
	  for(String s : playerData.getConfigurationSection("Cooldowns").getKeys(false)){
		UnlockedAbilities ab = UnlockedAbilities.fromString(s);
		long cooldown = playerData.getLong("Cooldowns." + s);
		if(cooldown <= 0){
		  continue;
		}
		abilitiesOnCooldown.put(ab, cooldown);
	  }
	}
	updatePowerLevel();
	for(Skill s : skills){
	  s.updateExpToLevel();
	}
  }

  public OfflinePlayer getOfflineMcMMOPlayer(){
	return Bukkit.getOfflinePlayer(uuid);
  }

  /**
   * Updates the power level of the player by adding together all of the levels of each skill
   *
   * @return The power level of the player
   */
  public int updatePowerLevel(){
	if(skills.isEmpty()){
	  powerLevel = 0;
	}
	else{
	  final AtomicInteger powerLevelUpdater = new AtomicInteger(0);
	  skills.stream().forEach(skill -> powerLevelUpdater.addAndGet(skill.getCurrentLevel()));
	  this.powerLevel = powerLevelUpdater.get();
	}
	return powerLevel;
  }

  /**
   * Get the instance of the players skill
   *
   * @param skill The skill you want to get an instance of. Will return null if the skill doesnt exist
   * @return The instance of the players skill of the type provided
   */
  public Skill getSkill(String skill){
	return skills.stream().filter(n -> n.getName().equalsIgnoreCase(skill)).findAny().orElse(null);
  }

  /**
   * Get the instance of the players skill
   *
   * @param skill The skill you want to get an instance of. Will return null if the skill doesnt exist. Good lucky getting null out of this xD
   * @return The instance of the players skill of the type provided
   */
  public Skill getSkill(Skills skill){
	return skills.stream().filter(n -> n.getName().equalsIgnoreCase(skill.getName())).findFirst().orElse(null);
  }

  public BaseAbility getBaseAbility(GenericAbility ability){
	return getSkill(ability.getSkill()).getAbility(ability);
  }

  public void giveExp(Skills skill, int exp, GainReason reason){
	getSkill(skill).giveExp(exp, reason);
  }

  /**
   * Get the cooldown of an ability.
   *
   * @param ability The ability type you want to check the cooldown for
   * @return The endtime of the cooldown in milis. If the cooldown doesnt exist return -1
   */
  public long getCooldown(GenericAbility ability){

	if(abilitiesOnCooldown.containsKey(ability)){
	  return TimeUnit.MILLISECONDS.toSeconds(abilitiesOnCooldown.get(ability) - Calendar.getInstance().getTimeInMillis());
	}
	else{
	  return -1;
	}
  }

  public long getCooldown(Skills skill){
    for(UnlockedAbilities ab : abilitiesOnCooldown.keySet()){
      if(ab.getSkill().equalsIgnoreCase(skill.getName())){
        return TimeUnit.MILLISECONDS.toSeconds(abilitiesOnCooldown.get(ab) - Calendar.getInstance().getTimeInMillis());
	  }
	}
	return -1;
  }

  public void addAbilityOnCooldown(UnlockedAbilities ability, long timeToEnd){
    abilitiesOnCooldown.put(ability, timeToEnd);
  }

  public void removeAbilityOnCooldown(UnlockedAbilities ability){
    abilitiesOnCooldown.remove(ability);
  }

  public void updateCooldowns(){
    ArrayList<UnlockedAbilities> toRemove = new ArrayList<>();
    for(UnlockedAbilities ability : abilitiesOnCooldown.keySet()){
      long timeToEnd = abilitiesOnCooldown.get(ability);
      if(Calendar.getInstance().getTimeInMillis() >= timeToEnd){
        this.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			Mcmmox.getInstance().getLangFile().getString("Messages.Players.CooldownExpire").replace("%Ability%", ability.getName())));
        toRemove.add(ability);
	  }
	}
	toRemove.stream().forEach(ab -> abilitiesOnCooldown.remove(ab));
  }

  public void saveData(){
	//for(Skills type : Skills.values()){
	Skills type = Skills.SWORDS;
	Skill skill = getSkill(type);
	playerData.set(type.getName() + ".Level", skill.getCurrentLevel());
	playerData.set(type.getName() + ".CurrentExp", skill.getCurrentExp());
	skill.getAbilityKeys().forEach(ability -> {
	  if(ability instanceof DefaultAbilities){
		playerData.set(type.getName() + "." + ability.getName() + ".IsToggled", skill.getAbility(ability).isToggled());
	  }
	  if(ability instanceof UnlockedAbilities){
		playerData.set(type.getName() + "." + ability.getName() + ".Tier", skill.getAbility(ability).getCurrentTier());
		playerData.set(type.getName() + "." + ability.getName() + ".IsToggled", skill.getAbility(ability).isToggled());
	  }
	  if(abilitiesOnCooldown.containsKey(ability)){
		playerData.set("Cooldowns." + ability.getName(), this.getCooldown(ability));
	  }
	});
	playerData.set("DisplayType", displayType.getName());
	playerData.set("AbilityPoints", abilityPoints);
	playerData.set("PendingAbilitiesUnlocked", pendingUnlockAbilities.stream().map(UnlockedAbilities::getName).collect(Collectors.toList()));
	playerData.set("AbilityLoadout", abilityLoadout.stream().map(ability -> ability.getGenericAbility().getName()).collect(Collectors.toList()));
	try{
	  playerData.save(playerFile);
	}catch(IOException e){
	  e.printStackTrace();
	}
  }

  public void addPendingAbilityUnlock(UnlockedAbilities abilities){
	this.pendingUnlockAbilities.add(abilities);
  }

  public void removePendingAbilityUnlock(UnlockedAbilities abilities){
    this.pendingUnlockAbilities.remove(abilities);
  }

  public boolean hasPendingAbility(){
	return !this.pendingUnlockAbilities.isEmpty();
  }

  public boolean isOnline(){
	return Bukkit.getOfflinePlayer(uuid).isOnline();
  }

  public Player getPlayer(){
	return (Player) Bukkit.getOfflinePlayer(uuid);
  }

  public void addAbilityToLoadout(BaseAbility ability){
	abilityLoadout.add(ability);
	saveData();
  }

  public boolean doesPlayerHaveAbilityInLoadout(GenericAbility ability){
	return abilityLoadout.stream().filter(ability1 -> ability1.getGenericAbility().getName().equalsIgnoreCase(ability.getName())).findFirst().orElse(null) != null;
  }

  public boolean doesPlayerHaveActiveAbilityFromSkill(Skills skill){
	return abilityLoadout.stream().filter(ability -> ability.getGenericAbility().getSkill().equals(skill.getName()))
		.filter(ability -> ability.getGenericAbility().getAbilityType() == AbilityType.ACTIVE).findFirst().orElse(null) != null;
  }

  public UnlockedAbilities getActiveAbilityForSkill(Skills skill){
	return (UnlockedAbilities) abilityLoadout.stream().filter(ability -> ability.getGenericAbility().getSkill().equals(skill.getName()))
		.filter(ability -> ability.getGenericAbility().getAbilityType() == AbilityType.ACTIVE).findFirst().orElse(null).getGenericAbility();
  }

  @Override
  public boolean equals(Object object){
	if(object instanceof McMMOPlayer){
	  return uuid.equals(((McMMOPlayer) object).getUuid());
	}
	else if(object instanceof Player){
	  return uuid.equals(((Player) object).getUniqueId());
	}
	else if(object instanceof UUID){
	  return uuid.equals((object));
	}
	return false;
  }
}
