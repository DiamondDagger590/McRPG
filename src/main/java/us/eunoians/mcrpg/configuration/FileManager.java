package us.eunoians.mcrpg.configuration;

import ch.jalu.configme.SettingsManager;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private final McRPG mcRPG;
    private final Map<FileType, SettingsManager> loadedFiles;

    public FileManager(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        this.loadedFiles = new HashMap<>();

        if (!mcRPG.getDataFolder().exists()) {
            mcRPG.getDataFolder().mkdirs();
        }

        loadFiles();
    }

    private void loadFiles() {
        for(FileType fileType : FileType.values()) {
            loadedFiles.put(fileType, fileType.initializeFile());
        }
    }

    public void reloadFiles() {
        for (SettingsManager settingsManager : loadedFiles.values()) {
            settingsManager.reload();
        }
    }

    @NotNull
    public SettingsManager getFile(@NotNull FileType fileType) {
        return loadedFiles.get(fileType);
    }
}
