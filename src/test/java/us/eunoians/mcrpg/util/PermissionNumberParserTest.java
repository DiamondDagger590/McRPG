package us.eunoians.mcrpg.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermissionNumberParserTest {

    private static final String PREFIX = "mcrpg.board.extra-slots.";

    @Test
    @DisplayName("Multiple matches returns highest")
    void multipleMatchesReturnsHighest() {
        var permissions = Set.of(
                PREFIX + "1",
                PREFIX + "5",
                PREFIX + "3"
        );
        assertEquals(OptionalInt.of(5), PermissionNumberParser.getHighestNumericSuffix(permissions, PREFIX));
    }

    @Test
    @DisplayName("Single match returns that value")
    void singleMatchReturnsThatValue() {
        var permissions = Set.of(PREFIX + "7");
        assertEquals(OptionalInt.of(7), PermissionNumberParser.getHighestNumericSuffix(permissions, PREFIX));
    }

    @Test
    @DisplayName("No matches returns empty")
    void noMatchesReturnsEmpty() {
        var permissions = Set.of("other.permission.node", "mcrpg.something.else");
        assertTrue(PermissionNumberParser.getHighestNumericSuffix(permissions, PREFIX).isEmpty());
    }

    @Test
    @DisplayName("Non-numeric suffix ignored")
    void nonNumericSuffixIgnored() {
        var permissions = Set.of(PREFIX + "abc", PREFIX + "x10");
        assertTrue(PermissionNumberParser.getHighestNumericSuffix(permissions, PREFIX).isEmpty());
    }

    @Test
    @DisplayName("Empty permission set returns empty")
    void emptyPermissionSetReturnsEmpty() {
        assertTrue(PermissionNumberParser.getHighestNumericSuffix(Set.of(), PREFIX).isEmpty());
    }

    @Test
    @DisplayName("Mixed numeric and non-numeric suffixes")
    void mixedNumericAndNonNumericSuffixes() {
        var permissions = Set.of(
                PREFIX + "1",
                PREFIX + "invalid",
                PREFIX + "10",
                PREFIX + "five"
        );
        assertEquals(OptionalInt.of(10), PermissionNumberParser.getHighestNumericSuffix(permissions, PREFIX));
    }

    @Test
    @DisplayName("Prefix with no matching permissions")
    void prefixWithNoMatchingPermissions() {
        var permissions = Set.of(
                "mcrpg.board.other.5",
                "mcrpg.other.extra-slots.3"
        );
        assertTrue(PermissionNumberParser.getHighestNumericSuffix(permissions, PREFIX).isEmpty());
    }
}
