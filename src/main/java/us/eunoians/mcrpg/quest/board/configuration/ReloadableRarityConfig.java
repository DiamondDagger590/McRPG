package us.eunoians.mcrpg.quest.board.configuration;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Custom {@link ReloadableContent} subclass that re-parses rarity config from {@code board.yml}
 * and pushes the result into the {@link QuestRarityRegistry} on reload.
 */
public class ReloadableRarityConfig extends ReloadableContent<Map<NamespacedKey, QuestRarity>> {

    public ReloadableRarityConfig(@NotNull YamlDocument boardConfig,
                                  @NotNull QuestRarityRegistry registry) {
        super(boardConfig, BoardConfigFile.RARITIES, (document, route) -> {
            Map<NamespacedKey, QuestRarity> map = new LinkedHashMap<>();
            Section section = document.getSection(route);
            if (section != null) {
                for (String rawKey : section.getRoutesAsStrings(false)) {
                    NamespacedKey key = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), rawKey.toLowerCase());
                    Section raritySection = section.getSection(rawKey);
                    // A rarity key with an empty body (e.g. "MYTHIC:" with nothing under it) yields a
                    // null section. Skip it with a WARNING rather than NPEing and aborting every rarity.
                    if (raritySection == null) {
                        McRPG.getInstance().getLogger().warning("[QuestBoard] Skipping rarity '" + rawKey
                                + "' in board.yml: it has no configuration body (expected weight,"
                                + " difficulty-multiplier, reward-multiplier).");
                        continue;
                    }
                    try {
                        Section displaySection = raritySection.getSection("display");
                        String nameColor = displaySection != null ? displaySection.getString("name-color") : null;

                        map.put(key, new QuestRarity(
                                key,
                                raritySection.getInt("weight"),
                                raritySection.getDouble("difficulty-multiplier"),
                                raritySection.getDouble("reward-multiplier"),
                                McRPGExpansion.EXPANSION_KEY,
                                displaySection,
                                nameColor
                        ));
                    } catch (RuntimeException exception) {
                        McRPG.getInstance().getLogger().log(Level.WARNING,
                                "[QuestBoard] Skipping malformed rarity '" + rawKey + "' in board.yml", exception);
                    }
                }
            }
            registry.replaceConfigRarities(map);
            return map;
        });
    }
}
