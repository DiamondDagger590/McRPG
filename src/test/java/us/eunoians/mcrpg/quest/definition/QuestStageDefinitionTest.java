package us.eunoians.mcrpg.quest.definition;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QuestStageDefinitionTest extends McRPGBaseTest {

    @DisplayName("Given an empty objectives list, when constructing, then it throws IllegalArgumentException")
    @Test
    public void constructor_throwsIllegalArgumentException_whenObjectivesEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuestStageDefinition(new NamespacedKey("mcrpg", "s"), List.of(), List.of()));
    }

    @DisplayName("Given a valid stage, when getting objectives, then returned list is immutable")
    @Test
    public void getObjectives_returnsImmutableList() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        assertThrows(UnsupportedOperationException.class, () -> stage.getObjectives().add(null));
    }

    @DisplayName("Given a valid stage, when getting rewards, then returned list is immutable")
    @Test
    public void getRewards_returnsImmutableList() {
        QuestStageDefinition stage = QuestTestHelper.singleStageDef("s", "o");
        assertThrows(UnsupportedOperationException.class, () -> stage.getRewards().add(null));
    }

    @DisplayName("Given a valid stage, when getting fields, then they match constructor args")
    @Test
    public void getters_returnConstructorValues() {
        QuestObjectiveDefinition obj = QuestTestHelper.singleObjectiveDef("o", 50);
        QuestStageDefinition stage = new QuestStageDefinition(
                new NamespacedKey("mcrpg", "my_stage"), List.of(obj), List.of());
        assertEquals(new NamespacedKey("mcrpg", "my_stage"), stage.getStageKey());
        assertEquals(1, stage.getObjectives().size());
        assertEquals(0, stage.getRewards().size());
    }
}
