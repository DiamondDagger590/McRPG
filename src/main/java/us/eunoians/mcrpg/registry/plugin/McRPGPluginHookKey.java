package us.eunoians.mcrpg.registry.plugin;

import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.plugin.PluginHook;
import com.diamonddagger590.mccore.registry.plugin.PluginHookKey;
import us.eunoians.mcrpg.external.geyser.GeyserHook;
import us.eunoians.mcrpg.external.lands.LandsHook;
import us.eunoians.mcrpg.external.lunar.LunarClientHook;
import us.eunoians.mcrpg.external.mcmmo.McMMOHook;
import us.eunoians.mcrpg.external.nocheatplus.NoCheatPlusHook;
import us.eunoians.mcrpg.external.papi.McRPGPapiHook;
import us.eunoians.mcrpg.external.sickle.SickleHook;
import us.eunoians.mcrpg.external.worldguard.WorldGuardHook;

import static com.diamonddagger590.mccore.registry.plugin.PluginHookKeyImpl.create;

/**
 * A soft enum of all {@link PluginHookKey}s supported by McRPG.
 * <p>
 * To use these, you will need access to the {@link com.diamonddagger590.mccore.registry.plugin.PluginHookRegistry}
 * via {@link com.diamonddagger590.mccore.registry.RegistryAccess#registry(RegistryKey)} while passing in
 * {@link RegistryKey#PLUGIN_HOOK}.
 */
public interface McRPGPluginHookKey extends PluginHookKey<PluginHook<?>> {

    PluginHookKey<LandsHook> LANDS = create(LandsHook.class);
    PluginHookKey<LunarClientHook> LUNAR_CLIENT = create(LunarClientHook.class);
    PluginHookKey<McRPGPapiHook> PAPI = create(McRPGPapiHook.class);
    PluginHookKey<WorldGuardHook> WORLDGUARD = create(WorldGuardHook.class);
    PluginHookKey<SickleHook> SICKLE = create(SickleHook.class);
    PluginHookKey<NoCheatPlusHook> NO_CHEAT_PLUS = create(NoCheatPlusHook.class);
    PluginHookKey<McMMOHook> MCMMO = create(McMMOHook.class);
    PluginHookKey<GeyserHook> GEYSER = create(GeyserHook.class);
}
