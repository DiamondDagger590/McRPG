package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.quest.definition.PhaseCompletionMode;
import us.eunoians.mcrpg.quest.board.BoardMetadata;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarityRegistry;
import us.eunoians.mcrpg.quest.definition.OnStartMessage;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionMetadata;
import us.eunoians.mcrpg.quest.definition.QuestObjectiveDefinition;
import us.eunoians.mcrpg.quest.definition.QuestPhaseDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.definition.QuestStageDefinition;
import us.eunoians.mcrpg.quest.board.distribution.DistributionRewardEntry;
import us.eunoians.mcrpg.quest.board.distribution.DistributionTierConfig;
import us.eunoians.mcrpg.quest.board.distribution.PotBehavior;
import us.eunoians.mcrpg.quest.board.distribution.RemainderStrategy;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;
import us.eunoians.mcrpg.quest.board.distribution.RewardSplitMode;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionParser;
import us.eunoians.mcrpg.quest.board.template.condition.RewardFallback;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateCondition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Loads quest definitions from YAML files in a directory.
 * <p>
 * Recursively scans all {@code .yml} and {@code .yaml} files within the provided directory,
 * parsing each file's {@code quests} section into {@link QuestDefinition} instances. Quest
 * identity comes from the YAML key, not the file name -- server owners can organize files
 * however they like.
 * <p>
 * The YAML format uses named map keys for phases, stages, objectives, and rewards rather than
 * YAML list syntax. Map keys are organizational labels; identity comes from {@code key:} fields
 * on stages and objectives.
 * <p>
 * Duplicate {@link NamespacedKey}s across files are logged as warnings; the first-loaded
 * definition wins.
 * <p>
 * Uses BoostedYaml ({@link YamlDocument}, {@link Section}) for all YAML parsing, consistent
 * with the rest of the McRPG codebase.
 */
public class QuestConfigLoader {

    private static final Pattern DURATION_PATTERN = Pattern.compile(
            "(?:(\\d+)d)?\\s*(?:(\\d+)h)?\\s*(?:(\\d+)m)?\\s*(?:(\\d+)s)?",
            Pattern.CASE_INSENSITIVE
    );

    private final ConditionParser conditionParser;

    public QuestConfigLoader(@NotNull ConditionParser conditionParser) {
        this.conditionParser = conditionParser;
    }

    /**
     * Recursively scans the given directory for {@code .yml}/{@code .yaml} files, parses
     * all quest definitions found within them, and collects paths of any files that are
     * flagged as chain files via {@code quest-chain-file: true}.
     * <p>
     * Chain files are silently skipped by this loader — their paths are returned in
     * {@link QuestLoadResult#chainFiles()} so the caller can pass them to
     * {@link QuestChainConfigLoader} in a second phase, after all quest definitions are
     * registered.
     *
     * @param questsDirectory the root directory to scan (e.g. {@code plugins/McRPG/quests/})
     * @return the parsed quest definitions and the paths of any flagged chain files
     */
    @NotNull
    public QuestLoadResult loadQuestsFromDirectory(@NotNull File questsDirectory) {
        Logger logger = McRPG.getInstance().getLogger();
        Map<NamespacedKey, QuestDefinition> definitions = new LinkedHashMap<>();
        List<Path> chainFiles = new ArrayList<>();

        if (!questsDirectory.exists() || !questsDirectory.isDirectory()) {
            logger.warning("Quests directory does not exist: " + questsDirectory.getAbsolutePath());
            return new QuestLoadResult(definitions, chainFiles);
        }

        try (Stream<Path> paths = Files.walk(questsDirectory.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".yml") || name.endsWith(".yaml");
                    })
                    .sorted()
                    .forEach(path -> loadQuestsFromFile(path.toFile(), definitions, chainFiles));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to walk quests directory: " + questsDirectory.getAbsolutePath(), e);
        }

        logger.info("Loaded " + definitions.size() + " quest definition(s) from " + questsDirectory.getName() + "/");
        return new QuestLoadResult(definitions, chainFiles);
    }

    /**
     * Parses all quest definitions from a single YAML file and adds them to the provided map.
     * If the file carries the {@code quest-chain-file: true} marker it is added to
     * {@code chainFilePaths} and skipped — chain files are handled by {@link QuestChainConfigLoader}.
     *
     * @param file           the YAML file to parse
     * @param definitions    the accumulator map; duplicate keys are skipped with a warning
     * @param chainFilePaths the accumulator list for chain file paths
     */
    private void loadQuestsFromFile(@NotNull File file,
                                    @NotNull Map<NamespacedKey, QuestDefinition> definitions,
                                    @NotNull List<Path> chainFilePaths) {
        Logger logger = McRPG.getInstance().getLogger();
        YamlDocument yaml;
        try {
            yaml = YamlDocument.create(file);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load YAML file " + file.getName(), e);
            return;
        }

        if (yaml.getBoolean("quest-chain-file", false)) {
            chainFilePaths.add(file.toPath());
            return;
        }

        Section questsSection = yaml.getSection("quests");
        if (questsSection == null) {
            logger.warning("No 'quests' section found in " + file.getName() + ", skipping file");
            return;
        }

        for (String questKeyString : questsSection.getRoutesAsStrings(false)) {
            Optional<NamespacedKey> questKeyOpt = parseNamespacedKey(questKeyString);
            if (questKeyOpt.isEmpty()) {
                logger.warning("Invalid quest key '" + questKeyString + "' in " + file.getName() + ", skipping");
                continue;
            }
            NamespacedKey questKey = questKeyOpt.get();

            if (definitions.containsKey(questKey)) {
                logger.warning("Duplicate quest key '" + questKey + "' in " + file.getName()
                        + ", skipping (first-loaded wins)");
                continue;
            }

            Section questSection = questsSection.getSection(questKeyString);
            if (questSection == null) {
                logger.warning("Quest '" + questKeyString + "' in " + file.getName()
                        + " has no configuration, skipping");
                continue;
            }

            try {
                QuestDefinition definition = parseQuestDefinition(questKey, questSection, file.getName());
                definitions.put(questKey, definition);
            } catch (Exception e) {
                logger.warning("Failed to parse quest '" + questKeyString + "' in " + file.getName()
                        + ": " + e.getMessage());
            }
        }
    }

    /**
     * Parses a single quest definition from a BoostedYaml section.
     *
     * @param questKey the parsed namespaced key for the quest
     * @param section  the BoostedYaml section containing the quest's configuration
     * @param fileName the source file name (for log messages)
     * @return the parsed quest definition
     * @throws IllegalArgumentException if the configuration is invalid
     */
    @NotNull
    private QuestDefinition parseQuestDefinition(@NotNull NamespacedKey questKey,
                                                 @NotNull Section section,
                                                 @NotNull String fileName) {
        String scopeString = section.getString("scope", "mcrpg:single_player");
        NamespacedKey scopeType = parseNamespacedKey(scopeString)
                .orElseThrow(() -> new IllegalArgumentException("Invalid scope type: " + scopeString));

        Duration expiration = null;
        if (section.contains("expiration")) {
            expiration = parseDuration(section.getString("expiration"));
        }

        QuestRepeatMode repeatMode = QuestRepeatMode.ONCE;
        if (section.contains("repeat-mode")) {
            try {
                repeatMode = QuestRepeatMode.valueOf(section.getString("repeat-mode").toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid repeat-mode '" + section.getString("repeat-mode")
                        + "' in quest " + questKey + ". Valid values: ONCE, REPEATABLE, COOLDOWN, LIMITED, COOLDOWN_LIMITED");
            }
        }

        Duration repeatCooldown = null;
        if (section.contains("repeat-cooldown")) {
            repeatCooldown = parseDuration(section.getString("repeat-cooldown"));
        }

        int repeatLimit = -1;
        if (section.contains("repeat-limit")) {
            repeatLimit = section.getInt("repeat-limit", -1);
        }

        if (repeatMode == QuestRepeatMode.COOLDOWN_LIMITED) {
            Logger logger = McRPG.getInstance().getLogger();
            if (repeatCooldown == null) {
                logger.warning("Quest " + questKey + " uses COOLDOWN_LIMITED but is missing 'repeat-cooldown'."
                        + " Cooldown will default to zero (no delay enforced).");
            }
            if (repeatLimit <= 0) {
                logger.warning("Quest " + questKey + " uses COOLDOWN_LIMITED but is missing a valid 'repeat-limit'."
                        + " Limit will default to 1.");
            }
        }

        NamespacedKey expansionKey = section.contains("expansion")
                ? parseNamespacedKey(section.getString("expansion")).orElse(null)
                : null;

        String questLocalizationPrefix = "quests." + questKey.getNamespace() + "." + questKey.getKey();
        Map<String, String> inlineDisplay = parseInlineDisplay(section);
        List<QuestRewardType> rewards = parseRewards(section, fileName, questKey.toString(), questLocalizationPrefix, inlineDisplay);

        Section phasesSection = section.getSection("phases");
        if (phasesSection == null) {
            throw new IllegalArgumentException("Quest must have at least one phase");
        }

        var phaseKeys = phasesSection.getRoutesAsStrings(false);
        if (phaseKeys.isEmpty()) {
            throw new IllegalArgumentException("Quest must have at least one phase");
        }

        List<QuestPhaseDefinition> phases = new ArrayList<>(phaseKeys.size());
        int phaseIndex = 0;
        for (String phaseLabel : phaseKeys) {
            Section phaseSection = phasesSection.getSection(phaseLabel);
            if (phaseSection == null) {
                throw new IllegalArgumentException("Phase '" + phaseLabel + "' in quest " + questKey
                        + " has no configuration");
            }
            phases.add(parsePhaseDefinition(phaseIndex, phaseSection, fileName, questKey));
            phaseIndex++;
        }

        Map<NamespacedKey, QuestDefinitionMetadata> metadata = parseBoardMetadata(section);
        List<OnStartMessage> onStartMessages = parseOnStartMessages(section);

        return new QuestDefinition.Builder(questKey, scopeType, phases)
                .rewards(rewards)
                .expiration(expiration)
                .repeatMode(repeatMode)
                .repeatCooldown(repeatCooldown)
                .repeatLimit(repeatLimit)
                .expansionKey(expansionKey)
                .metadata(metadata.isEmpty() ? null : metadata)
                .rewardDistribution(parseRewardDistribution(section, fileName, questKey.toString(), conditionParser).orElse(null))
                .inlineDisplay(inlineDisplay.isEmpty() ? null : inlineDisplay)
                .onStartMessages(onStartMessages)
                .build();
    }

    /**
     * Parses the optional {@code board-metadata} section from a quest definition.
     *
     * @param section the quest definition section
     * @return metadata map (empty if no board-metadata present)
     */
    @NotNull
    private Map<NamespacedKey, QuestDefinitionMetadata> parseBoardMetadata(@NotNull Section section) {
        Map<NamespacedKey, QuestDefinitionMetadata> metadata = new HashMap<>();
        if (!section.contains("board-metadata")) {
            return metadata;
        }

        Section boardSection = section.getSection("board-metadata");
        if (boardSection == null) {
            return metadata;
        }

        boolean boardEligible = boardSection.getBoolean("board-eligible", true);

        Set<NamespacedKey> supportedRarities = new LinkedHashSet<>();
        if (boardSection.contains("supported-rarities")) {
            for (String rawRarity : boardSection.getStringList("supported-rarities")) {
                NamespacedKey key = McRPGMethods.parseNamespacedKey(rawRarity);
                supportedRarities.add(key);
            }
        } else {
            QuestRarityRegistry rarityRegistry = RegistryAccess.registryAccess()
                    .registry(McRPGRegistryKey.QUEST_RARITY);
            supportedRarities.addAll(rarityRegistry.getRegisteredKeys());
        }

        Duration acceptanceCooldown = null;
        if (boardSection.contains("acceptance-cooldown")) {
            acceptanceCooldown = parseDuration(boardSection.getString("acceptance-cooldown"));
        }

        String cooldownScope = null;
        if (boardSection.contains("cooldown-scope")) {
            cooldownScope = boardSection.getString("cooldown-scope").toUpperCase();
        }

        Set<String> supportedRefreshTypes = new LinkedHashSet<>();
        if (boardSection.contains("supported-refresh-types")) {
            for (String rt : boardSection.getStringList("supported-refresh-types")) {
                supportedRefreshTypes.add(rt.toUpperCase());
            }
        }

        BoardMetadata boardMetadata = new BoardMetadata(boardEligible, Set.copyOf(supportedRarities),
                Set.copyOf(supportedRefreshTypes), acceptanceCooldown, cooldownScope);
        metadata.put(BoardMetadata.METADATA_KEY, boardMetadata);
        return metadata;
    }

    /**
     * Parses the optional {@code display} section from a quest definition into a flat
     * string map used as inline fallback display strings.
     * <p>
     * Supported keys: {@code name}, {@code description}, and per-objective descriptions
     * under {@code objectives.<key>}. Reward display labels are resolved via auto-derived
     * localization routes rather than an inline {@code rewards} block, so that section
     * is intentionally not parsed here.
     *
     * @param section the quest definition section
     * @return a map of display keys to values, empty if no display section present
     */
    @NotNull
    private Map<String, String> parseInlineDisplay(@NotNull Section section) {
        Map<String, String> display = new LinkedHashMap<>();
        if (!section.contains("display")) {
            return display;
        }
        Section displaySection = section.getSection("display");
        if (displaySection == null) {
            return display;
        }
        if (displaySection.contains("name")) {
            display.put("name", displaySection.getString("name"));
        }
        if (displaySection.contains("description")) {
            display.put("description", displaySection.getString("description"));
        }
        if (displaySection.contains("objectives")) {
            Section objSection = displaySection.getSection("objectives");
            if (objSection != null) {
                for (String objKey : objSection.getRoutesAsStrings(false)) {
                    display.put("objective." + objKey, objSection.getString(objKey));
                }
            }
        }
        if (displaySection.contains("rewards")) {
            Section rewardDisplaySection = displaySection.getSection("rewards");
            if (rewardDisplaySection != null) {
                for (String rewardKey : rewardDisplaySection.getRoutesAsStrings(false)) {
                    display.put("reward." + rewardKey, rewardDisplaySection.getString(rewardKey));
                }
            }
        }
        return display;
    }

    /**
     * Parses the optional {@code on-start-messages} section from a quest definition.
     * Each child entry is either locale-backed ({@code key:}) or inline ({@code messages:}).
     *
     * @param section the quest definition section
     * @return the parsed on-start messages, empty if the section is absent
     */
    @NotNull
    private List<OnStartMessage> parseOnStartMessages(@NotNull Section section) {
        if (!section.contains("on-start-messages")) {
            return List.of();
        }
        Section messagesSection = section.getSection("on-start-messages");
        if (messagesSection == null) {
            return List.of();
        }
        List<OnStartMessage> messages = new ArrayList<>();
        for (String entryLabel : messagesSection.getRoutesAsStrings(false)) {
            Section entrySection = messagesSection.getSection(entryLabel);
            if (entrySection == null) {
                continue;
            }
            if (entrySection.contains("key")) {
                String keyValue = entrySection.getString("key");
                if (keyValue == null || keyValue.isBlank()) {
                    McRPG.getInstance().getLogger().warning(
                            "on-start-messages entry '" + entryLabel + "' has a blank 'key' — skipping");
                    continue;
                }
                messages.add(OnStartMessage.fromLocaleKey(keyValue));
            } else if (entrySection.contains("messages")) {
                List<String> inlineMessages = entrySection.getStringList("messages");
                if (inlineMessages.isEmpty()) {
                    McRPG.getInstance().getLogger().warning(
                            "on-start-messages entry '" + entryLabel + "' has an empty 'messages' list — skipping");
                    continue;
                }
                messages.add(OnStartMessage.fromInline(inlineMessages));
            } else {
                McRPG.getInstance().getLogger().warning(
                        "on-start-messages entry '" + entryLabel + "' has no 'key' or 'messages' — skipping");
            }
        }
        return messages;
    }

    /**
     * Parses a phase definition from a BoostedYaml section.
     *
     * @param phaseIndex   the zero-based index of the phase within its parent quest
     * @param phaseSection the section for this phase
     * @param fileName     the source file name (for log messages)
     * @param questKey     the parent quest's namespaced key
     * @return the parsed phase definition
     */
    @NotNull
    private QuestPhaseDefinition parsePhaseDefinition(int phaseIndex,
                                                      @NotNull Section phaseSection,
                                                      @NotNull String fileName,
                                                      @NotNull NamespacedKey questKey) {
        String modeString = phaseSection.getString("completion-mode", "ALL");
        PhaseCompletionMode mode;
        try {
            mode = PhaseCompletionMode.valueOf(modeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid completion-mode '" + modeString
                    + "' in phase " + phaseIndex + " of quest " + questKey);
        }

        Section stagesSection = phaseSection.getSection("stages");
        if (stagesSection == null) {
            throw new IllegalArgumentException("Phase " + phaseIndex + " of quest " + questKey
                    + " must have at least one stage");
        }

        var stageKeys = stagesSection.getRoutesAsStrings(false);
        if (stageKeys.isEmpty()) {
            throw new IllegalArgumentException("Phase " + phaseIndex + " of quest " + questKey
                    + " must have at least one stage");
        }

        List<QuestStageDefinition> stages = new ArrayList<>(stageKeys.size());
        for (String stageLabel : stageKeys) {
            Section stageSection = stagesSection.getSection(stageLabel);
            if (stageSection == null) {
                throw new IllegalArgumentException("Stage '" + stageLabel + "' in phase " + phaseIndex
                        + " of quest " + questKey + " has no configuration");
            }
            stages.add(parseStageDefinition(stageSection, fileName, questKey));
        }

        String phaseLocalizationPrefix = "quests." + questKey.getNamespace() + "." + questKey.getKey()
                + ".phases.phase-" + phaseIndex;
        List<QuestRewardType> rewards = parseRewards(phaseSection, fileName,
                questKey + "/phase-" + phaseIndex, phaseLocalizationPrefix, null);

        return new QuestPhaseDefinition(phaseIndex, mode, stages, rewards,
                parseRewardDistribution(phaseSection, fileName, questKey + "/phase-" + phaseIndex, conditionParser).orElse(null));
    }

    /**
     * Parses a stage definition from a BoostedYaml section.
     *
     * @param stageSection the section for this stage
     * @param fileName     the source file name (for log messages)
     * @param questKey     the parent quest's namespaced key
     * @return the parsed stage definition
     */
    @NotNull
    private QuestStageDefinition parseStageDefinition(@NotNull Section stageSection,
                                                      @NotNull String fileName,
                                                      @NotNull NamespacedKey questKey) {
        String stageKeyString = stageSection.getString("key");
        if (stageKeyString == null || stageKeyString.isEmpty()) {
            throw new IllegalArgumentException("Stage in quest " + questKey + " is missing a 'key'");
        }

        NamespacedKey stageKey = parseNamespacedKey(stageKeyString)
                .orElseThrow(() -> new IllegalArgumentException("Invalid stage key '" + stageKeyString
                    + "' in quest " + questKey));

        String stageLocalizationPrefix = "quests." + questKey.getNamespace() + "." + questKey.getKey()
                + ".stages." + stageKey.getKey();
        List<QuestRewardType> rewards = parseRewards(stageSection, fileName, questKey + "/" + stageKey,
                stageLocalizationPrefix, null);

        Section objectivesSection = stageSection.getSection("objectives");
        if (objectivesSection == null) {
            throw new IllegalArgumentException("Stage '" + stageKey + "' in quest " + questKey
                    + " must have at least one objective");
        }

        var objectiveKeys = objectivesSection.getRoutesAsStrings(false);
        if (objectiveKeys.isEmpty()) {
            throw new IllegalArgumentException("Stage '" + stageKey + "' in quest " + questKey
                    + " must have at least one objective");
        }

        List<QuestObjectiveDefinition> objectives = new ArrayList<>(objectiveKeys.size());
        for (String objectiveLabel : objectiveKeys) {
            Section objectiveSection = objectivesSection.getSection(objectiveLabel);
            if (objectiveSection == null) {
                throw new IllegalArgumentException("Objective '" + objectiveLabel + "' in stage '" + stageKey
                        + "' of quest " + questKey + " has no configuration");
            }
            objectives.add(parseObjectiveDefinition(objectiveSection, fileName, questKey));
        }

        return new QuestStageDefinition(stageKey, objectives, rewards,
                parseRewardDistribution(stageSection, fileName, questKey + "/" + stageKey, conditionParser).orElse(null));
    }

    /**
     * Parses an objective definition from a BoostedYaml section.
     *
     * @param objectiveSection the section for this objective
     * @param fileName         the source file name (for log messages)
     * @param questKey         the parent quest's namespaced key
     * @return the parsed objective definition
     */
    @NotNull
    private QuestObjectiveDefinition parseObjectiveDefinition(@NotNull Section objectiveSection,
                                                              @NotNull String fileName,
                                                              @NotNull NamespacedKey questKey) {
        QuestObjectiveTypeRegistry objectiveTypeRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);

        String objectiveKeyString = objectiveSection.getString("key");
        if (objectiveKeyString == null || objectiveKeyString.isEmpty()) {
            throw new IllegalArgumentException("Objective in quest " + questKey + " is missing a 'key'");
        }

        NamespacedKey objectiveKey = parseNamespacedKey(objectiveKeyString)
                .orElseThrow(() -> new IllegalArgumentException("Invalid objective key '" + objectiveKeyString
                    + "' in quest " + questKey));

        String typeKeyString = objectiveSection.getString("type");
        if (typeKeyString == null || typeKeyString.isEmpty()) {
            throw new IllegalArgumentException("Objective '" + objectiveKey + "' in quest " + questKey
                    + " is missing a 'type'");
        }

        NamespacedKey typeKey = parseNamespacedKey(typeKeyString)
                .orElseThrow(() -> new IllegalArgumentException("Invalid objective type key '" + typeKeyString
                    + "' in quest " + questKey));

        Optional<QuestObjectiveType> baseType = objectiveTypeRegistry.get(typeKey);
        if (baseType.isEmpty()) {
            throw new IllegalArgumentException("Unknown objective type '" + typeKey
                    + "' for objective '" + objectiveKey + "' in quest " + questKey
                    + ". Is the type registered?");
        }

        Object requiredProgressObj = objectiveSection.get("required-progress");
        Long requiredProgress = null;
        String requiredProgressExpression = null;
        if (requiredProgressObj instanceof Number n) {
            requiredProgress = n.longValue();
        } else if (requiredProgressObj instanceof String s) {
            if (!s.isBlank()) {
                requiredProgressExpression = s;
            }
        }

        if (requiredProgress != null && requiredProgress <= 0) {
            throw new IllegalArgumentException("Objective '" + objectiveKey + "' in quest " + questKey
                    + " must have a positive required-progress, was: " + requiredProgress);
        }
        if (requiredProgress == null && requiredProgressExpression == null) {
            throw new IllegalArgumentException("Objective '" + objectiveKey + "' in quest " + questKey
                    + " is missing a 'required-progress' value (number or expression string)");
        }

        QuestObjectiveType configuredType = baseType.get();
        Section configSection = objectiveSection.getSection("config");
        if (configSection != null) {
            configuredType = configuredType.parseConfig(configSection);
        }

        String objectiveLocalizationPrefix = "quests." + questKey.getNamespace() + "." + questKey.getKey()
                + ".objectives." + objectiveKey.getKey();
        List<QuestRewardType> rewards = parseRewards(objectiveSection, fileName, questKey + "/" + objectiveKey,
                objectiveLocalizationPrefix, null);
        RewardDistributionConfig rewardDistribution = parseRewardDistribution(objectiveSection, fileName,
                questKey + "/" + objectiveKey, conditionParser).orElse(null);

        if (requiredProgress != null) {
            return new QuestObjectiveDefinition(objectiveKey, configuredType, requiredProgress, rewards, rewardDistribution);
        }
        return new QuestObjectiveDefinition(objectiveKey, configuredType, requiredProgressExpression, rewards, rewardDistribution);
    }

    /**
     * Parses rewards from a BoostedYaml {@link Section} containing a {@code rewards} subsection.
     * Each reward is a named subsection within {@code rewards} (map-based, not list-based).
     * <p>
     * When {@code localizationPrefix} is provided, each parsed reward is given an auto-derived
     * localization route via {@link QuestRewardType#withLocalizationRoute}. The route follows
     * the pattern {@code <prefix>.rewards.<rewardLabel>}, e.g.
     * {@code quests.mcrpg.my_quest.objectives.break_gold.rewards.hero_title}.
     * <p>
     * When {@code inlineDisplay} is provided, each reward's inline display label is sourced from
     * the {@code reward.<rewardLabel>} entry in the map (populated from the quest's
     * {@code display.rewards} block). This keeps all inline display text co-located in the
     * {@code display:} block rather than scattered across reward definitions.
     *
     * @param parentSection       the parent section containing the optional {@code rewards} subsection
     * @param fileName            the source file name (for log messages)
     * @param contextKey          a human-readable context path (for log messages)
     * @param localizationPrefix  the localization route prefix up to (but not including) {@code .rewards.<label>},
     *                            or {@code null} to skip route assignment
     * @param inlineDisplay       the inline display map from the parent quest's {@code display:} block,
     *                            or {@code null} if no inline display context is available
     * @return the list of configured reward type instances (may be empty)
     */
    @NotNull
    static List<QuestRewardType> parseRewards(@NotNull Section parentSection,
                                              @NotNull String fileName,
                                              @NotNull String contextKey,
                                              @Nullable String localizationPrefix,
                                              @Nullable Map<String, String> inlineDisplay) {
        Section rewardsSection = parentSection.getSection("rewards");
        if (rewardsSection == null) {
            return List.of();
        }

        Logger logger = McRPG.getInstance().getLogger();
        QuestRewardTypeRegistry rewardTypeRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_REWARD_TYPE);

        var rewardKeys = rewardsSection.getRoutesAsStrings(false);
        if (rewardKeys.isEmpty()) {
            return List.of();
        }

        List<QuestRewardType> rewards = new ArrayList<>(rewardKeys.size());
        for (String rewardLabel : rewardKeys) {
            Section rewardSection = rewardsSection.getSection(rewardLabel);
            if (rewardSection == null) {
                logger.warning("Reward '" + rewardLabel + "' in " + contextKey
                        + " (" + fileName + ") has no configuration, skipping");
                continue;
            }

            String typeKeyString = rewardSection.getString("type");
            if (typeKeyString == null || typeKeyString.isEmpty()) {
                logger.warning("Reward '" + rewardLabel + "' in " + contextKey
                        + " (" + fileName + ") is missing a 'type', skipping");
                continue;
            }

            Optional<NamespacedKey> typeKeyOpt = parseNamespacedKey(typeKeyString);
            if (typeKeyOpt.isEmpty()) {
                logger.warning("Invalid reward type key '" + typeKeyString + "' in " + contextKey
                        + " (" + fileName + "), skipping");
                continue;
            }

            NamespacedKey typeKey = typeKeyOpt.get();
            Optional<QuestRewardType> baseType = rewardTypeRegistry.get(typeKey);
            if (baseType.isEmpty()) {
                logger.warning("Unknown reward type '" + typeKey + "' in " + contextKey
                        + " (" + fileName + "), skipping. Is the type registered?");
                continue;
            }

            try {
                QuestRewardType configuredReward = baseType.get().parseConfig(rewardSection);
                if (localizationPrefix != null) {
                    configuredReward = configuredReward.withLocalizationRoute(
                            Route.fromString(localizationPrefix + ".rewards." + rewardLabel));
                }
                if (inlineDisplay != null) {
                    String inlineLabel = inlineDisplay.get("reward." + rewardLabel);
                    if (inlineLabel != null && !inlineLabel.isEmpty()) {
                        configuredReward = configuredReward.withInlineDisplayLabel(inlineLabel);
                    }
                }
                rewards.add(configuredReward);
            } catch (Exception e) {
                logger.warning("Failed to parse reward of type '" + typeKey + "' in " + contextKey
                        + " (" + fileName + "): " + e.getMessage());
            }
        }

        return rewards;
    }

    /**
     * Parses an optional {@code reward-distribution} section from a parent YAML section.
     * Each child key within {@code reward-distribution} represents a named tier.
     *
     * @param parentSection the parent section that may contain a {@code reward-distribution} subsection
     * @param fileName      the source file name (for log messages)
     * @param contextKey    a human-readable context path (for log messages)
     * @return an {@link Optional} containing the parsed config, or empty if no section is present
     */
    @NotNull
    static Optional<RewardDistributionConfig> parseRewardDistribution(@NotNull Section parentSection,
                                                                      @NotNull String fileName,
                                                                      @NotNull String contextKey,
                                                                      @NotNull ConditionParser conditionParser) {
        Section distSection = parentSection.getSection("reward-distribution");
        if (distSection == null) {
            return Optional.empty();
        }

        Logger logger = McRPG.getInstance().getLogger();
        var tierKeys = distSection.getRoutesAsStrings(false);
        if (tierKeys.isEmpty()) {
            return Optional.empty();
        }

        List<DistributionTierConfig> tiers = new ArrayList<>(tierKeys.size());
        for (String tierLabel : tierKeys) {
            Section tierSection = distSection.getSection(tierLabel);
            if (tierSection == null) {
                logger.warning("Distribution tier '" + tierLabel + "' in " + contextKey
                        + " (" + fileName + ") has no configuration, skipping");
                continue;
            }

            String typeKeyString = tierSection.getString("type");
            if (typeKeyString == null || typeKeyString.isEmpty()) {
                logger.warning("Distribution tier '" + tierLabel + "' in " + contextKey
                        + " (" + fileName + ") is missing a 'type', skipping");
                continue;
            }

            Optional<NamespacedKey> typeKeyOpt = parseNamespacedKey(typeKeyString);
            if (typeKeyOpt.isEmpty()) {
                logger.warning("Invalid distribution type key '" + typeKeyString + "' in " + contextKey
                        + " (" + fileName + "), skipping");
                continue;
            }

            NamespacedKey typeKey = typeKeyOpt.get();
            RewardSplitMode splitMode = RewardSplitMode.INDIVIDUAL;
            if (tierSection.contains("split-mode")) {
                try {
                    splitMode = RewardSplitMode.valueOf(tierSection.getString("split-mode").toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid split-mode '" + tierSection.getString("split-mode")
                            + "' in distribution tier '" + tierLabel + "' of " + contextKey
                            + " (" + fileName + "), defaulting to INDIVIDUAL");
                }
            }

            List<DistributionRewardEntry> rewardEntries = parseDistributionRewardEntries(
                    tierSection, fileName, contextKey + "/dist:" + tierLabel, conditionParser);

            Map<String, Object> typeParameters = new HashMap<>();
            if (tierSection.contains("top-player-count")) {
                typeParameters.put(DistributionTierConfig.PARAM_TOP_PLAYER_COUNT,
                        tierSection.getInt("top-player-count"));
            }
            if (tierSection.contains("min-contribution-percent")) {
                typeParameters.put(DistributionTierConfig.PARAM_MIN_CONTRIBUTION_PERCENT,
                        tierSection.getDouble("min-contribution-percent"));
            }
            Section paramsSection = tierSection.getSection("type-parameters");
            if (paramsSection != null) {
                for (String paramKey : paramsSection.getRoutesAsStrings(false)) {
                    typeParameters.put(paramKey, paramsSection.get(paramKey));
                }
            }

            NamespacedKey minRarity = tierSection.contains("min-rarity")
                    ? parseNamespacedKey(tierSection.getString("min-rarity")).orElse(null)
                    : null;

            NamespacedKey requiredRarity = tierSection.contains("required-rarity")
                    ? parseNamespacedKey(tierSection.getString("required-rarity")).orElse(null)
                    : null;

            tiers.add(new DistributionTierConfig(tierLabel, typeKey, splitMode, rewardEntries,
                    typeParameters, minRarity, requiredRarity));
        }

        if (tiers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new RewardDistributionConfig(tiers));
    }

    /**
     * Parses reward entries within a distribution tier, extracting per-reward
     * pot-behavior, remainder-strategy, min-scaled-amount, top-count, and fallback fields.
     * Falls back to wrapping plain rewards with default settings for backward compatibility.
     */
    @NotNull
    private static List<DistributionRewardEntry> parseDistributionRewardEntries(
            @NotNull Section tierSection,
            @NotNull String fileName,
            @NotNull String contextKey,
            @NotNull ConditionParser conditionParser) {
        Section rewardsSection = tierSection.getSection("rewards");
        if (rewardsSection == null) {
            return List.of();
        }

        List<DistributionRewardEntry> entries = new ArrayList<>();
        for (String rewardKey : rewardsSection.getRoutesAsStrings(false)) {
            Section rewardSection = rewardsSection.getSection(rewardKey);
            if (rewardSection == null) {
                continue;
            }

            String typeStr = rewardSection.getString("type");
            if (typeStr == null || typeStr.isBlank()) {
                continue;
            }
            Optional<NamespacedKey> typeKeyOpt = parseNamespacedKey(typeStr);
            if (typeKeyOpt.isEmpty()) {
                continue;
            }

            QuestRewardTypeRegistry rewardTypeRegistry = RegistryAccess.registryAccess()
                    .registry(McRPGRegistryKey.QUEST_REWARD_TYPE);
            QuestRewardType baseType = rewardTypeRegistry.get(typeKeyOpt.get()).orElse(null);
            if (baseType == null) {
                McRPG.getInstance().getLogger().warning("Unknown reward type '" + typeStr
                        + "' in " + contextKey + " (" + fileName + "), skipping");
                continue;
            }

            Map<String, Object> rewardConfig = new HashMap<>();
            for (String key : rewardSection.getRoutesAsStrings(false)) {
                if ("type".equals(key) || "pot-behavior".equals(key)
                        || "remainder-strategy".equals(key) || "min-scaled-amount".equals(key)
                        || "top-count".equals(key) || "fallback".equals(key)) {
                    continue;
                }
                rewardConfig.put(key, rewardSection.get(key));
            }

            QuestRewardType reward = rewardConfig.isEmpty()
                    ? baseType
                    : baseType.fromSerializedConfig(rewardConfig);

            PotBehavior potBehavior = PotBehavior.SCALE;
            if (rewardSection.contains("pot-behavior")) {
                try {
                    potBehavior = PotBehavior.valueOf(rewardSection.getString("pot-behavior").toUpperCase());
                } catch (IllegalArgumentException e) {
                    McRPG.getInstance().getLogger().warning("Invalid pot-behavior '"
                            + rewardSection.getString("pot-behavior") + "' in " + contextKey
                            + " (" + fileName + "), defaulting to SCALE");
                }
            }

            RemainderStrategy remainder = RemainderStrategy.DISCARD;
            if (rewardSection.contains("remainder-strategy")) {
                try {
                    remainder = RemainderStrategy.valueOf(
                            rewardSection.getString("remainder-strategy").toUpperCase());
                } catch (IllegalArgumentException e) {
                    McRPG.getInstance().getLogger().warning("Invalid remainder-strategy '"
                            + rewardSection.getString("remainder-strategy") + "' in " + contextKey
                            + " (" + fileName + "), defaulting to DISCARD");
                }
            }

            int minScaled = rewardSection.getInt("min-scaled-amount", 1);
            int topCount = rewardSection.getInt("top-count", 1);

            RewardFallback fallback = null;
            if (rewardSection.contains("fallback")) {
                Section fallbackSection = rewardSection.getSection("fallback");
                if (fallbackSection != null) {
                    try {
                        Section conditionSection = fallbackSection.getSection("condition");
                        Section fallbackRewardSection = fallbackSection.getSection("reward");
                        if (conditionSection == null) {
                            McRPG.getInstance().getLogger().warning("Fallback for reward '" + rewardKey
                                    + "' in " + contextKey + " (" + fileName
                                    + ") is missing a 'condition' block, skipping fallback");
                        } else if (fallbackRewardSection == null) {
                            McRPG.getInstance().getLogger().warning("Fallback for reward '" + rewardKey
                                    + "' in " + contextKey + " (" + fileName
                                    + ") is missing a 'reward' block, skipping fallback");
                        } else {
                            TemplateCondition condition = conditionParser.parseSingle(conditionSection);
                            QuestRewardType fallbackReward = parseSingleReward(fallbackRewardSection, fileName,
                                    contextKey + "." + rewardKey + ".fallback.reward");
                            if (fallbackReward != null) {
                                fallback = new RewardFallback(condition, fallbackReward);
                            }
                        }
                    } catch (Exception e) {
                        McRPG.getInstance().getLogger().warning("Failed to parse fallback for reward '"
                                + rewardKey + "' in " + contextKey + " (" + fileName + "): " + e.getMessage());
                    }
                }
            }

            entries.add(new DistributionRewardEntry(reward, potBehavior, remainder, minScaled, topCount, fallback));
        }

        return entries;
    }

    /**
     * Parses a single reward from a YAML section containing a {@code type:} key and
     * optional reward-specific config entries.
     * <p>
     * Logs a warning and returns {@code null} if the type key is missing, invalid,
     * or not registered — the caller is responsible for deciding how to handle a
     * {@code null} result (typically by skipping the surrounding construct).
     *
     * @param rewardSection the section containing the reward's {@code type:} and config keys
     * @param fileName      the source file name (for log messages)
     * @param contextKey    a human-readable context path (for log messages)
     * @return the configured reward, or {@code null} on any parse failure
     */
    @Nullable
    private static QuestRewardType parseSingleReward(@NotNull Section rewardSection,
                                                     @NotNull String fileName,
                                                     @NotNull String contextKey) {
        Logger logger = McRPG.getInstance().getLogger();
        String typeStr = rewardSection.getString("type");
        if (typeStr == null || typeStr.isBlank()) {
            logger.warning("Reward in " + contextKey + " (" + fileName + ") is missing a 'type', skipping");
            return null;
        }
        Optional<NamespacedKey> typeKeyOpt = parseNamespacedKey(typeStr);
        if (typeKeyOpt.isEmpty()) {
            logger.warning("Invalid reward type key '" + typeStr + "' in " + contextKey
                    + " (" + fileName + "), skipping");
            return null;
        }
        QuestRewardTypeRegistry rewardTypeRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_REWARD_TYPE);
        Optional<QuestRewardType> baseType = rewardTypeRegistry.get(typeKeyOpt.get());
        if (baseType.isEmpty()) {
            logger.warning("Unknown reward type '" + typeKeyOpt.get() + "' in " + contextKey
                    + " (" + fileName + "), skipping. Is the type registered?");
            return null;
        }
        try {
            return baseType.get().parseConfig(rewardSection);
        } catch (Exception e) {
            logger.warning("Failed to parse reward in " + contextKey + " (" + fileName + "): " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses a {@link NamespacedKey} from a string. If the string contains a colon,
     * it is parsed as {@code namespace:key}. Otherwise, the McRPG namespace is used
     * with the input lowercased as the key.
     *
     * @param input the string to parse
     * @return an {@link Optional} containing the parsed key, or empty if the input
     *         is null, empty, or invalid
     */
    @NotNull
    static Optional<NamespacedKey> parseNamespacedKey(@Nullable String input) {
        return Optional.ofNullable(McRPGMethods.parseNamespacedKey(input));
    }

    /**
     * Parses a human-friendly duration string into a {@link Duration}.
     * <p>
     * Supported formats:
     * <ul>
     *     <li>{@code 24h} -- 24 hours</li>
     *     <li>{@code 7d} -- 7 days</li>
     *     <li>{@code 1d12h30m} -- 1 day, 12 hours, 30 minutes</li>
     *     <li>{@code 3600} -- 3600 seconds (plain number)</li>
     * </ul>
     *
     * @param input the duration string to parse
     * @return the parsed duration, or {@code null} if the input is null or empty
     * @throws IllegalArgumentException if the format is unrecognized
     */
    @Nullable
    public static Duration parseDuration(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String trimmed = input.trim();

        try {
            long seconds = Long.parseLong(trimmed);
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException ignored) {
        }

        Matcher matcher = DURATION_PATTERN.matcher(trimmed);
        if (!matcher.matches() || trimmed.isEmpty()) {
            throw new IllegalArgumentException("Invalid duration format: '" + input
                    + "'. Expected formats: 24h, 7d, 1d12h30m, or plain seconds");
        }

        boolean anyGroupMatched = false;
        long days = 0, hours = 0, minutes = 0, seconds = 0;

        if (matcher.group(1) != null) {
            days = Long.parseLong(matcher.group(1));
            anyGroupMatched = true;
        }
        if (matcher.group(2) != null) {
            hours = Long.parseLong(matcher.group(2));
            anyGroupMatched = true;
        }
        if (matcher.group(3) != null) {
            minutes = Long.parseLong(matcher.group(3));
            anyGroupMatched = true;
        }
        if (matcher.group(4) != null) {
            seconds = Long.parseLong(matcher.group(4));
            anyGroupMatched = true;
        }

        if (!anyGroupMatched) {
            throw new IllegalArgumentException("Invalid duration format: '" + input
                    + "'. Expected formats: 24h, 7d, 1d12h30m, or plain seconds");
        }

        return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
    }
}
