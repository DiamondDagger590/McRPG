package us.eunoians.mcrpg.configuration;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private final McRPG mcRPG;
    private final Map<FileType, YamlDocument> loadedFiles;

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
        for (YamlDocument yamlDocument : loadedFiles.values()) {
            try {
                yamlDocument.reload();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @NotNull
    public YamlDocument getFile(@NotNull FileType fileType) {
        return loadedFiles.get(fileType);
    }
}
