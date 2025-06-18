package us.eunoians.mcrpg.ability.impl.mining.remotetransfer;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ReloadableRemoteTransferMap extends ReloadableContent<Map<String, RemoteTransferCategory>> {

    public ReloadableRemoteTransferMap(@NotNull YamlDocument yamlDocument, @NotNull Route route) {
        super(yamlDocument, route, (document, categoryRoute) -> {
            Map<String, RemoteTransferCategory> map = new HashMap<>();
            Section categorySection = yamlDocument.getSection(route);
            for (String categoryKey : categorySection.getRoutesAsStrings(false)) {
                RemoteTransferCategory category = new RemoteTransferCategory(categoryKey);
                categorySection.getStringList(categoryKey).forEach(string -> )
                map.put(categoryKey, category);
            }
        });
    }
}
