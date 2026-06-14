package us.eunoians.mcrpg.quest.board;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.configuration.file.BoardConfigFile;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link QuestBoard} construction, config-backed getters,
 * rotation setters, and reloadable content tracking.
 */
class QuestBoardTest {

    private static final NamespacedKey BOARD_KEY = new NamespacedKey("mcrpg", "default_board");

    private YamlDocument boardConfig;
    private QuestBoard questBoard;

    @BeforeEach
    void setUp() {
        boardConfig = mock(YamlDocument.class);
        when(boardConfig.getInt(BoardConfigFile.MAX_ACCEPTED_QUESTS)).thenReturn(5);
        when(boardConfig.getInt(BoardConfigFile.MINIMUM_TOTAL_OFFERINGS)).thenReturn(12);
        when(boardConfig.getInt(BoardConfigFile.MAX_SCOPED_QUESTS_PER_ENTITY)).thenReturn(3);
        questBoard = new QuestBoard(BOARD_KEY, boardConfig);
    }

    @Nested
    @DisplayName("Construction and getters")
    class ConstructionAndGetters {

        @Test
        @DisplayName("getBoardKey returns the key passed to constructor")
        void getBoardKey_returnsConstructorKey() {
            assertEquals(BOARD_KEY, questBoard.getBoardKey());
        }

        @Test
        @DisplayName("getMaxAcceptedQuests returns config value")
        void getMaxAcceptedQuests_returnsConfigValue() {
            assertEquals(5, questBoard.getMaxAcceptedQuests());
        }

        @Test
        @DisplayName("getMinimumTotalOfferings returns config value")
        void getMinimumTotalOfferings_returnsConfigValue() {
            assertEquals(12, questBoard.getMinimumTotalOfferings());
        }

        @Test
        @DisplayName("getMaxScopedQuestsPerEntity returns config value")
        void getMaxScopedQuestsPerEntity_returnsConfigValue() {
            assertEquals(3, questBoard.getMaxScopedQuestsPerEntity());
        }

        @Test
        @DisplayName("Config value changes are reflected after reloadContent")
        void configChange_reflectedAfterReload() {
            assertEquals(5, questBoard.getMaxAcceptedQuests());

            when(boardConfig.getInt(BoardConfigFile.MAX_ACCEPTED_QUESTS)).thenReturn(10);
            questBoard.getReloadableContent().forEach(
                    com.diamonddagger590.mccore.configuration.ReloadableContent::reloadContent);

            assertEquals(10, questBoard.getMaxAcceptedQuests());
        }
    }

    @Nested
    @DisplayName("Daily rotation")
    class DailyRotation {

        @Test
        @DisplayName("getCurrentDailyRotation returns empty when not set")
        void getCurrentDailyRotation_empty_whenNotSet() {
            assertEquals(Optional.empty(), questBoard.getCurrentDailyRotation());
        }

        @Test
        @DisplayName("setCurrentDailyRotation sets the rotation")
        void setCurrentDailyRotation_setsRotation() {
            BoardRotation rotation = createRotation("daily");

            questBoard.setCurrentDailyRotation(rotation);

            assertTrue(questBoard.getCurrentDailyRotation().isPresent());
            assertSame(rotation, questBoard.getCurrentDailyRotation().orElseThrow());
        }

        @Test
        @DisplayName("setCurrentDailyRotation with null clears rotation")
        void setCurrentDailyRotation_null_clearsRotation() {
            BoardRotation rotation = createRotation("daily");
            questBoard.setCurrentDailyRotation(rotation);

            questBoard.setCurrentDailyRotation(null);

            assertEquals(Optional.empty(), questBoard.getCurrentDailyRotation());
        }

        @Test
        @DisplayName("setCurrentDailyRotation replaces existing rotation")
        void setCurrentDailyRotation_replacesExisting() {
            BoardRotation first = createRotation("daily");
            BoardRotation second = createRotation("daily");

            questBoard.setCurrentDailyRotation(first);
            questBoard.setCurrentDailyRotation(second);

            assertSame(second, questBoard.getCurrentDailyRotation().orElseThrow());
        }
    }

    @Nested
    @DisplayName("Weekly rotation")
    class WeeklyRotation {

        @Test
        @DisplayName("getCurrentWeeklyRotation returns empty when not set")
        void getCurrentWeeklyRotation_empty_whenNotSet() {
            assertEquals(Optional.empty(), questBoard.getCurrentWeeklyRotation());
        }

        @Test
        @DisplayName("setCurrentWeeklyRotation sets the rotation")
        void setCurrentWeeklyRotation_setsRotation() {
            BoardRotation rotation = createRotation("weekly");

            questBoard.setCurrentWeeklyRotation(rotation);

            assertTrue(questBoard.getCurrentWeeklyRotation().isPresent());
            assertSame(rotation, questBoard.getCurrentWeeklyRotation().orElseThrow());
        }

        @Test
        @DisplayName("setCurrentWeeklyRotation replaces existing rotation")
        void setCurrentWeeklyRotation_replacesExisting() {
            BoardRotation first = createRotation("weekly");
            BoardRotation second = createRotation("weekly");

            questBoard.setCurrentWeeklyRotation(first);
            questBoard.setCurrentWeeklyRotation(second);

            assertSame(second, questBoard.getCurrentWeeklyRotation().orElseThrow());
        }

        @Test
        @DisplayName("setCurrentWeeklyRotation with null clears rotation")
        void setCurrentWeeklyRotation_null_clearsRotation() {
            BoardRotation rotation = createRotation("weekly");
            questBoard.setCurrentWeeklyRotation(rotation);

            questBoard.setCurrentWeeklyRotation(null);

            assertEquals(Optional.empty(), questBoard.getCurrentWeeklyRotation());
        }

        @Test
        @DisplayName("Daily and weekly rotations are independent")
        void dailyAndWeekly_areIndependent() {
            BoardRotation daily = createRotation("daily");
            BoardRotation weekly = createRotation("weekly");

            questBoard.setCurrentDailyRotation(daily);
            questBoard.setCurrentWeeklyRotation(weekly);

            assertSame(daily, questBoard.getCurrentDailyRotation().orElseThrow());
            assertSame(weekly, questBoard.getCurrentWeeklyRotation().orElseThrow());
        }
    }

    @Nested
    @DisplayName("Reloadable content")
    class ReloadableContent {

        @Test
        @DisplayName("getReloadableContent returns non-empty set")
        void getReloadableContent_returnsNonEmpty() {
            Set<com.diamonddagger590.mccore.configuration.ReloadableContent<?>> content =
                    questBoard.getReloadableContent();

            assertNotNull(content);
            assertFalse(content.isEmpty());
        }

        @Test
        @DisplayName("getReloadableContent returns exactly 3 entries")
        void getReloadableContent_returnsThreeEntries() {
            assertEquals(3, questBoard.getReloadableContent().size());
        }

        @Test
        @DisplayName("getReloadableContent returns immutable set")
        void getReloadableContent_returnsImmutableSet() {
            Set<com.diamonddagger590.mccore.configuration.ReloadableContent<?>> content =
                    questBoard.getReloadableContent();

            org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                    () -> content.add(null));
        }
    }

    private BoardRotation createRotation(String refreshType) {
        return new BoardRotation(
                UUID.randomUUID(),
                BOARD_KEY,
                new NamespacedKey("mcrpg", refreshType),
                1L,
                1_000_000L,
                1_086_400L
        );
    }
}
