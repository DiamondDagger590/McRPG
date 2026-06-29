package us.eunoians.mcrpg.builder.item.skill;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillItemPlaceholderKeysTest {

    @Nested
    @DisplayName("Enum values")
    class EnumValues {

        @Test
        @DisplayName("Given SkillItemPlaceholderKeys enum, when values() is called, then it contains exactly five values")
        void values_containsFive() {
            assertEquals(5, SkillItemPlaceholderKeys.values().length);
        }

        @Test
        @DisplayName("Given SkillItemPlaceholderKeys enum, when SKILL is referenced, then it is non-null")
        void skill_exists() {
            assertNotNull(SkillItemPlaceholderKeys.SKILL);
        }

        @Test
        @DisplayName("Given SkillItemPlaceholderKeys enum, when LEVEL is referenced, then it is non-null")
        void level_exists() {
            assertNotNull(SkillItemPlaceholderKeys.LEVEL);
        }

        @Test
        @DisplayName("Given SkillItemPlaceholderKeys enum, when CURRENT_EXPERIENCE is referenced, then it is non-null")
        void currentExperience_exists() {
            assertNotNull(SkillItemPlaceholderKeys.CURRENT_EXPERIENCE);
        }

        @Test
        @DisplayName("Given SkillItemPlaceholderKeys enum, when REQUIRED_EXPERIENCE_TO_LEVEL_UP is referenced, then it is non-null")
        void requiredExperienceToLevelUp_exists() {
            assertNotNull(SkillItemPlaceholderKeys.REQUIRED_EXPERIENCE_TO_LEVEL_UP);
        }

        @Test
        @DisplayName("Given SkillItemPlaceholderKeys enum, when REMAINING_EXPERIENCE_TO_LEVEL_UP is referenced, then it is non-null")
        void remainingExperienceToLevelUp_exists() {
            assertNotNull(SkillItemPlaceholderKeys.REMAINING_EXPERIENCE_TO_LEVEL_UP);
        }
    }

    @Nested
    @DisplayName("getKey")
    class GetKey {

        @Test
        @DisplayName("Given SKILL placeholder, when getKey is called, then it returns 'skill'")
        void skill_hasExpectedKey() {
            assertEquals("skill", SkillItemPlaceholderKeys.SKILL.getKey());
        }

        @Test
        @DisplayName("Given LEVEL placeholder, when getKey is called, then it returns 'level'")
        void level_hasExpectedKey() {
            assertEquals("level", SkillItemPlaceholderKeys.LEVEL.getKey());
        }

        @Test
        @DisplayName("Given CURRENT_EXPERIENCE placeholder, when getKey is called, then it returns 'current-experience'")
        void currentExperience_hasExpectedKey() {
            assertEquals("current-experience", SkillItemPlaceholderKeys.CURRENT_EXPERIENCE.getKey());
        }

        @Test
        @DisplayName("Given REQUIRED_EXPERIENCE_TO_LEVEL_UP placeholder, when getKey is called, then it returns 'required-experience-to-level-up'")
        void requiredExperienceToLevelUp_hasExpectedKey() {
            assertEquals("required-experience-to-level-up", SkillItemPlaceholderKeys.REQUIRED_EXPERIENCE_TO_LEVEL_UP.getKey());
        }

        @DisplayName("REMAINING_EXPERIENCE_TO_LEVEL_UP key is 'remaining-experience-to-level-up'")
        @Test
        void remainingExperienceToLevelUp_hasExpectedKey() {
            assertEquals("remaining-experience-to-level-up", SkillItemPlaceholderKeys.REMAINING_EXPERIENCE_TO_LEVEL_UP.getKey());
        }

        @DisplayName("every key is non-null and non-empty")
        @ParameterizedTest
        @EnumSource(SkillItemPlaceholderKeys.class)
        void allKeys_areNonNullAndNonEmpty(SkillItemPlaceholderKeys key) {
            assertNotNull(key.getKey());
            assertFalse(key.getKey().isEmpty());
        }

        @DisplayName("all keys are unique")
        @Test
        void allKeys_areUnique() {
            Set<String> keys = new HashSet<>();
            for (SkillItemPlaceholderKeys placeholderKey : SkillItemPlaceholderKeys.values()) {
                assertTrue(keys.add(placeholderKey.getKey()),
                        "duplicate key: " + placeholderKey.getKey());
            }
        }

        @DisplayName("all keys use kebab-case format")
        @ParameterizedTest
        @EnumSource(SkillItemPlaceholderKeys.class)
        void allKeys_useKebabCase(SkillItemPlaceholderKeys key) {
            assertTrue(key.getKey().matches("[a-z][a-z0-9-]*"),
                    key.getKey() + " does not match kebab-case pattern");
        }
    }
}
