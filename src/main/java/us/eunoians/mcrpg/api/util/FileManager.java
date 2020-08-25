package us.eunoians.mcrpg.api.util;

import de.articdive.enum_to_yaml.EnumConfigurationBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.types.Skills;
import us.eunoians.mcrpg.util.IOUtil;
import us.eunoians.mcrpg.util.configuration.ConfigEnum;
import us.eunoians.mcrpg.util.configuration.LangEnum;
import us.eunoians.mcrpg.util.configuration.SoundEnum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author BadBones69
 * @version v1.0
 */
public class FileManager {

  private Plugin plugin;
  private String prefix = "";
  private Boolean log = false;
  private Map<Files, File> files = new HashMap<>();
  private List<String> homeFolders = new ArrayList<>();
  private List<CustomFile> customFiles = new ArrayList<>();
  private Map<String, String> autoGenerateFiles = new HashMap<>();
  private Map<Files, FileConfiguration> configurations = new HashMap<>();

  private static FileManager instance = new FileManager();

  public static FileManager getInstance() {
    return instance;
  }

  /**
   * Sets up the plugin and loads all necessary files.
   *
   * @param plugin The plugin this is getting loading for.
   */
  public FileManager setup(Plugin plugin) {
    //Auto gen some configs
    EnumConfigurationBuilder config = new EnumConfigurationBuilder(new File(McRPG.getInstance().getDataFolder() + File.separator + "config.yml"), ConfigEnum.class);
    EnumConfigurationBuilder enConfig = new EnumConfigurationBuilder(new File(McRPG.getInstance().getDataFolder() + File.separator + "localization" + File.separator + "en.yml"), LangEnum.class);
    EnumConfigurationBuilder soundConfig = new EnumConfigurationBuilder(new File(McRPG.getInstance().getDataFolder()  + File.separator + "sounds.yml"), SoundEnum.class);
    config.setWidth(100000);
    enConfig.setWidth(100000);
    soundConfig.setWidth(100000);
    config.build();
    enConfig.build();
    soundConfig.build();
    prefix = "[" + plugin.getName() + "] ";
    this.plugin = plugin;
    if (!plugin.getDataFolder().exists()) {
      plugin.getDataFolder().mkdirs();
    }
    files.clear();
    customFiles.clear();
    //Loads all the normal static files.
    for (Files file : Files.values()) {
      File newFile = new File(plugin.getDataFolder(), file.getFileLocation());
      if (log) System.out.println(prefix + "Loading the " + file.getFileName());
      if (!newFile.exists()) {
        try {
          IOUtil.saveResource((McRPG) plugin, file.fileLocation, false);
        } catch (Exception e) {
          if (log) System.out.println(prefix + "Failed to load " + file.getFileName());
          e.printStackTrace();
          continue;
        }
      }
      files.put(file, newFile);
      YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(newFile);
      if(file == Files.SWORDS_CONFIG){
        if(yamlConfiguration.getString("DeeperWoundConfig.Item.Material").equals("ROSE_RED")){
          yamlConfiguration.set("DeeperWoundConfig.Item.Material", "RED_DYE");
        }
      }
      configurations.put(file, yamlConfiguration);
      if (log) System.out.println(prefix + "Successfully loaded " + file.getFileName());
    }
    //Starts to load all the custom files.
    if (homeFolders.size() > 0) {
      if (log) System.out.println(prefix + "Loading custom files.");
      for (String homeFolder : homeFolders) {
        File homeFile = new File(plugin.getDataFolder(), "/" + homeFolder);
        if (homeFile.exists()) {
          String[] list = homeFile.list();
          if (list != null) {
            for (String name : list) {
              if (name.endsWith(".yml")) {
                CustomFile file = new CustomFile(name, homeFolder, plugin);
                if (file.exists()) {
                  customFiles.add(file);
                  if (log) System.out.println(prefix + "Loaded new custom file: " + homeFolder + "/" + name + ".");
                }
              }
            }
          }

        } else {
          homeFile.mkdir();
          if (log) System.out.println(prefix + "The folder " + homeFolder + "/ was not found so it was created.");
          for (String fileName : autoGenerateFiles.keySet()) {
            if (autoGenerateFiles.get(fileName).equalsIgnoreCase(homeFolder)) {
              homeFolder = autoGenerateFiles.get(fileName);
              try {
                File serverFile = new File(plugin.getDataFolder(), homeFolder + "/" + fileName);
                InputStream jarFile = getClass().getResourceAsStream(homeFolder + "/" + fileName);
                copyFile(jarFile, serverFile);
                if (fileName.toLowerCase().endsWith(".yml")) {
                  customFiles.add(new CustomFile(fileName, homeFolder, plugin));
                }
                if (log) System.out.println(prefix + "Created new default file: " + homeFolder + "/" + fileName + ".");
              } catch (Exception e) {
                if (log)
                  System.out.println(prefix + "Failed to create new default file: " + homeFolder + "/" + fileName + "!");
                e.printStackTrace();
              }
            }
          }
        }
      }
      if (log) System.out.println(prefix + "Finished loading custom files.");
    }
    return this;
  }

  /**
   * Turn on the logger system for the FileManager.
   *
   * @param log True to turn it on and false for it to be off.
   */
  public FileManager logInfo(Boolean log) {
    this.log = log;
    return this;
  }

  /**
   * Check if the logger is logging in console.
   *
   * @return True if it is and false if it isn't.
   */
  public Boolean isLogging() {
    return log;
  }

  /**
   * Register a folder that has custom files in it. Make sure to have a "/" in front of the folder name.
   *
   * @param homeFolder The folder that has custom files in it.
   */
  public FileManager registerCustomFilesFolder(String homeFolder) {
    homeFolders.add(homeFolder);
    return this;
  }

  /**
   * Unregister a folder that has custom files in it. Make sure to have a "/" in front of the folder name.
   *
   * @param homeFolder The folder with custom files in it.
   */
  public FileManager unregisterCustomFilesFolder(String homeFolder) {
    homeFolders.remove(homeFolder);
    return this;
  }

  /**
   * Register a file that needs to be generated when it's home folder doesn't exist. Make sure to have a "/" in front of the home folder's name.
   *
   * @param fileName   The name of the file you want to auto-generate when the folder doesn't exist.
   * @param homeFolder The folder that has custom files in it.
   */
  public FileManager registerDefaultGenerateFiles(String fileName, String homeFolder) {
    autoGenerateFiles.put(fileName, homeFolder);
    return this;
  }

  /**
   * Unregister a file that doesn't need to be generated when it's home folder doesn't exist. Make sure to have a "/" in front of the home folder's name.
   *
   * @param fileName The file that you want to remove from auto-generating.
   */
  public FileManager unregisterDefaultGenerateFiles(String fileName) {
    autoGenerateFiles.remove(fileName);
    return this;
  }

  /**
   * Gets the file from the system.
   *
   * @return The file from the system.
   */
  public FileConfiguration getFile(Files file) {
    return configurations.get(file);
  }

  /**
   * Get a custom file from the loaded custom files instead of a hardcoded one.
   * This allows you to get custom files like Per player data files.
   *
   * @param name Name of the crate you want. (Without the .yml)
   * @return The custom file you wanted otherwise if not found will return null.
   */
  public CustomFile getFile(String name) {
    for (CustomFile file : customFiles) {
      if (file.getName().toLowerCase().equalsIgnoreCase(name.toLowerCase())) {
        return file;
      }
    }
    return null;
  }

  /**
   * Saves the file from the loaded state to the file system.
   */
  public void saveFile(Files file) {
    try {
      configurations.get(file).save(files.get(file));
    } catch (IOException e) {
      System.out.println(prefix + "Could not save " + file.getFileName() + "!");
      e.printStackTrace();
    }
  }

  /**
   * Save a custom file.
   *
   * @param name The name of the custom file.
   */
  public void saveFile(String name) {
    CustomFile file = getFile(name);
    if (file != null) {
      try {
        file.getFile().save(new File(plugin.getDataFolder(), file.getHomeFolder() + "/" + file.getFileName()));
        if (log) System.out.println(prefix + "Successfuly saved the " + file.getFileName() + ".");
      } catch (Exception e) {
        System.out.println(prefix + "Could not save " + file.getFileName() + "!");
        e.printStackTrace();
      }
    } else {
      if (log) System.out.println(prefix + "The file " + name + ".yml could not be found!");
    }
  }

  /**
   * Save a custom file.
   *
   * @param file The custom file you are saving.
   * @return True if the file saved correct and false if there was an error.
   */
  public Boolean saveFile(CustomFile file) {
    return file.saveFile();
  }

  /**
   * Overrides the loaded state file and loads the file systems file.
   */
  public void reloadFile(Files file) {
    YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(files.get(file));
    if(file == Files.SWORDS_CONFIG){
      if(yamlConfiguration.getString("DeeperWoundConfig.Item.Material").equals("ROSE_RED")){
        yamlConfiguration.set("DeeperWoundConfig.Item.Material", "RED_DYE");
      }
    }
    configurations.put(file, yamlConfiguration);
  }

  public void reloadFiles() {
    Arrays.stream(Files.values()).forEach(this::reloadFile);
  }

  /**
   * Overrides the loaded state file and loads the file systems file.
   */
  public void reloadFile(String name) {
    CustomFile file = getFile(name);
    if (file != null) {
      try {
        file.file = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "/" + file.getHomeFolder() + "/" + file.getFileName()));
        if (log) System.out.println(prefix + "Successfully reload the " + file.getFileName() + ".");
      } catch (Exception e) {
        System.out.println(prefix + "Could not reload the " + file.getFileName() + "!");
        e.printStackTrace();
      }
    } else {
      if (log) System.out.println(prefix + "The file " + name + ".yml could not be found!");
    }
  }

  /**
   * Overrides the loaded state file and loads the filesystems file.
   *
   * @return True if it reloaded correct and false if the file wasn't found.
   */
  public Boolean reloadFile(CustomFile file) {
    return file.reloadFile();
  }

  /**
   * Was found here: https://bukkit.org/threads/extracting-file-from-jar.16962
   */
  private void copyFile(InputStream in, File out) throws Exception {
    try (FileOutputStream fos = new FileOutputStream(out)) {
      byte[] buf = new byte[1024];
      int i;
      while ((i = in.read(buf)) != -1) {
        fos.write(buf, 0, i);
      }
    } finally {
      if (in != null) {
        in.close();
      }

    }
  }
  
  public enum Files{
    ABILITY_OVERRIDE_GUI("abilityoverridegui.yml", "guis/abilityoverridegui.yml"),
    ACCEPT_ABILITY_GUI("acceptabilitygui.yml", "guis/acceptabilitygui.yml"),
    ARCHERY_CONFIG("archery.yml", "skills/archery.yml"),
    ARTIFACT_FILE("artifacts.yml", "artifacts/artifacts.yml"),
    BLOOD_FILE("blood.yml", "blood/blood.yml"),
    AXES_CONFIG("axes.yml", "skills/axes.yml"),
    BREWING_GUI("brewinggui.yml", "guis/brewinggui.yml"),
    BREWING_ITEMS_CONFIG("potionitems.yml", "skills/potionitems.yml"),
    CONFIG("config.yml", "config.yml"),
    CUSTOM("custom.yml", "localization/custom.yml"),
    EDIT_DEFAULT_ABILITIES_GUI("editdefaultabilitiesgui.yml", "guis/editdefaultabilitiesgui.yml"),
    EDIT_LOADOUT_GUI("editloadoutgui.yml", "guis/editloadoutgui.yml"),
    EDIT_LOADOUT_SELECT_GUI("editloadoutgselectui.yml", "guis/editloadoutselectgui.yml"),
    ENGLISH_FILE("en.yml", "localization/en.yml"),
    EXCAVATION_CONFIG("excavation.yml", "skills/excavation.yml"),
    EXP_PERM_FILE("exp_perms.yml", "exp_perms.yml"),
    FILTER("filter.yml", "filter.yml"),
    FISHING_CONFIG("fishing.yml", "skills/fishing.yml"),
    FISHING_LOOT("fishingloot.yml", "skills/fishingloot.yml"),
    FITNESS_CONFIG("fitness.yml", "skills/fitness.yml"),
    HERBALISM_CONFIG("herbalism.yml", "skills/herbalism.yml"),
    LEVEL_COMMAND("level_commands.yml", "level_commands.yml"),
    LOCATIONS("locations.yml", "data/locations.yml"),
    MAIN_GUI("maingui.yml", "guis/maingui.yml"),
    MINING_CONFIG("mining.yml", "skills/mining.yml"),
    PARTY_CONFIG("party_config.yml", "party_config.yml"),
    PARTY_MAIN_GUI("partymastergui.yml", "guis/parties/partymastergui.yml"),
    PARTY_MEMBER_GUI("partymembergui.yml", "guis/parties/partymembergui.yml"),
    PARTY_ROLE_GUI("partyrolegui.yml", "guis/parties/partyrolegui.yml"),
    PARTY_UPGRADES_GUI("partyupgradesgui.yml", "guis/parties/partyupgradesgui.yml"),
    REDEEM_GUI("redeemgui.yml", "guis/redeemgui.yml"),
    REMOTE_TRANSFER_GUI("remotetransfergui.yml", "guis/remotetransfergui.yml"),
    REPLACE_SKILLS_GUI("replaceskillsgui.yml", "guis/replaceskillsgui.yml"),
    SELECT_REPLACE_SKILLS_GUI("selectreplaceskillsgui.yml", "guis/selectreplaceskillsgui.yml"),
    SETTINGS_GUI("playersettingsgui.yml", "guis/playersettingsgui.yml"),
    SIGN_CONFIG("signdata.yml", "data/signdata.yml"),
    SKILLS_GUI("skillgui.yml", "guis/skillsgui.yml"),
    SORCERY_CONFIG("sorcery.yml", "skills/sorcery.yml"),
    SOUNDS_FILE("sounds.yml", "sounds.yml"),
    SUBSKILL_GUI("subskillgui.yml", "guis/subskillgui.yml"),
    SWORDS_CONFIG("swords.yml", "skills/swords.yml"),
    TAMING_CONFIG("taming.yml", "skills/taming.yml"),
    UNARMED_CONFIG("unarmed.yml", "skills/unarmed.yml"),
    UNLOCK_BOOKS("unlock_books.yml", "skill_books/unlock_books.yml"),
    UPGRADE_BOOKS("upgrade_books.yml", "skill_books/upgrade_books.yml"),
    WOODCUTTING_CONFIG("woodcutting.yml", "skills/woodcutting.yml"),
    WORLDGUARD_CONFIG("wg_support.yml", "wg_support.yml"),
    WORLD_MODIFIER("world_modifier.yml", "world_modifier.yml");
    
    private String fileName;
    private String fileLocation;
    
    /**
     * The files that the server will try and load.
     *
     * @param fileName     The file name that will be in the plugin's folder.
     * @param fileLocation The location the file is in while in the Jar.
     */
    Files(String fileName, String fileLocation){
      this.fileName = fileName;
      this.fileLocation = fileLocation;
    }
    
    /**
     * Get the name of the file.
     *
     * @return The name of the file.
     */
    public String getFileName(){
      return fileName;
    }
    
    /**
     * The location the jar it is at.
     *
     * @return The location in the jar the file is in.
     */
    public String getFileLocation(){
      return fileLocation;
    }
    
    /**
     * Gets the file from the system.
     *
     * @return The file from the system.
     */
    public FileConfiguration getFile(){
      return getInstance().getFile(this);
    }
    
    public static Files fromString(String file){
      for(Files f : Files.values()){
        if(f.getFileName().replaceAll(".yml", "").equalsIgnoreCase(file)){
          return f;
        }
      }
      return null;
    }
    
    /**
     * Saves the file from the loaded state to the file system.
     */
    public void saveFile(){
      getInstance().saveFile(this);
    }
    
    /**
     * Overrides the loaded state file and loads the file systems file.
     */
    public void reloadFile(){
      getInstance().reloadFile(this);
    }
    
    public static Files getSkillFile(Skills skill){
      return fromString(skill.getName());
    }
    
  }
  
  public class CustomFile {

    private String name;
    private Plugin plugin;
    private String fileName;
    private String homeFolder;
    private FileConfiguration file;

    /**
     * A custom file that is being made.
     *
     * @param name       Name of the file.
     * @param homeFolder The home folder of the file.
     * @param plugin     The plugin the files belong to.
     */
    public CustomFile(String name, String homeFolder, Plugin plugin) {
      this.name = name.replace(".yml", "");
      this.plugin = plugin;
      this.fileName = name;
      this.homeFolder = homeFolder;
      if (new File(plugin.getDataFolder(), "/" + homeFolder).exists()) {
        if (new File(plugin.getDataFolder(), "/" + homeFolder + "/" + name).exists()) {
          file = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "/" + homeFolder + "/" + name));
        } else {
          file = null;
        }
      } else {
        new File(plugin.getDataFolder(), "/" + homeFolder).mkdir();
        if (log) System.out.println(prefix + "The folder " + homeFolder + "/ was not found so it was created.");
        file = null;
      }
    }

    /**
     * Get the name of the file without the .yml part.
     *
     * @return The name of the file without the .yml.
     */
    public String getName() {
      return name;
    }

    /**
     * Get the full name of the file.
     *
     * @return Full name of the file.
     */
    public String getFileName() {
      return fileName;
    }

    /**
     * Get the name of the home folder of the file.
     *
     * @return The name of the home folder the files are in.
     */
    public String getHomeFolder() {
      return homeFolder;
    }

    /**
     * Get the plugin the file belongs to.
     *
     * @return The plugin the file belongs to.
     */
    public Plugin getPlugin() {
      return plugin;
    }

    /**
     * Get the ConfigurationFile.
     *
     * @return The ConfigurationFile of this file.
     */
    public FileConfiguration getFile() {
      return file;
    }

    /**
     * Check if the file actually exists in the file system.
     *
     * @return True if it does and false if it doesn't.
     */
    public Boolean exists() {
      return file != null;
    }

    /**
     * Save the custom file.
     *
     * @return True if it saved correct and false if something went wrong.
     */
    public Boolean saveFile() {
      if (file != null) {
        try {
          file.save(new File(plugin.getDataFolder(), homeFolder + "/" + fileName));
          if (log) System.out.println(prefix + "Successfuly saved the " + fileName + ".");
          return true;
        } catch (Exception e) {
          System.out.println(prefix + "Could not save " + fileName + "!");
          e.printStackTrace();
          return false;
        }
      } else {
        if (log) System.out.println(prefix + "There was a null custom file that could not be found!");
      }
      return false;
    }

    /**
     * Overrides the loaded state file and loads the filesystems file.
     *
     * @return True if it reloaded correct and false if the file wasn't found or errored.
     */
    public Boolean reloadFile() {
      if (file != null) {
        try {
          file = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "/" + homeFolder + "/" + fileName));
          if (log) System.out.println(prefix + "Successfuly reload the " + fileName + ".");
          return true;
        } catch (Exception e) {
          System.out.println(prefix + "Could not reload the " + fileName + "!");
          e.printStackTrace();
        }
      } else {
        if (log) System.out.println(prefix + "There was a null custom file that was not found!");
      }
      return false;
    }

  }

}
