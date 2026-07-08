package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionParser;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.chain.QuestChainDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.AbilityActivateObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.AbilityUnlockObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.GuiOpenObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.QuestBoardAcceptObjectiveType;
import us.eunoians.mcrpg.quest.objective.type.builtin.SkillLevelUpObjectiveType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that all 7 tutorial quest YAML files and the tutorial chain YAML
 * parse without error via the production loaders. Ensures chain steps reference
 * all 7 quest keys and each quest has at least one objective.
 */
public class TutorialQuestYamlValidationTest extends McRPGBaseTest {

    private static final Set<NamespacedKey> EXPECTED_QUEST_KEYS = Set.of(
            new NamespacedKey("mcrpg", "tutorial_first_steps"),
            new NamespacedKey("mcrpg", "tutorial_explore_menu"),
            new NamespacedKey("mcrpg", "tutorial_passive_unlock"),
            new NamespacedKey("mcrpg", "tutorial_open_loadout"),
            new NamespacedKey("mcrpg", "tutorial_active_unlock"),
            new NamespacedKey("mcrpg", "tutorial_combo_strike"),
            new NamespacedKey("mcrpg", "tutorial_quest_board")
    );

    private static final NamespacedKey CHAIN_KEY = new NamespacedKey("mcrpg", "tutorial_chain");

    private QuestConfigLoader questLoader;
    private QuestChainConfigLoader chainLoader;

    @BeforeEach
    public void setup() {
        TemplateConditionRegistry conditionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.TEMPLATE_CONDITION);
        questLoader = new QuestConfigLoader(new ConditionParser(conditionRegistry));
        chainLoader = new QuestChainConfigLoader();

        QuestObjectiveTypeRegistry objReg = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        registerIfAbsent(objReg, SkillLevelUpObjectiveType.KEY, new SkillLevelUpObjectiveType());
        registerIfAbsent(objReg, GuiOpenObjectiveType.KEY, new GuiOpenObjectiveType());
        registerIfAbsent(objReg, AbilityUnlockObjectiveType.KEY, new AbilityUnlockObjectiveType());
        registerIfAbsent(objReg, AbilityActivateObjectiveType.KEY, new AbilityActivateObjectiveType());
        registerIfAbsent(objReg, QuestBoardAcceptObjectiveType.KEY, new QuestBoardAcceptObjectiveType());
    }

    @Test
    @DisplayName("Given tutorial quest YAML files, when loaded via QuestConfigLoader, then all 7 definitions parse without error")
    public void tutorialQuestYamls_allParseWithoutError() {
        File tutorialDir = getTutorialQuestDir();
        QuestLoadResult result = questLoader.loadQuestsFromDirectory(tutorialDir);

        Map<NamespacedKey, QuestDefinition> definitions = result.definitions();
        Map<NamespacedKey, QuestDefinition> tutorialDefs = definitions.entrySet().stream()
                .filter(e -> EXPECTED_QUEST_KEYS.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertEquals(EXPECTED_QUEST_KEYS.size(), tutorialDefs.size(),
                "Expected all 7 tutorial quests to parse; found: " + tutorialDefs.keySet());
    }

    @Test
    @DisplayName("Given tutorial chain YAML, when loaded via QuestChainConfigLoader, then chain parses without error")
    public void tutorialChainYaml_parsesWithoutError() {
        File tutorialDir = getTutorialQuestDir();
        Map<NamespacedKey, QuestChainDefinition> chains = chainLoader.loadChainsFromDirectory(tutorialDir);

        assertTrue(chains.containsKey(CHAIN_KEY), "Chain mcrpg:tutorial_chain was not found in loaded chains");
    }

    @Test
    @DisplayName("Given the tutorial chain definition, when steps are examined, then chain references all 7 tutorial quest keys")
    public void tutorialChain_referencesAllSevenQuestKeys() {
        File tutorialDir = getTutorialQuestDir();
        Map<NamespacedKey, QuestChainDefinition> chains = chainLoader.loadChainsFromDirectory(tutorialDir);
        QuestChainDefinition chain = chains.get(CHAIN_KEY);
        assertNotNull(chain, "tutorial_chain definition must be loaded");

        List<NamespacedKey> stepKeys = chain.getSteps().stream()
                .map(step -> step.questKey())
                .collect(Collectors.toList());

        assertEquals(7, stepKeys.size(), "Tutorial chain should have exactly 7 steps");
        for (NamespacedKey expectedKey : EXPECTED_QUEST_KEYS) {
            assertTrue(stepKeys.contains(expectedKey),
                    "Tutorial chain missing step for quest: " + expectedKey);
        }
    }

    @Test
    @DisplayName("Given all 7 tutorial quest definitions, when phases are checked, then each has at least one phase with at least one objective")
    public void tutorialQuests_eachHasAtLeastOneObjective() {
        File tutorialDir = getTutorialQuestDir();
        QuestLoadResult result = questLoader.loadQuestsFromDirectory(tutorialDir);
        Map<NamespacedKey, QuestDefinition> definitions = result.definitions();

        for (NamespacedKey key : EXPECTED_QUEST_KEYS) {
            QuestDefinition def = definitions.get(key);
            assertNotNull(def, "Missing definition for: " + key);
            assertFalse(def.getPhases().isEmpty(), key + " must have at least one phase");
            assertFalse(def.getPhases().get(0).getStages().isEmpty(), key + " phase must have at least one stage");
            assertFalse(def.getPhases().get(0).getStages().get(0).getObjectives().isEmpty(),
                    key + " stage must have at least one objective");
        }
    }

    /**
     * Resolves the bundled {@code quests/tutorial/} resource directory from the classpath.
     *
     * @return the tutorial quest directory
     */
    private File getTutorialQuestDir() {
        URL url = getClass().getClassLoader().getResource("quests/tutorial");
        assertNotNull(url, "quests/tutorial resource directory not found on classpath");
        return new File(url.getFile());
    }

    /**
     * Registers a quest objective type only if its key is not already present in the registry.
     * Prevents duplicate registration errors when tests share the MockBukkit server lifecycle.
     *
     * @param registry the objective type registry
     * @param key      the objective type key
     * @param type     the objective type to register
     */
    private void registerIfAbsent(@NotNull QuestObjectiveTypeRegistry registry,
                                  @NotNull NamespacedKey key,
                                  @NotNull us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType type) {
        if (registry.get(key).isEmpty()) {
            registry.register(type);
        }
    }
}
