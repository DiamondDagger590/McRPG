package us.eunoians.mcrpg;

/*import com.cyr1en.javen.Javen;
import com.cyr1en.javen.annotation.Lib;
import com.cyr1en.mcutils.PluginUpdater;
import com.cyr1en.mcutils.initializers.Initializable;
import com.cyr1en.mcutils.initializers.annotation.Ignore;
import com.cyr1en.mcutils.initializers.annotation.Initialize;
import com.cyr1en.mcutils.initializers.annotation.process.Initializer;
import com.cyr1en.mcutils.logger.Logger;*/

import com.cyr1en.javen.Javen;
import com.cyr1en.javen.annotation.Lib;
import com.google.common.base.Charsets;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import lombok.Setter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.api.displays.DisplayManager;
import us.eunoians.mcrpg.api.leaderboards.LeaderboardHeadManager;
import us.eunoians.mcrpg.api.leaderboards.LeaderboardManager;
import us.eunoians.mcrpg.api.util.BuriedTreasureData;
import us.eunoians.mcrpg.api.util.DiamondFlowersData;
import us.eunoians.mcrpg.api.util.FileManager;
import us.eunoians.mcrpg.api.util.HiddenConfig;
import us.eunoians.mcrpg.api.util.McRPGPlaceHolders;
import us.eunoians.mcrpg.api.util.RemoteTransferTracker;
import us.eunoians.mcrpg.api.util.WorldModifierManager;
import us.eunoians.mcrpg.api.util.artifacts.ArtifactManager;
import us.eunoians.mcrpg.api.util.blood.BloodManager;
import us.eunoians.mcrpg.api.util.books.BookManager;
import us.eunoians.mcrpg.api.util.brewing.BrewingStandManager;
import us.eunoians.mcrpg.api.util.brewing.PotionRecipeManager;
import us.eunoians.mcrpg.api.util.exp.ExpPermissionManager;
import us.eunoians.mcrpg.api.util.fishing.FishingItemManager;
import us.eunoians.mcrpg.api.util.levelcmds.LevelCommandManager;
import us.eunoians.mcrpg.commands.GUIOpenCommand;
import us.eunoians.mcrpg.commands.McAdmin;
import us.eunoians.mcrpg.commands.McConvert;
import us.eunoians.mcrpg.commands.McDisplay;
import us.eunoians.mcrpg.commands.McExp;
import us.eunoians.mcrpg.commands.McHelp;
import us.eunoians.mcrpg.commands.McLink;
import us.eunoians.mcrpg.commands.McParty;
import us.eunoians.mcrpg.commands.McRPGStub;
import us.eunoians.mcrpg.commands.McRank;
import us.eunoians.mcrpg.commands.McRedeem;
import us.eunoians.mcrpg.commands.McSpy;
import us.eunoians.mcrpg.commands.McUnlink;
import us.eunoians.mcrpg.commands.prompts.McAdminPrompt;
import us.eunoians.mcrpg.commands.prompts.McDisplayPrompt;
import us.eunoians.mcrpg.commands.prompts.McExpPrompt;
import us.eunoians.mcrpg.commands.prompts.McHelpPrompt;
import us.eunoians.mcrpg.commands.prompts.McPartyPrompt;
import us.eunoians.mcrpg.commands.prompts.McRankPrompt;
import us.eunoians.mcrpg.commands.prompts.McRedeemPrompt;
import us.eunoians.mcrpg.database.McRPGDb;
import us.eunoians.mcrpg.events.external.sickle.Sickle;
import us.eunoians.mcrpg.events.mcrpg.AbilityActivate;
import us.eunoians.mcrpg.events.mcrpg.AbilityUnlock;
import us.eunoians.mcrpg.events.mcrpg.AbilityUpgrade;
import us.eunoians.mcrpg.events.mcrpg.BleedHandler;
import us.eunoians.mcrpg.events.mcrpg.DisarmHandler;
import us.eunoians.mcrpg.events.mcrpg.LoadoutAdd;
import us.eunoians.mcrpg.events.mcrpg.McRPGExpGain;
import us.eunoians.mcrpg.events.mcrpg.McRPGPlayerLevelChange;
import us.eunoians.mcrpg.events.mcrpg.PartyLevelUp;
import us.eunoians.mcrpg.events.vanilla.ArrowHitEvent;
import us.eunoians.mcrpg.events.vanilla.BreakEvent;
import us.eunoians.mcrpg.events.vanilla.CallOfWildListener;
import us.eunoians.mcrpg.events.vanilla.ChatEvent;
import us.eunoians.mcrpg.events.vanilla.CheckReadyEvent;
import us.eunoians.mcrpg.events.vanilla.DeathEvent;
import us.eunoians.mcrpg.events.vanilla.DropItemEvent;
import us.eunoians.mcrpg.events.vanilla.EnchantingEvent;
import us.eunoians.mcrpg.events.vanilla.EntityDeathListener;
import us.eunoians.mcrpg.events.vanilla.EntityTameListener;
import us.eunoians.mcrpg.events.vanilla.FishCatchEvent;
import us.eunoians.mcrpg.events.vanilla.InteractHandler;
import us.eunoians.mcrpg.events.vanilla.InvClickEvent;
import us.eunoians.mcrpg.events.vanilla.InvCloseEvent;
import us.eunoians.mcrpg.events.vanilla.MoveEvent;
import us.eunoians.mcrpg.events.vanilla.MoveItemEvent;
import us.eunoians.mcrpg.events.vanilla.PickupEvent;
import us.eunoians.mcrpg.events.vanilla.PlayerLoginEvent;
import us.eunoians.mcrpg.events.vanilla.PlayerLogoutEvent;
import us.eunoians.mcrpg.events.vanilla.PlayerNomNomEvent;
import us.eunoians.mcrpg.events.vanilla.PlayerTossItemEvent;
import us.eunoians.mcrpg.events.vanilla.PotionDrinkEvent;
import us.eunoians.mcrpg.events.vanilla.PotionEffectEvent;
import us.eunoians.mcrpg.events.vanilla.ShiftToggle;
import us.eunoians.mcrpg.events.vanilla.ShootEvent;
import us.eunoians.mcrpg.events.vanilla.SignEvent;
import us.eunoians.mcrpg.events.vanilla.SpawnEvent;
import us.eunoians.mcrpg.events.vanilla.VanillaDamageEvent;
import us.eunoians.mcrpg.events.vanilla.WolfValidator;
import us.eunoians.mcrpg.events.vanilla.WorldListener;
import us.eunoians.mcrpg.party.Party;
import us.eunoians.mcrpg.party.PartyManager;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.util.blockmeta.chunkmeta.ChunkManager;
import us.eunoians.mcrpg.util.blockmeta.chunkmeta.ChunkManagerFactory;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/*JAVEN ISSUES*/
@Lib(group = "com.google.guava", name = "guava", version = "27.1-jre")
@Lib(group = "com.github.DiamondDagger590", name = "FlatDB", version = "1.0.7")
//@Lib(group = "org.javassist", name = "javassist", version = "3.21.0-GA")
@Lib(group = "com.github.DiamondDagger590", name = "EnumToYAML", version = "1.0")

public class McRPG extends JavaPlugin {//implements //Initializable {

  private static McRPG instance;
  private static final int id = 6386;
  
 // @Getter private PluginUpdater pluginUpdater;
  @Getter private FileManager fileManager;
  @Getter private ExpPermissionManager expPermissionManager;
  @Getter private McRPGDb mcRPGDb;
  @Getter private DisplayManager displayManager;
  @Getter private static ChunkManager placeStore;
  @Getter private RemoteTransferTracker remoteTransferTracker;
  @Getter private FishingItemManager fishingItemManager;
  @Getter private LeaderboardManager leaderboardManager;
  @Getter private LeaderboardHeadManager leaderboardHeadManager;
  @Getter private BookManager bookManager;
  @Getter private ArtifactManager artifactManager;
  @Getter private PotionRecipeManager potionRecipeManager;
  @Getter private BrewingStandManager brewingStandManager;
  @Getter private LevelCommandManager levelCommandManager;
  @Getter private WorldModifierManager worldModifierManager;
  @Getter private PartyManager partyManager;

  //Needed to support McMMO's Healthbars
  @Getter private final String customNameKey = "mcMMO: Custom Name";
  @Getter private final String customVisibleKey = "mcMMO: Name Visibility";
  @Getter private boolean healthBarPluginEnabled = false;
  @Getter private boolean mvdwEnabled = false;
  @Getter private boolean papiEnabled = false;
  @Getter private boolean ncpEnabled = false;
  @Getter private boolean sickleEnabled = false;
  @Getter private boolean worldGuardEnabled = false;
  @Getter private boolean mcmmoEnabled = false;
  @Getter @Setter private WGSupportManager wgSupportManager;

  @Override
  public void onEnable() {
   // Logger.init("McRPG");
    if(!getDataFolder().exists()){
      getDataFolder().mkdir();
    }
    //JAVEN ISSUES
    Path path = Paths.get(getDataFolder().getAbsolutePath() + "/libs");
    Javen javen = new Javen(path);
    javen.addRepository("jitPack", "https://jitpack.io");
    javen.addClassLoader(this.getClass().getClassLoader());
    javen.loadDependencies();
    McRPG t = this;
    //Misc
    //localizationFiles = new LocalizationFiles(this, true);
    instance = this;
    fileManager = FileManager.getInstance().setup(this);
    if((Bukkit.getVersion().contains("1.14") || Bukkit.getVersion().contains("1.15")) &&
         fileManager.getFile(FileManager.Files.SWORDS_CONFIG).getString("DeeperWoundConfig.Item.Material").equals("ROSE_RED")){
      new BukkitRunnable(){
        @Override
        public void run(){
          Bukkit.getLogger().log(Level.WARNING, "You are on 1.14+. Please ensure in the swords.yml that you have changed ROSE_RED to RED_DYE" +
                                                  ". A fix has been put in place to retroactively handle this but it is still better to manually change it.");
        }
      }.runTaskLater(this, 400);
    }
    expPermissionManager = ExpPermissionManager.getInstance().setup(this);
    this.mcRPGDb = new McRPGDb(this);
    healthBarPluginEnabled = getServer().getPluginManager().getPlugin("HealthBar") != null;
    sickleEnabled = getServer().getPluginManager().getPlugin("Sickle") != null;
    fishingItemManager = new FishingItemManager();
    bookManager = new BookManager(this);
    artifactManager = new ArtifactManager(this);
    worldModifierManager = new WorldModifierManager();
    leaderboardManager = new LeaderboardManager(this);
    leaderboardHeadManager = new LeaderboardHeadManager();
    this.partyManager = new PartyManager();
    partyManager.init();
    Metrics metrics = new Metrics(this, id);
    brewingStandManager = new BrewingStandManager();
    brewingStandManager.updateNamingFormat();
    levelCommandManager = new LevelCommandManager();
    getLogger().info("Loading Potions");
    potionRecipeManager = new PotionRecipeManager();
    new PlayerManager(this);
    new BloodManager(this);
    if (healthBarPluginEnabled) {
      getLogger().info("HealthBar plugin found, McRPG's healthbars are automatically disabled.");
    }
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      papiEnabled = true;
      getLogger().info("Papi PlaceholderAPI found... registering hooks");
      new McRPGPlaceHolders().register();
    }
    new BukkitRunnable(){
      @Override
      public void run(){
        if(Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")){
          ncpEnabled = true;
          getLogger().info("NoCheatPlus found... will enable anticheat support");
        }
      }
    }.runTaskLater(this, 10 * 20);
    new BukkitRunnable(){
      public void run(){
        if(Bukkit.getPluginManager().isPluginEnabled("mcMMO")){
          mcmmoEnabled = true;
          getLogger().info("McMMO found... ready to convert.");
        }
      }
    }.runTaskLater(this, 400);
    if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
      worldGuardEnabled = true;
      wgSupportManager = new WGSupportManager(this);
    }
    placeStore = ChunkManagerFactory.getChunkManager(); // Get our ChunkletManager
    remoteTransferTracker = new RemoteTransferTracker();
    File folder = new File(getDataFolder(), File.separator + "remote_transfer_data");
    if (!folder.exists()) {
      folder.mkdir();
    }
    displayManager = DisplayManager.getInstance();
    DiamondFlowersData.init();
    BuriedTreasureData.init();
    HiddenConfig.getInstance();
    PlayerManager.startSave(this);
    //Commands
    getCommand("mcrpg").setExecutor(new McRPGStub());
    getCommand("mcdisplay").setExecutor(new McDisplay());
    getCommand("mcadmin").setExecutor(new McAdmin());
    getCommand("mclink").setExecutor(new McLink());
    getCommand("mcunlink").setExecutor(new McUnlink());
    getCommand("mchelp").setExecutor(new McHelp());
    getCommand("mcconvert").setExecutor(new McConvert());
    getCommand("mcredeem").setExecutor(new McRedeem());
    getCommand("mcrank").setExecutor(new McRank());
    getCommand("mcparty").setExecutor(new McParty());
    getCommand("mcexp").setExecutor(new McExp());
    getCommand("mcspy").setExecutor(new McSpy());
    getCommand("mcgui").setExecutor(new GUIOpenCommand());
    getCommand("mcdisplay").setTabCompleter(new McDisplayPrompt());
    getCommand("mcredeem").setTabCompleter(new McRedeemPrompt());
    getCommand("mcrank").setTabCompleter(new McRankPrompt());
    getCommand("mcadmin").setTabCompleter(new McAdminPrompt());
    getCommand("mcparty").setTabCompleter(new McPartyPrompt());
    getCommand("mcexp").setTabCompleter(new McExpPrompt());
    getCommand("mchelp").setTabCompleter(new McHelpPrompt());
    //Events
    getServer().getPluginManager().registerEvents(new PlayerLoginEvent(), this);
    getServer().getPluginManager().registerEvents(new MoveEvent(), this);
    getServer().getPluginManager().registerEvents(new PlayerLogoutEvent(), this);
    getServer().getPluginManager().registerEvents(new InvClickEvent(this), this);
    getServer().getPluginManager().registerEvents(new AbilityActivate(), this);
    getServer().getPluginManager().registerEvents(new McRPGPlayerLevelChange(), this);
    getServer().getPluginManager().registerEvents(new VanillaDamageEvent(), this);
    getServer().getPluginManager().registerEvents(new McRPGExpGain(), this);
    getServer().getPluginManager().registerEvents(new InvCloseEvent(), this);
    getServer().getPluginManager().registerEvents(new BleedHandler(), this);
    getServer().getPluginManager().registerEvents(new CheckReadyEvent(), this);
    getServer().getPluginManager().registerEvents(new ShiftToggle(), this);
    getServer().getPluginManager().registerEvents(new WorldListener(this), this);
    getServer().getPluginManager().registerEvents(new McLink(), this);
    getServer().getPluginManager().registerEvents(new BreakEvent(), this);
    getServer().getPluginManager().registerEvents(new AbilityUpgrade(), this);
    getServer().getPluginManager().registerEvents(new LoadoutAdd(), this);
    getServer().getPluginManager().registerEvents(new InteractHandler(), this);
    getServer().getPluginManager().registerEvents(new DisarmHandler(), this);
    getServer().getPluginManager().registerEvents(new PlayerNomNomEvent(), this);
    getServer().getPluginManager().registerEvents(new AbilityUnlock(), this);
    getServer().getPluginManager().registerEvents(new PickupEvent(), this);
    getServer().getPluginManager().registerEvents(new DropItemEvent(), this);
    getServer().getPluginManager().registerEvents(new ShootEvent(), this);
    getServer().getPluginManager().registerEvents(new ArrowHitEvent(), this);
    getServer().getPluginManager().registerEvents(new ChatEvent(), this);
    getServer().getPluginManager().registerEvents(new PlayerTossItemEvent(), this);
    getServer().getPluginManager().registerEvents(new FishCatchEvent(), this);
    getServer().getPluginManager().registerEvents(new DeathEvent(), this);
    getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
    getServer().getPluginManager().registerEvents(new SignEvent(), this);
    getServer().getPluginManager().registerEvents(new SpawnEvent(), this);
    getServer().getPluginManager().registerEvents(new PotionDrinkEvent(), this);
    getServer().getPluginManager().registerEvents(new PotionEffectEvent(), this);
    getServer().getPluginManager().registerEvents(new EnchantingEvent(), this);
    getServer().getPluginManager().registerEvents(new MoveItemEvent(), this);
    getServer().getPluginManager().registerEvents(new PartyLevelUp(), this);
    getServer().getPluginManager().registerEvents(new EntityTameListener(), this);
    getServer().getPluginManager().registerEvents(new WolfValidator(), this);
    getServer().getPluginManager().registerEvents(new CallOfWildListener(), this);
    
    if(sickleEnabled){
      getServer().getPluginManager().registerEvents(new Sickle(), this);
    }
    new BukkitRunnable(){
      @Override
      public void run(){
        if(bookManager == null){
          Bukkit.getLogger().log(Level.WARNING, "There was an error on startup for McRPG. Please seek the developer on Discord for support" +
                  " or a special JAR distribution");
        }
      }
    }.runTaskLater(this, 400);
    
    new BukkitRunnable(){
      @Override
      public void run(){
        Bukkit.getLogger().log(Level.INFO, "Purging parties of inactive players...");
        AtomicInteger amountToKick = new AtomicInteger(0);
        Collection<Party> parties = partyManager.getParties();
        Iterator<Party> iterator = parties.iterator();
        //This will run over time and not cause as much lag
        new BukkitRunnable(){
          @Override
          public void run(){
            if(!iterator.hasNext()){
              Bukkit.getLogger().log(Level.INFO, "Purged " + amountToKick.get() + " players from parties.");
              cancel();
            }
            else{
              Party party = iterator.next();
              if(party != null){
                amountToKick.addAndGet(party.purgeInactive(McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("InactivePurge.TimeInHoursToPurge", 168)));
              }
            }
          }
        }.runTaskTimer(McRPG.getInstance(), 10 * 20, 10 * 20);
      }
    }.runTaskTimer(this, 5 * 60 * 20, McRPG.getInstance().getFileManager().getFile(FileManager.Files.PARTY_CONFIG).getInt("InactivePurge.PurgeTaskDelay", 30) * 60 * 20);
  
    //Preload nbt class
    ItemStack itemStack = new ItemStack(Material.DIAMOND);
    NBTItem item = new NBTItem(itemStack);
    item.setString("temp", "temp");
  }

  @Override
  public void onDisable() {
    /*if (!Initializer.finished())
      Initializer.interrupt();*/
    PlayerManager.shutDownManager();
    brewingStandManager.shutDown();
    partyManager.saveAllParties();
    placeStore.saveAll();
  }

  public static McRPG getInstance() {
    if (instance == null)
      throw new NullPointerException("Plugin was not initialized.");
    return instance;
  }

  public String getPluginPrefix() {
    return getLangFile().getString("Messages.PluginInfo.PluginPrefix");
  }

  @Override
  public FileConfiguration getConfig() {
    return fileManager.getFile(FileManager.Files.CONFIG);
  }

  public FileConfiguration getLangFile() {
    return FileManager.Files.fromString(getConfig().getString("Configuration.LangFile")).getFile();
  }

  public InputStreamReader getResourceAsReader(String fileName) {
    InputStream in = getResource(fileName);
    return in == null ? null : new InputStreamReader(in, Charsets.UTF_8);
  }

  public static void resetPlaceStore(){
    placeStore = ChunkManagerFactory.getChunkManager(); // Get our ChunkletManager
  }
}
