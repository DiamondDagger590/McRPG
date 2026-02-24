package us.eunoians.mcrpg.quest.board.configuration;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.QuestTemplateConfigLoader;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;
import us.eunoians.mcrpg.quest.board.template.QuestTemplate;
import us.eunoians.mcrpg.quest.board.template.QuestTemplateRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom {@link ReloadableContent} subclass that re-scans the primary templates directory
 * plus all expansion-registered directories and pushes the result into the
 * {@link QuestTemplateRegistry} on reload.
 * <p>
 * Uses {@link BoardConfigFile#MINIMUM_TOTAL_OFFERINGS} as the trigger route so the
 * reload fires when the board config is reloaded. Only config-loaded templates are
 * replaced; expansion-registered (programmatic) templates survive reloads.
 */
public class ReloadableTemplateConfig extends ReloadableContent<Map<NamespacedKey, QuestTemplate>> {

    public ReloadableTemplateConfig(@NotNull YamlDocument boardConfig,
                                    @NotNull QuestTemplateRegistry registry,
                                    @NotNull File primaryTemplatesDirectory) {
        super(boardConfig, BoardConfigFile.MINIMUM_TOTAL_OFFERINGS, (doc, route) -> {
            QuestTemplateConfigLoader loader = new QuestTemplateConfigLoader(
                    java.util.logging.Logger.getLogger(ReloadableTemplateConfig.class.getName()));

            List<File> allDirectories = new ArrayList<>();
            allDirectories.add(primaryTemplatesDirectory);
            allDirectories.addAll(registry.getExpansionDirectories());

            Map<NamespacedKey, QuestTemplate> map = loader.loadTemplatesFromDirectories(allDirectories);
            registry.replaceConfigTemplates(map);
            return map;
        });
    }
}
