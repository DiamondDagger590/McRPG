package us.eunoians.mcrpg.quest.source.builtin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.util.McRPGMethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Quest source builtins")
public class QuestSourceBuiltinCoverageTest extends McRPGBaseTest {

    @Nested
    @DisplayName("AbilityUpgradeQuestSource")
    class AbilityUpgrade {

        private final AbilityUpgradeQuestSource source = new AbilityUpgradeQuestSource();

        @Test
        @DisplayName("getKey returns ability_upgrade key")
        public void getKey_returnsAbilityUpgradeKey() {
            assertEquals(AbilityUpgradeQuestSource.KEY, source.getKey());
        }

        @Test
        @DisplayName("key namespace is mcrpg")
        public void getKey_namespaceIsMcRPG() {
            assertEquals(McRPGMethods.getMcRPGNamespace(), source.getKey().getNamespace());
        }

        @Test
        @DisplayName("key value is ability_upgrade")
        public void getKey_valueIsAbilityUpgrade() {
            assertEquals("ability_upgrade", source.getKey().getKey());
        }

        @Test
        @DisplayName("isAbandonable returns false")
        public void isAbandonable_returnsFalse() {
            assertFalse(source.isAbandonable());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPG expansion key")
        public void getExpansionKey_returnsMcRPGKey() {
            assertTrue(source.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().orElseThrow());
        }
    }

    @Nested
    @DisplayName("BoardPersonalQuestSource")
    class BoardPersonal {

        private final BoardPersonalQuestSource source = new BoardPersonalQuestSource();

        @Test
        @DisplayName("getKey returns board_personal key")
        public void getKey_returnsBoardPersonalKey() {
            assertEquals(BoardPersonalQuestSource.KEY, source.getKey());
        }

        @Test
        @DisplayName("key value is board_personal")
        public void getKey_valueIsBoardPersonal() {
            assertEquals("board_personal", source.getKey().getKey());
        }

        @Test
        @DisplayName("isAbandonable returns true")
        public void isAbandonable_returnsTrue() {
            assertTrue(source.isAbandonable());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPG expansion key")
        public void getExpansionKey_returnsMcRPGKey() {
            assertTrue(source.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().orElseThrow());
        }
    }

    @Nested
    @DisplayName("BoardLandQuestSource")
    class BoardLand {

        private final BoardLandQuestSource source = new BoardLandQuestSource();

        @Test
        @DisplayName("getKey returns board_land key")
        public void getKey_returnsBoardLandKey() {
            assertEquals(BoardLandQuestSource.KEY, source.getKey());
        }

        @Test
        @DisplayName("key value is board_land")
        public void getKey_valueIsBoardLand() {
            assertEquals("board_land", source.getKey().getKey());
        }

        @Test
        @DisplayName("isAbandonable returns true")
        public void isAbandonable_returnsTrue() {
            assertTrue(source.isAbandonable());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPG expansion key")
        public void getExpansionKey_returnsMcRPGKey() {
            assertTrue(source.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().orElseThrow());
        }
    }

    @Nested
    @DisplayName("ManualQuestSource")
    class Manual {

        private final ManualQuestSource source = new ManualQuestSource();

        @Test
        @DisplayName("getKey returns manual key")
        public void getKey_returnsManualKey() {
            assertEquals(ManualQuestSource.KEY, source.getKey());
        }

        @Test
        @DisplayName("key value is manual")
        public void getKey_valueIsManual() {
            assertEquals("manual", source.getKey().getKey());
        }

        @Test
        @DisplayName("isAbandonable returns false")
        public void isAbandonable_returnsFalse() {
            assertFalse(source.isAbandonable());
        }

        @Test
        @DisplayName("getExpansionKey returns McRPG expansion key")
        public void getExpansionKey_returnsMcRPGKey() {
            assertTrue(source.getExpansionKey().isPresent());
            assertEquals(McRPGExpansion.EXPANSION_KEY, source.getExpansionKey().orElseThrow());
        }
    }

    @Nested
    @DisplayName("Key uniqueness")
    class KeyUniqueness {

        @Test
        @DisplayName("all source keys are unique")
        public void allKeys_areUnique() {
            var keys = java.util.Set.of(
                    AbilityUpgradeQuestSource.KEY,
                    BoardPersonalQuestSource.KEY,
                    BoardLandQuestSource.KEY,
                    ManualQuestSource.KEY
            );
            assertEquals(4, keys.size());
        }

        @Test
        @DisplayName("all source keys have mcrpg namespace")
        public void allKeys_haveMcRPGNamespace() {
            var sources = java.util.List.of(
                    new AbilityUpgradeQuestSource(),
                    new BoardPersonalQuestSource(),
                    new BoardLandQuestSource(),
                    new ManualQuestSource()
            );
            for (var source : sources) {
                assertEquals(McRPGMethods.getMcRPGNamespace(), source.getKey().getNamespace(),
                        "Source " + source.getKey().getKey() + " should have mcrpg namespace");
            }
        }

        @Test
        @DisplayName("all sources have non-null expansion key")
        public void allSources_haveExpansionKey() {
            var sources = java.util.List.of(
                    new AbilityUpgradeQuestSource(),
                    new BoardPersonalQuestSource(),
                    new BoardLandQuestSource(),
                    new ManualQuestSource()
            );
            for (var source : sources) {
                assertNotNull(source.getExpansionKey());
                assertTrue(source.getExpansionKey().isPresent(),
                        "Source " + source.getKey().getKey() + " should have an expansion key");
            }
        }
    }
}
