package us.eunoians.mcrpg.ability.impl.mining.orescanner;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A type of {@link ReloadableContent} that contains a {@link Set} of {@link OreScannerBlockType}s.
 * <p>
 * This will pull information from the configuration file and convert each section under the provided {@link Route} into
 * its own {@link OreScannerBlockType}.
 */
public class ReloadableOreScannerBlocks extends ReloadableContent<Set<OreScannerBlockType>> {

    public ReloadableOreScannerBlocks(@NotNull YamlDocument yamlDocument, @NotNull Route route) {
        super(yamlDocument, route, ((yamlDocument1, route1) -> {
            Set<OreScannerBlockType> types = new HashSet<>();
            Section section = yamlDocument1.getSection(route1);
            for (String scanner : section.getRoutesAsStrings(false)) {
                Route scannerRoute = Route.addTo(route1, scanner);
                String typeName = yamlDocument1.getString(Route.addTo(scannerRoute, "type-name"));
                ChatColor chatColor = ChatColor.getByChar(yamlDocument1.getChar(Route.addTo(scannerRoute, "color-code")));
                Set<Material> materials = yamlDocument1.getStringList(Route.addTo(scannerRoute, "materials")).stream().map(Material::getMaterial).collect(Collectors.toSet());
                int weight = yamlDocument1.getInt(Route.addTo(scannerRoute, "weight"));
                types.add(new OreScannerBlockType(materials, typeName, chatColor, weight));
            }
            return types;
        }));
    }
}
