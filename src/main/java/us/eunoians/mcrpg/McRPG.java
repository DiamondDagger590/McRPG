package us.eunoians.mcrpg;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.player.PlayerManager;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.impl.swords.Bleed;
import us.eunoians.mcrpg.ability.impl.swords.DeeperWound;
import us.eunoians.mcrpg.chunk.ChunkManager;
import us.eunoians.mcrpg.chunk.ChunkManagerFactory;
import us.eunoians.mcrpg.chunk.ChunkStore;
import us.eunoians.mcrpg.config.BaseMainConfig;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.entity.AbilityHolderTracker;
import us.eunoians.mcrpg.listener.ability.OnAttackAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnBleedActivateListener;
import us.eunoians.mcrpg.listener.player.OnPlayerLevelUpListener;
import us.eunoians.mcrpg.listener.player.PlayerJoinListener;
import us.eunoians.mcrpg.listener.skill.OnAttackLevelListener;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.swords.Swords;

import java.io.File;
import java.io.IOException;

/**
 * The main class for McRPG where developers should be able to access various components of the API's provided by McRPG
 */
public class McRPG extends CorePlugin {

    private static ChunkManager placeStore;

    private static final int id = 6386;

    //Needed to support McMMO's Healthbars
    private static final String customNameKey = "mcMMO: Custom Name";
    private static final String customVisibleKey = "mcMMO: Name Visibility";

    private FileManager fileManager;

    private AbilityRegistry abilityRegistry;
    private SkillRegistry skillRegistry;
    private AbilityAttributeManager abilityAttributeManager;

    private AbilityHolderTracker entityManager;

    private boolean healthBarPluginEnabled = false;
    private boolean mvdwEnabled = false;
    private boolean papiEnabled = false;
    private boolean ncpEnabled = false;
    private boolean sickleEnabled = false;
    private boolean worldGuardEnabled = false;
    private boolean mcmmoEnabled = false;

    @Override
    public void onEnable() {

        super.onEnable();

        placeStore = ChunkManagerFactory.getChunkManager(); // Get our ChunkManager

        initializeFiles();

        entityManager = new AbilityHolderTracker(this);
        playerManager = new PlayerManager(this);

        abilityRegistry = new AbilityRegistry(this);
        skillRegistry = new SkillRegistry(this);

        preloadNBTAPI();
        setupHooks();
        initializeDatabase();
        registerListeners();

        abilityAttributeManager = new AbilityAttributeManager(this);

        //TODO remove after testing
        getAbilityRegistry().registerAbility(new Bleed());
        getAbilityRegistry().registerAbility(new DeeperWound());
        getSkillRegistry().registerSkill(new Swords());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeDatabase() {
        this.databaseManager = new McRPGDatabaseManager(this);
        this.databaseManager.initializeDatabase();
    }

    /**
     * Initializes the {@link FileManager} and populates it with all files McRPG needs
     */
    private void initializeFiles() {
        fileManager = new FileManager(this);
        fileManager.initializeAndLoadFiles();

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File configFile = new File(getDataFolder().getPath() + File.separator + "config.yml");
        if(!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        SettingsManager settingsManager = SettingsManagerBuilder
                .withYamlFile(configFile)
                .configurationData(BaseMainConfig.class)
                .useDefaultMigrationService()
                .create();
    }

    /**
     * Setup 3rd party plugin hooks that are natively supported by McRPG
     */
    private void setupHooks() {

        healthBarPluginEnabled = getServer().getPluginManager().getPlugin("HealthBar") != null;
        sickleEnabled = getServer().getPluginManager().getPlugin("Sickle") != null;

        if (healthBarPluginEnabled) {
            getLogger().info("HealthBar plugin found, McRPG's healthbars are automatically disabled.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            papiEnabled = true;
            getLogger().info("Papi PlaceholderAPI found... registering hooks");
            //new McRPGPlaceHolders().register();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
            ncpEnabled = true;
            getLogger().info("NoCheatPlus found... will enable anticheat support");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            mcmmoEnabled = true;
            getLogger().info("McMMO found... ready to convert.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            worldGuardEnabled = true;
        }
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAttackAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAttackLevelListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerLevelUpListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnBleedActivateListener(), this);
    }

    /**
     * Preloads the NBTItem classes, as this can cause lag on the first call for some reason
     */
    private void preloadNBTAPI() {
        //Preload nbt class
        ItemStack itemStack = new ItemStack(Material.DIAMOND);
        NBTItem item = new NBTItem(itemStack);
        item.setString("temp", "temp");
    }

    /**
     * Get the {@link FileManager} used by McRPG
     *
     * @return The {@link FileManager} used by McRPG
     */
    @NotNull
    public FileManager getFileManager() {
        return fileManager;
    }

    @NotNull
    public AbilityHolderTracker getEntityManager() {
        return entityManager;
    }

    @Override
    @NotNull
    public McRPGDatabaseManager getDatabaseManager() {
        return (McRPGDatabaseManager) databaseManager;
    }

    /**
     * Gets the {@link AbilityRegistry} used by McRPG
     *
     * @return The {@link AbilityRegistry} used by McRPG
     */
    @NotNull
    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    /**
     * Gets the {@link SkillRegistry} used by McRPG
     *
     * @return The {@link SkillRegistry} used by McRPG
     */
    @NotNull
    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }

    /**
     * Gets the {@link AbilityAttributeManager} used by McRPG
     *
     * @return The {@link AbilityAttributeManager} used by McRPG
     */
    public AbilityAttributeManager getAbilityAttributeManager() {
        return abilityAttributeManager;
    }

    /**
     * Gets the {@link ChunkStore} used by McRPG
     *
     * @return The {@link ChunkStore} used by McRPG
     */
    @NotNull
    public ChunkStore getChunkStore() {
        return getChunkStore();
    }

    @NotNull
    public static McRPG getInstance() {
        return (McRPG) CorePlugin.getInstance();
    }
}
