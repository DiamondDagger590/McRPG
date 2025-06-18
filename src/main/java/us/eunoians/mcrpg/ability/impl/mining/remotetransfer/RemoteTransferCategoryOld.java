package us.eunoians.mcrpg.ability.impl.mining.remotetransfer;

import com.diamonddagger590.mccore.configuration.collection.ReloadableSet;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class allows for automated reload support for a given {@link RemoteTransferCategoryType}.
 */
public class RemoteTransferCategoryOld extends ReloadableSet<Material> {

    private final RemoteTransferCategoryType categoryType;

    public RemoteTransferCategoryOld(@NotNull RemoteTransferCategoryType categoryType) {
        super(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG), categoryType.getConfigurationRoute(), strings -> strings.stream().map(Material::getMaterial).collect(Collectors.toSet()));
        this.categoryType = categoryType;
    }

    public RemoteTransferCategoryOld(@NotNull RemoteTransferCategoryType categoryType, @NotNull Set<Material> content) {
        super(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG), categoryType.getConfigurationRoute(), strings -> strings.stream().map(Material::getMaterial).collect(Collectors.toSet()), content);
        this.categoryType = categoryType;
    }

    /**
     * Get the {@link RemoteTransferCategoryType} used to load data into this category.
     *
     * @return The {@link RemoteTransferCategoryType} used to load data into this category.
     */
    @NotNull
    public RemoteTransferCategoryType getCategoryType() {
        return categoryType;
    }
}
