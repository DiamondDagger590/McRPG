package us.eunoians.mcrpg.gui.skill;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.skill.Skill;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillSortTypeTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("Given SkillSortType enum, when values() is called, then it contains exactly three values")
        void values_containsThree() {
            assertEquals(3, SkillSortType.values().length);
        }

        @Test
        @DisplayName("Given SkillSortType enum, when ALPHABETICAL is referenced, then it is non-null")
        void alphabetical_exists() {
            assertNotNull(SkillSortType.ALPHABETICAL);
        }

        @Test
        @DisplayName("Given SkillSortType enum, when SKILL_LEVEL is referenced, then it is non-null")
        void skillLevel_exists() {
            assertNotNull(SkillSortType.SKILL_LEVEL);
        }

        @Test
        @DisplayName("Given SkillSortType enum, when SKILL_EXPERIENCE_TO_LEVEL is referenced, then it is non-null")
        void skillExperienceToLevel_exists() {
            assertNotNull(SkillSortType.SKILL_EXPERIENCE_TO_LEVEL);
        }
    }

    @Nested
    @DisplayName("Linked node chain")
    class LinkedNodeChain {

        @Test
        @DisplayName("Given the sort type chain, when getFirstSortType is called, then it returns non-null")
        void getFirstSortType_returnsNonNull() {
            assertNotNull(SkillSortType.getFirstSortType());
        }

        @Test
        @DisplayName("Given the sort type chain, when getFirstSortType is called, then its value is ALPHABETICAL")
        void getFirstSortType_isAlphabetical() {
            assertEquals(SkillSortType.ALPHABETICAL, SkillSortType.getFirstSortType().getNodeValue());
        }

        @Test
        @DisplayName("Given the sort type chain, when traversed, then it contains all enum values exactly once")
        void chain_containsAllValues() {
            Set<SkillSortType> visited = EnumSet.noneOf(SkillSortType.class);
            LinkedNode<SkillSortType> current = SkillSortType.getFirstSortType();
            int count = 0;
            do {
                visited.add(current.getNodeValue());
                current = current.getNextNode();
                count++;
            } while (current != SkillSortType.getFirstSortType() && count < 100);

            assertEquals(SkillSortType.values().length, visited.size());
            for (SkillSortType type : SkillSortType.values()) {
                assertTrue(visited.contains(type), type + " missing from chain");
            }
        }

        @Test
        @DisplayName("Given the sort type chain, when traversed for the full length, then it forms a closed loop back to the start")
        void chain_formsClosedLoop() {
            LinkedNode<SkillSortType> current = SkillSortType.getFirstSortType();
            int totalValues = SkillSortType.values().length;
            for (int i = 0; i < totalValues; i++) {
                assertTrue(current.hasNext(), "chain broken at index " + i);
                current = current.getNextNode();
            }
            assertEquals(SkillSortType.getFirstSortType(), current);
        }

        @Test
        @DisplayName("Given the sort type chain, when its length is counted, then it equals the enum value count")
        void chain_lengthEqualsValueCount() {
            LinkedNode<SkillSortType> current = SkillSortType.getFirstSortType();
            int count = 0;
            do {
                current = current.getNextNode();
                count++;
            } while (current != SkillSortType.getFirstSortType());
            assertEquals(SkillSortType.values().length, count);
        }

        @Test
        @DisplayName("Given the sort type chain, when traversed twice, then it returns to the start")
        void chain_twoFullTraversals_returnToStart() {
            LinkedNode<SkillSortType> start = SkillSortType.getFirstSortType();
            LinkedNode<SkillSortType> current = start;
            int totalValues = SkillSortType.values().length;
            for (int i = 0; i < totalValues * 2; i++) {
                current = current.getNextNode();
            }
            assertEquals(start, current);
        }

        @Test
        @DisplayName("Given the sort type chain, when each node's next is checked, then every next node is distinct")
        void chain_eachNodeHasDistinctNext() {
            Set<SkillSortType> nextValues = new HashSet<>();
            LinkedNode<SkillSortType> current = SkillSortType.getFirstSortType();
            do {
                SkillSortType nextValue = current.getNextNode().getNodeValue();
                assertTrue(nextValues.add(nextValue), "duplicate next node: " + nextValue);
                current = current.getNextNode();
            } while (current != SkillSortType.getFirstSortType());
        }
    }

    @Nested
    @DisplayName("Chain traversal order")
    class ChainTraversalOrder {

        @Test
        @DisplayName("Given the sort type chain, when traversed from ALPHABETICAL, then it visits all values")
        void chain_startsAtAlphabeticalAndVisitsAll() {
            List<SkillSortType> traversalOrder = new ArrayList<>();
            LinkedNode<SkillSortType> current = SkillSortType.getFirstSortType();
            do {
                traversalOrder.add(current.getNodeValue());
                current = current.getNextNode();
            } while (current != SkillSortType.getFirstSortType());

            assertEquals(SkillSortType.values().length, traversalOrder.size());
            assertEquals(SkillSortType.ALPHABETICAL, traversalOrder.get(0));
        }

        @Test
        @DisplayName("Given ALPHABETICAL in the chain, when getNextNode is called, then it returns SKILL_LEVEL")
        void alphabetical_nextIsSkillLevel() {
            LinkedNode<SkillSortType> current = SkillSortType.getFirstSortType();
            assertEquals(SkillSortType.ALPHABETICAL, current.getNodeValue());
            assertEquals(SkillSortType.SKILL_LEVEL, current.getNextNode().getNodeValue());
        }

        @DisplayName("SKILL_LEVEL -> SKILL_EXPERIENCE_TO_LEVEL in chain")
        @Test
        void skillLevel_nextIsExperienceToLevel() {
            LinkedNode<SkillSortType> current = SkillSortType.getFirstSortType().getNextNode();
            assertEquals(SkillSortType.SKILL_LEVEL, current.getNodeValue());
            assertEquals(SkillSortType.SKILL_EXPERIENCE_TO_LEVEL, current.getNextNode().getNodeValue());
        }

        @DisplayName("SKILL_EXPERIENCE_TO_LEVEL wraps back to ALPHABETICAL")
        @Test
        void experienceToLevel_wrapsToAlphabetical() {
            LinkedNode<SkillSortType> current = SkillSortType.getFirstSortType()
                    .getNextNode()
                    .getNextNode();
            assertEquals(SkillSortType.SKILL_EXPERIENCE_TO_LEVEL, current.getNodeValue());
            assertEquals(SkillSortType.ALPHABETICAL, current.getNextNode().getNodeValue());
        }
    }

    @Nested
    @DisplayName("Filter assignment")
    class FilterAssignment {

        @DisplayName("ALPHABETICAL has no filter")
        @Test
        void alphabetical_hasNoFilter() {
            List<Skill> original = List.of();
            List<Skill> filtered = SkillSortType.ALPHABETICAL.filter(null, original);
            assertEquals(original, filtered);
        }

        @DisplayName("SKILL_LEVEL has a filter applied")
        @Test
        void skillLevel_hasFilter() {
            assertThrows(NullPointerException.class,
                    () -> SkillSortType.SKILL_LEVEL.filter(null, List.of()));
        }

        @DisplayName("SKILL_EXPERIENCE_TO_LEVEL has a filter applied")
        @Test
        void experienceToLevel_hasFilter() {
            assertThrows(NullPointerException.class,
                    () -> SkillSortType.SKILL_EXPERIENCE_TO_LEVEL.filter(null, List.of()));
        }
    }

    @Nested
    @DisplayName("getSlot")
    class GetSlot {

        @DisplayName("every sort type returns a non-null slot")
        @ParameterizedTest
        @EnumSource(SkillSortType.class)
        void getSlot_returnsNonNull(SkillSortType sortType) {
            assertNotNull(sortType.getSlot());
        }
    }
}
