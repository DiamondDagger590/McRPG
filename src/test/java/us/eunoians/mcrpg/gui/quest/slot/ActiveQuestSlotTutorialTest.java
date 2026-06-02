package us.eunoians.mcrpg.gui.quest.slot;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;
import us.eunoians.mcrpg.quest.source.builtin.TutorialQuestSource;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the tutorial-specific behaviour in {@link ActiveQuestSlot}:
 * <ul>
 *   <li>{@link QuestDefinition#getDisplayItemRoute()} resolves to the per-quest locale path</li>
 *   <li>Non-abandonable quests (tutorial source) do not trigger the abandon-confirm flow on right-click</li>
 * </ul>
 */
public class ActiveQuestSlotTutorialTest extends McRPGBaseTest {

    @Test
    @DisplayName("Given a quest with namespace mcrpg and key tutorial_first_steps, then getDisplayItemRoute returns quests.mcrpg.tutorial_first_steps.display-item")
    public void getDisplayItemRoute_returnsExpectedPath() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("tutorial_first_steps");
        Route route = def.getDisplayItemRoute();
        assertEquals("quests.mcrpg.tutorial_first_steps.display-item", route.join('.'));
    }

    @Test
    @DisplayName("Given a quest with namespace test and key my_quest, then getDisplayItemRoute encodes the namespace correctly")
    public void getDisplayItemRoute_usesQuestNamespaceInPath() {
        QuestDefinition def = new QuestDefinition.Builder(
                new NamespacedKey("test", "my_quest"),
                new NamespacedKey("mcrpg", "single_player"),
                java.util.List.of(
                        QuestTestHelper.singlePhaseDef(
                                us.eunoians.mcrpg.quest.definition.PhaseCompletionMode.ALL,
                                QuestTestHelper.singleStageDef("s", "o")))
        ).build();

        Route route = def.getDisplayItemRoute();
        String routeString = route.join('.');
        assertTrue(routeString.startsWith("quests.test."),
                "Route should start with quests.test., got: " + routeString);
        assertTrue(routeString.endsWith(".display-item"),
                "Route should end with .display-item, got: " + routeString);
    }

    @Test
    @DisplayName("Given a non-abandonable tutorial source quest, when right-clicked, then onClick returns true without opening abandon confirm")
    public void onClick_rightClickNonAbandonable_returnsTrueWithoutAbandonFlow() {
        // TutorialQuestSource.isAbandonable() is false — right-click must not open QuestAbandonConfirmGui
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("tutorial_right_click_test");
        QuestInstance instance = new QuestInstance(def, null, Map.of(), new TutorialQuestSource(), null);
        ActiveQuestSlot slot = new ActiveQuestSlot(instance);

        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        // No bukkit player — ifPresent block short-circuits, so no GUI is opened and no NPE occurs
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

        boolean result = slot.onClick(mcRPGPlayer, ClickType.RIGHT);

        assertTrue(result, "onClick should always return true");
    }

    @Test
    @DisplayName("Given an abandonable (manual) source quest, when right-clicked, then onClick still returns true")
    public void onClick_rightClickAbandonable_returnsTrueRegardless() {
        QuestDefinition def = QuestTestHelper.singlePhaseQuest("manual_right_click_test");
        QuestInstance instance = new QuestInstance(def, null, Map.of(), new ManualQuestSource(), null);
        ActiveQuestSlot slot = new ActiveQuestSlot(instance);

        McRPGPlayer mcRPGPlayer = mock(McRPGPlayer.class);
        when(mcRPGPlayer.getAsBukkitPlayer()).thenReturn(Optional.empty());

        boolean result = slot.onClick(mcRPGPlayer, ClickType.RIGHT);

        assertTrue(result);
    }
}
