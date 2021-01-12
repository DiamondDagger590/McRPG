package us.eunoians.mcrpg;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed;
import us.eunoians.mcrpg.api.registry.AbilityRegistry;
import us.eunoians.mcrpg.api.manager.BleedManager;
import us.eunoians.mcrpg.player.PlayerContainer;
import us.eunoians.mcrpg.api.registry.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.Swords;
import us.eunoians.mcrpg.skill.impl.Taming;

import java.io.File;

public class McRPG extends JavaPlugin {

    /**
     * Plugin instance
     */
    private static McRPG instance;

    /**
     * The player container, this object holds all the McRPG playesr
     */
    private PlayerContainer playerContainer;

    /**
     * The central ability registry object
     */
    private AbilityRegistry abilityRegistry;

    /**
     * The central skill registry object
     */
    private SkillRegistry skillRegistry;

    /**
     * Handles various aspects relating to the {@link Bleed}
     * ability.
     */
    private BleedManager bleedManager;

    /**
     * Constructor used for unit tests.
     */
    public McRPG () {
        super();
    }

    /**
     * Constructor used for unit tests.
     */
    public McRPG (JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        instance = this;

        this.playerContainer = new PlayerContainer();
        this.abilityRegistry = new AbilityRegistry();
        this.skillRegistry = new SkillRegistry();
        this.bleedManager = new BleedManager();

        // TODO: Move this to an appropriate place
        // Register skills
        McRPG.getInstance().getSkillRegistry().registerSkill(getNamespacedKey("taming"), Taming::new);
        McRPG.getInstance().getSkillRegistry().registerSkill(getNamespacedKey("swords"), Swords::new);
    }

    @Override
    public void onDisable() {
    }

    /**
     * Initialize our various listeners
     */
    private void initListeners(){

        //Initialize the Bleed manager
        getServer().getPluginManager().registerEvents(getBleedManager(), this);
    }

    /**
     * Get the {@link McRPG} plugin instance.
     *
     * @return the plugin instance
     */
    public static McRPG getInstance() {
        return instance;
    }

    /**
     * Get a {@link NamespacedKey} for {@link McRPG}.
     *
     * @param key the value of the key.
     *
     * @return the {@link NamespacedKey} using the McRPG namespace
     */
    public static NamespacedKey getNamespacedKey (String key) {
        return new NamespacedKey(instance, key);
    }

    /**
     * Get the {@link PlayerContainer}.
     *
     * @return the player container
     */
    public PlayerContainer getPlayerContainer() {
        return playerContainer;
    }

    /**
     * Get the {@link SkillRegistry} object that can be used to register skills to McRPG.
     *
     * @return the skill registry object
     */
    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }

    /**
     * Gets the {@link BleedManager} object that handles all {@link Bleed}
     * related logic.
     *
     * @return The {@link BleedManager} object
     */
    public BleedManager getBleedManager(){
        return bleedManager;
    }
}
