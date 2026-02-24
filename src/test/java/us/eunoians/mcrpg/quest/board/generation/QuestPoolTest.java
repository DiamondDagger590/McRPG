package us.eunoians.mcrpg.quest.board.generation;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.board.BoardMetadata;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.definition.QuestDefinitionRegistry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuestPoolTest extends McRPGBaseTest {

    private static final NamespacedKey COMMON_KEY = new NamespacedKey("mcrpg", "common");
    private static final NamespacedKey RARE_KEY = new NamespacedKey("mcrpg", "rare");
    private static final NamespacedKey EPIC_KEY = new NamespacedKey("mcrpg", "epic");

    private QuestDefinitionRegistry registry;
    private QuestPool questPool;

    @BeforeEach
    void setUp() {
        registry = new QuestDefinitionRegistry();
        questPool = new QuestPool(registry);
    }

    private static final NamespacedKey SINGLE_PLAYER_SCOPE = new NamespacedKey("mcrpg", "single_player");

    private QuestDefinition questWithBoardMetadata(String questKey, boolean boardEligible, Set<NamespacedKey> supportedRarities) {
        var stage = QuestTestHelper.singleStageDef(questKey + "_stage", questKey + "_obj");
        var phase = QuestTestHelper.singlePhaseDef(us.eunoians.mcrpg.quest.definition.PhaseCompletionMode.ALL, stage);
        Map<NamespacedKey, us.eunoians.mcrpg.quest.definition.QuestDefinitionMetadata> metadata = Map.of(
                BoardMetadata.METADATA_KEY, new BoardMetadata(boardEligible, supportedRarities, null, null)
        );
        return new QuestDefinition(
                new NamespacedKey("mcrpg", questKey),
                SINGLE_PLAYER_SCOPE,
                null,
                List.of(phase),
                List.of(),
                us.eunoians.mcrpg.quest.definition.QuestRepeatMode.ONCE,
                null,
                -1,
                null,
                metadata
        );
    }

    @DisplayName("getEligibleDefinitions: returns only definitions with matching rarity in supportedRarities")
    @Test
    void getEligibleDefinitions_returnsOnlyMatchingRarity() {
        QuestDefinition commonQuest = questWithBoardMetadata("common_quest", true, Set.of(COMMON_KEY));
        QuestDefinition rareQuest = questWithBoardMetadata("rare_quest", true, Set.of(RARE_KEY));
        QuestDefinition bothQuest = questWithBoardMetadata("both_quest", true, Set.of(COMMON_KEY, RARE_KEY));

        registry.register(commonQuest);
        registry.register(rareQuest);
        registry.register(bothQuest);

        List<NamespacedKey> commonEligible = questPool.getEligibleDefinitions(COMMON_KEY);
        assertEquals(2, commonEligible.size());
        assertTrue(commonEligible.contains(commonQuest.getQuestKey()));
        assertTrue(commonEligible.contains(bothQuest.getQuestKey()));
        assertFalse(commonEligible.contains(rareQuest.getQuestKey()));

        List<NamespacedKey> rareEligible = questPool.getEligibleDefinitions(RARE_KEY);
        assertEquals(2, rareEligible.size());
        assertTrue(rareEligible.contains(rareQuest.getQuestKey()));
        assertTrue(rareEligible.contains(bothQuest.getQuestKey()));
    }

    @DisplayName("getEligibleDefinitions: definitions without board metadata excluded")
    @Test
    void getEligibleDefinitions_definitionsWithoutBoardMetadataExcluded() {
        QuestDefinition noBoardMeta = QuestTestHelper.singlePhaseQuest("no_board");
        QuestDefinition withBoardMeta = questWithBoardMetadata("with_board", true, Set.of(COMMON_KEY));

        registry.register(noBoardMeta);
        registry.register(withBoardMeta);

        List<NamespacedKey> eligible = questPool.getEligibleDefinitions(COMMON_KEY);
        assertEquals(1, eligible.size());
        assertEquals(withBoardMeta.getQuestKey(), eligible.get(0));
    }

    @DisplayName("getEligibleDefinitions: boardEligible=false excluded")
    @Test
    void getEligibleDefinitions_boardEligibleFalseExcluded() {
        QuestDefinition ineligible = questWithBoardMetadata("ineligible", false, Set.of(COMMON_KEY));
        QuestDefinition eligible = questWithBoardMetadata("eligible", true, Set.of(COMMON_KEY));

        registry.register(ineligible);
        registry.register(eligible);

        List<NamespacedKey> result = questPool.getEligibleDefinitions(COMMON_KEY);
        assertEquals(1, result.size());
        assertEquals(eligible.getQuestKey(), result.get(0));
    }

    @DisplayName("getEligibleDefinitions: empty registry returns empty list")
    @Test
    void getEligibleDefinitions_emptyRegistry_returnsEmptyList() {
        List<NamespacedKey> result = questPool.getEligibleDefinitions(COMMON_KEY);
        assertTrue(result.isEmpty());
    }

    @DisplayName("getAllBoardEligibleDefinitions: returns all board-eligible definitions regardless of rarity")
    @Test
    void getAllBoardEligibleDefinitions_returnsAllRegardlessOfRarity() {
        QuestDefinition commonQuest = questWithBoardMetadata("common_quest", true, Set.of(COMMON_KEY));
        QuestDefinition rareQuest = questWithBoardMetadata("rare_quest", true, Set.of(RARE_KEY));
        QuestDefinition epicQuest = questWithBoardMetadata("epic_quest", true, Set.of(EPIC_KEY));

        registry.register(commonQuest);
        registry.register(rareQuest);
        registry.register(epicQuest);

        List<NamespacedKey> all = questPool.getAllBoardEligibleDefinitions();
        assertEquals(3, all.size());
        assertTrue(all.contains(commonQuest.getQuestKey()));
        assertTrue(all.contains(rareQuest.getQuestKey()));
        assertTrue(all.contains(epicQuest.getQuestKey()));
    }

    @DisplayName("getAllBoardEligibleDefinitions: non-eligible excluded")
    @Test
    void getAllBoardEligibleDefinitions_nonEligibleExcluded() {
        QuestDefinition eligible = questWithBoardMetadata("eligible", true, Set.of(COMMON_KEY));
        QuestDefinition ineligible = questWithBoardMetadata("ineligible", false, Set.of(COMMON_KEY));
        QuestDefinition noBoardMeta = QuestTestHelper.singlePhaseQuest("no_meta");

        registry.register(eligible);
        registry.register(ineligible);
        registry.register(noBoardMeta);

        List<NamespacedKey> result = questPool.getAllBoardEligibleDefinitions();
        assertEquals(1, result.size());
        assertEquals(eligible.getQuestKey(), result.get(0));
    }
}
