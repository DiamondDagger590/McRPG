package us.eunoians.mcrpg.configuration.file;

import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import dev.dejvokep.boostedyaml.spigot.SpigotSerializer;
import org.jetbrains.annotations.NotNull;

public abstract class ConfigFile {

    @NotNull
    public GeneralSettings getGeneralSettings() {
        return GeneralSettings.builder().setKeyFormat(GeneralSettings.KeyFormat.STRING).setSerializer(SpigotSerializer.getInstance()).build();
    }

    @NotNull
    public LoaderSettings getLoaderSettings() {
        return LoaderSettings.builder().setAutoUpdate(true).build();
    }

    @NotNull
    public DumperSettings getDumperSettings() {
        return DumperSettings.DEFAULT;
    }

    public UpdaterSettings getUpdaterSettings() {
        return UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version")).build();
    }
}
