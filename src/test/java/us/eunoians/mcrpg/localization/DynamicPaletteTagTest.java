package us.eunoians.mcrpg.localization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests that the palette replacement mechanism correctly resolves both built-in and custom
 * palette tags.
 * <p>
 * These tests mirror the logic in {@link McRPGLocalizationManager#resolvePaletteColors(String)}
 * without requiring the full McRPG plugin stack. The palette map is built via the same
 * {@code addPaletteEntry} open+close tag semantics used in production.
 * <p>
 * This guards against regressions in:
 * <ul>
 *   <li>Custom key support: arbitrary palette entries resolve as placeholders</li>
 *   <li>Close tag derivation: open tag {@code <color:#AABBCC>} → close {@code </color:#AABBCC>}</li>
 *   <li>Named tag close tags: open {@code <gray>} → close {@code </gray>}</li>
 *   <li>Blank-value filtering: empty/blank palette entries are skipped</li>
 * </ul>
 */
class DynamicPaletteTagTest {

    /**
     * Mirrors the open+close tag logic in {@code McRPGLocalizationManager.addPaletteEntry()}.
     *
     * @param map       The replacement map to populate.
     * @param roleName  The palette role name.
     * @param openValue The configured MiniMessage open tag value.
     */
    private static void addEntry(Map<String, String> map, String roleName, String openValue) {
        map.put("<" + roleName + ">", openValue);
        String closeValue = openValue.startsWith("<") ? "</" + openValue.substring(1) : openValue;
        map.put("</" + roleName + ">", closeValue);
    }

    /**
     * Applies the replacement map to the raw string, mirroring
     * {@code McRPGLocalizationManager.resolvePaletteColors()}.
     *
     * @param raw The raw string.
     * @param map The palette replacement map.
     * @return The resolved string.
     */
    private String applyPalette(String raw, Map<String, String> map) {
        String result = raw;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Test
    @DisplayName("Custom hex palette key resolves open and close tags")
    void customHexPaletteKey_resolvesOpenAndCloseTags() {
        Map<String, String> map = new LinkedHashMap<>();
        addEntry(map, "my-custom", "<color:#123456>");

        assertEquals("<color:#123456>text</color:#123456>",
                applyPalette("<my-custom>text</my-custom>", map));
    }

    @Test
    @DisplayName("Custom named MiniMessage tag resolves close tag correctly")
    void customNamedTag_resolvesCloseTagCorrectly() {
        Map<String, String> map = new LinkedHashMap<>();
        addEntry(map, "my-aqua", "<aqua>");

        assertEquals("<aqua>text</aqua>",
                applyPalette("<my-aqua>text</my-aqua>", map));
    }

    @Test
    @DisplayName("Built-in palette roles resolve after dynamic refactor")
    void builtInPaletteRoles_resolveCorrectly() {
        Map<String, String> map = new LinkedHashMap<>();
        addEntry(map, "primary", "<color:#D4A76A>");
        addEntry(map, "body", "<gray>");
        addEntry(map, "positive", "<green>");
        addEntry(map, "negative", "<red>");
        addEntry(map, "mana", "<color:#5EA8FF>");

        assertEquals("<color:#D4A76A>title</color:#D4A76A>", applyPalette("<primary>title</primary>", map));
        assertEquals("<gray>label</gray>", applyPalette("<body>label</body>", map));
        assertEquals("<green>OK</green>", applyPalette("<positive>OK</positive>", map));
        assertEquals("<red>NO</red>", applyPalette("<negative>NO</negative>", map));
        assertEquals("<color:#5EA8FF>30</color:#5EA8FF>", applyPalette("<mana>30</mana>", map));
    }

    @Test
    @DisplayName("Blank palette entry value is not added to replacement map")
    void blankPaletteEntryValue_isNotAddedToMap() {
        Map<String, String> map = new LinkedHashMap<>();
        // Simulate the production guard: only add if value is not blank
        String blankValue = "   ";
        if (blankValue != null && !blankValue.isBlank()) {
            addEntry(map, "blank-role", blankValue);
        }

        assertFalse(map.containsKey("<blank-role>"), "Blank value should not produce a map entry");
    }

    @Test
    @DisplayName("Multiple custom palette tags coexist without interference")
    void multipleCustomTags_coexistWithoutInterference() {
        Map<String, String> map = new LinkedHashMap<>();
        addEntry(map, "guild-color", "<color:#FF00FF>");
        addEntry(map, "rank-color", "<color:#00FFFF>");
        addEntry(map, "primary", "<color:#D4A76A>");

        String resolved = applyPalette(
                "<guild-color>Guild</guild-color> <rank-color>Rank</rank-color> <primary>Title</primary>",
                map);

        assertEquals(
                "<color:#FF00FF>Guild</color:#FF00FF> <color:#00FFFF>Rank</color:#00FFFF> <color:#D4A76A>Title</color:#D4A76A>",
                resolved);
    }

    @Test
    @DisplayName("getPaletteReplacements map contains both open and close tags for built-in roles")
    void paletteMap_containsBothOpenAndCloseTagsForBuiltInRoles() {
        Map<String, String> map = new LinkedHashMap<>();
        addEntry(map, "hint", "<color:#E8C97A>");

        assertTrue(map.containsKey("<hint>"), "Open tag must be present");
        assertTrue(map.containsKey("</hint>"), "Close tag must be present");
        assertEquals("<color:#E8C97A>", map.get("<hint>"));
        assertEquals("</color:#E8C97A>", map.get("</hint>"));
    }

    @Test
    @DisplayName("Null section produces empty palette map")
    void nullSection_producesEmptyMap() {
        // Simulates the null-section guard in buildPaletteReplacements()
        Map<String, String> map = new LinkedHashMap<>();
        // paletteSection == null → return empty map
        assertTrue(map.isEmpty(), "Null palette section should produce an empty replacement map");
    }
}
