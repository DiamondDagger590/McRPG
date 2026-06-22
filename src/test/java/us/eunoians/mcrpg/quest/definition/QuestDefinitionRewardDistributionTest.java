package us.eunoians.mcrpg.quest.definition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.board.distribution.RewardDistributionConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Quest definition reward distribution coverage")
class QuestDefinitionRewardDistributionTest extends McRPGBaseTest {

    @Nested
    @DisplayName("QuestPhaseDefinition")
    class PhaseDefinitionDistribution {

        @Test
        @DisplayName("getRewardDistribution returns empty when null")
        void getRewardDistribution_returnsEmpty_whenNull() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = new QuestPhaseDefinition(
                    0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

            assertTrue(phase.getRewardDistribution().isEmpty());
        }

        @Test
        @DisplayName("getRewardDistribution returns present when provided")
        void getRewardDistribution_returnsPresent_whenProvided() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            RewardDistributionConfig config = new RewardDistributionConfig(List.of());
            QuestPhaseDefinition phase = new QuestPhaseDefinition(
                    0, PhaseCompletionMode.ALL, List.of(stage), List.of(), config);

            assertTrue(phase.getRewardDistribution().isPresent());
            assertSame(config, phase.getRewardDistribution().get());
        }

        @Test
        @DisplayName("getRewards returns immutable list")
        void getRewards_returnsImmutableList() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = new QuestPhaseDefinition(
                    0, PhaseCompletionMode.ALL, List.of(stage), List.of(), null);

            assertThrows(UnsupportedOperationException.class, () -> phase.getRewards().add(null));
        }

        @Test
        @DisplayName("phaseIndex zero is valid")
        void phaseIndex_zeroIsValid() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            QuestPhaseDefinition phase = new QuestPhaseDefinition(
                    0, PhaseCompletionMode.ANY, List.of(stage), List.of(), null);

            assertEquals(0, phase.getPhaseIndex());
            assertEquals(PhaseCompletionMode.ANY, phase.getCompletionMode());
        }

        @Test
        @DisplayName("multiple stages are preserved in order")
        void multipleStages_preservedInOrder() {
            QuestStageDefinition s1 = QuestTestHelper.singleStageDef("s1", "o1");
            QuestStageDefinition s2 = QuestTestHelper.singleStageDef("s2", "o2");
            QuestPhaseDefinition phase = new QuestPhaseDefinition(
                    1, PhaseCompletionMode.ALL, List.of(s1, s2), List.of(), null);

            assertEquals(2, phase.getStages().size());
            assertEquals(new NamespacedKey("mcrpg", "s1"), phase.getStages().get(0).getStageKey());
            assertEquals(new NamespacedKey("mcrpg", "s2"), phase.getStages().get(1).getStageKey());
        }
    }

    @Nested
    @DisplayName("QuestStageDefinition")
    class StageDefinitionDistribution {

        @Test
        @DisplayName("getRewardDistribution returns empty when null")
        void getRewardDistribution_returnsEmpty_whenNull() {
            QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
            assertTrue(stage.getRewardDistribution().isEmpty());
        }

        @Test
        @DisplayName("getRewardDistribution returns present when provided")
        void getRewardDistribution_returnsPresent_whenProvided() {
            RewardDistributionConfig config = new RewardDistributionConfig(List.of());
            QuestObjectiveDefinition obj = QuestTestHelper.singleObjectiveDef("obj", 10);
            QuestStageDefinition stage = new QuestStageDefinition(
                    new NamespacedKey("mcrpg", "stage"),
                    List.of(obj),
                    List.of(),
                    config
            );

            assertTrue(stage.getRewardDistribution().isPresent());
            assertSame(config, stage.getRewardDistribution().get());
        }

        @Test
        @DisplayName("multiple objectives are preserved")
        void multipleObjectives_preserved() {
            QuestObjectiveDefinition o1 = QuestTestHelper.singleObjectiveDef("o1", 10);
            QuestObjectiveDefinition o2 = QuestTestHelper.singleObjectiveDef("o2", 20);
            QuestStageDefinition stage = new QuestStageDefinition(
                    new NamespacedKey("mcrpg", "multi_stage"),
                    List.of(o1, o2),
                    List.of(),
                    null
            );

            assertEquals(2, stage.getObjectives().size());
            assertEquals(new NamespacedKey("mcrpg", "o1"), stage.getObjectives().get(0).getObjectiveKey());
            assertEquals(new NamespacedKey("mcrpg", "o2"), stage.getObjectives().get(1).getObjectiveKey());
        }

        @Test
        @DisplayName("stageKey is preserved")
        void stageKey_preserved() {
            QuestObjectiveDefinition obj = QuestTestHelper.singleObjectiveDef("o", 5);
            NamespacedKey key = new NamespacedKey("mcrpg", "custom_stage");
            QuestStageDefinition stage = new QuestStageDefinition(key, List.of(obj), List.of(), null);

            assertEquals(key, stage.getStageKey());
        }
    }

    @Nested
    @DisplayName("QuestRepeatMode")
    class RepeatModeTests {

        @Test
        @DisplayName("all five modes are defined")
        void allFiveModes_areDefined() {
            QuestRepeatMode[] modes = QuestRepeatMode.values();
            assertEquals(5, modes.length);
        }

        @ParameterizedTest
        @EnumSource(QuestRepeatMode.class)
        @DisplayName("valueOf round-trips for each mode")
        void valueOf_roundTrips(QuestRepeatMode mode) {
            assertEquals(mode, QuestRepeatMode.valueOf(mode.name()));
        }

        @Test
        @DisplayName("ONCE mode exists")
        void once_exists() {
            assertEquals(QuestRepeatMode.ONCE, QuestRepeatMode.valueOf("ONCE"));
        }

        @Test
        @DisplayName("COOLDOWN_LIMITED mode exists")
        void cooldownLimited_exists() {
            assertEquals(QuestRepeatMode.COOLDOWN_LIMITED, QuestRepeatMode.valueOf("COOLDOWN_LIMITED"));
        }
    }

    @Nested
    @DisplayName("PhaseCompletionMode")
    class CompletionModeTests {

        @Test
        @DisplayName("ALL and ANY modes exist")
        void allAndAny_exist() {
            PhaseCompletionMode[] modes = PhaseCompletionMode.values();
            assertEquals(2, modes.length);
            assertEquals(PhaseCompletionMode.ALL, PhaseCompletionMode.valueOf("ALL"));
            assertEquals(PhaseCompletionMode.ANY, PhaseCompletionMode.valueOf("ANY"));
        }
    }
}
