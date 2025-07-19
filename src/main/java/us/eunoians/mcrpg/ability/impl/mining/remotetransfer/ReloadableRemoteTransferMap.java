package us.eunoians.mcrpg.ability.impl.mining.remotetransfer;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomBlockWrapper;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;

/**
 * A reloadable map of all the {@link RemoteTransferCategory RemoteTransferCategories} mapped to their key as specified in the config.
 *
 * This map supports {@link CustomBlockWrapper}s which means {@link org.bukkit.Material}s and custom block identifiers can be used
 * interchangeably.
 */
public class ReloadableRemoteTransferMap extends ReloadableContent<Map<String, RemoteTransferCategory>> {

    public ReloadableRemoteTransferMap() {
        super(McRPG.getInstance().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE).getFile(FileType.MINING_CONFIG),
                Route.fromString(MiningConfigFile.REMOTE_TRANSFER_CATEGORIES_HEADER), (document, categoryRoute) -> {
            Map<String, RemoteTransferCategory> map = new HashMap<>();
            RemoteTransferCategory allCategory = new RemoteTransferCategory("all");
            Section categorySection = document.getSection(categoryRoute);
            for (String categoryKey : categorySection.getRoutesAsStrings(false)) {
                if (categoryKey.equalsIgnoreCase("all")) {
                    throw new RuntimeException("'All' category is not allowed to be defined in the mining configuration file. Please remove it and restart the server to fix this.");
                }
                RemoteTransferCategory category = new RemoteTransferCategory(categoryKey);
                categorySection.getStringList(categoryKey).forEach(string -> {
                    CustomItemWrapper customItemWrapper = new CustomItemWrapper(string);
                    // Add to the current category and the all category
                    category.addCategoryItem(customItemWrapper);
                    allCategory.addCategoryItem(customItemWrapper);
                });
                map.put(categoryKey, category);
            }
            map.put("all", allCategory);
            return map;
        });
    }
}
