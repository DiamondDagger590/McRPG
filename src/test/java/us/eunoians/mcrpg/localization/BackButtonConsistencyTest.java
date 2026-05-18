package us.eunoians.mcrpg.localization;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that every {@code previous-gui-button} entry in {@code en_gui.yml}
 * follows the standardized back-button pattern defined in the Phase 1 LLD.
 * <p>
 * Rules enforced:
 * <ul>
 *   <li>The {@code name} value must start with {@code <primary>Back to}</li>
 *   <li>The {@code material} value must be {@code BARRIER}</li>
 *   <li>The {@code lore} value must be a YAML list (not a scalar string)</li>
 * </ul>
 */
class BackButtonConsistencyTest {

    private static final Path LOCALE_PATH =
            Path.of("src", "main", "resources", "localization", "english", "en_gui.yml");
    private static final String BACK_BUTTON_KEY = "previous-gui-button";
    private static final String EXPECTED_NAME_PREFIX = "<primary>Back to";
    private static final String EXPECTED_MATERIAL = "BARRIER";

    private static YamlDocument document;

    @BeforeAll
    static void loadDocument() throws IOException {
        document = YamlDocument.create(LOCALE_PATH.toFile());
    }

    @Test
    @DisplayName("Every previous-gui-button name starts with '<primary>Back to'")
    void allBackButtons_nameStartsWithPrimaryBackTo() {
        List<String> failures = new ArrayList<>();
        collectBackButtonFailures(document, "", failures);
        assertTrue(failures.isEmpty(),
                "Back button naming/material/lore violations:\n" + String.join("\n", failures));
    }

    /**
     * Recursively walks all sections in the document looking for keys named
     * {@value #BACK_BUTTON_KEY} and validates the three back-button rules.
     *
     * @param section   The current section being inspected.
     * @param path      The dot-separated path to the current section (for error messages).
     * @param failures  Accumulator for human-readable failure messages.
     */
    private void collectBackButtonFailures(Section section, String path, List<String> failures) {
        for (String key : section.getRoutesAsStrings(false)) {
            Object value = section.get(key);
            String childPath = path.isEmpty() ? key : path + "." + key;

            if (value instanceof Section childSection) {
                if (key.equals(BACK_BUTTON_KEY)) {
                    validateBackButton(childSection, childPath, failures);
                } else {
                    collectBackButtonFailures(childSection, childPath, failures);
                }
            }
        }
    }

    /**
     * Validates a single {@code previous-gui-button} section against the three rules.
     * <p>
     * Two layouts are supported:
     * <ul>
     *   <li><b>Direct:</b> {@code previous-gui-button.display-item} — the {@code display-item}
     *       section lives directly under the button key.</li>
     *   <li><b>Context-variant:</b> {@code previous-gui-button.from-board.display-item} — the
     *       button key contains named sub-context sections, each of which holds its own
     *       {@code display-item}. Each variant is validated independently.</li>
     * </ul>
     *
     * @param section  The button section to validate.
     * @param path     The dot-separated path (used in error messages).
     * @param failures Accumulator for failure messages.
     */
    private void validateBackButton(Section section, String path, List<String> failures) {
        Object directDisplayItem = section.get("display-item");
        if (directDisplayItem instanceof Section displayItem) {
            validateDisplayItem(displayItem, path + ".display-item", failures);
            return;
        }
        // Context-variant layout: each child is a named context (e.g. from-board, from-history)
        // containing its own display-item section.
        for (String contextKey : section.getRoutesAsStrings(false)) {
            Object contextObj = section.get(contextKey);
            if (contextObj instanceof Section contextSection) {
                Object contextDisplayItem = contextSection.get("display-item");
                if (contextDisplayItem instanceof Section displayItem) {
                    validateDisplayItem(displayItem, path + "." + contextKey + ".display-item", failures);
                }
            }
        }
    }

    /**
     * Validates a resolved {@code display-item} section (name, material, lore rules).
     *
     * @param displayItem The display-item section.
     * @param path        The full dot-separated path to this section (used in error messages).
     * @param failures    Accumulator for failure messages.
     */
    private void validateDisplayItem(Section displayItem, String path, List<String> failures) {
        String name = displayItem.getString("name", "");
        if (!name.startsWith(EXPECTED_NAME_PREFIX)) {
            failures.add(path + ".name: expected to start with '"
                    + EXPECTED_NAME_PREFIX + "' but was: '" + name + "'");
        }

        String material = displayItem.getString("material", "");
        if (!EXPECTED_MATERIAL.equals(material)) {
            failures.add(path + ".material: expected '" + EXPECTED_MATERIAL
                    + "' but was: '" + material + "'");
        }

        Object lore = displayItem.get("lore");
        if (lore != null && !(lore instanceof List)) {
            failures.add(path + ".lore: expected a YAML list but found a scalar: '" + lore + "'");
        }
    }
}
