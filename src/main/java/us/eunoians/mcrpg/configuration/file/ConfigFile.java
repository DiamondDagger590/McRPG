package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * The base for any configuration file used by McRPG.
 * <p>
 * This provides methods needed for constructing an instance of {@link dev.dejvokep.boostedyaml.YamlDocument}.
 */
public abstract class ConfigFile {

    /**
     * Gets the {@link GeneralSettings} for this config file.
     *
     * @return The {@link GeneralSettings} for this config file.
     */
    @NotNull
    public GeneralSettings getGeneralSettings() {
        return GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.STRING).setSerializer(SpigotSerializer.getInstance()).build();
    }

    /**
     * Gets the {@link LoaderSettings} for this config file.
     *
     * @return The {@link LoaderSettings} for this config file.
     */
    @NotNull
    public LoaderSettings getLoaderSettings() {
        return LoaderSettings.builder().setAutoUpdate(true).build();
    }

    /**
     * Gets the {@link DumperSettings} for this config file.
     *
     * @return The {@link DumperSettings} for this config.
     */
    @NotNull
    public DumperSettings getDumperSettings() {
        return DumperSettings.DEFAULT;
    }

    /**
     * Gets the {@link UpdaterSettings} for this config file.
     *
     * @return The {@link UpdaterSettings} for this config file.
     */
    @NotNull
    public UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build();
    }
}
