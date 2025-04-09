package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.command.DisplayNameCommand;
import com.diamonddagger590.mccore.command.LoreCommand;
import com.diamonddagger590.mccore.configuration.ReloadableTask;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.jeff_media.customblockdata.CustomBlockData;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.geysermc.api.Geyser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.impl.swords.bleed.BleedManager;
import us.eunoians.mcrpg.command.TestGuiCommand;
import us.eunoians.mcrpg.command.admin.DebugCommand;
import us.eunoians.mcrpg.command.admin.ReloadPluginCommand;
import us.eunoians.mcrpg.command.admin.reset.ResetPlayerCommand;
import us.eunoians.mcrpg.command.admin.reset.ResetSkillCommand;
import us.eunoians.mcrpg.command.give.GiveExperienceCommand;
import us.eunoians.mcrpg.command.give.GiveLevelsCommand;
import us.eunoians.mcrpg.command.link.LinkChestCommand;
import us.eunoians.mcrpg.command.link.UnlinkChestCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutEditCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutSetCommand;
import us.eunoians.mcrpg.command.quest.TestQuestStartCommand;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.database.McRPGDatabase;
import us.eunoians.mcrpg.database.driver.McRPGSqliteDriver;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.expansion.handler.ContentHandlerType;
import us.eunoians.mcrpg.external.lands.LandsHook;
import us.eunoians.mcrpg.external.lunar.LunarUtils;
import us.eunoians.mcrpg.external.papi.McRPGPapiExpansion;
import us.eunoians.mcrpg.external.worldguard.WorldGuardHook;
import us.eunoians.mcrpg.listener.ability.OnAbilityActivateListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityCooldownExpireListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityPutOnCooldownListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityUnlockListener;
import us.eunoians.mcrpg.listener.ability.OnAttackAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnBleedActivateListener;
import us.eunoians.mcrpg.listener.ability.OnBlockBreakListener;
import us.eunoians.mcrpg.listener.ability.OnBlockDropItemListener;
import us.eunoians.mcrpg.listener.ability.OnExtraOreActivateListener;
import us.eunoians.mcrpg.listener.ability.OnFoodLevelChangeAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnInteractAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnPlayerMoveAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnSneakAbilityListener;
import us.eunoians.mcrpg.listener.entity.EntitySpawnListener;
import us.eunoians.mcrpg.listener.entity.holder.OnAbilityHolderReadyListener;
import us.eunoians.mcrpg.listener.entity.holder.OnAbilityHolderUnreadyListener;
import us.eunoians.mcrpg.listener.entity.player.CorePlayerLoadListener;
import us.eunoians.mcrpg.listener.entity.player.CorePlayerUnloadListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerJoinListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerLeaveListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerPickupItemListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerSettingChangeListener;
import us.eunoians.mcrpg.listener.quest.QuestCompleteListener;
import us.eunoians.mcrpg.listener.quest.QuestObjectiveCompleteListener;
import us.eunoians.mcrpg.listener.skill.OnAttackLevelListener;
import us.eunoians.mcrpg.listener.skill.OnBlockBreakLevelListener;
import us.eunoians.mcrpg.listener.skill.OnSkillLevelUpListener;
import us.eunoians.mcrpg.listener.world.FakeBlockBreakListener;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.experience.modifier.BoostedExperienceModifier;
import us.eunoians.mcrpg.skill.experience.modifier.HeldItemBonusModifier;
import us.eunoians.mcrpg.skill.experience.modifier.RestedExperienceModifier;
import us.eunoians.mcrpg.skill.experience.modifier.SpawnReasonModifier;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager;
import us.eunoians.mcrpg.task.experience.RestedExperienceAccumulationTask;
import us.eunoians.mcrpg.task.player.McRPGPlayerSaveTask;
import us.eunoians.mcrpg.world.WorldManager;
import us.eunoians.mcrpg.world.safezone.SafeZoneManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

/**
 * The main class for McRPG where developers should be able to access various components of the API's provided by McRPG
 */
public class McRPG extends CorePlugin {

    private static final int id = 6386;

    //Needed to support McMMO's Healthbars
    private static final String customNameKey = "mcMMO: Custom Name";
    private static final String customVisibleKey = "mcMMO: Name Visibility";

    private FileManager fileManager;
    private McRPGDatabase database;

    private PlayerManager<McRPG, McRPGPlayer> playerManager;
    private AbilityRegistry abilityRegistry;
    private SkillRegistry skillRegistry;
    private AbilityAttributeManager abilityAttributeManager;
    private EntityManager entityManager;
    private DisplayManager displayManager;
    private QuestManager questManager;
    private BleedManager bleedManager;
    private ContentExpansionManager contentExpansionManager;
    private WorldManager worldManager;
    private SafeZoneManager safeZoneManager;
    private ExperienceModifierRegistry experienceModifierRegistry;
    private RestedExperienceManager restedExperienceManager;
    private McRPGLocalizationManager localizationManager;

    private GlowingBlocks glowingBlocks;
    private GlowingEntities glowingEntities;

    private boolean healthBarPluginEnabled = false;
    private boolean mvdwEnabled = false;
    private boolean papiEnabled = false;
    private boolean ncpEnabled = false;
    private boolean sickleEnabled = false;
    private boolean mcmmoEnabled = false;
    private boolean geyserEnabled = false;
    private boolean lunarEnabled = false;
    @Nullable
    private LandsHook landsHook;
    @Nullable
    private WorldGuardHook worldGuardHook;

    @Override
    public void onEnable() {
        super.onEnable();
        if (!isUnitTest()) {
            initializeFiles();
            glowingBlocks = new GlowingBlocks(this);
            glowingEntities = new GlowingEntities(this);
        }

        entityManager = new EntityManager(this);
        playerManager = new PlayerManager<>(this);
        abilityRegistry = new AbilityRegistry(this);
        skillRegistry = new SkillRegistry(this);
        localizationManager = new McRPGLocalizationManager(this);

        abilityAttributeManager = new AbilityAttributeManager(this);
        displayManager = new DisplayManager(this);
        questManager = new QuestManager();
        bleedManager = new BleedManager(this);
        contentExpansionManager = new ContentExpansionManager(this);
        worldManager = new WorldManager(this);
        safeZoneManager = new SafeZoneManager(this);
        experienceModifierRegistry = new ExperienceModifierRegistry(this);
        restedExperienceManager = new RestedExperienceManager(this);
        localizationManager = new McRPGLocalizationManager(this);

        if (!isUnitTest()) {
            registerNativeExpansions();
        }

        setupHooks();
        if (!isUnitTest()) {
            database = new McRPGDatabase(this, DatabaseDriverType.SQLITE);
            registerListeners();
            constructCommands();
            registerExperienceModifiers();
            registerBackgroundTasks();
            reloadableContentRegistry.reloadAllContent();
        }
    }

    @Override
    public void onDisable() {
        if (!isUnitTest()) {
            glowingBlocks.disable();
            glowingEntities.disable();
            try (Connection connection = getDatabase().getConnection()) {
                for (CorePlayer corePlayer : playerManager.getAllPlayers()) {
                    if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                        mcRPGPlayer.savePlayer(connection);
                        if (isLunarEnabled()) {
                            LunarUtils.clearCooldowns(mcRPGPlayer.getUUID());
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        super.onDisable();
    }

    /**
     * Initializes the {@link FileManager} and populates it with all files McRPG needs
     */
    private void initializeFiles() {
        fileManager = new FileManager(this);
    }

    @Override
    public void constructCommands() {
        super.constructCommands();
        TestGuiCommand.registerCommand();

        LoadoutCommand.registerCommand();
        LoadoutEditCommand.registerCommand();
        LoadoutSetCommand.registerCommand();

        // Give Commands
        GiveLevelsCommand.registerCommand();
        GiveExperienceCommand.registerCommand();

        // Reset commands
        ResetSkillCommand.registerCommand();
        ResetPlayerCommand.registerCommand();

        // Debug Command
        DebugCommand.registerCommand();

        // Quest Command
        TestQuestStartCommand.registerCommand();

        // Reload command
        ReloadPluginCommand.registerCommand();

        // Link commands
        LinkChestCommand.registerCommand();
        UnlinkChestCommand.registerCommand();

        // Test commands
        LoreCommand.registerCommand();
        DisplayNameCommand.registerCommand();
    }

    @Override
    public void registerListeners() {
        // Register core listeners
        super.registerListeners();

        // Player load/save
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLeaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new CorePlayerLoadListener(), this);
        Bukkit.getPluginManager().registerEvents(new CorePlayerUnloadListener(), this);

        // Ability activation/ready listeners
        Bukkit.getPluginManager().registerEvents(new OnAttackAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnBleedActivateListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnInteractAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnSneakAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnBlockBreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnBlockDropItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnExtraOreActivateListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnFoodLevelChangeAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerMoveAbilityListener(), this);

        // Skill listeners
        Bukkit.getPluginManager().registerEvents(new OnSkillLevelUpListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAttackLevelListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnBlockBreakLevelListener(), this);

        // Ability listeners
        Bukkit.getPluginManager().registerEvents(new OnAbilityHolderReadyListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAbilityHolderUnreadyListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAbilityUnlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAbilityCooldownExpireListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAbilityPutOnCooldownListener(), this);

        // Quest Listeners
        Bukkit.getPluginManager().registerEvents(new QuestCompleteListener(), this);
        Bukkit.getPluginManager().registerEvents(new QuestObjectiveCompleteListener(), this);

        // World listener
        Bukkit.getPluginManager().registerEvents(new FakeBlockBreakListener(), this);
        CustomBlockData.registerListener(this);

        // Entity Listeners
        Bukkit.getPluginManager().registerEvents(new EntitySpawnListener(), this);

        // Debug Listener
        Bukkit.getPluginManager().registerEvents(new OnAbilityActivateListener(), this);

        // Setting listener
        Bukkit.getPluginManager().registerEvents(new PlayerSettingChangeListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerPickupItemListener(), this);
    }

    @Override
    public void registerDrivers() {
        driverManager.registerDriver(new McRPGSqliteDriver(this));
    }

    @NotNull
    @Override
    public McRPGDatabase getDatabase() {
        return database;
    }

    @NotNull
    @Override
    public PlayerManager<McRPG, McRPGPlayer> getPlayerManager() {
        return playerManager;
    }

    @NotNull
    @Override
    public McRPGLocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    /**
     * Setup 3rd party plugin hooks that are natively supported by McRPG
     */
    @Override
    protected void setupHooks() {
        super.setupHooks();

        healthBarPluginEnabled = getServer().getPluginManager().getPlugin("HealthBar") != null;
        sickleEnabled = getServer().getPluginManager().getPlugin("Sickle") != null;

        if (healthBarPluginEnabled) {
            getLogger().info("HealthBar plugin found, McRPG's healthbars are automatically disabled.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("Papi PlaceholderAPI found... registering placeholders");
            new McRPGPapiExpansion(this).register();
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
            worldGuardHook = new WorldGuardHook(this);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Geyser")) {
            geyserEnabled = true;
            getLogger().info("Geyser found... enabling support.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Apollo-Bukkit")) {
            lunarEnabled = true;
            getLogger().info("Apollo found... enabling Lunar Client support.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Lands")) {
            landsHook = new LandsHook(this);
        }
    }

    /**
     * Registers the native {@link us.eunoians.mcrpg.expansion.ContentExpansion}s for McRPG
     */
    private void registerNativeExpansions() {
        Arrays.stream(ContentHandlerType.values()).forEach(contentHandlerType -> contentExpansionManager.registerContentHandler(contentHandlerType.getContentHandler()));
        contentExpansionManager.registerContentExpansion(new McRPGExpansion(this));
    }

    /**
     * Register all background tasks that McROG uses.
     */
    private void registerBackgroundTasks() {
        ReloadableTask<McRPGPlayerSaveTask> saveTask = new ReloadableTask<>(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.SAVE_TASK_FREQUENCY,
                (yamlDocument, route) -> {
                    int frequency = yamlDocument.getInt(route);
                    return new McRPGPlayerSaveTask(this, frequency, frequency);
                }, true);
        ReloadableTask<RestedExperienceAccumulationTask> safeZoneUpdateTask = new ReloadableTask<>(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.ONLINE_RESTED_EXPERIENCE_TASK_FREQUENCY,
                (yamlDocument, route) -> {
                    int frequency = yamlDocument.getInt(route);
                    return new RestedExperienceAccumulationTask(this, frequency, frequency);
                }, false);
        reloadableContentRegistry.trackReloadableContent(Set.of(saveTask, safeZoneUpdateTask));
    }

    /**
     * Registers all the natively supported {@link us.eunoians.mcrpg.skill.experience.modifier.ExperienceModifier}s.
     */
    private void registerExperienceModifiers() {
        experienceModifierRegistry.registerModifier(new HeldItemBonusModifier());
        experienceModifierRegistry.registerModifier(new SpawnReasonModifier());
        experienceModifierRegistry.registerModifier(new BoostedExperienceModifier(this));
        experienceModifierRegistry.registerModifier(new RestedExperienceModifier(this));
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
     * Get the {@link EntityManager} used by McRPG
     *
     * @return The {@link EntityManager} used by McRPG
     */
    @NotNull
    public EntityManager getEntityManager() {
        return entityManager;
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
    @NotNull
    public AbilityAttributeManager getAbilityAttributeManager() {
        return abilityAttributeManager;
    }

    /**
     * Gets the {@link DisplayManager} used by McRPG
     *
     * @return The {@link DisplayManager} used by McRPG
     */
    @NotNull
    public DisplayManager getDisplayManager() {
        return displayManager;
    }

    /**
     * Gets the {@link QuestManager} used by McRPG
     *
     * @return The {@link QuestManager} used by McRPG
     */
    @NotNull
    public QuestManager getQuestManager() {
        return questManager;
    }

    /**
     * Gets the {@link GlowingBlocks} used by McRPG.
     *
     * @return The {@link GlowingBlocks} used by McRPG.
     */
    @NotNull
    public GlowingBlocks getGlowingBlocks() {
        return glowingBlocks;
    }

    /**
     * Gets the {@link GlowingEntities} used by McRPG.
     *
     * @return The {@link GlowingEntities} used by McRPG.
     */
    @NotNull
    public GlowingEntities getGlowingEntities() {
        return glowingEntities;
    }

    /**
     * Gets the {@link BleedManager} used by McRPG.
     *
     * @return The {@link BleedManager} used by McRPG.
     */
    @NotNull
    public BleedManager getBleedManager() {
        return bleedManager;
    }

    /**
     * Gets the {@link ContentExpansionManager} used by McRPG.
     *
     * @return The {@link ContentExpansionManager} used by McRPG.
     */
    @NotNull
    public ContentExpansionManager getContentExpansionManager() {
        return contentExpansionManager;
    }

    /**
     * Gets the {@link WorldManager} used by McRPG.
     *
     * @return The {@link WorldManager} used by McRPG.
     */
    @NotNull
    public WorldManager getWorldManager() {
        return worldManager;
    }

    /**
     * Gets the {@link SafeZoneManager} used by McRPG.
     *
     * @return The {@link SafeZoneManager} used by McRPG.
     */
    @NotNull
    public SafeZoneManager getSafeZoneManager() {
        return safeZoneManager;
    }

    /**
     * Gets the {@link ExperienceModifierRegistry} used by McRPG.
     *
     * @return The {@link ExperienceModifierRegistry} used by McRPG.
     */
    @NotNull
    public ExperienceModifierRegistry getExperienceModifierRegistry() {
        return experienceModifierRegistry;
    }

    /**
     * Gets the {@link RestedExperienceManager} used by McRPG.
     *
     * @return The {@link RestedExperienceManager} used by McRPG.
     */
    @NotNull
    public RestedExperienceManager getRestedExperienceManager() {
        return restedExperienceManager;
    }

    /**
     * Checks to see if Lunar Client support is enabled.
     *
     * @return {@code true} if Lunar Client support is enabled
     */
    public boolean isLunarEnabled() {
        return lunarEnabled;
    }

    /**
     * Checks to see if Geyser is enabled and registered.
     *
     * @return {@code true} if Geyser is enabled and registered.
     */
    public boolean isGeyserEnabled() {
        return geyserEnabled && Geyser.isRegistered();
    }

    /**
     * Gets the {@link LandsHook} McRPG uses to support Lands.
     *
     * @return An {@link Optional} containing the {@link LandsHook} McRPG uses to support
     * <a href="https://www.spigotmc.org/resources/lands-%E2%AD%95-land-claim-plugin-%E2%9C%85-grief-prevention-protection-gui-management-nations-wars-1-21-support.53313/">Lands</a>
     * if Lands is running.
     */
    @NotNull
    public Optional<LandsHook> getLandsHook() {
        return Optional.ofNullable(landsHook);
    }

    /**
     * Gets the {@link WorldGuardHook} McRPG uses to support WorldGuard.
     *
     * @return An {@link Optional} containing the {@link WorldGuardHook} McRPG uses to support
     * <a href="https://modrinth.com/plugin/worldguard/versions">WorldGuard</a> if the plugin is running.
     */
    @NotNull
    public Optional<WorldGuardHook> getWorldGuardHook() {
        return Optional.ofNullable(worldGuardHook);
    }


    /**
     * Gets the running instance of {@link McRPG}.
     *
     * @return The running instance of {@link McRPG}.
     */
    @NotNull
    public static McRPG getInstance() {
        return (McRPG) CorePlugin.getInstance();
    }
}
