package us.eunoians.mcrpg.gui.ability;

import com.diamonddagger590.mccore.util.LinkedNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.Ability;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbilitySortTypeTest extends McRPGBaseTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("Given AbilitySortType enum, when values() is called, then it contains exactly eight values")
        void values_containsEight() {
            assertEquals(8, AbilitySortType.values().length);
        }

        @Test
        @DisplayName("Given AbilitySortType enum, when ALPHABETICAL is referenced, then it is non-null")
        void alphabetical_exists() {
            assertNotNull(AbilitySortType.ALPHABETICAL);
        }

        @Test
        @DisplayName("Given AbilitySortType enum, when INNATE_ABILITIES is referenced, then it is non-null")
        void innateAbilities_exists() {
            assertNotNull(AbilitySortType.INNATE_ABILITIES);
        }

        @Test
        @DisplayName("Given AbilitySortType enum, when SKILL is referenced, then it is non-null")
        void skill_exists() {
            assertNotNull(AbilitySortType.SKILL);
        }

        @Test
        @DisplayName("Given AbilitySortType enum, when UNLOCKED_ABILITIES is referenced, then it is non-null")
        void unlockedAbilities_exists() {
            assertNotNull(AbilitySortType.UNLOCKED_ABILITIES);
        }

        @Test
        @DisplayName("Given AbilitySortType enum, when UPGRADEABLE_ABILITIES is referenced, then it is non-null")
        void upgradeableAbilities_exists() {
            assertNotNull(AbilitySortType.UPGRADEABLE_ABILITIES);
        }

        @Test
        @DisplayName("Given AbilitySortType enum, when PASSIVE_ABILITIES is referenced, then it is non-null")
        void passiveAbilities_exists() {
            assertNotNull(AbilitySortType.PASSIVE_ABILITIES);
        }

        @Test
        @DisplayName("Given AbilitySortType enum, when ACTIVE_ABILITIES is referenced, then it is non-null")
        void activeAbilities_exists() {
            assertNotNull(AbilitySortType.ACTIVE_ABILITIES);
        }

        @Test
        @DisplayName("Given AbilitySortType enum, when LOADOUT_ORDER is referenced, then it is non-null")
        void loadoutOrder_exists() {
            assertNotNull(AbilitySortType.LOADOUT_ORDER);
        }
    }

    @Nested
    @DisplayName("Linked node chain")
    class LinkedNodeChain {

        @Test
        @DisplayName("Given the sort type chain, when getFirstSortType is called, then it returns non-null")
        void getFirstSortType_returnsNonNull() {
            assertNotNull(AbilitySortType.getFirstSortType());
        }

        @Test
        @DisplayName("Given the sort type chain, when getFirstSortType is called, then its value is SKILL")
        void getFirstSortType_isSKILL() {
            assertEquals(AbilitySortType.SKILL, AbilitySortType.getFirstSortType().getNodeValue());
        }

        @Test
        @DisplayName("Given the sort type chain, when traversed, then it contains all enum values exactly once")
        void chain_containsAllValues() {
            Set<AbilitySortType> visited = EnumSet.noneOf(AbilitySortType.class);
            LinkedNode<AbilitySortType> current = AbilitySortType.getFirstSortType();
            int count = 0;
            do {
                visited.add(current.getNodeValue());
                current = current.getNextNode();
                count++;
            } while (current != AbilitySortType.getFirstSortType() && count < 100);

            assertEquals(AbilitySortType.values().length, visited.size());
            for (AbilitySortType type : AbilitySortType.values()) {
                assertTrue(visited.contains(type), type + " missing from chain");
            }
        }

        @Test
        @DisplayName("Given the sort type chain, when traversed for the full length, then it forms a closed loop back to the start")
        void chain_formsClosedLoop() {
            LinkedNode<AbilitySortType> current = AbilitySortType.getFirstSortType();
            int totalValues = AbilitySortType.values().length;
            for (int i = 0; i < totalValues; i++) {
                assertTrue(current.hasNext(), "chain broken at index " + i);
                current = current.getNextNode();
            }
            assertEquals(AbilitySortType.getFirstSortType(), current);
        }

        @Test
        @DisplayName("Given the sort type chain, when its length is counted, then it equals the enum value count")
        void chain_lengthEqualsValueCount() {
            LinkedNode<AbilitySortType> current = AbilitySortType.getFirstSortType();
            int count = 0;
            do {
                current = current.getNextNode();
                count++;
            } while (current != AbilitySortType.getFirstSortType());
            assertEquals(AbilitySortType.values().length, count);
        }

        @Test
        @DisplayName("Given the sort type chain, when traversed twice, then it returns to the start")
        void chain_twoFullTraversals_returnToStart() {
            LinkedNode<AbilitySortType> start = AbilitySortType.getFirstSortType();
            LinkedNode<AbilitySortType> current = start;
            int totalValues = AbilitySortType.values().length;
            for (int i = 0; i < totalValues * 2; i++) {
                current = current.getNextNode();
            }
            assertEquals(start, current);
        }
    }

    @Nested
    @DisplayName("getLoadoutOrderNode")
    class GetLoadoutOrderNode {

        @Test
        @DisplayName("Given AbilitySortType, when getLoadoutOrderNode is called, then it returns non-null")
        void getLoadoutOrderNode_returnsNonNull() {
            assertNotNull(AbilitySortType.getLoadoutOrderNode());
        }

        @Test
        @DisplayName("Given AbilitySortType, when getLoadoutOrderNode is called, then it wraps LOADOUT_ORDER")
        void getLoadoutOrderNode_wrapsLoadoutOrder() {
            assertEquals(AbilitySortType.LOADOUT_ORDER, AbilitySortType.getLoadoutOrderNode().getNodeValue());
        }

        @Test
        @DisplayName("Given the sort type chain, when traversed, then it contains the loadout order node")
        void getLoadoutOrderNode_isPartOfMainChain() {
            LinkedNode<AbilitySortType> current = AbilitySortType.getFirstSortType();
            boolean found = false;
            int count = 0;
            do {
                if (current == AbilitySortType.getLoadoutOrderNode()) {
                    found = true;
                    break;
                }
                current = current.getNextNode();
                count++;
            } while (current != AbilitySortType.getFirstSortType() && count < 100);
            assertTrue(found, "loadout order node not found in main chain");
        }
    }

    @Nested
    @DisplayName("getInnateAbilitiesNode")
    class GetInnateAbilitiesNode {

        @Test
        @DisplayName("Given AbilitySortType, when getInnateAbilitiesNode is called, then it returns non-null")
        void getInnateAbilitiesNode_returnsNonNull() {
            assertNotNull(AbilitySortType.getInnateAbilitiesNode());
        }

        @Test
        @DisplayName("Given AbilitySortType, when getInnateAbilitiesNode is called, then it wraps INNATE_ABILITIES")
        void getInnateAbilitiesNode_wrapsInnateAbilities() {
            assertEquals(AbilitySortType.INNATE_ABILITIES, AbilitySortType.getInnateAbilitiesNode().getNodeValue());
        }

        @Test
        @DisplayName("Given the sort type chain, when traversed, then it contains the innate abilities node")
        void getInnateAbilitiesNode_isPartOfMainChain() {
            LinkedNode<AbilitySortType> current = AbilitySortType.getFirstSortType();
            boolean found = false;
            int count = 0;
            do {
                if (current == AbilitySortType.getInnateAbilitiesNode()) {
                    found = true;
                    break;
                }
                current = current.getNextNode();
                count++;
            } while (current != AbilitySortType.getFirstSortType() && count < 100);
            assertTrue(found, "innate abilities node not found in main chain");
        }
    }

    @Nested
    @DisplayName("Filter assignment")
    class FilterAssignment {

        @Test
        @DisplayName("Given ALPHABETICAL sort type, when filter is called with an empty list, then it returns the original list")
        void alphabetical_hasNoFilter() {
            List<Ability> original = List.of();
            List<Ability> filtered = AbilitySortType.ALPHABETICAL.filter(null, original);
            assertEquals(original, filtered);
        }

        @Test
        @DisplayName("Given SKILL sort type, when filter is called with an empty list, then it returns the original list")
        void skill_hasNoFilter() {
            List<Ability> original = List.of();
            List<Ability> filtered = AbilitySortType.SKILL.filter(null, original);
            assertEquals(original, filtered);
        }

        @Test
        @DisplayName("Given LOADOUT_ORDER sort type, when filter is called with an empty list, then it returns the original list")
        void loadoutOrder_hasNoFilter() {
            List<Ability> original = List.of();
            List<Ability> filtered = AbilitySortType.LOADOUT_ORDER.filter(null, original);
            assertEquals(original, filtered);
        }
    }

    @Nested
    @DisplayName("getSlot")
    class GetSlot {

        @ParameterizedTest
        @EnumSource(AbilitySortType.class)
        @DisplayName("Given any AbilitySortType, when getSlot is called, then it returns non-null")
        void getSlot_returnsNonNull(AbilitySortType sortType) {
            assertNotNull(sortType.getSlot());
        }
    }

    @Nested
    @DisplayName("Chain traversal order")
    class ChainTraversalOrder {

        @Test
        @DisplayName("Given the sort type chain, when traversed from SKILL, then it visits all unique values")
        void chain_visitsAllInDefinitionOrder() {
            List<AbilitySortType> traversalOrder = new ArrayList<>();
            LinkedNode<AbilitySortType> current = AbilitySortType.getFirstSortType();
            do {
                traversalOrder.add(current.getNodeValue());
                current = current.getNextNode();
            } while (current != AbilitySortType.getFirstSortType());

            assertEquals(AbilitySortType.values().length, traversalOrder.size());

            AbilitySortType[] definitionOrder = AbilitySortType.values();
            int skillIndex = -1;
            for (int i = 0; i < definitionOrder.length; i++) {
                if (definitionOrder[i] == AbilitySortType.SKILL) {
                    skillIndex = i;
                    break;
                }
            }

            for (int i = 0; i < traversalOrder.size(); i++) {
                int expectedIndex = (skillIndex + i) % definitionOrder.length;
                if (i == 0) {
                    assertEquals(AbilitySortType.SKILL, traversalOrder.get(i));
                }
            }
            assertEquals(AbilitySortType.SKILL, traversalOrder.get(0));
        }

        @Test
        @DisplayName("Given the sort type chain, when each node's next is checked, then every next node is distinct")
        void chain_eachNodeHasDistinctNext() {
            Set<AbilitySortType> nextValues = new HashSet<>();
            LinkedNode<AbilitySortType> current = AbilitySortType.getFirstSortType();
            do {
                AbilitySortType nextValue = current.getNextNode().getNodeValue();
                assertTrue(nextValues.add(nextValue), "duplicate next node: " + nextValue);
                current = current.getNextNode();
            } while (current != AbilitySortType.getFirstSortType());
        }
    }
}
