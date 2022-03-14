package us.eunoians.mcrpg;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.util.blockmeta.ChunkManager;
import us.eunoians.mcrpg.util.blockmeta.ChunkManagerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * The main class for McRPG where developers should be able to access various components of the API's provided by McRPG
 */
public class McRPG extends JavaPlugin {

    private static McRPG instance;
    private static ChunkManager placeStore;

    private static final int id = 6386;

    //Needed to support McMMO's Healthbars
    private static final String customNameKey = "mcMMO: Custom Name";
    private static final String customVisibleKey = "mcMMO: Name Visibility";

    private FileManager fileManager;

    private DatabaseManager databaseManager;
    private AbilityRegistry abilityRegistry;
    private AbilityAttributeManager abilityAttributeManager;

    private boolean healthBarPluginEnabled = false;
    private boolean mvdwEnabled = false;
    private boolean papiEnabled = false;
    private boolean ncpEnabled = false;
    private boolean sickleEnabled = false;
    private boolean worldGuardEnabled = false;
    private boolean mcmmoEnabled = false;

    @Override
    public void onEnable() {

        instance = this;
        placeStore = ChunkManagerFactory.getChunkManager(); // Get our ChunkManager

        initializeFiles();

        abilityRegistry = new AbilityRegistry(this);

        preloadNBTAPI();
        setupHooks();
        initializeDatabase();
        abilityAttributeManager = new AbilityAttributeManager(this);
    }

    @Override
    public void onDisable() {
        databaseManager.getDatabaseExecutorService().shutdown();
    }

    /**
     * Initializes the {@link FileManager} and populates it with all files McRPG needs
     */
    private void initializeFiles() {
        fileManager = new FileManager(this);
        fileManager.initializeAndLoadFiles();
    }

    /**
     * Initializes the databases for McRPG
     *
     * @return A {@link CompletableFuture} that is completed whenever all databases are created and/or updated
     */
    @NotNull
    private CompletableFuture<Void> initializeDatabase() {
        this.databaseManager = new DatabaseManager(this);
        return databaseManager.initialize()
            .exceptionally(throwable -> {
                throwable.printStackTrace();
                return null;
            });
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

    /**
     * Get the {@link DatabaseManager} used by McRPG
     *
     * @return The {@link DatabaseManager} used by McRPG
     */
    @NotNull
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
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

    @NotNull
    public static McRPG getInstance() {
        if (instance == null) {
            throw new NullPointerException("Plugin was not initialized.");
        }
        return instance;
    }
}
