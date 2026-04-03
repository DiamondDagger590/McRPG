package us.eunoians.mcrpg.fishing;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.FishingMobSpawnConfigFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A {@link ReloadableContent} that wraps the fishing mob pool configuration.
 * On reload, re-parses the {@code mob-pool} YAML section and constructs a
 * fresh {@link MobPoolSelector}.
 * <p>
 * Registered with the {@code ReloadableContentManager} at startup so that
 * {@code /mcrpg admin reload} picks up pool changes without a server restart.
 */
public class ReloadableMobPool extends ReloadableContent<MobPoolSelector> {

    /**
     * Creates a new reloadable mob pool backed by the given config document.
     *
     * @param config the fishing mob spawn configuration YAML document
     */
    public ReloadableMobPool(@NotNull YamlDocument config) {
        super(config, FishingMobSpawnConfigFile.MOB_POOL, (doc, route) -> {
            List<MobPoolEntry> entries = parseMobPool(doc);
            return new MobPoolSelector(entries);
        });
    }

    @NotNull
    private static List<MobPoolEntry> parseMobPool(@NotNull YamlDocument config) {
        Logger logger = McRPG.getInstance().getLogger();
        if (!config.contains(FishingMobSpawnConfigFile.MOB_POOL)) {
            logger.warning("No mob-pool section found in fishing mob spawn configuration.");
            return Collections.emptyList();
        }

        var poolSection = config.getSection(FishingMobSpawnConfigFile.MOB_POOL);
        if (poolSection == null) {
            logger.warning("Mob pool section is empty in fishing mob spawn configuration.");
            return Collections.emptyList();
        }

        List<MobPoolEntry> entries = new ArrayList<>();

        for (Object keyObj : poolSection.getKeys()) {
            String key = keyObj.toString();
            var entrySection = poolSection.getSection(key);
            if (entrySection == null) {
                logger.warning("Mob pool entry '" + key + "' is not a valid section, skipping.");
                continue;
            }

            String mobId = entrySection.getString("mythicmobs-mob-id");
            if (mobId == null || mobId.isBlank()) {
                logger.warning("Mob pool entry '" + key + "' missing 'mythicmobs-mob-id', skipping.");
                continue;
            }

            int weight = entrySection.getInt("weight", 1);
            if (weight <= 0) {
                logger.warning("Mob pool entry '" + key + "' has weight <= 0, skipping.");
                continue;
            }

            double minThreshold = entrySection.getDouble("min-chance-threshold", 0.0);
            double mobLevel = entrySection.getDouble("mob-level", 1.0);

            Set<String> allowedBiomes = toStringSet(entrySection.getStringList("allowed-biomes"));
            Set<String> deniedBiomes = toStringSet(entrySection.getStringList("denied-biomes"));
            Set<String> allowedWorlds = toStringSet(entrySection.getStringList("allowed-worlds"));
            Set<String> deniedWorlds = toStringSet(entrySection.getStringList("denied-worlds"));
            Set<String> allowedRegions = toStringSet(entrySection.getStringList("allowed-regions"));
            Set<String> deniedRegions = toStringSet(entrySection.getStringList("denied-regions"));

            entries.add(new MobPoolEntry(key, mobId, weight, minThreshold, mobLevel,
                    allowedBiomes, deniedBiomes, allowedWorlds, deniedWorlds,
                    allowedRegions, deniedRegions));

            logger.info("Loaded mob pool entry: " + key + " -> " + mobId
                        + " (weight=" + weight + ", threshold=" + minThreshold + ")");
        }

        return Collections.unmodifiableList(entries);
    }

    @NotNull
    private static Set<String> toStringSet(@Nullable List<String> list) {
        if (list == null || list.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(list);
    }
}
