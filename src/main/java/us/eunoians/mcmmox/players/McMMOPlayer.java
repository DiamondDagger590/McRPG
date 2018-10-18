package us.eunoians.mcmmox.players;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.abilities.mining.*;
import us.eunoians.mcmmox.abilities.swords.*;
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
  private ArrayList<UnlockedAbilities> abilityLoadout = new ArrayList<>();

  @Getter
  @Setter
  private DisplayType displayType = DisplayType.EXP_SCOREBOARD;

  @Getter
  @Setter
  private boolean isReadying = false;

  @Getter
  @Setter
  private PlayerReadyBit readyingAbilityBit = null;

  @Getter
  @Setter
  private boolean isLinkedToRemoteTransfer = false;

  @Getter
  @Setter
  private Location remoteTransferLocation = null;

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
	  playerData.set("Mining.RemoteTransfer.LinkedLocation", 0);
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
		//Initialize Bleed+
		BleedPlus bleedPlus = new BleedPlus();
		bleedPlus.setToggled(playerData.getBoolean("Swords.Bleed+.IsToggled"));
		bleedPlus.setCurrentTier(playerData.getInt("Swords.Bleed+.Tier"));
		if(playerData.getInt("Swords.Bleed+.Tier") != 0){
		  bleedPlus.setUnlocked(true);
		}
		//Initialize Vampire
		Vampire vampire = new Vampire();
		vampire.setToggled(playerData.getBoolean("Swords.Vampire.IsToggled"));
		vampire.setCurrentTier(playerData.getInt("Swords.Vampire.Tier"));
		if(playerData.getInt("Swords.Vampire.Tier") != 0){
		  vampire.setUnlocked(true);
		}
		//Initialize Serrated Strikes
		SerratedStrikes serratedStrikes = new SerratedStrikes();
		serratedStrikes.setToggled(playerData.getBoolean("Swords.SerratedStrikes.IsToggled"));
		serratedStrikes.setCurrentTier(playerData.getInt("Swords.SerratedStrikes.Tier"));
		if(playerData.getInt("Swords.SerratedStrikes.Tier") != 0){
		  serratedStrikes.setUnlocked(true);
		}
		//Initialize Rage Spike
		RageSpike rageSpike = new RageSpike();
		rageSpike.setToggled(playerData.getBoolean("Swords.RageSpike.IsToggled"));
		rageSpike.setCurrentTier(playerData.getInt("Swords.RageSpike.Tier"));
		if(playerData.getInt("Swords.RageSpike.Tier") != 0){
		  rageSpike.setUnlocked(true);
		}
		//Initialize Tainted Blade
		TaintedBlade taintedBlade = new TaintedBlade();
		taintedBlade.setToggled(playerData.getBoolean("Swords.TaintedBlade.IsToggled"));
		taintedBlade.setCurrentTier(playerData.getInt("Swords.TaintedBlade.Tier"));
		if(playerData.getInt("Swords.TaintedBlade.Tier") != 0){
		  taintedBlade.setUnlocked(true);
		}
		abilityMap.put(DefaultAbilities.BLEED, bleed);
		abilityMap.put(UnlockedAbilities.DEEPER_WOUND, deeperWound);
		abilityMap.put(UnlockedAbilities.BLEED_PLUS, bleedPlus);
		abilityMap.put(UnlockedAbilities.VAMPIRE, vampire);
		abilityMap.put(UnlockedAbilities.SERRATED_STRIKES, serratedStrikes);
		abilityMap.put(UnlockedAbilities.RAGE_SPIKE, rageSpike);
		abilityMap.put(UnlockedAbilities.TAINTED_BLADE, taintedBlade);
		//Create skill
		Swords swords = new Swords(playerData.getInt("Swords.Level"),
			playerData.getInt("Swords.CurrentExp"), abilityMap, this);
		skills.add(swords);
	  }
	  else if(skill.equals(Skills.MINING)){
	    //Initialize DoubleDrops
		DoubleDrop doubleDrop = new DoubleDrop();
		doubleDrop.setToggled(playerData.getBoolean("Mining.DoubleDrop.IsToggled"));

		//Initialize RicherOres
		RicherOres richerOres = new RicherOres();
		richerOres.setToggled(playerData.getBoolean("Mining.RicherOres.IsToggled"));
		richerOres.setCurrentTier(playerData.getInt("Mining.RicherOres.Tier"));
		if(playerData.getInt("Mining.RicherOres.Tier") != 0){
		  richerOres.setUnlocked(true);
		}

		//Initialize ItsATriple
		ItsATriple itsATriple = new ItsATriple();
		itsATriple.setToggled(playerData.getBoolean("Mining.ItsATriple.IsToggled"));
		itsATriple.setCurrentTier(playerData.getInt("Mining.ItsATriple.Tier"));
		if(playerData.getInt("Mining.ItsATriple.Tier") != 0){
		  itsATriple.setUnlocked(true);
		}

		//Initialize RemoteTransfer
		RemoteTransfer remoteTransfer = new RemoteTransfer();
		remoteTransfer.setToggled(playerData.getBoolean("Mining.RemoteTransfer.IsToggled"));
		remoteTransfer.setCurrentTier(playerData.getInt("Mining.RemoteTransfer.Tier"));
		if(playerData.getInt("Mining.RemoteTransfer.Tier") != 0){
		  remoteTransfer.setUnlocked(true);
		}

		//Initialize SuperBreaker
		SuperBreaker superBreaker = new SuperBreaker();
		superBreaker.setToggled(playerData.getBoolean("Mining.SuperBreaker.IsToggled"));
		superBreaker.setCurrentTier(playerData.getInt("Mining.SuperBreaker.Tier"));
		if(playerData.getInt("Mining.SuperBreaker.Tier") != 0){
		  superBreaker.setUnlocked(true);
		}

		//Initialize BlastMining
		BlastMining blastMining = new BlastMining();
		blastMining.setToggled(playerData.getBoolean("Mining.BlastMining.IsToggled"));
		blastMining.setCurrentTier(playerData.getInt("Mining.BlastMining.Tier"));
		if(playerData.getInt("Mining.BlastMining.Tier") != 0){
		  blastMining.setUnlocked(true);
		}

		abilityMap.put(DefaultAbilities.DOUBLE_DROP, doubleDrop);
		abilityMap.put(UnlockedAbilities.RICHER_ORES, richerOres);
		abilityMap.put(UnlockedAbilities.ITS_A_TRIPLE, itsATriple);
		abilityMap.put(UnlockedAbilities.REMOTE_TRANSFER, remoteTransfer);
		abilityMap.put(UnlockedAbilities.SUPER_BREAKER, superBreaker);

	  }
	});
	for(String s : playerData.getStringList("AbilityLoadout")){
	  //It has to be an unlocked ability since default ones cant be in the loadout
	  UnlockedAbilities ability = UnlockedAbilities.fromString(s);
	  abilityLoadout.add(ability);
	}
	if(playerData.contains("Cooldowns")){
	  for(String s : playerData.getConfigurationSection("Cooldowns").getKeys(false)){
		UnlockedAbilities ab = UnlockedAbilities.fromString(s);
		long cooldown = playerData.getLong("Cooldowns." + s);
		if(cooldown <= 0){
		  continue;
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, (int) cooldown);
		abilitiesOnCooldown.put(ab, cal.getTimeInMillis());
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
	  skills.forEach(skill -> powerLevelUpdater.addAndGet(skill.getCurrentLevel()));
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

  /**
   * Get the cooldown of an ability (this works since a skill can only have one active ability unlocked
   *
   * @param skill The skill to check
   * @return The time to end in millis or -1 if it doesnt exist
   */
  public long getCooldown(Skills skill){
    for(UnlockedAbilities ab : abilitiesOnCooldown.keySet()){
      if(ab.getSkill().equalsIgnoreCase(skill.getName())){
        return TimeUnit.MILLISECONDS.toSeconds(abilitiesOnCooldown.get(ab) - Calendar.getInstance().getTimeInMillis());
	  }
	}
	return -1;
  }

  /**
   *
   * @param ability Ability to add on cooldown
   * @param timeToEnd The end time in milis
   */
  public void addAbilityOnCooldown(UnlockedAbilities ability, long timeToEnd){
    abilitiesOnCooldown.put(ability, timeToEnd);
  }

  /**
   *
   * @param ability Ability to remove from array
   */
  public void removeAbilityOnCooldown(UnlockedAbilities ability){
    abilitiesOnCooldown.remove(ability);
  }

  /**
   * Update all the cooldowns and verify if they are valid
   */
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
	if(!toRemove.isEmpty()){
	  for(UnlockedAbilities ab : abilitiesOnCooldown.keySet()){
		playerData.set("Cooldowns." + ab.getName(), null);
		abilitiesOnCooldown.remove(ab);
		try{
		  playerData.save(playerFile);
		}catch(IOException e){
		  e.printStackTrace();
		}
	  }
	}
  }

  /**
   * Save players data
   */
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
	if(abilitiesOnCooldown.isEmpty()){
	  playerData.set("Cooldowns.placeholder", null);
	}
	playerData.set("DisplayType", displayType.getName());
	playerData.set("AbilityPoints", abilityPoints);
	playerData.set("PendingAbilitiesUnlocked", pendingUnlockAbilities.stream().map(UnlockedAbilities::getName).collect(Collectors.toList()));
	playerData.set("AbilityLoadout", abilityLoadout.stream().map(UnlockedAbilities::getName).collect(Collectors.toList()));
	try{
	  playerData.save(playerFile);
	}catch(IOException e){
	  e.printStackTrace();
	}
  }

  /**
   *
   * @param abilities The ability to add to the pending list
   */
  public void addPendingAbilityUnlock(UnlockedAbilities abilities){
	this.pendingUnlockAbilities.add(abilities);
  }

  /**
   *
   * @param abilities The ability to remove from the pending list
   */
  public void removePendingAbilityUnlock(UnlockedAbilities abilities){
    this.pendingUnlockAbilities.remove(abilities);
  }

  /**
   *
   * @return true if the player has a pending ability and false if not
   */
  public boolean hasPendingAbility(){
	return !this.pendingUnlockAbilities.isEmpty();
  }

  /**
   *
   * @return true if player is online false if not
   */
  public boolean isOnline(){
	return Bukkit.getOfflinePlayer(uuid).isOnline();
  }

  /**
   *
   * @return Player instance of the mcmmo player. We dont safe check if they are online here
   */
  public Player getPlayer(){
	return (Player) Bukkit.getOfflinePlayer(uuid);
  }

  /**
   *
   * @param ability Ability to add to the loadout
   */
  public void addAbilityToLoadout(UnlockedAbilities ability){
	abilityLoadout.add(ability);
	saveData();
  }

  /**
   *
   * @param ability Ability to check for
   * @return true if the player has the ability in their loadout, false if not
   */
  public boolean doesPlayerHaveAbilityInLoadout(UnlockedAbilities ability){
	return abilityLoadout.stream().filter(ability1 -> ability1.getName().equalsIgnoreCase(ability.getName())).findFirst().orElse(null) != null;
  }

  /**
   *
   * @param skill The skill to check if they have an active ability for
   * @return true if the player has an active ability, false if not
   */
  public boolean doesPlayerHaveActiveAbilityFromSkill(Skills skill){
	return abilityLoadout.stream().filter(ability -> ability.getSkill().equals(skill.getName()))
		.filter(ability -> ability.getAbilityType() == AbilityType.ACTIVE).findFirst().orElse(null) != null;
  }

  /**
   *
   * @param skill Skill to get the ability for
   * @return The UnlockedAbilities instance of the active ability belonging to the provided skill a player has, or null if they dont have any
   */
  public UnlockedAbilities getActiveAbilityForSkill(Skills skill){
	return abilityLoadout.stream().filter(ability -> ability.getSkill().equals(skill.getName()))
		.filter(ability -> ability.getAbilityType() == AbilityType.ACTIVE).findFirst().orElse(null);
  }

  public void replaceAbility(UnlockedAbilities oldAbility, UnlockedAbilities newAbility){
    for(int i = 0; i < abilityLoadout.size(); i++){
      if(abilityLoadout.get(i).equals(oldAbility)){
        abilityLoadout.set(i, newAbility);
        return;
	  }
	}
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
