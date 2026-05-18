package us.eunoians.mcrpg.localization;

import com.diamonddagger590.mccore.configuration.ReloadableContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link McRPGLocalizationManager}.
 * <p>
 * The manager is normally registered as a full mock in the test registry (see {@code TestBootstrap}).
 * These tests use {@code CALLS_REAL_METHODS} to exercise concrete methods on the class without
 * starting the full plugin environment.
 */
class McRPGLocalizationManagerTest {

    /**
     * Default palette values mirroring the {@code config.yml} defaults. Used to inject
     * a known palette state into the manager for {@code resolvePaletteColors} tests.
     */
    private static final Map<String, String> DEFAULT_PALETTE;

    static {
        DEFAULT_PALETTE = new LinkedHashMap<>();
        addEntry(DEFAULT_PALETTE, "primary", "<color:#D4A76A>");
        addEntry(DEFAULT_PALETTE, "hint", "<color:#E8C97A>");
        addEntry(DEFAULT_PALETTE, "mana", "<color:#5EA8FF>");
        addEntry(DEFAULT_PALETTE, "ability-active", "<color:#FF7B5E>");
        addEntry(DEFAULT_PALETTE, "ability-passive", "<color:#7FB87F>");
        addEntry(DEFAULT_PALETTE, "ability-innate", "<color:#9E9E9E>");
        addEntry(DEFAULT_PALETTE, "body", "<gray>");
        addEntry(DEFAULT_PALETTE, "positive", "<green>");
        addEntry(DEFAULT_PALETTE, "negative", "<red>");
        addEntry(DEFAULT_PALETTE, "warning", "<yellow>");
    }

    /**
     * Mirrors the open+close tag logic in {@link McRPGLocalizationManager#addPaletteEntry}.
     */
    private static void addEntry(Map<String, String> map, String role, String openValue) {
        map.put("<" + role + ">", openValue);
        map.put("</" + role + ">", "</" + openValue.substring(1));
    }

    private McRPGLocalizationManager manager;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() throws Exception {
        manager = mock(McRPGLocalizationManager.class, CALLS_REAL_METHODS);
        ReloadableContent<Map<String, String>> mockReloadable = mock(ReloadableContent.class);
        when(mockReloadable.getContent()).thenReturn(DEFAULT_PALETTE);
        Field paletteField = McRPGLocalizationManager.class.getDeclaredField("paletteReplacements");
        paletteField.setAccessible(true);
        paletteField.set(manager, mockReloadable);
    }

    @DisplayName("Given a template with a single placeholder, when getLocalizedMessage is called, then the placeholder is replaced")
    @Test
    void getLocalizedMessage_singlePlaceholder_isReplaced() {
        String result = manager.getLocalizedMessage("You gained <amount> XP", Map.of("amount", "500"));

        assertEquals("You gained 500 XP", result);
    }

    @DisplayName("Given a template with multiple placeholders, when getLocalizedMessage is called, then all placeholders are replaced")
    @Test
    void getLocalizedMessage_multiplePlaceholders_allReplaced() {
        String result = manager.getLocalizedMessage("<skill> +<amount> XP", Map.of("skill", "Mining", "amount", "250"));

        assertEquals("Mining +250 XP", result);
    }

    @DisplayName("Given a template with a placeholder not in the map, when getLocalizedMessage is called, then the token is left unchanged")
    @Test
    void getLocalizedMessage_unknownPlaceholder_leftUnchanged() {
        String result = manager.getLocalizedMessage("You gained <amount> <skill> XP", Map.of("amount", "100"));

        assertEquals("You gained 100 <skill> XP", result);
    }

    @DisplayName("Given an empty placeholder map, when getLocalizedMessage is called, then the template is returned unchanged")
    @Test
    void getLocalizedMessage_emptyMap_returnsTemplateUnchanged() {
        String template = "Complete the quest to earn a reward";

        String result = manager.getLocalizedMessage(template, Map.of());

        assertEquals(template, result);
    }

    @DisplayName("Given a template containing MiniMessage color tags, when getLocalizedMessage is called, then the tags are preserved")
    @Test
    void getLocalizedMessage_miniMessageTagsPreserved() {
        String result = manager.getLocalizedMessage("<gold><amount> XP", Map.of("amount", "500"));

        assertEquals("<gold>500 XP", result);
    }

    @DisplayName("Given a string with <primary>, when resolvePaletteColors is called, then it is replaced with the configured value")
    @Test
    void resolvePaletteColors_primary_isReplaced() {
        assertEquals("<color:#D4A76A>Home GUI", manager.resolvePaletteColors("<primary>Home GUI"));
    }

    @DisplayName("Given a string with <hint>, when resolvePaletteColors is called, then it is replaced with the configured value")
    @Test
    void resolvePaletteColors_hint_isReplaced() {
        assertEquals("<color:#E8C97A>Click to open.", manager.resolvePaletteColors("<hint>Click to open."));
    }

    @DisplayName("Given a string with <body>, when resolvePaletteColors is called, then it is replaced with <gray>")
    @Test
    void resolvePaletteColors_body_isReplaced() {
        assertEquals("<gray>Descriptive text.", manager.resolvePaletteColors("<body>Descriptive text."));
    }

    @DisplayName("Given a string with <positive>, when resolvePaletteColors is called, then it is replaced with <green>")
    @Test
    void resolvePaletteColors_positive_isReplaced() {
        assertEquals("<green>Enabled", manager.resolvePaletteColors("<positive>Enabled"));
    }

    @DisplayName("Given a string with <negative>, when resolvePaletteColors is called, then it is replaced with <red>")
    @Test
    void resolvePaletteColors_negative_isReplaced() {
        assertEquals("<red>Disabled", manager.resolvePaletteColors("<negative>Disabled"));
    }

    @DisplayName("Given a string with <warning>, when resolvePaletteColors is called, then it is replaced with <yellow>")
    @Test
    void resolvePaletteColors_warning_isReplaced() {
        assertEquals("<yellow>Expires in 5m", manager.resolvePaletteColors("<warning>Expires in 5m"));
    }

    @DisplayName("Given a string with all 10 palette roles, when resolvePaletteColors is called, then all are replaced")
    @Test
    void resolvePaletteColors_allTenRoles_allReplaced() {
        String input = "<primary><hint><mana><ability-active><ability-passive><ability-innate><body><positive><negative><warning>";
        String expected = "<color:#D4A76A><color:#E8C97A><color:#5EA8FF><color:#FF7B5E><color:#7FB87F><color:#9E9E9E><gray><green><red><yellow>";
        assertEquals(expected, manager.resolvePaletteColors(input));
    }

    @DisplayName("Given a string with an unknown placeholder, when resolvePaletteColors is called, then it passes through unchanged")
    @Test
    void resolvePaletteColors_unknownPlaceholder_passesThrough() {
        String input = "<unknown>Some text";
        assertEquals(input, manager.resolvePaletteColors(input));
    }

    @DisplayName("Given a string with no palette placeholders, when resolvePaletteColors is called, then the string is returned unchanged")
    @Test
    void resolvePaletteColors_noPlaceholders_returnedUnchanged() {
        String input = "Hello, world!";
        assertEquals(input, manager.resolvePaletteColors(input));
    }

    @DisplayName("Given a string with nested palette tags, when resolvePaletteColors is called, then all are replaced in order")
    @Test
    void resolvePaletteColors_nestedPaletteTags_allReplaced() {
        assertEquals("<gray>Mana Cost: <color:#5EA8FF>30", manager.resolvePaletteColors("<body>Mana Cost: <mana>30"));
    }

    @DisplayName("Given an empty string, when resolvePaletteColors is called, then an empty string is returned")
    @Test
    void resolvePaletteColors_emptyString_returnsEmpty() {
        assertEquals("", manager.resolvePaletteColors(""));
    }

    @DisplayName("Given a string that is only a palette placeholder, when resolvePaletteColors is called, then just the replacement value is returned")
    @Test
    void resolvePaletteColors_onlyPlaceholder_returnsReplacementValue() {
        assertEquals("<color:#D4A76A>", manager.resolvePaletteColors("<primary>"));
    }

    @DisplayName("Given a string with a close tag like </primary>, when resolvePaletteColors is called, then it is replaced with the corresponding MiniMessage close tag")
    @Test
    void resolvePaletteColors_closeTag_isReplaced() {
        assertEquals("<color:#D4A76A>50%</color:#D4A76A> towards next level",
                manager.resolvePaletteColors("<primary>50%</primary> towards next level"));
    }

    @DisplayName("Given a string with </body> close tag, when resolvePaletteColors is called, then it resolves to </gray>")
    @Test
    void resolvePaletteColors_bodyCloseTag_isReplaced() {
        assertEquals("<green>Enabled</green> state",
                manager.resolvePaletteColors("<positive>Enabled</positive> state"));
    }
}
