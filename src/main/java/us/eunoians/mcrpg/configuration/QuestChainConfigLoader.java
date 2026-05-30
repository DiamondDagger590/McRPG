package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.chain.QuestChainRepeatMode;
import us.eunoians.mcrpg.quest.chain.QuestChainStep;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Loads quest chain definitions from YAML files in a directory.
 * <p>
 * Files that carry the {@code quest-chain-file: true} marker at their root are parsed as chain
 * files. All other files are silently skipped (they are handled by {@link QuestConfigLoader}).
 * <p>
 * Each chain entry under the {@code chains:} section is parsed into a {@link QuestChainDefinition}
 * using the chain's YAML key as its {@link NamespacedKey}. Duplicate chain keys across files are
 * skipped with a {@code WARNING}; the first-loaded definition wins (matching quest behavior).
 * <p>
 * Soft validation is applied to source keys, trigger keys, and step quest keys — unrecognized
 * keys log a {@code WARNING} but the chain is still loaded. This allows chains to reference
 * content registered later via content expansions.
 */
public class QuestChainConfigLoader {

    /**
     * Loads chain definitions from an explicit list of file paths. Each path must point to a
     * YAML file carrying {@code quest-chain-file: true}; files without the marker are skipped.
     * <p>
     * This overload is used for two-phase loading: {@link QuestConfigLoader} collects chain file
     * paths during its directory walk, and this method processes only those flagged files after
     * all quest definitions have been registered in the registry.
     *
     * @param chainFilePaths paths to YAML files identified as chain files
     * @return ordered map of chain key to parsed chain definition
     */
    @NotNull
    public Map<NamespacedKey, QuestChainDefinition> loadChainsFromPaths(@NotNull List<Path> chainFilePaths) {
        Map<NamespacedKey, QuestChainDefinition> definitions = new LinkedHashMap<>();
        for (Path path : chainFilePaths) {
            loadChainsFromFile(path.toFile(), definitions);
        }
        McRPG.getInstance().getLogger().info("[QuestChainConfigLoader] Loaded " + definitions.size()
                + " quest chain definition(s) from " + chainFilePaths.size() + " chain file(s)");
        return definitions;
    }

    /**
     * Loads all chain definitions from the given directory recursively. Files without the
     * {@code quest-chain-file: true} marker are silently skipped.
     *
     * @param questsDirectory the root directory to scan for chain files
     * @return ordered map of chain key to parsed chain definition
     */
    @NotNull
    public Map<NamespacedKey, QuestChainDefinition> loadChainsFromDirectory(@NotNull File questsDirectory) {
        Logger logger = McRPG.getInstance().getLogger();
        Map<NamespacedKey, QuestChainDefinition> definitions = new LinkedHashMap<>();

        if (!questsDirectory.exists() || !questsDirectory.isDirectory()) {
            logger.warning("[QuestChainConfigLoader] Quests directory does not exist: " + questsDirectory.getAbsolutePath());
            return definitions;
        }

        try (Stream<Path> paths = Files.walk(questsDirectory.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".yml") || name.endsWith(".yaml");
                    })
                    .sorted()
                    .forEach(path -> loadChainsFromFile(path.toFile(), definitions));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "[QuestChainConfigLoader] Failed to walk quests directory: " + questsDirectory.getAbsolutePath(), e);
        }

        logger.info("[QuestChainConfigLoader] Loaded " + definitions.size() + " quest chain definition(s) from " + questsDirectory.getName() + "/");
        return definitions;
    }

    /**
     * Parses all chain definitions from a single YAML file. Silently skips the file if it
     * does not have {@code quest-chain-file: true} at the root.
     *
     * @param file        the YAML file to parse
     * @param definitions the accumulator map; duplicate keys are skipped with a warning
     */
    private void loadChainsFromFile(@NotNull File file,
                                    @NotNull Map<NamespacedKey, QuestChainDefinition> definitions) {
        Logger logger = McRPG.getInstance().getLogger();
        YamlDocument yaml;
        try {
            yaml = YamlDocument.create(file);
        } catch (IOException e) {
            logger.log(Level.WARNING, "[QuestChainConfigLoader] Failed to load YAML file " + file.getName(), e);
            return;
        }

        // Chain files must carry the marker; files without it belong to QuestConfigLoader
        if (!yaml.getBoolean("quest-chain-file", false)) {
            return;
        }

        Section chainsSection = yaml.getSection("chains");
        if (chainsSection == null) {
            logger.warning("[QuestChainConfigLoader] Chain file '" + file.getName() +
                    "' has quest-chain-file: true but no 'chains' section — skipping file");
            return;
        }

        for (String chainKeyString : chainsSection.getRoutesAsStrings(false)) {
            Optional<NamespacedKey> chainKeyOpt = parseNamespacedKey(chainKeyString);
            if (chainKeyOpt.isEmpty()) {
                logger.warning("[QuestChainConfigLoader] Invalid chain key '" + chainKeyString +
                        "' in " + file.getName() + " — skipping entry");
                continue;
            }
            NamespacedKey chainKey = chainKeyOpt.get();

            if (definitions.containsKey(chainKey)) {
                logger.warning("[QuestChainConfigLoader] Duplicate chain key '" + chainKey +
                        "' in " + file.getName() + " — skipping (first-loaded wins)");
                continue;
            }

            Section chainSection = chainsSection.getSection(chainKeyString);
            if (chainSection == null) {
                logger.warning("[QuestChainConfigLoader] Chain '" + chainKeyString +
                        "' in " + file.getName() + " has no configuration — skipping");
                continue;
            }

            try {
                QuestChainDefinition definition = parseChainDefinition(chainKey, chainSection, file.getName());
                definitions.put(chainKey, definition);
            } catch (Exception e) {
                logger.log(Level.WARNING, "[QuestChainConfigLoader] Failed to parse chain '" + chainKeyString +
                        "' in " + file.getName(), e);
            }
        }
    }

    /**
     * Parses a single chain definition from a BoostedYaml section.
     *
     * @param chainKey    the parsed namespaced key for the chain
     * @param section     the BoostedYaml section containing the chain's configuration
     * @param fileName    the source file name (for log messages)
     * @return the parsed chain definition
     * @throws IllegalArgumentException if required configuration is missing or invalid
     */
    @NotNull
    private QuestChainDefinition parseChainDefinition(@NotNull NamespacedKey chainKey,
                                                       @NotNull Section section,
                                                       @NotNull String fileName) {
        Logger logger = McRPG.getInstance().getLogger();

        String displayName = section.getString("display-name");

        // Source key (soft validation — sources may register later via content expansion)
        String sourceString = section.getString("source");
        if (sourceString == null || sourceString.isBlank()) {
            throw new IllegalArgumentException("missing required field 'source'");
        }
        NamespacedKey sourceKey = parseNamespacedKey(sourceString)
                .orElseThrow(() -> new IllegalArgumentException("invalid source key: '" + sourceString + "'"));
        var questSourceRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_SOURCE);
        if (questSourceRegistry != null && questSourceRegistry.get(sourceKey).isEmpty()) {
            logger.warning("[QuestChainConfigLoader] Chain '" + chainKey + "' in " + fileName +
                    " references unknown source '" + sourceKey + "' — source may register later via content expansion");
        }

        // Auto-start trigger (soft validation)
        Section autoStartSection = section.getSection("auto-start");
        if (autoStartSection == null || !autoStartSection.contains("trigger")) {
            throw new IllegalArgumentException("missing required field 'auto-start.trigger'");
        }
        String triggerString = autoStartSection.getString("trigger");
        NamespacedKey triggerKey = parseNamespacedKey(triggerString)
                .orElseThrow(() -> new IllegalArgumentException("invalid trigger key: '" + triggerString + "'"));

        // Repeat mode (defaults to ONCE)
        QuestChainRepeatMode repeatMode = QuestChainRepeatMode.ONCE;
        if (section.contains("repeat-mode")) {
            String repeatModeString = section.getString("repeat-mode");
            Optional<QuestChainRepeatMode> repeatModeOpt = QuestChainRepeatMode.fromString(repeatModeString);
            if (repeatModeOpt.isEmpty()) {
                logger.warning("[QuestChainConfigLoader] Chain '" + chainKey + "' in " + fileName +
                        " has invalid repeat-mode '" + repeatModeString + "' — defaulting to ONCE");
            } else {
                repeatMode = repeatModeOpt.get();
            }
        }

        // Steps (required)
        Section stepsSection = section.getSection("steps");
        if (stepsSection == null) {
            throw new IllegalArgumentException("missing required field 'steps'");
        }
        Set<String> stepKeys = stepsSection.getRoutesAsStrings(false);
        if (stepKeys.isEmpty()) {
            throw new IllegalArgumentException("'steps' section is empty — chains must have at least one step");
        }

        List<QuestChainStep> steps = new ArrayList<>();
        String defaultExpireBehavior = "fail-chain";
        for (String stepKey : stepKeys) {
            Section stepSection = stepsSection.getSection(stepKey);
            if (stepSection == null) {
                logger.warning("[QuestChainConfigLoader] Chain '" + chainKey + "' step '" + stepKey +
                        "' in " + fileName + " has no configuration — skipping step");
                continue;
            }

            String questString = stepSection.getString("quest");
            if (questString == null || questString.isBlank()) {
                logger.warning("[QuestChainConfigLoader] Chain '" + chainKey + "' step '" + stepKey +
                        "' in " + fileName + " is missing 'quest' — skipping step");
                continue;
            }
            NamespacedKey questKey = parseNamespacedKey(questString).orElse(null);
            if (questKey == null) {
                logger.warning("[QuestChainConfigLoader] Chain '" + chainKey + "' step '" + stepKey +
                        "' in " + fileName + " has invalid quest key '" + questString + "' — skipping step");
                continue;
            }

            // Soft validation — quest may not be loaded yet
            var questDefinitionRegistry = RegistryAccess.registryAccess().registry(McRPGRegistryKey.QUEST_DEFINITION);
            if (questDefinitionRegistry != null && questDefinitionRegistry.get(questKey).isEmpty()) {
                logger.warning("[QuestChainConfigLoader] Chain '" + chainKey + "' step '" + stepKey +
                        "' references unknown quest '" + questKey + "' — quest may register later via content expansion");
            }

            String onQuestExpire = defaultExpireBehavior;
            if (stepSection.contains("on-quest-expire")) {
                String expireValue = stepSection.getString("on-quest-expire");
                if (expireValue != null && !expireValue.isBlank()) {
                    if (!expireValue.equals("fail-chain")) {
                        logger.warning("[QuestChainConfigLoader] Chain '" + chainKey + "' step '" + stepKey +
                                "' has unsupported on-quest-expire value '" + expireValue +
                                "' — value stored for forward compatibility");
                    }
                    onQuestExpire = expireValue;
                }
            }

            steps.add(new QuestChainStep(questKey, List.of(), onQuestExpire, -1));
        }

        if (steps.isEmpty()) {
            throw new IllegalArgumentException("all steps were invalid or skipped — chains must have at least one valid step");
        }

        var builder = new QuestChainDefinition.Builder(chainKey, sourceKey, triggerKey, steps)
                .repeatMode(repeatMode);

        if (displayName != null && !displayName.isBlank()) {
            builder.displayName(displayName);
        }

        return builder.build();
    }

    /**
     * Parses a {@link NamespacedKey} from a string in {@code namespace:value} format.
     * Returns empty if the string is null, blank, or not in a valid format.
     *
     * @param keyString the string to parse
     * @return the parsed key, or empty if invalid
     */
    @NotNull
    private Optional<NamespacedKey> parseNamespacedKey(@NotNull String keyString) {
        if (keyString.isBlank()) {
            return Optional.empty();
        }
        String[] parts = keyString.split(":", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new NamespacedKey(parts[0], parts[1]));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
