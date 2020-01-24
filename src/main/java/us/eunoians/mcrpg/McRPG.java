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
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.eunoians.mcrpg.api.displays.DisplayManager;
import us.eunoians.mcrpg.api.leaderboards.LeaderboardHeadManager;
import us.eunoians.mcrpg.api.leaderboards.LeaderboardManager;
import us.eunoians.mcrpg.api.util.*;
import us.eunoians.mcrpg.api.util.books.BookManager;
import us.eunoians.mcrpg.api.util.exp.ExpPermissionManager;
import us.eunoians.mcrpg.api.util.fishing.FishingItemManager;
import us.eunoians.mcrpg.commands.*;
import us.eunoians.mcrpg.commands.prompts.McAdminPrompt;
import us.eunoians.mcrpg.commands.prompts.McDisplayPrompt;
import us.eunoians.mcrpg.commands.prompts.McRankPrompt;
import us.eunoians.mcrpg.commands.prompts.McRedeemPrompt;
import us.eunoians.mcrpg.database.McRPGDb;
import us.eunoians.mcrpg.events.external.sickle.Sickle;
import us.eunoians.mcrpg.events.mcrpg.*;
import us.eunoians.mcrpg.events.vanilla.*;
import us.eunoians.mcrpg.players.PlayerManager;
import us.eunoians.mcrpg.util.blockmeta.chunkmeta.ChunkManager;
import us.eunoians.mcrpg.util.blockmeta.chunkmeta.ChunkManagerFactory;
import us.eunoians.mcrpg.util.worldguard.WGSupportManager;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

/*JAVEN ISSUES*/
@Lib(group = "com.google.guava", name = "guava", version = "27.1-jre")
@Lib(group = "com.github.DiamondDagger590", name = "FlatDB", version = "1.0.7")
//@Lib(group = "org.javassist", name = "javassist", version = "3.21.0-GA")
@Lib(group = "com.github.DiamondDagger590", name = "EnumToYAML", version = "1.0")

public class McRPG extends JavaPlugin {//implements //Initializable {

  private static McRPG instance;
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
  @Getter private WorldModifierManager worldModifierManager;
  //Needed to support McMMO's Healthbars
  @Getter private final String customNameKey = "mcMMO: Custom Name";
  @Getter private final String customVisibleKey = "mcMMO: Name Visibility";
  @Getter private boolean healthBarPluginEnabled = false;
  @Getter private boolean mvdwEnabled = false;
  @Getter private boolean papiEnabled = false;
  @Getter private boolean ncpEnabled = false;
  @Getter private boolean sickleEnabled = false;
  @Getter private boolean worldGuardEnabled = false;
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
    if(Bukkit.getVersion().contains("1.14")){
      new BukkitRunnable(){
        @Override
        public void run(){
          Bukkit.getLogger().log(Level.WARNING, "You are on 1.14. Please ensure in the swords.yml that you have changed ROSE_RED to RED_DYE" +
                  ", otherwise the plugin will error. Make these changes and then do /mcrpg reload");
        }
      }.runTaskLater(this, 400);
    }

    //Misc
    //localizationFiles = new LocalizationFiles(this, true);
    instance = this;
    fileManager = FileManager.getInstance().setup(this);
    expPermissionManager = ExpPermissionManager.getInstance().setup(this);
    this.mcRPGDb = new McRPGDb(this);
    healthBarPluginEnabled = getServer().getPluginManager().getPlugin("HealthBar") != null;
    sickleEnabled = getServer().getPluginManager().getPlugin("Sickle") != null;
    fishingItemManager = new FishingItemManager();
    bookManager = new BookManager(this);
    worldModifierManager = new WorldModifierManager();
    leaderboardManager = new LeaderboardManager(this);
    leaderboardHeadManager = new LeaderboardHeadManager();
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

    getCommand("mcdisplay").setTabCompleter(new McDisplayPrompt());
    getCommand("mcredeem").setTabCompleter(new McRedeemPrompt());
    getCommand("mcrank").setTabCompleter(new McRankPrompt());
    getCommand("mcadmin").setTabCompleter(new McAdminPrompt());

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
    getServer().getPluginManager().registerEvents(new EntityDeathEvent(), this);
    getServer().getPluginManager().registerEvents(new SignEvent(), this);
    getServer().getPluginManager().registerEvents(new SpawnEvent(), this);
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
  }

  @Override
  public void onDisable() {
    /*if (!Initializer.finished())
      Initializer.interrupt();*/
    PlayerManager.shutDownManager();
    placeStore.saveAll();
  }

  /*@Initialize(priority = 0)
  private void preInit() {
    var configManager = new ConfigManager(this);
    mConfigManager = new MConfigManager(configManager);
    /*if (!mConfigManager.setupConfigs(
            GeneralConfig.class, SwordsConfig.class))
      getServer().shutdown();
    //Logger.setDebugMode(mConfigManager.getGeneralConfig().isDebugMode());
    //Locale.init(mConfigManager);
  }*/

  /*
  @SuppressWarnings("Duplicates")
  @Initialize(priority = 2)
  private void initPrimaryInstance() {
    //localizationFiles = new LocalizationFiles(this, true);
    instance = this;
    fileManager = FileManager.getInstance().setup(this);
    expPermissionManager = ExpPermissionManager.getInstance().setup(this);
    this.mcRPGDb = new McRPGDb(this);
    healthBarPluginEnabled = getServer().getPluginManager().getPlugin("HealthBar") != null;
    sickleEnabled = getServer().getPluginManager().getPlugin("Sickle") != null;
    fishingItemManager = new FishingItemManager();
    bookManager = new BookManager(this);
    worldModifierManager = new WorldModifierManager();
    leaderboardManager = new LeaderboardManager(this);
    leaderboardHeadManager = new LeaderboardHeadManager();
    if (healthBarPluginEnabled) {
      getLogger().info("HealthBar plugin found, McRPG's healthbars are automatically disabled.");
    }
    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      papiEnabled = true;
      getLogger().info("Papi PlaceholderAPI found... registering hooks");
      new McRPGPlaceHolders().register();
    }
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
  }*/

/*  @Initialize(priority = 3)
  private void initCmds() {
    getCommand("mcrpg").setExecutor(new McRPGStub());
    getCommand("mcdisplay").setExecutor(new McDisplay());
    getCommand("mcadmin").setExecutor(new McAdmin());
    getCommand("mclink").setExecutor(new McLink());
    getCommand("mcunlink").setExecutor(new McUnlink());
    getCommand("mchelp").setExecutor(new McHelp());
    getCommand("mcconvert").setExecutor(new McConvert());
    getCommand("mcredeem").setExecutor(new McRedeem());
    getCommand("mcrank").setExecutor(new McRank());
  }*/

  /*
  @Initialize(priority = 4)
  private void initListener() {
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
    getServer().getPluginManager().registerEvents(new EntityDeathEvent(), this);
    getServer().getPluginManager().registerEvents(new SignEvent(), this);
    getServer().getPluginManager().registerEvents(new SpawnEvent(), this);
    if(sickleEnabled){
      getServer().getPluginManager().registerEvents(new Sickle(), this);
    }
  }*/

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
