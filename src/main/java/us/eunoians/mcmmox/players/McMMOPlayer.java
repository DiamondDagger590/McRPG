package us.eunoians.mcmmox.players;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import us.eunoians.mcmmox.Mcmmox;
import us.eunoians.mcmmox.abilities.BaseAbility;
import us.eunoians.mcmmox.abilities.herbalism.*;
import us.eunoians.mcmmox.abilities.mining.*;
import us.eunoians.mcmmox.abilities.swords.*;
import us.eunoians.mcmmox.abilities.unarmed.*;
import us.eunoians.mcmmox.api.events.mcmmo.SmitingFistEvent;
import us.eunoians.mcmmox.api.util.Methods;
import us.eunoians.mcmmox.skills.*;
import us.eunoians.mcmmox.types.*;
import us.eunoians.mcmmox.util.mcmmo.MobHealthbarUtils;

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
  private DisplayType displayType = DisplayType.SCOREBOARD;

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
  private boolean canSmite;

  @Getter
  @Setter
  private SmitingFistEvent smitingFistData;

  @Getter
  @Setter
  private boolean canDenseImpact;

  @Getter
  @Setter
  private int armourDmg;

  @Getter
  @Setter
  private MobHealthbarUtils.MobHealthbarType healthbarType = MobHealthbarUtils.MobHealthbarType.BAR;

  @Getter
  @Setter
  private long endTimeForReplaceCooldown;

  @Getter
  @Setter
  private boolean keepHandEmpty = false;

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
		playerData.set(ability.getSkill() + "." + ability.getName().replace(" " , "").replace("_", "") + ".IsToggled", true);
	  }
	  for(UnlockedAbilities ability : UnlockedAbilities.values()){
		playerData.set(ability.getSkill() + "." + ability.getName() + ".Tier", 0);
		playerData.set(ability.getSkill() + "." + ability.getName() + ".IsToggled", true);
	  }
	  playerData.set("DisplayType", displayType.getName());
	  playerData.set("HealthType", healthbarType.getName());
	  playerData.set("KeepHandEmpty", keepHandEmpty);
	  playerData.set("Cooldowns.placeholder", null);
	  playerData.set("AbilityPoints", 0);
	  playerData.set("RemoteTransferBlocks", null);
	  playerData.set("PendingAbilitiesUnlocked.placeholder", null);
	  playerData.set("AbilityLoadout.placeholder", null);
	  playerData.set("Mining.RemoteTransfer.LinkedLocation", 0);
	  playerData.set("ReplaceAbilityCooldown.placeholder", null);
	  try{
		playerData.save(playerFile);
	  }catch(IOException e){
		e.printStackTrace();
	  }
	}
	this.healthbarType = MobHealthbarUtils.MobHealthbarType.fromString(playerData.getString("HealthType"));
	this.keepHandEmpty = playerData.getBoolean("KeepHandEmpty");
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
		if(playerData.get("Mining.RemoteTransfer.LinkedLocation").equals(0)){
		  remoteTransfer.setLinkedChestLocation(null);
		}
		else{
		  remoteTransfer.setLinkedChestLocation((Location) playerData.get("Mining.RemoteTransfer.LinkedLocation"));
		  setLinkedToRemoteTransfer(true);
		}

		if(playerData.contains("RemoteTransferBlocks")){
		  for(String s : playerData.getConfigurationSection("RemoteTransferBlocks").getKeys(false)){
			remoteTransfer.getItemsToSync().put(Material.getMaterial(s), playerData.getBoolean("RemoteTransferBlocks." + s));
		  }
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

		//Initilize OreScanner
		OreScanner oreScanner = new OreScanner();
		oreScanner.setToggled(playerData.getBoolean("Mining.OreScanner.IsToggled"));
		oreScanner.setCurrentTier(playerData.getInt("Mining.OreScanner.Tier"));
		if(playerData.getInt("Mining.OreScanner.Tier") != 0){
		  oreScanner.setUnlocked(true);
		}

		abilityMap.put(DefaultAbilities.DOUBLE_DROP, doubleDrop);
		abilityMap.put(UnlockedAbilities.RICHER_ORES, richerOres);
		abilityMap.put(UnlockedAbilities.ITS_A_TRIPLE, itsATriple);
		abilityMap.put(UnlockedAbilities.REMOTE_TRANSFER, remoteTransfer);
		abilityMap.put(UnlockedAbilities.SUPER_BREAKER, superBreaker);
		abilityMap.put(UnlockedAbilities.BLAST_MINING, blastMining);
		abilityMap.put(UnlockedAbilities.ORE_SCANNER, oreScanner);

		Mining mining = new Mining(playerData.getInt("Mining.Level"),
			playerData.getInt("Mining.CurrentExp"), abilityMap, this);
		skills.add(mining);
	  }
	  else if(skill.equals(Skills.UNARMED)){
		//Initialize Sticky Fingers
		StickyFingers stickyFingers = new StickyFingers();
		stickyFingers.setToggled(playerData.getBoolean("Unarmed.StickyFingers.IsToggled"));
		//Initialize Tighter Grip
		TighterGrip tighterGrip = new TighterGrip();
		tighterGrip.setToggled(playerData.getBoolean("Unarmed.TighterGrip.IsToggled"));
		tighterGrip.setCurrentTier(playerData.getInt("Unarmed.TighterGrip.Tier"));
		if(playerData.getInt("Unarmed.TighterGrip.Tier") != 0){
		  tighterGrip.setUnlocked(true);
		}
		//Initialize Disarm
		Disarm disarm = new Disarm();
		disarm.setToggled(playerData.getBoolean("Unarmed.Disarm.IsToggled"));
		disarm.setCurrentTier(playerData.getInt("Unarmed.Disarm.Tier"));
		if(playerData.getInt("Unarmed.Disarm.Tier") != 0){
		  disarm.setUnlocked(true);
		}
		//Initialize Iron Arm
		IronArm ironArm = new IronArm();
		ironArm.setToggled(playerData.getBoolean("Unarmed.IronArm.IsToggled"));
		ironArm.setCurrentTier(playerData.getInt("Unarmed.IronArm.Tier"));
		if(playerData.getInt("Unarmed.IronArm.Tier") != 0){
		  ironArm.setUnlocked(true);
		}
		//Initialize Berserk
		Berserk berserk = new Berserk();
		berserk.setToggled(playerData.getBoolean("Unarmed.Berserk.IsToggled"));
		berserk.setCurrentTier(playerData.getInt("Unarmed.Berserk.Tier"));
		if(playerData.getInt("Unarmed.Berserk.Tier") != 0){
		  berserk.setUnlocked(true);
		}
		//Initialize Smiting Fist
		SmitingFist smitingFist = new SmitingFist();
		smitingFist.setToggled(playerData.getBoolean("Unarmed.SmitingFist.IsToggled"));
		smitingFist.setCurrentTier(playerData.getInt("Unarmed.SmitingFist.Tier"));
		if(playerData.getInt("Unarmed.SmitingFist.Tier") != 0){
		  smitingFist.setUnlocked(true);
		}
		//Initialize Dense Impact
		DenseImpact denseImpact = new DenseImpact();
		denseImpact.setToggled(playerData.getBoolean("Unarmed.DenseImpact.IsToggled"));
		denseImpact.setCurrentTier(playerData.getInt("Unarmed.DenseImpact.Tier"));
		if(playerData.getInt("Unarmed.DenseImpact.Tier") != 0){
		  denseImpact.setUnlocked(true);
		}
		abilityMap.put(DefaultAbilities.STICKY_FINGERS, stickyFingers);
		abilityMap.put(UnlockedAbilities.TIGHTER_GRIP, tighterGrip);
		abilityMap.put(UnlockedAbilities.DISARM, disarm);
		abilityMap.put(UnlockedAbilities.IRON_ARM, ironArm);
		abilityMap.put(UnlockedAbilities.BERSERK, berserk);
		abilityMap.put(UnlockedAbilities.SMITING_FIST, smitingFist);
		abilityMap.put(UnlockedAbilities.DENSE_IMPACT, denseImpact);
		//Create skill
		Unarmed unarmed = new Unarmed(playerData.getInt("Unarmed.Level"),
			playerData.getInt("Unarmed.CurrentExp"), abilityMap, this);
		skills.add(unarmed);
	  }
	  //Add herbalism
	  else if(skill.equals(Skills.HERBALISM)){
		//Initialize Too Many Plants
		TooManyPlants tooManyPlants = new TooManyPlants();
		tooManyPlants.setToggled(playerData.getBoolean("Herbalism.TooManyPlants.IsToggled"));
		//Initialize Replanting
		Replanting replanting = new Replanting();
		replanting.setToggled(playerData.getBoolean("Herbalism.Replanting.IsToggled"));
		replanting.setCurrentTier(playerData.getInt("Herbalism.Replanting.Tier"));
		if(playerData.getInt("Herbalism.Replanting.Tier") != 0){
		  replanting.setUnlocked(true);
		}
		//Initialize Farmers Diet
		FarmersDiet farmersDiet = new FarmersDiet();
		farmersDiet.setToggled(playerData.getBoolean("Herbalism.FarmersDiet.IsToggled"));
		farmersDiet.setCurrentTier(playerData.getInt("Herbalism.FarmersDiet.Tier"));
		if(playerData.getInt("Herbalism.FarmersDiet.Tier") != 0){
		  farmersDiet.setUnlocked(true);
		}
		//Initialize Diamond Flowers
		DiamondFlowers diamondFlowers = new DiamondFlowers();
		diamondFlowers.setToggled(playerData.getBoolean("Herbalism.DiamondFlowers.IsToggled"));
		diamondFlowers.setCurrentTier(playerData.getInt("Herbalism.DiamondFlowers.Tier"));
		if(playerData.getInt("Herbalism.DiamondFlowers.Tier") != 0){
		  diamondFlowers.setUnlocked(true);
		}
		//Initialize Mass Harvest
		MassHarvest massHarvest = new MassHarvest();
		massHarvest.setToggled(playerData.getBoolean("Herbalism.MassHarvest.IsToggled"));
		massHarvest.setCurrentTier(playerData.getInt("Herbalism.MassHarvest.Tier"));
		if(playerData.getInt("Herbalism.MassHarvest.Tier") != 0){
		  massHarvest.setUnlocked(true);
		}
		//Initialize Pans Blessing
		PansBlessing pansBlessing = new PansBlessing();
		pansBlessing.setToggled(playerData.getBoolean("Herbalism.PansBlessing.IsToggled"));
		pansBlessing.setCurrentTier(playerData.getInt("Herbalism.PansBlessing.Tier"));
		if(playerData.getInt("Herbalism.PansBlessing.Tier") != 0){
		  pansBlessing.setUnlocked(true);
		}
		//Initialize Natures Wrath
		NaturesWrath naturesWrath = new NaturesWrath();
		naturesWrath.setToggled(playerData.getBoolean("Herbalism.NaturesWrath.IsToggled"));
		naturesWrath.setCurrentTier(playerData.getInt("Herbalism.NaturesWrath.Tier"));
		if(playerData.getInt("Herbalism.NaturesWrath.Tier") != 0){
		  naturesWrath.setUnlocked(true);
		}
		abilityMap.put(DefaultAbilities.TOO_MANY_PLANTS, tooManyPlants);
		abilityMap.put(UnlockedAbilities.REPLANTING, replanting);
		abilityMap.put(UnlockedAbilities.FARMERS_DIET, farmersDiet);
		abilityMap.put(UnlockedAbilities.DIAMOND_FLOWERS, diamondFlowers);
		abilityMap.put(UnlockedAbilities.MASS_HARVEST, massHarvest);
		abilityMap.put(UnlockedAbilities.PANS_BLESSING, pansBlessing);
		abilityMap.put(UnlockedAbilities.NATURES_WRATH, naturesWrath);
		//Create skill
		Herbalism herbalism = new Herbalism(playerData.getInt("Herbalism.Level"),
			playerData.getInt("Herbalism.CurrentExp"), abilityMap, this);
		skills.add(herbalism);
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
		int cooldown = playerData.getInt("Cooldowns." + s);
		if(cooldown <= 0){
		  continue;
		}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, cooldown);
		abilitiesOnCooldown.put(ab, cal.getTimeInMillis());
	  }
	}
	if(playerData.contains("ReplaceAbilityCooldown")){
	  int cooldown = playerData.getInt("ReplaceAbilityCooldown");
	  if(cooldown > 0){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, cooldown);
		this.endTimeForReplaceCooldown = cal.getTimeInMillis();
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
   * @param ability   Ability to add on cooldown
   * @param timeToEnd The end time in milis
   */
  public void addAbilityOnCooldown(UnlockedAbilities ability, long timeToEnd){
	abilitiesOnCooldown.put(ability, timeToEnd);
  }

  /**
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
	if(abilitiesOnCooldown.isEmpty() && endTimeForReplaceCooldown == 0){
	  return;
	}
	for(UnlockedAbilities ability : abilitiesOnCooldown.keySet()){
	  long timeToEnd = abilitiesOnCooldown.get(ability);
	  if(Calendar.getInstance().getTimeInMillis() >= timeToEnd){
	    if(Bukkit.getOfflinePlayer(uuid).isOnline()){
		  this.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			  Mcmmox.getInstance().getLangFile().getString("Messages.Players.CooldownExpire").replace("%Ability%", ability.getName())));
		}
		toRemove.add(ability);
	  }
	}
	if(!toRemove.isEmpty()){
	  for(UnlockedAbilities ab : toRemove){
		playerData.set("Cooldowns." + ab.getName(), null);
		abilitiesOnCooldown.remove(ab);
	  }
	}
	long timeToEnd = this.endTimeForReplaceCooldown;
	if(timeToEnd != 0 && Calendar.getInstance().getTimeInMillis() >= timeToEnd){
	  playerData.set("ReplaceAbilityCooldown", null);
	  this.endTimeForReplaceCooldown = 0;
	  if(Bukkit.getOfflinePlayer(uuid).isOnline()){
		this.getPlayer().sendMessage(Methods.color(Mcmmox.getInstance().getPluginPrefix() +
			Mcmmox.getInstance().getLangFile().getString("Messages.Players.ReplaceCooldownExpire")));
	  }
	}
	try{
	  playerData.save(playerFile);
	}catch(IOException e){
	  e.printStackTrace();
	}
  }

  public void resetCooldowns(){
    abilitiesOnCooldown.clear();
    endTimeForReplaceCooldown = 0;
	playerData.set("Cooldowns.placeholder", null);
	playerData.set("ReplaceAbilityCooldown.void", null);
  }

  /**
   * Save players data
   */
  public void saveData(){
	//for(Skills type : Skills.values()){
	for(Skills type : Skills.values()){
	  Skill skill = getSkill(type);
	  playerData.set(type.getName() + ".Level", skill.getCurrentLevel());
	  playerData.set(type.getName() + ".CurrentExp", skill.getCurrentExp());
	  skill.getAbilityKeys().forEach(ability -> {
		if(ability instanceof DefaultAbilities){
		  playerData.set(type.getName() + "." + ability.getName().replace(" ", "").replace("_", "") + ".IsToggled", skill.getDefaultAbility().isToggled());
		}
		if(ability instanceof UnlockedAbilities){
		  playerData.set(type.getName() + "." + ability.getName() + ".Tier", skill.getAbility(ability).getCurrentTier());
		  playerData.set(type.getName() + "." + ability.getName() + ".IsToggled", skill.getAbility(ability).isToggled());
		}
		Calendar cal = Calendar.getInstance();
		if(abilitiesOnCooldown.containsKey(ability)){
		  Calendar temp = Calendar.getInstance();
		  temp.setTimeInMillis(this.getCooldown(ability));
		  int seconds = (int) (temp.getTimeInMillis() - cal.getTimeInMillis() )/ 1000;
		  playerData.set("Cooldowns." + ability.getName(), seconds);
		}
	  });
	}
	if(endTimeForReplaceCooldown  != 0){
	  Calendar temp = Calendar.getInstance();
	  temp.setTimeInMillis(endTimeForReplaceCooldown);
	  int seconds = (int) (temp.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() )/ 1000;
	  playerData.set("ReplaceAbilityCooldown", seconds);
	}
	RemoteTransfer remoteTransfer = (RemoteTransfer) getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
	if(remoteTransfer.getLinkedChestLocation() != null){
	  playerData.set("Mining.RemoteTransfer.LinkedLocation", remoteTransfer.getLinkedChestLocation());
	}
	else{
	  playerData.set("Mining.RemoteTransfer.LinkedLocation", 0);
	}
	if(abilitiesOnCooldown.isEmpty()){
	  playerData.set("Cooldowns.placeholder", null);
	}
	playerData.set("DisplayType", displayType.getName());
	playerData.set("KeepHandEmpty", keepHandEmpty);
	playerData.set("AbilityPoints", abilityPoints);
	playerData.set("PendingAbilitiesUnlocked", pendingUnlockAbilities.stream().map(UnlockedAbilities::getName).collect(Collectors.toList()));
	playerData.set("AbilityLoadout", abilityLoadout.stream().map(UnlockedAbilities::getName).collect(Collectors.toList()));

	RemoteTransfer transfer = (RemoteTransfer) getBaseAbility(UnlockedAbilities.REMOTE_TRANSFER);
	for(Material mat : transfer.getItemsToSync().keySet()){
	  playerData.set("RemoteTransferBlocks." + mat.toString(), transfer.getItemsToSync().get(mat));
	}
	try{
	  playerData.save(playerFile);
	}catch(IOException e){
	  e.printStackTrace();
	}
  }

  /**
   * @param abilities The ability to add to the pending list
   */
  public void addPendingAbilityUnlock(UnlockedAbilities abilities){
	this.pendingUnlockAbilities.add(abilities);
  }

  /**
   * @param abilities The ability to remove from the pending list
   */
  public void removePendingAbilityUnlock(UnlockedAbilities abilities){
	this.pendingUnlockAbilities.remove(abilities);
  }

  /**
   * @return true if the player has a pending ability and false if not
   */
  public boolean hasPendingAbility(){
	return !this.pendingUnlockAbilities.isEmpty();
  }

  /**
   * @return true if player is online false if not
   */
  public boolean isOnline(){
	return Bukkit.getOfflinePlayer(uuid).isOnline();
  }

  /**
   * @return Player instance of the mcmmo player. We dont safe check if they are online here
   */
  public Player getPlayer(){
	return (Player) Bukkit.getOfflinePlayer(uuid);
  }

  /**
   * @param ability Ability to add to the loadout
   */
  public void addAbilityToLoadout(UnlockedAbilities ability){
	abilityLoadout.add(ability);
	saveData();
  }

  /**
   * @param ability Ability to check for
   * @return true if the player has the ability in their loadout, false if not
   */
  public boolean doesPlayerHaveAbilityInLoadout(UnlockedAbilities ability){
	return abilityLoadout.stream().filter(ability1 -> ability1.getName().equalsIgnoreCase(ability.getName())).findFirst().orElse(null) != null;
  }

  /**
   * @param skill The skill to check if they have an active ability for
   * @return true if the player has an active ability, false if not
   */
  public boolean doesPlayerHaveActiveAbilityFromSkill(Skills skill){
	return abilityLoadout.stream().filter(ability -> ability.getSkill().equalsIgnoreCase(skill.getName()))
		.filter(ability -> ability.getAbilityType() == AbilityType.ACTIVE).findFirst().orElse(null) != null;
  }

  /**
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