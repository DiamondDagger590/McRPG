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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that every ability's {@code name:} in {@code en_abilities.yml} uses one of the
 * correct palette type color placeholders ({@code <ability-active>}, {@code <ability-passive>},
 * or {@code <ability-innate>}) and does not contain deprecated or raw color tags.
 * <p>
 * Ability names must use the semantic type placeholder that matches the ability's classification
 * ({@code ComboActivatable} → {@code <ability-active>}; tierable passive → {@code <ability-passive>};
 * innate/always-on → {@code <ability-innate>}) rather than hardcoded tags like {@code <red>} or
 * {@code <gold>}. This test acts as a CI guard so that newly-added or modified abilities cannot
 * ship with incorrect colors.
 * <p>
 * Specifically verified:
 * <ul>
 *   <li>All ability {@code name:} fields start with an approved type placeholder</li>
 *   <li>No ability {@code name:} contains deprecated {@code <red>} or {@code <gold>} tags</li>
 *   <li>The five innate abilities (Bleed, Extra Ore, Extra Lumber, Too Many Plants,
 *       Instant Irrigation) use {@code <ability-innate>}</li>
 * </ul>
 */
class AbilityNameColorConsistencyTest {

    private static final Path LOCALE_PATH =
            Path.of("src", "main", "resources", "localization", "english", "en_abilities.yml");

    private static final Set<String> APPROVED_PREFIXES = Set.of(
            "<ability-active>",
            "<ability-passive>",
            "<ability-innate>"
    );

    private static final Set<String> INNATE_ABILITY_KEYS = Set.of(
            "bleed",
            "extra-ore",
            "extra-lumber",
            "too-many-plants",
            "instant-irrigation"
    );

    private static final Set<String> ACTIVE_ABILITY_KEYS = Set.of(
            "rage-spike",
            "serrated-strikes",
            "ore-scanner",
            "verdant-surge",
            "mass-harvest"
    );

    private static YamlDocument document;

    @BeforeAll
    static void loadDocument() throws IOException {
        document = YamlDocument.create(LOCALE_PATH.toFile());
    }

    @Test
    @DisplayName("Every ability name in en_abilities.yml starts with an approved type palette placeholder")
    void allAbilityNames_startWithApprovedTypePlaceholder() {
        List<String> failures = new ArrayList<>();
        Section abilitySection = document.getSection("ability.ability-specific-localization");
        if (abilitySection == null) {
            failures.add("ability.ability-specific-localization section not found in en_abilities.yml");
            assertTrue(failures.isEmpty(), String.join("\n", failures));
            return;
        }

        for (String abilityKey : abilitySection.getRoutesAsStrings(false)) {
            Section displayItem = abilitySection.getSection(abilityKey + ".display-item");
            if (displayItem == null) {
                continue;
            }
            Object nameValue = displayItem.get("name");
            if (!(nameValue instanceof String name)) {
                continue;
            }

            boolean hasApprovedPrefix = APPROVED_PREFIXES.stream().anyMatch(name::startsWith);
            if (!hasApprovedPrefix) {
                failures.add("Ability '" + abilityKey + "' name does not start with an approved type placeholder. Found: '" + name + "'");
            }

            if (name.contains("<red>") || name.contains("<gold>")) {
                failures.add("Ability '" + abilityKey + "' name contains deprecated color tag. Found: '" + name + "'");
            }
        }

        assertTrue(failures.isEmpty(),
                "Ability name color consistency failures:\n" + String.join("\n", failures));
    }

    @Test
    @DisplayName("Innate abilities use <ability-innate> color in their name")
    void innateAbilities_useInnateColor() {
        List<String> failures = new ArrayList<>();
        Section abilitySection = document.getSection("ability.ability-specific-localization");
        if (abilitySection == null) {
            failures.add("ability.ability-specific-localization section not found");
            assertTrue(failures.isEmpty(), String.join("\n", failures));
            return;
        }

        for (String innateKey : INNATE_ABILITY_KEYS) {
            Section displayItem = abilitySection.getSection(innateKey + ".display-item");
            if (displayItem == null) {
                failures.add("Innate ability '" + innateKey + "' has no display-item section");
                continue;
            }
            Object nameValue = displayItem.get("name");
            if (!(nameValue instanceof String name)) {
                failures.add("Innate ability '" + innateKey + "' has non-string name");
                continue;
            }
            if (!name.startsWith("<ability-innate>")) {
                failures.add("Innate ability '" + innateKey + "' should use <ability-innate> but found: '" + name + "'");
            }
        }

        assertTrue(failures.isEmpty(),
                "Innate ability color failures:\n" + String.join("\n", failures));
    }

    @Test
    @DisplayName("Active abilities use <ability-active> color in their name")
    void activeAbilities_useActiveColor() {
        List<String> failures = new ArrayList<>();
        Section abilitySection = document.getSection("ability.ability-specific-localization");
        if (abilitySection == null) {
            failures.add("ability.ability-specific-localization section not found");
            assertTrue(failures.isEmpty(), String.join("\n", failures));
            return;
        }

        for (String activeKey : ACTIVE_ABILITY_KEYS) {
            Section displayItem = abilitySection.getSection(activeKey + ".display-item");
            if (displayItem == null) {
                failures.add("Active ability '" + activeKey + "' has no display-item section");
                continue;
            }
            Object nameValue = displayItem.get("name");
            if (!(nameValue instanceof String name)) {
                failures.add("Active ability '" + activeKey + "' has non-string name");
                continue;
            }
            if (!name.startsWith("<ability-active>")) {
                failures.add("Active ability '" + activeKey + "' should use <ability-active> but found: '" + name + "'");
            }
        }

        assertTrue(failures.isEmpty(),
                "Active ability color failures:\n" + String.join("\n", failures));
    }

    @Test
    @DisplayName("No ability name contains deprecated <red> tag")
    void noAbilityName_containsDeprecatedRedTag() {
        List<String> failures = new ArrayList<>();
        Section abilitySection = document.getSection("ability.ability-specific-localization");
        if (abilitySection == null) {
            assertTrue(false, "ability.ability-specific-localization section not found");
            return;
        }

        for (String abilityKey : abilitySection.getRoutesAsStrings(false)) {
            Section displayItem = abilitySection.getSection(abilityKey + ".display-item");
            if (displayItem == null) {
                continue;
            }
            Object nameValue = displayItem.get("name");
            if (nameValue instanceof String name && name.contains("<red>")) {
                failures.add("Ability '" + abilityKey + "' still uses deprecated <red> tag: '" + name + "'");
            }
        }

        assertTrue(failures.isEmpty(),
                "Deprecated <red> tag found in ability names:\n" + String.join("\n", failures));
    }
}
