package us.eunoians.mcrpg.localization;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that every string value in {@code en_abilities.yml} parses without error after the
 * default palette replacements have been applied.
 * <p>
 * This test acts as a guard against regressions such as broken MiniMessage close tags
 * that would silently corrupt rendered text in-game. It:
 * <ol>
 *   <li>Loads {@code en_abilities.yml} directly from the resource directory</li>
 *   <li>Applies the default palette replacement map to every string value</li>
 *   <li>Attempts {@link MiniMessage#deserialize(String)} on each resolved string</li>
 *   <li>Fails if any string causes an uncaught exception</li>
 * </ol>
 * <p>
 * Note: MiniMessage is lenient and does not throw on unknown/malformed tags — it renders
 * them as plain text. This test therefore catches hard errors (null returns, unexpected
 * exceptions from custom resolvers) rather than semantic tag mistakes.
 */
class AbilitiesLocaleParseVerificationTest {

    private static final Path LOCALE_PATH =
            Path.of("src", "main", "resources", "localization", "english", "en_abilities.yml");

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
     * Mirrors the open+close tag logic in {@link McRPGLocalizationManager}.
     *
     * @param map       The replacement map to populate.
     * @param role      The palette role name.
     * @param openValue The configured MiniMessage open tag value.
     */
    private static void addEntry(Map<String, String> map, String role, String openValue) {
        map.put("<" + role + ">", openValue);
        map.put("</" + role + ">", "</" + openValue.substring(1));
    }

    private static YamlDocument document;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @BeforeAll
    static void loadDocument() throws IOException {
        document = YamlDocument.create(LOCALE_PATH.toFile());
    }

    @Test
    @DisplayName("Every string in en_abilities.yml parses without exception after palette replacement")
    void allAbilityStrings_parseWithoutException_afterPaletteReplacement() {
        List<String> failures = new ArrayList<>();
        collectStrings(document, "", failures);
        assertTrue(failures.isEmpty(),
                "Locale parse failures in en_abilities.yml:\n" + String.join("\n", failures));
    }

    /**
     * Recursively collects all string and string-list values in a section, applies the default
     * palette replacements, and attempts MiniMessage deserialization. Failures are appended
     * to the {@code failures} list with their full YAML path for identification.
     *
     * @param section  The current YAML section to traverse.
     * @param path     The dot-separated path to this section.
     * @param failures Accumulator for failure messages.
     */
    private void collectStrings(Section section, String path, List<String> failures) {
        for (String key : section.getRoutesAsStrings(false)) {
            Object value = section.get(key);
            String childPath = path.isEmpty() ? key : path + "." + key;

            if (value instanceof Section childSection) {
                collectStrings(childSection, childPath, failures);
            } else if (value instanceof String str) {
                tryParse(childPath, str, failures);
            } else if (value instanceof List<?> list) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) instanceof String str) {
                        tryParse(childPath + "[" + i + "]", str, failures);
                    }
                }
            }
        }
    }

    /**
     * Applies palette replacements to the raw string and then attempts MiniMessage parsing.
     * Records a failure if an unexpected exception is thrown.
     *
     * @param path     The dot-separated YAML path.
     * @param raw      The raw string value.
     * @param failures Accumulator for failure messages.
     */
    private void tryParse(String path, String raw, List<String> failures) {
        String resolved = applyPalette(raw);
        try {
            MINI_MESSAGE.deserialize(resolved);
        } catch (Exception e) {
            failures.add(path + ": '" + resolved + "' threw " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * Applies the default palette replacement map to the given string, mirroring
     * the behavior of {@link McRPGLocalizationManager#resolvePaletteColors(String)}.
     *
     * @param raw The raw string potentially containing palette placeholders.
     * @return The string with all palette placeholders substituted.
     */
    private String applyPalette(String raw) {
        String result = raw;
        for (Map.Entry<String, String> entry : DEFAULT_PALETTE.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
