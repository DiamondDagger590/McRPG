package us.eunoians.mcrpg;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.impl.swords.bleed.Bleed;
import us.eunoians.mcrpg.ability.impl.swords.bleedplus.BleedPlus;
import us.eunoians.mcrpg.ability.impl.swords.deeperwound.DeeperWound;
import us.eunoians.mcrpg.ability.impl.taming.gore.Gore;
import us.eunoians.mcrpg.ability.listener.CooldownableAbilityListener;
import us.eunoians.mcrpg.ability.listener.ReadyableAbilityCheckListener;
import us.eunoians.mcrpg.api.chat.MessageSender;
import us.eunoians.mcrpg.api.lunar.LunarClientHook;
import us.eunoians.mcrpg.api.manager.BleedManager;
import us.eunoians.mcrpg.api.manager.CooldownManager;
import us.eunoians.mcrpg.api.manager.ReadyTaskManager;
import us.eunoians.mcrpg.player.PlayerContainer;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.Swords;
import us.eunoians.mcrpg.skill.impl.Taming;
import us.eunoians.mcrpg.util.blockmeta.chunkmeta.ChunkManager;
import us.eunoians.mcrpg.util.blockmeta.chunkmeta.ChunkManagerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class McRPG extends JavaPlugin {

    /**
     * Plugin instance
     */
    private static McRPG instance;

    /**
     * Handles block data
     */
    private static ChunkManager placeStore;

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
     * Handles sending messages to {@link org.bukkit.entity.Player}s
     */
    private MessageSender messageSender;

    /**
     * Handles readying {@link us.eunoians.mcrpg.ability.ReadyableAbility}s
     */
    private ReadyTaskManager readyTaskManager;

    /**
     * Handles checking to see if lunar client is enabled or not
     */
    private LunarClientHook lunarClientHook;

    /**
     * Handles managing cooldowns for {@link us.eunoians.mcrpg.ability.CooldownableAbility}s
     */
    private CooldownManager cooldownManager;

    //Needed to support McMMO's Healthbars
    private final String customNameKey = "mcMMO: Custom Name";
    private final String customVisibleKey = "mcMMO: Name Visibility";
    private boolean healthBarPluginEnabled = false;

    /**
     * Constructor used for unit tests.
     */
    public McRPG() {
        super();
    }

    /**
     * Constructor used for unit tests.
     */
    public McRPG(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        instance = this;
        placeStore = ChunkManagerFactory.getChunkManager();

        this.playerContainer = new PlayerContainer();
        this.abilityRegistry = new AbilityRegistry();
        this.skillRegistry = new SkillRegistry();
        this.bleedManager = new BleedManager();
        this.messageSender = new MessageSender();
        this.readyTaskManager = new ReadyTaskManager();
        this.cooldownManager = new CooldownManager();
        healthBarPluginEnabled = getServer().getPluginManager().getPlugin("HealthBar") != null;

        if (Bukkit.getPluginManager().isPluginEnabled("LunarClient-API")) {
            this.lunarClientHook = new LunarClientHook();
        }

        initAbilities();
        initSkills();
        initListeners();


    }

    @Override
    public void onDisable() {
    }

    public String getPluginPrefix() {
        //TODO
        return "";
    }

    /**
     * Initialize our various listeners
     */
    private void initListeners() {

        //Initialize the Bleed manager
        getServer().getPluginManager().registerEvents(getBleedManager(), this);

        getServer().getPluginManager().registerEvents(new ReadyableAbilityCheckListener(), this);
        getServer().getPluginManager().registerEvents(new CooldownableAbilityListener(), this);
    }

    /**
     * Initialize all skills that McRPG provides
     */
    private void initSkills() {
        getSkillRegistry().registerSkill(getNamespacedKey("taming"), Taming::new);
        getSkillRegistry().registerSkill(getNamespacedKey("swords"), Swords::new);
    }

    /**
     * Initialize all abilities that McRPG provides
     */
    private void initAbilities() {
        //Swords abilities
        getAbilityRegistry().registerAbility(Ability.getId(Bleed.class), Bleed::new);
        getAbilityRegistry().registerAbility(Ability.getId(DeeperWound.class), DeeperWound::new);
        getAbilityRegistry().registerAbility(Ability.getId(BleedPlus.class), BleedPlus::new);

        //Taming abilities
        getAbilityRegistry().registerAbility(getNamespacedKey("gore"), Gore::new);
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
     * Gets the {@link ChunkManager} that is handling block tracking
     *
     * @return The {@link ChunkManager} object
     */
    public static ChunkManager getPlaceStore() {
        return placeStore;
    }

    /**
     * Get a {@link NamespacedKey} for {@link McRPG}.
     *
     * @param key the value of the key.
     * @return the {@link NamespacedKey} using the McRPG namespace
     */
    public static NamespacedKey getNamespacedKey(String key) {
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
     * Get the {@link AbilityRegistry} object that can be used to register abilities to McRPG.
     *
     * @return the ability registry object
     */
    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    /**
     * Gets the {@link BleedManager} object that handles all {@link Bleed}
     * related logic.
     *
     * @return The {@link BleedManager} object
     */
    public BleedManager getBleedManager() {
        return bleedManager;
    }

    /**
     * Gets the {@link MessageSender} object that handles sending messages
     * to {@link org.bukkit.entity.Player}s.
     *
     * @return The {@link MessageSender} object
     */
    public MessageSender getMessageSender() {
        return messageSender;
    }

    /**
     * Gets the {@link ReadyTaskManager} object that handles "readying" {@link us.eunoians.mcrpg.ability.ReadyableAbility}s.
     *
     * @return The {@link ReadyTaskManager} object
     */
    public ReadyTaskManager getReadyTaskManager() {
        return readyTaskManager;
    }

    /**
     * Returns the {@link LunarClientHook} provided the API is running
     *
     * @return {@code null} if the lunar API is not running or the {@link LunarClientHook} object
     */
    @Nullable
    public LunarClientHook getLunarClientHook() {
        return this.lunarClientHook;
    }

    /**
     * Gets the {@link CooldownManager} object that handles cooldowns for {@link us.eunoians.mcrpg.ability.CooldownableAbility}s
     *
     * @return The {@link CooldownManager} object that handles cooldowns for {@link us.eunoians.mcrpg.ability.CooldownableAbility}s
     */
    public CooldownManager getCooldownManager() {
        return this.cooldownManager;
    }

    /**
     * Gets the key to store the custom name for an entity
     * @return The key to store the custom name for an entity
     */
    @NotNull
    public String getCustomNameKey() {
        return customNameKey;
    }

    /**
     * Gets the key that relates to if an entities custom name is visible
     * @return The key that relates to if an entities custom name is visible
     */
    @NotNull
    public String getCustomVisibleKey() {
        return customVisibleKey;
    }

    /**
     * Checks to see if a supported healthbar plugin is already enabled
     * @return {@code true} if a supported healthbar plugin is already enabled
     */
    public boolean isHealthBarPluginEnabled() {
        return healthBarPluginEnabled;
    }

    /**
     * Reads in a file as an {@link InputStreamReader}
     * @param fileName The path of the file to read
     * @return The {@link InputStreamReader} or {@code null} if invalid
     */
    @Nullable
    public InputStreamReader getResourceAsReader(@NotNull String fileName) {
        InputStream in = getResource(fileName);
        return in == null ? null : new InputStreamReader(in, Charsets.UTF_8);
    }
}
