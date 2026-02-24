package us.eunoians.mcrpg.quest.definition;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QuestObjectiveDefinitionTest extends McRPGBaseTest {

    @DisplayName("Given zero requiredProgress, when constructing, then it throws IllegalArgumentException")
    @Test
    public void constructor_throwsIllegalArgumentException_whenRequiredProgressZero() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuestObjectiveDefinition(
                        new NamespacedKey("mcrpg", "o"),
                        QuestTestHelper.mockObjectiveType("t"),
                        0,
                        List.of()
                ));
    }

    @DisplayName("Given negative requiredProgress, when constructing, then it throws IllegalArgumentException")
    @Test
    public void constructor_throwsIllegalArgumentException_whenRequiredProgressNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new QuestObjectiveDefinition(
                        new NamespacedKey("mcrpg", "o"),
                        QuestTestHelper.mockObjectiveType("t"),
                        -5,
                        List.of()
                ));
    }

    @DisplayName("Given a valid objective, when getting rewards, then returned list is immutable")
    @Test
    public void getRewards_returnsImmutableList() {
        QuestObjectiveDefinition obj = QuestTestHelper.singleObjectiveDef("o", 10);
        assertThrows(UnsupportedOperationException.class, () -> obj.getRewards().add(null));
    }

    @DisplayName("Given a valid objective, when getting fields, then they match constructor args")
    @Test
    public void getters_returnConstructorValues() {
        QuestObjectiveDefinition obj = QuestTestHelper.singleObjectiveDef("my_obj", 42);
        assertEquals(new NamespacedKey("mcrpg", "my_obj"), obj.getObjectiveKey());
        assertEquals(42, obj.getRequiredProgress());
    }

    @DisplayName("Given a valid objective, when getting descriptionRoute, then route follows quests.{ns}.{quest}.objectives.{obj}.description pattern")
    @Test
    public void getDescriptionRoute_returnsCorrectRoute() {
        QuestObjectiveDefinition obj = QuestTestHelper.singleObjectiveDef("break_stone", 10);
        NamespacedKey questKey = new NamespacedKey("mcrpg", "daily_mining");
        Route expected = Route.fromString("quests.mcrpg.daily_mining.objectives.break_stone.description");
        assertEquals(expected, obj.getDescriptionRoute(questKey));
    }

    @DisplayName("Given a valid objective, when getting descriptionRoute with different quest namespace, then route includes that namespace")
    @Test
    public void getDescriptionRoute_includesQuestNamespace() {
        QuestObjectiveDefinition obj = QuestTestHelper.singleObjectiveDef("gather_wood", 5);
        NamespacedKey questKey = new NamespacedKey("expansion", "seasonal_quest");
        Route expected = Route.fromString("quests.expansion.seasonal_quest.objectives.gather_wood.description");
        assertEquals(expected, obj.getDescriptionRoute(questKey));
    }
}
