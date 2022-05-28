package us.eunoians.mcrpg.configuration.file;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.FileType;

public enum SwordsConfigurationFile implements McRPGConfigurationFile {

    ;

    private final String path;
    private final Object defaultValue;
    private final boolean acceptsPlaceholders;

    SwordsConfigurationFile(@NotNull String path, @NotNull Object defaultValue, boolean acceptsPlaceholders){
        this.path = path;
        this.defaultValue = defaultValue;
        this.acceptsPlaceholders = acceptsPlaceholders;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean acceptsPlaceholders() {
        return acceptsPlaceholders;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public FileType getFileType() {
        return FileType.SWORDS_CONFIG;
    }
}
