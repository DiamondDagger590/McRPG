package us.eunoians.mcrpg.configuration;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.TestFileUtils;
import us.eunoians.mcrpg.quest.board.BoardMetadata;
import us.eunoians.mcrpg.quest.board.template.condition.ConditionParser;
import us.eunoians.mcrpg.quest.board.template.condition.TemplateConditionRegistry;
import us.eunoians.mcrpg.quest.definition.OnStartMessage;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestRepeatMode;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveTypeRegistry;
import us.eunoians.mcrpg.quest.objective.type.builtin.BlockBreakObjectiveType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestConfigLoaderBoardMetadataTest extends McRPGBaseTest {

    private QuestConfigLoader loader;

    @BeforeEach
    public void setup() {
        TemplateConditionRegistry conditionRegistry = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.TEMPLATE_CONDITION);
        loader = new QuestConfigLoader(new ConditionParser(conditionRegistry));
        QuestObjectiveTypeRegistry objReg = RegistryAccess.registryAccess()
                .registry(McRPGRegistryKey.QUEST_OBJECTIVE_TYPE);
        if (objReg.get(BlockBreakObjectiveType.KEY).isEmpty()) {
            objReg.register(new BlockBreakObjectiveType());
        }
    }

    @DisplayName("Given board-metadata with all fields, when loading, then BoardMetadata is parsed with correct values")
    @Test
    public void loadQuests_parsesBoardMetadata_whenAllFieldsPresent() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_board_meta");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:board_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    board-metadata:\n" +
                    "      board-eligible: true\n" +
                    "      supported-rarities:\n" +
                    "        - \"mcrpg:common\"\n" +
                    "        - \"mcrpg:rare\"\n" +
                    "      acceptance-cooldown: \"2h\"\n" +
                    "      cooldown-scope: \"PLAYER\"\n" +
                    "      supported-refresh-types:\n" +
                    "        - \"daily\"\n" +
                    "        - \"weekly\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "board_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:board_quest"));
            assertNotNull(def);

            Optional<BoardMetadata> metaOpt = def.getBoardMetadata();
            assertTrue(metaOpt.isPresent());
            BoardMetadata meta = metaOpt.get();
            assertTrue(meta.boardEligible());
            assertEquals(2, meta.supportedRarities().size());
            assertTrue(meta.supportedRarities().contains(NamespacedKey.fromString("mcrpg:common")));
            assertTrue(meta.supportedRarities().contains(NamespacedKey.fromString("mcrpg:rare")));
            assertEquals(Duration.ofHours(2), meta.acceptanceCooldown());
            assertEquals("PLAYER", meta.cooldownScope());
            assertEquals(2, meta.supportedRefreshTypes().size());
            assertTrue(meta.supportedRefreshTypes().contains("DAILY"));
            assertTrue(meta.supportedRefreshTypes().contains("WEEKLY"));
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given board-metadata with board-eligible false, when loading, then boardEligible is false")
    @Test
    public void loadQuests_parsesBoardEligibleFalse() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_board_meta_ineligible");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:ineligible_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    board-metadata:\n" +
                    "      board-eligible: false\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "ineligible_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:ineligible_quest"));
            assertNotNull(def);

            Optional<BoardMetadata> metaOpt = def.getBoardMetadata();
            assertTrue(metaOpt.isPresent());
            assertFalse(metaOpt.get().boardEligible());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given no board-metadata section, when loading, then BoardMetadata is absent")
    @Test
    public void loadQuests_returnsEmptyBoardMetadata_whenNoSection() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_board_meta_absent");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:no_meta_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "no_meta_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:no_meta_quest"));
            assertNotNull(def);
            assertFalse(def.getBoardMetadata().isPresent());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given display section with name, description, objectives, and rewards, when loading, then inline display is populated")
    @Test
    public void loadQuests_parsesInlineDisplay_whenAllFieldsPresent() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_inline_display");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:display_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    display:\n" +
                    "      name: \"Mining Quest\"\n" +
                    "      description: \"Mine some blocks\"\n" +
                    "      objectives:\n" +
                    "        obj_1: \"Break 10 stone\"\n" +
                    "        obj_2: \"Break 5 iron ore\"\n" +
                    "      rewards:\n" +
                    "        reward_1: \"100 XP\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "display_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:display_quest"));
            assertNotNull(def);

            Map<String, String> display = def.getInlineDisplay();
            assertEquals("Mining Quest", display.get("name"));
            assertEquals("Mine some blocks", display.get("description"));
            assertEquals("Break 10 stone", display.get("objective.obj_1"));
            assertEquals("Break 5 iron ore", display.get("objective.obj_2"));
            assertEquals("100 XP", display.get("reward.reward_1"));
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given no display section, when loading, then inline display is empty")
    @Test
    public void loadQuests_returnsEmptyDisplay_whenNoDisplaySection() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_no_display");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:no_display_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "no_display_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:no_display_quest"));
            assertNotNull(def);
            assertTrue(def.getInlineDisplay().isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given on-start-messages with locale key entry, when loading, then locale-backed message is parsed")
    @Test
    public void loadQuests_parsesLocaleKeyMessage_whenOnStartMessagesHasKey() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_on_start_locale");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:start_msg_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    on-start-messages:\n" +
                    "      welcome:\n" +
                    "        key: \"quests.mining.start-welcome\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "start_msg_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:start_msg_quest"));
            assertNotNull(def);

            List<OnStartMessage> messages = def.getOnStartMessages();
            assertEquals(1, messages.size());
            OnStartMessage msg = messages.get(0);
            assertTrue(msg.localeKey().isPresent());
            assertEquals("quests.mining.start-welcome", msg.localeKey().get());
            assertTrue(msg.inlineMessages().isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given on-start-messages with inline messages, when loading, then inline message is parsed")
    @Test
    public void loadQuests_parsesInlineMessages_whenOnStartMessagesHasMessages() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_on_start_inline");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:inline_msg_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    on-start-messages:\n" +
                    "      hint:\n" +
                    "        messages:\n" +
                    "          - \"<gold>Quest started!\"\n" +
                    "          - \"<gray>Break some blocks.\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "inline_msg_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:inline_msg_quest"));
            assertNotNull(def);

            List<OnStartMessage> messages = def.getOnStartMessages();
            assertEquals(1, messages.size());
            OnStartMessage msg = messages.get(0);
            assertFalse(msg.localeKey().isPresent());
            assertEquals(2, msg.inlineMessages().size());
            assertEquals("<gold>Quest started!", msg.inlineMessages().get(0));
            assertEquals("<gray>Break some blocks.", msg.inlineMessages().get(1));
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given no on-start-messages section, when loading, then on-start messages list is empty")
    @Test
    public void loadQuests_returnsEmptyMessages_whenNoOnStartMessagesSection() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_on_start_absent");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:no_start_msg_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "no_start_msg_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:no_start_msg_quest"));
            assertNotNull(def);
            assertTrue(def.getOnStartMessages().isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given on-start-messages entry with neither key nor messages, when loading, then entry is skipped")
    @Test
    public void loadQuests_skipsMessage_whenEntryHasNoKeyOrMessages() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_on_start_skip");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:skip_msg_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    on-start-messages:\n" +
                    "      bad_entry:\n" +
                    "        other-field: \"value\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "skip_msg_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:skip_msg_quest"));
            assertNotNull(def);
            assertTrue(def.getOnStartMessages().isEmpty());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given quest-chain-file flag, when loading, then quest file is skipped for definitions")
    @Test
    public void loadQuests_skipsDefinitions_whenChainFileFlagIsTrue() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_chain_file");
        try {
            String yaml = "quest-chain-file: true\n" +
                    "quests:\n" +
                    "  mcrpg:chain_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "chain_quest.yml", yaml);

            QuestLoadResult loadResult = loader.loadQuestsFromDirectory(tempDir.toFile());
            assertTrue(loadResult.definitions().isEmpty(), "Chain files should not produce quest definitions");
            assertFalse(loadResult.chainFiles().isEmpty(), "Chain file path should be recorded");
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given COOLDOWN_LIMITED repeat mode without cooldown or limit, when loading, then quest still loads with warnings")
    @Test
    public void loadQuests_loadsQuest_whenCooldownLimitedMissingFields() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_repeat_mode");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:repeat_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    repeat-mode: COOLDOWN_LIMITED\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "repeat_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:repeat_quest"));
            assertNotNull(def, "Quest with COOLDOWN_LIMITED and missing fields should still load");
            assertEquals(QuestRepeatMode.COOLDOWN_LIMITED, def.getRepeatMode());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given expiration in days-hours-minutes format, when loading, then expiration duration is correct")
    @Test
    public void loadQuests_parsesExpiration_whenCombinedDaysHoursFormat() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_expiration");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:expire_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    expiration: \"1d6h\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "expire_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:expire_quest"));
            assertNotNull(def);
            assertTrue(def.getExpiration().isPresent());
            assertEquals(Duration.ofDays(1).plusHours(6), def.getExpiration().get());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given multiple on-start-messages entries mixing locale and inline, when loading, then both are preserved in order")
    @Test
    public void loadQuests_preservesMessageOrder_whenMixedLocaleAndInlineEntries() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_on_start_mixed");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:mixed_msg_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    on-start-messages:\n" +
                    "      locale_entry:\n" +
                    "        key: \"quests.mining.start\"\n" +
                    "      inline_entry:\n" +
                    "        messages:\n" +
                    "          - \"<gold>Good luck!\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "mixed_msg_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:mixed_msg_quest"));
            assertNotNull(def);

            List<OnStartMessage> messages = def.getOnStartMessages();
            assertEquals(2, messages.size());
            assertTrue(messages.get(0).localeKey().isPresent());
            assertFalse(messages.get(1).localeKey().isPresent());
            assertEquals(1, messages.get(1).inlineMessages().size());
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given invalid completion-mode, when loading, then quest is skipped")
    @Test
    public void loadQuests_skipsQuest_whenInvalidCompletionMode() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_bad_completion");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:bad_mode_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: INVALID_MODE\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "bad_mode_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            assertTrue(result.isEmpty(), "Quest with invalid completion-mode should be skipped");
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given quest with no phases section, when loading, then quest is skipped")
    @Test
    public void loadQuests_skipsQuest_whenNoPhasesSection() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_no_phases");
        try {
            String yaml = "quests:\n" +
                    "  mcrpg:no_phase_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n";
            TestFileUtils.writeFile(tempDir, "no_phase_quest.yml", yaml);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            assertTrue(result.isEmpty(), "Quest with no phases should be skipped");
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }

    @DisplayName("Given duplicate quest key across files, when loading, then first-loaded wins")
    @Test
    public void loadQuests_keepsOneDefinition_whenDuplicateQuestKey() throws IOException {
        Path tempDir = Files.createTempDirectory("quest_duplicate");
        try {
            String yaml1 = "quests:\n" +
                    "  mcrpg:dup_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    expiration: \"1h\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            String yaml2 = "quests:\n" +
                    "  mcrpg:dup_quest:\n" +
                    "    scope: \"mcrpg:single_player\"\n" +
                    "    expiration: \"2h\"\n" +
                    "    phases:\n" +
                    "      phase:\n" +
                    "        completion-mode: ALL\n" +
                    "        stages:\n" +
                    "          stage:\n" +
                    "            key: \"mcrpg:stage_1\"\n" +
                    "            objectives:\n" +
                    "              objective:\n" +
                    "                key: \"mcrpg:obj_1\"\n" +
                    "                type: \"mcrpg:block_break\"\n" +
                    "                required-progress: 10\n";
            TestFileUtils.writeFile(tempDir, "dup_quest_a.yml", yaml1);
            TestFileUtils.writeFile(tempDir, "dup_quest_b.yml", yaml2);

            Map<NamespacedKey, QuestDefinition> result = loader.loadQuestsFromDirectory(tempDir.toFile()).definitions();
            assertEquals(1, result.size(), "Duplicate key should only produce one definition");
            QuestDefinition def = result.get(NamespacedKey.fromString("mcrpg:dup_quest"));
            assertNotNull(def);
            assertTrue(def.getExpiration().isPresent(), "Surviving definition should retain its expiration");
            Duration expiration = def.getExpiration().get();
            assertTrue(expiration.equals(Duration.ofHours(1)) || expiration.equals(Duration.ofHours(2)),
                    "Surviving definition should have expiration from one of the source files, got: " + expiration);
        } finally {
            TestFileUtils.deleteRecursively(tempDir.toFile());
        }
    }
}
