package us.eunoians.mcrpg.bootstrap;

import com.diamonddagger590.mccore.bootstrap.BootstrapContext;
import com.diamonddagger590.mccore.bootstrap.registrar.Registrar;
import com.diamonddagger590.mccore.configuration.task.ReloadableTask;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.task.experience.RestedExperienceAccumulationTask;
import us.eunoians.mcrpg.task.player.McRPGPlayerSafeZoneCheckTask;
import us.eunoians.mcrpg.task.player.McRPGPlayerSaveTask;
import us.eunoians.mcrpg.task.player.PlayerActionBarTask;

import java.util.Set;

/**
 * This registrar is in charge of registering background tasks for McRPG.
 * <p>
 * These tasks tend to be {@link ReloadableTask}s and will constantly run
 * from the time the plugin is initialized until the server stops and kills them.
 */
final class McRPGBackgroundTaskRegistrar implements Registrar<McRPG> {

    @Override
    public void register(@NotNull BootstrapContext<McRPG> context) {
        McRPG plugin = context.plugin();
        FileManager fileManager = plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE);
        ReloadableTask<McRPGPlayerSaveTask> saveTask = new ReloadableTask<>(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.SAVE_TASK_FREQUENCY,
                (yamlDocument, route) -> {
                    double frequency = yamlDocument.getDouble(route);
                    return new McRPGPlayerSaveTask(plugin, frequency, frequency);
                }, true);
        ReloadableTask<RestedExperienceAccumulationTask> restedExperienceAccumulationTask = new ReloadableTask<>(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.ONLINE_RESTED_EXPERIENCE_TASK_FREQUENCY,
                (yamlDocument, route) -> {
                    double frequency = yamlDocument.getDouble(route);
                    return new RestedExperienceAccumulationTask(plugin, frequency, frequency);
                }, false);
        ReloadableTask<McRPGPlayerSafeZoneCheckTask> safeZoneCheckTask = new ReloadableTask<>(fileManager.getFile(FileType.MAIN_CONFIG), MainConfigFile.SAFE_ZONE_UPDATE_TASK_TICK_FREQUENCY,
                (yamlDocument, route) -> {
                    double frequency = yamlDocument.getDouble(route);
                    return new McRPGPlayerSafeZoneCheckTask(plugin, frequency, frequency);
                }, false);
        PlayerActionBarTask playerActionBarTask = new PlayerActionBarTask(plugin);
        playerActionBarTask.runTask();
        plugin.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(Set.of(saveTask, restedExperienceAccumulationTask, safeZoneCheckTask));
    }
}
