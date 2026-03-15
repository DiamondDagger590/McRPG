package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.CoreBootstrap;
import com.diamonddagger590.mccore.bootstrap.StartupProfile;
import com.diamonddagger590.mccore.database.Database;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeRegistry;
import us.eunoians.mcrpg.ability.impl.swords.bleed.BleedManager;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.McRPGPlayerManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.external.glowing.GlowingManager;
import us.eunoians.mcrpg.gui.McRPGGuiManager;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.registry.plugin.McRPGPluginHookKey;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.experience.ExperienceModifierRegistry;
import us.eunoians.mcrpg.skill.experience.rested.RestedExperienceManager;
import us.eunoians.mcrpg.world.WorldManager;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This bootstrap is the main bootstrap used to initialize McRPG
 * and all of its dependencies on things like managers, registries, plugin hooks
 * and more.
 */
public class McRPGBootstrap extends CoreBootstrap<McRPG> {

    public McRPGBootstrap(@NotNull McRPG plugin) {
        super(plugin);
    }

    @Override
    public void start(@NotNull StartupProfile startupProfile) {
        McRPG mcRPG = getPlugin();
        RegistryAccess registryAccess = mcRPG.registryAccess();
        BootstrapContext<McRPG> bootstrapContext = new BootstrapContext<>(mcRPG, startupProfile);
        super.start(startupProfile);

        registryAccess.registry(RegistryKey.MANAGER).register(new FileManager(mcRPG));
        registryAccess.registry(RegistryKey.MANAGER).register(new McRPGLocalizationManager(mcRPG));
        registryAccess.registry(RegistryKey.MANAGER).register(new ContentExpansionManager(mcRPG));
        registryAccess.register(new AbilityRegistry(mcRPG));
        registryAccess.register(new AbilityAttributeRegistry());
        registryAccess.register(new SkillRegistry());
        new McRPGExpansionRegistrar().register(bootstrapContext);
        registryAccess.registry(RegistryKey.MANAGER).register(new GlowingManager(mcRPG));
        registryAccess.registry(RegistryKey.MANAGER).register(new EntityManager(mcRPG));
        registryAccess.registry(RegistryKey.MANAGER).register(new McRPGPlayerManager(mcRPG));
        registryAccess.registry(RegistryKey.MANAGER).register(new DisplayManager(mcRPG));
        registryAccess.registry(RegistryKey.MANAGER).register(new QuestManager(mcRPG));
        registryAccess.registry(RegistryKey.MANAGER).register(new BleedManager(mcRPG));
        registryAccess.registry(RegistryKey.MANAGER).register(new WorldManager(mcRPG));
        registryAccess.register(new ExperienceModifierRegistry());
        registryAccess.registry(RegistryKey.MANAGER).register(new RestedExperienceManager(mcRPG));
        registryAccess.registry(RegistryKey.MANAGER).register(new McRPGGuiManager(mcRPG));

        new McRPGListenerRegistrar().register(bootstrapContext);
        new McRPGHooksRegistrar().register(bootstrapContext);

        if (startupProfile == StartupProfile.PROD) {
            new McRPGDriverRegistrar().register(bootstrapContext);
            registryAccess.registry(RegistryKey.MANAGER).register(new McRPGDatabaseManager(mcRPG));
            new McRPGCommandRegistrar().register(bootstrapContext);
            new McRPGExperienceModifiersRegistrar().register(bootstrapContext);
            new McRPGBackgroundTaskRegistrar().register(bootstrapContext);
        }

        registryAccess.registry(RegistryKey.MANAGER).manager(ManagerKey.RELOADABLE_CONTENT).reloadAllContent();
    }

    @Override
    public void stop(@NotNull StartupProfile startupProfile) {
        if (startupProfile == StartupProfile.PROD) {
            RegistryAccess registryAccess = getPlugin().registryAccess();
            if (registryAccess.registry(RegistryKey.MANAGER).registered(McRPGManagerKey.GLOWING)) {
                registryAccess.registry(RegistryKey.MANAGER).manager(McRPGManagerKey.GLOWING).shutdown();
            }
            if (registryAccess.registry(RegistryKey.MANAGER).registered(McRPGManagerKey.DATABASE)) {
                Database database = registryAccess.registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DATABASE).getDatabase();
                try (Connection connection = database.getConnection()) {
                    var lunarClientHook = McRPG.getInstance().registryAccess().registry(RegistryKey.PLUGIN_HOOK).pluginHook(McRPGPluginHookKey.LUNAR_CLIENT);
                    for (McRPGPlayer mcRPGPlayer : registryAccess.registry(RegistryKey.MANAGER).manager(McRPGManagerKey.PLAYER).getAllPlayers()) {
                        mcRPGPlayer.savePlayer(connection);
                        mcRPGPlayer.savePlayerLogoutTime(connection);
                        lunarClientHook.ifPresent(pluginHook -> pluginHook.clearCooldowns(mcRPGPlayer.getUUID()));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                database.shutdown();
            }
        }
    }
}
