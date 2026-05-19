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
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that every skill's {@code name:} field in {@code en_skills.yml} uses the correct
 * per-skill palette placeholder ({@code <skill-swords>}, {@code <skill-mining>}, etc.) and does
 * not contain deprecated color tags such as {@code <gold>} or raw hex strings.
 * <p>
 * Skill names must use the semantic per-skill placeholder so that server owners can theme each
 * skill independently via the {@code palette:} section in {@code config.yml}. Hardcoded color
 * tags bypass that mechanism and break the palette contract.
 * <p>
 * Specifically verified:
 * <ul>
 *   <li>All skill {@code name:} fields start with the expected {@code <skill-{skillKey}>} placeholder</li>
 *   <li>No skill {@code name:} contains deprecated {@code <gold>} or {@code <red>} tags</li>
 *   <li>Each known bundled skill maps to exactly its own palette placeholder</li>
 * </ul>
 */
class SkillNameColorConsistencyTest {

    private static final Path LOCALE_PATH =
            Path.of("src", "main", "resources", "localization", "english", "en_skills.yml");

    /**
     * The set of YAML keys that are not skill entries in the {@code skills:} section
     * (e.g. the shared {@code level-up} block).
     */
    private static final Set<String> NON_SKILL_KEYS = Set.of("level-up");

    /**
     * Maps each bundled skill YAML key to the palette placeholder its {@code name:} must start with.
     */
    private static final Map<String, String> EXPECTED_PREFIX_BY_SKILL = Map.of(
            "swords", "<skill-swords>",
            "mining", "<skill-mining>",
            "herbalism", "<skill-herbalism>",
            "woodcutting", "<skill-woodcutting>"
    );

    private static YamlDocument document;

    @BeforeAll
    static void loadDocument() throws IOException {
        document = YamlDocument.create(LOCALE_PATH.toFile());
    }

    @Test
    @DisplayName("Every skill name in en_skills.yml starts with its per-skill palette placeholder")
    void allSkillNames_startWithPerSkillPalettePlaceholder() {
        List<String> failures = new ArrayList<>();
        Section skillsSection = document.getSection("skills");
        assertNotNull(skillsSection, "skills section not found in en_skills.yml");

        for (String key : skillsSection.getRoutesAsStrings(false)) {
            if (NON_SKILL_KEYS.contains(key)) {
                continue;
            }
            Section displayItem = skillsSection.getSection(key + ".display-item");
            if (displayItem == null) {
                continue;
            }
            Object nameValue = displayItem.get("name");
            if (!(nameValue instanceof String name)) {
                continue;
            }

            String expectedPrefix = "<skill-" + key + ">";
            if (!name.startsWith(expectedPrefix)) {
                failures.add("Skill '" + key + "' name should start with '" + expectedPrefix + "' but found: '" + name + "'");
            }
        }

        assertTrue(failures.isEmpty(),
                "Skill name color consistency failures:\n" + String.join("\n", failures));
    }

    @Test
    @DisplayName("Each bundled skill uses its exact per-skill palette placeholder")
    void bundledSkills_useCorrectPalettePlaceholder() {
        List<String> failures = new ArrayList<>();
        Section skillsSection = document.getSection("skills");
        assertNotNull(skillsSection, "skills section not found in en_skills.yml");

        for (Map.Entry<String, String> entry : EXPECTED_PREFIX_BY_SKILL.entrySet()) {
            String skillKey = entry.getKey();
            String expectedPrefix = entry.getValue();

            Section displayItem = skillsSection.getSection(skillKey + ".display-item");
            if (displayItem == null) {
                failures.add("Skill '" + skillKey + "' has no display-item section");
                continue;
            }
            Object nameValue = displayItem.get("name");
            if (!(nameValue instanceof String name)) {
                failures.add("Skill '" + skillKey + "' has non-string name");
                continue;
            }
            if (!name.startsWith(expectedPrefix)) {
                failures.add("Skill '" + skillKey + "' should start with '" + expectedPrefix + "' but found: '" + name + "'");
            }
        }

        assertTrue(failures.isEmpty(),
                "Bundled skill palette placeholder failures:\n" + String.join("\n", failures));
    }

    @Test
    @DisplayName("No skill name contains deprecated <gold> tag")
    void noSkillName_containsDeprecatedGoldTag() {
        List<String> failures = new ArrayList<>();
        Section skillsSection = document.getSection("skills");
        assertNotNull(skillsSection, "skills section not found in en_skills.yml");

        for (String key : skillsSection.getRoutesAsStrings(false)) {
            if (NON_SKILL_KEYS.contains(key)) {
                continue;
            }
            Section displayItem = skillsSection.getSection(key + ".display-item");
            if (displayItem == null) {
                continue;
            }
            Object nameValue = displayItem.get("name");
            if (nameValue instanceof String name && name.contains("<gold>")) {
                failures.add("Skill '" + key + "' still uses deprecated <gold> tag: '" + name + "'");
            }
        }

        assertTrue(failures.isEmpty(),
                "Deprecated <gold> tag found in skill names:\n" + String.join("\n", failures));
    }

    @Test
    @DisplayName("No skill name contains deprecated <red> tag")
    void noSkillName_containsDeprecatedRedTag() {
        List<String> failures = new ArrayList<>();
        Section skillsSection = document.getSection("skills");
        assertNotNull(skillsSection, "skills section not found in en_skills.yml");

        for (String key : skillsSection.getRoutesAsStrings(false)) {
            if (NON_SKILL_KEYS.contains(key)) {
                continue;
            }
            Section displayItem = skillsSection.getSection(key + ".display-item");
            if (displayItem == null) {
                continue;
            }
            Object nameValue = displayItem.get("name");
            if (nameValue instanceof String name && name.contains("<red>")) {
                failures.add("Skill '" + key + "' still uses deprecated <red> tag: '" + name + "'");
            }
        }

        assertTrue(failures.isEmpty(),
                "Deprecated <red> tag found in skill names:\n" + String.join("\n", failures));
    }
}
