package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.external.geyser.GeyserHook;
import us.eunoians.mcrpg.external.lands.LandsHook;
import us.eunoians.mcrpg.external.lunar.LunarClientHook;
import us.eunoians.mcrpg.external.mcmmo.McMMOHook;
import us.eunoians.mcrpg.external.nocheatplus.NoCheatPlusHook;
import us.eunoians.mcrpg.external.papi.McRPGPapiHook;
import us.eunoians.mcrpg.external.sickle.SickleHook;
import us.eunoians.mcrpg.external.worldguard.WorldGuardHook;

import java.util.logging.Logger;

/**
 * This registrar is in charge of registering {@link com.diamonddagger590.mccore.registry.plugin.PluginHook}s
 * for McRPG.
 */
final class McRPGHooksRegistrar implements Registrar<McRPG> {

    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        McRPG plugin = context.plugin();
        Logger logger = plugin.getLogger();
        PluginHookRegistry pluginHookRegistry = plugin.registryAccess().registry(RegistryKey.PLUGIN_HOOK);
        if (Bukkit.getPluginManager().isPluginEnabled("Sickle")) {
            pluginHookRegistry.register(new SickleHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger.info("PlaceholderAPI found... registering placeholders");
            pluginHookRegistry.register(new McRPGPapiHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
            logger.info("NoCheatPlus found... will enable anticheat support");
            pluginHookRegistry.register(new NoCheatPlusHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            logger.info("McMMO found... ready to convert.");
            pluginHookRegistry.register(new McMMOHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            logger.info("WorldGuard found... enabling support");
            pluginHookRegistry.register(new WorldGuardHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Geyser")) {
            logger.info("Geyser found... enabling support.");
            pluginHookRegistry.register(new GeyserHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Apollo-Bukkit")) {
            logger.info("Apollo found... enabling Lunar Client support.");
            pluginHookRegistry.register(new LunarClientHook(plugin));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Lands")) {
            logger.info("Lands found... enabling support.");
            pluginHookRegistry.register(new LandsHook(plugin));
        }
    }
}
