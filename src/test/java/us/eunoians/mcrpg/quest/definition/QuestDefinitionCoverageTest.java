package us.eunoians.mcrpg.quest.definition;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.board.BoardMetadata;
import us.eunoians.mcrpg.quest.board.template.condition.QuestRewardEntry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

public class QuestDefinitionCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("getDescriptionRoute")
    class GetDescriptionRoute {

        @DisplayName("route follows quests.{namespace}.{key}.description pattern")
        @Test
        void getDescriptionRoute_returnsCorrectRoute() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("desc_route_test");
            Route expected = Route.fromString("quests.mcrpg.desc_route_test.description");
            assertEquals(expected, def.getDescriptionRoute());
        }

        @DisplayName("route includes custom namespace")
        @Test
        void getDescriptionRoute_includesCustomNamespace() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("custom", "my_quest"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .build();
            Route expected = Route.fromString("quests.custom.my_quest.description");
            assertEquals(expected, def.getDescriptionRoute());
        }
    }

    @Nested
    @DisplayName("formatFallbackDisplayName")
    @ExtendWith(McRPGPlayerExtension.class)
    class FormatFallbackDisplayName {

        private QuestDefinition defWithKey(String key) {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            return new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", key),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .build();
        }

        private String getDisplayNameViaFallback(QuestDefinition def, McRPGPlayer player) {
            try (MockedStatic<RegistryAccess> mocked = mockStatic(RegistryAccess.class)) {
                mocked.when(RegistryAccess::registryAccess).thenThrow(new RuntimeException("force fallback"));
                return def.getDisplayName(player);
            }
        }

        @DisplayName("strips gen_template_ prefix and UUID suffix")
        @Test
        void formatFallback_stripsGenTemplateAndUuid(McRPGPlayer player) {
            QuestDefinition def = defWithKey("gen_template_choose_path_1f97a1b5");
            assertEquals("Choose Path", getDisplayNameViaFallback(def, player));
        }

        @DisplayName("strips gen_ prefix")
        @Test
        void formatFallback_stripsGenPrefix(McRPGPlayer player) {
            QuestDefinition def = defWithKey("gen_daily_mining");
            assertEquals("Daily Mining", getDisplayNameViaFallback(def, player));
        }

        @DisplayName("title-cases underscore-separated parts")
        @Test
        void formatFallback_titleCasesWords(McRPGPlayer player) {
            QuestDefinition def = defWithKey("gather_rare_ores");
            assertEquals("Gather Rare Ores", getDisplayNameViaFallback(def, player));
        }

        @DisplayName("strips long UUID-like suffix")
        @Test
        void formatFallback_stripsLongUuidSuffix(McRPGPlayer player) {
            QuestDefinition def = defWithKey("gen_template_hunt_beasts_abcdef0123456789");
            assertEquals("Hunt Beasts", getDisplayNameViaFallback(def, player));
        }

        @DisplayName("single-word key title-cases correctly")
        @Test
        void formatFallback_singleWord(McRPGPlayer player) {
            QuestDefinition def = defWithKey("mining");
            assertEquals("Mining", getDisplayNameViaFallback(def, player));
        }

        @DisplayName("prefers inline name over fallback")
        @Test
        void getDisplayName_prefersInlineName(McRPGPlayer player) {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "gen_ugly_key_abc123ff"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .inlineDisplay(Map.of("name", "Pretty Display Name"))
                    .build();
            assertEquals("Pretty Display Name", getDisplayNameViaFallback(def, player));
        }

        @DisplayName("empty inline name falls through to fallback")
        @Test
        void getDisplayName_emptyInlineFallsThrough(McRPGPlayer player) {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "daily_mining"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .inlineDisplay(Map.of("name", ""))
                    .build();
            assertEquals("Daily Mining", getDisplayNameViaFallback(def, player));
        }
    }

    @Nested
    @DisplayName("getDescription")
    @ExtendWith(McRPGPlayerExtension.class)
    class GetDescription {

        @DisplayName("returns inline description when localization unavailable")
        @Test
        void getDescription_returnsInlineDescription(McRPGPlayer player) {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "desc_test"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .inlineDisplay(Map.of("description", "Mine some blocks"))
                    .build();
            try (MockedStatic<RegistryAccess> mocked = mockStatic(RegistryAccess.class)) {
                mocked.when(RegistryAccess::registryAccess).thenThrow(new RuntimeException("force fallback"));
                var result = def.getDescription(player);
                assertTrue(result.isPresent());
                assertEquals("Mine some blocks", result.get());
            }
        }

        @DisplayName("returns empty when no inline description and localization unavailable")
        @Test
        void getDescription_returnsEmpty_whenNoInline(McRPGPlayer player) {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("no_desc");
            try (MockedStatic<RegistryAccess> mocked = mockStatic(RegistryAccess.class)) {
                mocked.when(RegistryAccess::registryAccess).thenThrow(new RuntimeException("force fallback"));
                var result = def.getDescription(player);
                assertTrue(result.isEmpty());
            }
        }

        @DisplayName("returns empty when inline description is empty string")
        @Test
        void getDescription_returnsEmpty_whenInlineIsEmpty(McRPGPlayer player) {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "empty_desc"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .inlineDisplay(Map.of("description", ""))
                    .build();
            try (MockedStatic<RegistryAccess> mocked = mockStatic(RegistryAccess.class)) {
                mocked.when(RegistryAccess::registryAccess).thenThrow(new RuntimeException("force fallback"));
                var result = def.getDescription(player);
                assertTrue(result.isEmpty());
            }
        }
    }

    @Nested
    @DisplayName("hasBoardMetadata")
    class HasBoardMetadata {

        @DisplayName("returns false when no metadata")
        @Test
        void hasBoardMetadata_returnsFalse_whenNoMetadata() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("no_meta");
            assertFalse(def.hasBoardMetadata());
        }

        @DisplayName("returns true when board metadata present")
        @Test
        void hasBoardMetadata_returnsTrue_whenPresent() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            BoardMetadata boardMeta = new BoardMetadata(true, Set.of(), Set.of(), null, null);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "meta_test"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .metadata(Map.of(BoardMetadata.METADATA_KEY, boardMeta))
                    .build();
            assertTrue(def.hasBoardMetadata());
        }
    }

    @Nested
    @DisplayName("getAllMetadata")
    class GetAllMetadata {

        @DisplayName("returns empty map when no metadata")
        @Test
        void getAllMetadata_returnsEmptyMap() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("no_meta_all");
            assertTrue(def.getAllMetadata().isEmpty());
        }

        @DisplayName("returns populated map when metadata present")
        @Test
        void getAllMetadata_returnsPopulatedMap() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            BoardMetadata boardMeta = new BoardMetadata(true, Set.of(), Set.of(), null, null);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "meta_all_test"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .metadata(Map.of(BoardMetadata.METADATA_KEY, boardMeta))
                    .build();
            assertEquals(1, def.getAllMetadata().size());
            assertTrue(def.getAllMetadata().containsKey(BoardMetadata.METADATA_KEY));
        }
    }

    @Nested
    @DisplayName("withEntries factory")
    class WithEntries {

        @DisplayName("creates definition with reward entries")
        @Test
        void withEntries_createsDefinition() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            var rewardType = QuestTestHelper.mockRewardType("test_reward");
            var entry = new QuestRewardEntry(rewardType, null);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "entries_test"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .rewardEntries(List.of(entry))
                    .build();
            assertNotNull(def);
            assertEquals(1, def.getRewardEntries().size());
            assertEquals(rewardType, def.getRewardEntries().get(0).reward());
        }
    }

    @Nested
    @DisplayName("inlineDisplay")
    class InlineDisplay {

        @DisplayName("getInlineDisplay returns empty map when null")
        @Test
        void getInlineDisplay_returnsEmptyMap_whenNull() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("inline_null");
            assertTrue(def.getInlineDisplay().isEmpty());
        }

        @DisplayName("getInlineDisplay returns populated map")
        @Test
        void getInlineDisplay_returnsPopulated() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "inline_test"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .inlineDisplay(Map.of("name", "Test", "description", "A test"))
                    .build();
            assertEquals(2, def.getInlineDisplay().size());
            assertEquals("Test", def.getInlineDisplay().get("name"));
        }

        @DisplayName("getInlineDisplayValue returns present for existing key")
        @Test
        void getInlineDisplayValue_returnsPresent() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "val_test"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .inlineDisplay(Map.of("name", "Hello"))
                    .build();
            assertEquals("Hello", def.getInlineDisplayValue("name").orElseThrow());
        }

        @DisplayName("getInlineDisplayValue returns empty for missing key")
        @Test
        void getInlineDisplayValue_returnsEmpty_whenMissing() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("val_missing");
            assertTrue(def.getInlineDisplayValue("nonexistent").isEmpty());
        }
    }

    @Nested
    @DisplayName("rewardDistribution")
    class RewardDistribution {

        @DisplayName("returns empty when null")
        @Test
        void getRewardDistribution_returnsEmpty() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("no_dist");
            assertTrue(def.getRewardDistribution().isEmpty());
        }
    }

    @Nested
    @DisplayName("getBoardMetadata")
    class GetBoardMetadata {

        @DisplayName("returns empty when no board metadata")
        @Test
        void getBoardMetadata_returnsEmpty() {
            QuestDefinition def = QuestTestHelper.singlePhaseQuest("no_board_meta");
            assertTrue(def.getBoardMetadata().isEmpty());
        }

        @DisplayName("returns board metadata when present")
        @Test
        void getBoardMetadata_returnsPresent() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            BoardMetadata boardMeta = new BoardMetadata(true, Set.of(), Set.of(), Duration.ofMinutes(5), "PLAYER");
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "board_meta_test"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .metadata(Map.of(BoardMetadata.METADATA_KEY, boardMeta))
                    .build();
            assertTrue(def.getBoardMetadata().isPresent());
            assertTrue(def.getBoardMetadata().get().boardEligible());
        }

        @DisplayName("returns empty when metadata key present but type is not BoardMetadata")
        @Test
        void getBoardMetadata_returnsEmpty_whenWrongType() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = QuestTestHelper.singlePhaseDef(PhaseCompletionMode.ALL, stage);
            QuestDefinitionMetadata fakeMeta = new QuestDefinitionMetadata() {
                @NotNull
                @Override
                public NamespacedKey getMetadataKey() {
                    return BoardMetadata.METADATA_KEY;
                }

                @NotNull
                @Override
                public Map<String, Object> serialize() {
                    return Map.of();
                }
            };
            QuestDefinition def = new QuestDefinition.Builder(
                    new NamespacedKey("mcrpg", "wrong_meta_test"),
                    new NamespacedKey("mcrpg", "single_player"),
                    List.of(phase))
                    .metadata(Map.of(BoardMetadata.METADATA_KEY, fakeMeta))
                    .build();
            assertTrue(def.getBoardMetadata().isEmpty());
        }
    }
}
