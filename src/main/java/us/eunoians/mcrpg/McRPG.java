package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.command.DisplayNameCommand;
import com.diamonddagger590.mccore.command.LoreCommand;
import com.diamonddagger590.mccore.configuration.ReloadableTask;
import com.diamonddagger590.mccore.database.driver.DatabaseDriverType;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import com.jeff_media.customblockdata.CustomBlockData;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
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
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.expansion.handler.ContentHandlerType;
import us.eunoians.mcrpg.external.geyser.GeyserHook;
import us.eunoians.mcrpg.external.lands.LandsHook;
import us.eunoians.mcrpg.external.lunar.LunarClientHook;
import us.eunoians.mcrpg.external.mcmmo.McMMOHook;
import us.eunoians.mcrpg.external.nocheatplus.NoCheatPlusHook;
import us.eunoians.mcrpg.external.papi.McRPGPapiHook;
import us.eunoians.mcrpg.external.sickle.SickleHook;
import us.eunoians.mcrpg.external.worldguard.WorldGuardHook;
import us.eunoians.mcrpg.gui.McRPGGuiManager;
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
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;
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
import java.util.Set;

/**
 * The main class for McRPG where developers should be able to access various components of the API's provided by McRPG
 */
public class McRPG extends CorePlugin {

    private static final int id = 6386;

    //Needed to support McMMO's Healthbars
    private static final String customNameKey = "mcMMO: Custom Name";
    private static final String customVisibleKey = "mcMMO: Name Visibility";

    private McRPGDatabase database;

    private GlowingBlocks glowingBlocks;
    private GlowingEntities glowingEntities;

    @Override
    public void onEnable() {
        super.onEnable();
        if (!isUnitTest()) {
            registryAccess().registry(RegistryKey.MANAGER).register(new FileManager(this));
            glowingBlocks = new GlowingBlocks(this);
            glowingEntities = new GlowingEntities(this);
        }

        registryAccess().registry(RegistryKey.MANAGER).register(new EntityManager(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new McRPGPlayerManager(this));
        registryAccess().register(new AbilityRegistry(this));
        registryAccess().register(new SkillRegistry(this));
        registryAccess().register(new AbilityAttributeRegistry(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new McRPGLocalizationManager(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new DisplayManager(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new QuestManager(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new BleedManager(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new ContentExpansionManager(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new WorldManager(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new SafeZoneManager(this));
        registryAccess().register(new ExperienceModifierRegistry(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new RestedExperienceManager(this));
        registryAccess().registry(RegistryKey.MANAGER).register(new McRPGGuiManager(this));

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
            registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.RELOADABLE_CONTENT).reloadAllContent();
        }
    }

    @Override
    public void onDisable() {
        if (!isUnitTest()) {
            glowingBlocks.disable();
            glowingEntities.disable();
            try (Connection connection = getDatabase().getConnection()) {
                var lunarClientHook = McRPG.getInstance().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.LUNAR_CLIENT);
                for (McRPGPlayer mcRPGPlayer : registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getAllPlayers()) {
                    mcRPGPlayer.savePlayer(connection);
                    lunarClientHook.ifPresent(pluginHook -> pluginHook.clearCooldowns(mcRPGPlayer.getUUID()));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        super.onDisable();
    }

    @Override
    protected void constructCommands() {
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
    protected void registerListeners() {
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
    protected void registerDrivers() {
        registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.DRIVER).registerDriver(new McRPGSqliteDriver(this));
    }

    @NotNull
    @Override
    public McRPGDatabase getDatabase() {
        return database;
    }

    /**
     * Setup 3rd party plugin hooks that are natively supported by McRPG
     */
    @Override
    protected void setupHooks() {
        super.setupHooks();

        PluginHookRegistry pluginHookRegistry = registryAccess().registry(RegistryKey.PLUGIN_HOOK);
        if (Bukkit.getPluginManager().isPluginEnabled("Sickle")) {
            pluginHookRegistry.register(new SickleHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI found... registering placeholders");
            pluginHookRegistry.register(new McRPGPapiHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
            getLogger().info("NoCheatPlus found... will enable anticheat support");
            pluginHookRegistry.register(new NoCheatPlusHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            getLogger().info("McMMO found... ready to convert.");
            pluginHookRegistry.register(new McMMOHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            getLogger().info("WorldGuard found... enabling support");
            pluginHookRegistry.register(new WorldGuardHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Geyser")) {
            getLogger().info("Geyser found... enabling support.");
            pluginHookRegistry.register(new GeyserHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Apollo-Bukkit")) {
            getLogger().info("Apollo found... enabling Lunar Client support.");
            pluginHookRegistry.register(new LunarClientHook(this));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Lands")) {
            getLogger().info("Lands found... enabling support.");
            pluginHookRegistry.register(new LandsHook(this));
        }
    }

    /**
     * Registers the native {@link us.eunoians.mcrpg.expansion.ContentExpansion}s for McRPG
     */
    private void registerNativeExpansions() {
        ContentExpansionManager contentExpansionManager = registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.CONTENT_EXPANSION);
        Arrays.stream(ContentHandlerType.values()).forEach(contentHandlerType -> contentExpansionManager.registerContentHandler(contentHandlerType.getContentHandler()));
        contentExpansionManager.registerContentExpansion(new McRPGExpansion(this));
    }

    /**
     * Register all background tasks that McROG uses.
     */
    private void registerBackgroundTasks() {
        FileManager fileManager = registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
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
        registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.RELOADABLE_CONTENT).trackReloadableContent(Set.of(saveTask, safeZoneUpdateTask));
    }

    /**
     * Registers all the natively supported {@link us.eunoians.mcrpg.skill.experience.modifier.ExperienceModifier}s.
     */
    private void registerExperienceModifiers() {
        ExperienceModifierRegistry experienceModifierRegistry = registryAccess().registry(McRPGRegistryKey.EXPERIENCE_MODIFIER);
        experienceModifierRegistry.register(new HeldItemBonusModifier());
        experienceModifierRegistry.register(new SpawnReasonModifier());
        experienceModifierRegistry.register(new BoostedExperienceModifier(this));
        experienceModifierRegistry.register(new RestedExperienceModifier(this));
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
     * Gets the running instance of {@link McRPG}.
     *
     * @return The running instance of {@link McRPG}.
     */
    @NotNull
    public static McRPG getInstance() {
        return (McRPG) CorePlugin.getInstance();
    }
}
