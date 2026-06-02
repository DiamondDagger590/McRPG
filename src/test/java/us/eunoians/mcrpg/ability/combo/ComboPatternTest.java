package us.eunoians.mcrpg.ability.combo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static us.eunoians.mcrpg.ability.combo.ComboInput.LEFT;
import static us.eunoians.mcrpg.ability.combo.ComboInput.RIGHT;

class ComboPatternTest {

    @Nested
    @DisplayName("Slot index")
    class SlotIndex {

        @DisplayName("SLOT_1 has slot index 1")
        @Test
        void slot1_hasIndex1() {
            assertEquals(1, ComboPattern.SLOT_1.getSlotIndex());
        }

        @DisplayName("SLOT_2 has slot index 2")
        @Test
        void slot2_hasIndex2() {
            assertEquals(2, ComboPattern.SLOT_2.getSlotIndex());
        }

        @DisplayName("SLOT_3 has slot index 3")
        @Test
        void slot3_hasIndex3() {
            assertEquals(3, ComboPattern.SLOT_3.getSlotIndex());
        }
    }

    @Nested
    @DisplayName("Input sequences")
    class InputSequences {

        @DisplayName("SLOT_1 is RIGHT-RIGHT-RIGHT")
        @Test
        void slot1_isRightRightRight() {
            assertEquals(List.of(RIGHT, RIGHT, RIGHT), ComboPattern.SLOT_1.getInputs());
        }

        @DisplayName("SLOT_2 is RIGHT-RIGHT-LEFT")
        @Test
        void slot2_isRightRightLeft() {
            assertEquals(List.of(RIGHT, RIGHT, LEFT), ComboPattern.SLOT_2.getInputs());
        }

        @DisplayName("SLOT_3 is RIGHT-LEFT-RIGHT")
        @Test
        void slot3_isRightLeftRight() {
            assertEquals(List.of(RIGHT, LEFT, RIGHT), ComboPattern.SLOT_3.getInputs());
        }

        @DisplayName("All patterns start with RIGHT")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void allPatterns_startWithRight(ComboPattern pattern) {
            assertEquals(RIGHT, pattern.getInputs().getFirst());
        }
    }

    @Nested
    @DisplayName("getLength")
    class GetLength {

        @DisplayName("All patterns have length 3")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void allPatterns_haveLength3(ComboPattern pattern) {
            assertEquals(3, pattern.getLength());
        }

        @DisplayName("getLength matches getInputs size")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void getLength_matchesInputsSize(ComboPattern pattern) {
            assertEquals(pattern.getInputs().size(), pattern.getLength());
        }
    }

    @Nested
    @DisplayName("isValidPrefix")
    class IsValidPrefix {

        @DisplayName("Single RIGHT is valid prefix for all patterns")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void singleRight_validForAll(ComboPattern pattern) {
            assertTrue(pattern.isValidPrefix(List.of(RIGHT)));
        }

        @DisplayName("Single LEFT is not a valid prefix for any pattern")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void singleLeft_invalidForAll(ComboPattern pattern) {
            assertFalse(pattern.isValidPrefix(List.of(LEFT)));
        }

        @DisplayName("Empty sequence is not a valid prefix")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void emptySequence_notValidPrefix(ComboPattern pattern) {
            assertFalse(pattern.isValidPrefix(List.of()));
        }

        @DisplayName("RIGHT-RIGHT is valid prefix for SLOT_1 and SLOT_2")
        @Test
        void rightRight_validForSlot1And2() {
            List<ComboInput> prefix = List.of(RIGHT, RIGHT);
            assertTrue(ComboPattern.SLOT_1.isValidPrefix(prefix));
            assertTrue(ComboPattern.SLOT_2.isValidPrefix(prefix));
            assertFalse(ComboPattern.SLOT_3.isValidPrefix(prefix));
        }

        @DisplayName("RIGHT-LEFT is valid prefix only for SLOT_3")
        @Test
        void rightLeft_validOnlyForSlot3() {
            List<ComboInput> prefix = List.of(RIGHT, LEFT);
            assertFalse(ComboPattern.SLOT_1.isValidPrefix(prefix));
            assertFalse(ComboPattern.SLOT_2.isValidPrefix(prefix));
            assertTrue(ComboPattern.SLOT_3.isValidPrefix(prefix));
        }

        @DisplayName("Complete sequence is a valid prefix")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void completeSequence_isValidPrefix(ComboPattern pattern) {
            assertTrue(pattern.isValidPrefix(pattern.getInputs()));
        }

        @DisplayName("Sequence longer than pattern is not a valid prefix")
        @Test
        void tooLongSequence_notValidPrefix() {
            assertFalse(ComboPattern.SLOT_1.isValidPrefix(List.of(RIGHT, RIGHT, RIGHT, LEFT)));
        }
    }

    @Nested
    @DisplayName("isCompleteMatch")
    class IsCompleteMatch {

        @DisplayName("Exact input sequence matches the pattern")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void exactSequence_matches(ComboPattern pattern) {
            assertTrue(pattern.isCompleteMatch(pattern.getInputs()));
        }

        @DisplayName("Partial prefix does not match")
        @Test
        void partialPrefix_doesNotMatch() {
            assertFalse(ComboPattern.SLOT_1.isCompleteMatch(List.of(RIGHT, RIGHT)));
        }

        @DisplayName("Empty sequence does not match")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void emptySequence_doesNotMatch(ComboPattern pattern) {
            assertFalse(pattern.isCompleteMatch(List.of()));
        }

        @DisplayName("Wrong pattern does not match")
        @Test
        void wrongPattern_doesNotMatch() {
            assertFalse(ComboPattern.SLOT_1.isCompleteMatch(List.of(RIGHT, RIGHT, LEFT)));
            assertFalse(ComboPattern.SLOT_2.isCompleteMatch(List.of(RIGHT, RIGHT, RIGHT)));
            assertFalse(ComboPattern.SLOT_3.isCompleteMatch(List.of(RIGHT, RIGHT, RIGHT)));
        }

        @DisplayName("Sequence longer than pattern does not match")
        @Test
        void tooLongSequence_doesNotMatch() {
            assertFalse(ComboPattern.SLOT_1.isCompleteMatch(List.of(RIGHT, RIGHT, RIGHT, RIGHT)));
        }

        @DisplayName("Each pattern matches exactly one pattern")
        @Test
        void eachPattern_matchesExactlyOne() {
            List<List<ComboInput>> sequences = List.of(
                    List.of(RIGHT, RIGHT, RIGHT),
                    List.of(RIGHT, RIGHT, LEFT),
                    List.of(RIGHT, LEFT, RIGHT)
            );
            for (ComboPattern pattern : ComboPattern.values()) {
                long matchCount = sequences.stream().filter(pattern::isCompleteMatch).count();
                assertEquals(1, matchCount, pattern + " should match exactly one sequence");
            }
        }
    }

    @Nested
    @DisplayName("forSlot")
    class ForSlot {

        @DisplayName("forSlot(1) returns SLOT_1")
        @Test
        void forSlot1_returnsSlot1() {
            assertEquals(ComboPattern.SLOT_1, ComboPattern.forSlot(1));
        }

        @DisplayName("forSlot(2) returns SLOT_2")
        @Test
        void forSlot2_returnsSlot2() {
            assertEquals(ComboPattern.SLOT_2, ComboPattern.forSlot(2));
        }

        @DisplayName("forSlot(3) returns SLOT_3")
        @Test
        void forSlot3_returnsSlot3() {
            assertEquals(ComboPattern.SLOT_3, ComboPattern.forSlot(3));
        }

        @DisplayName("forSlot(0) returns null")
        @Test
        void forSlot0_returnsNull() {
            assertNull(ComboPattern.forSlot(0));
        }

        @DisplayName("forSlot(4) returns null")
        @Test
        void forSlot4_returnsNull() {
            assertNull(ComboPattern.forSlot(4));
        }

        @DisplayName("forSlot(-1) returns null")
        @Test
        void forSlotNegative_returnsNull() {
            assertNull(ComboPattern.forSlot(-1));
        }

        @DisplayName("forSlot round-trips with getSlotIndex")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void forSlot_roundTripsWithGetSlotIndex(ComboPattern pattern) {
            assertEquals(pattern, ComboPattern.forSlot(pattern.getSlotIndex()));
        }
    }

    @Nested
    @DisplayName("allPatterns")
    class AllPatterns {

        @DisplayName("Returns all three patterns")
        @Test
        void allPatterns_returnsThreePatterns() {
            assertEquals(3, ComboPattern.allPatterns().size());
        }

        @DisplayName("Returns patterns in slot order")
        @Test
        void allPatterns_inSlotOrder() {
            List<ComboPattern> patterns = ComboPattern.allPatterns();
            assertEquals(ComboPattern.SLOT_1, patterns.get(0));
            assertEquals(ComboPattern.SLOT_2, patterns.get(1));
            assertEquals(ComboPattern.SLOT_3, patterns.get(2));
        }

        @DisplayName("Contains all enum values")
        @Test
        void allPatterns_containsAllValues() {
            List<ComboPattern> patterns = ComboPattern.allPatterns();
            for (ComboPattern pattern : ComboPattern.values()) {
                assertTrue(patterns.contains(pattern));
            }
        }
    }

    @Nested
    @DisplayName("getLocalizationKey")
    class GetLocalizationKey {

        @DisplayName("SLOT_1 returns SLOT_1 localization key")
        @Test
        void slot1_returnsCorrectKey() {
            assertEquals(LocalizationKey.LOADOUT_GUI_ACTIVE_COMBO_SLOT_PATTERN_SLOT_1,
                    ComboPattern.SLOT_1.getLocalizationKey());
        }

        @DisplayName("SLOT_2 returns SLOT_2 localization key")
        @Test
        void slot2_returnsCorrectKey() {
            assertEquals(LocalizationKey.LOADOUT_GUI_ACTIVE_COMBO_SLOT_PATTERN_SLOT_2,
                    ComboPattern.SLOT_2.getLocalizationKey());
        }

        @DisplayName("SLOT_3 returns SLOT_3 localization key")
        @Test
        void slot3_returnsCorrectKey() {
            assertEquals(LocalizationKey.LOADOUT_GUI_ACTIVE_COMBO_SLOT_PATTERN_SLOT_3,
                    ComboPattern.SLOT_3.getLocalizationKey());
        }

        @DisplayName("Every pattern has a non-null localization key")
        @ParameterizedTest
        @EnumSource(ComboPattern.class)
        void allPatterns_haveNonNullKey(ComboPattern pattern) {
            assertNotNull(pattern.getLocalizationKey());
        }
    }
}
