package us.eunoians.mcrpg.builder.item.skill;

import org.jetbrains.annotations.NotNull;

/**
 * All the different placeholders supported in configuration files
 * to be consumed by {@link SkillItemBuilder}s.
 */
public enum SkillItemPlaceholderKeys {
    SKILL("skill"),
    LEVEL("level"),
    CURRENT_EXPERIENCE("current-experience"),
    EXPERIENCE_TO_LEVEL_UP("experience-to-level-up"),
    ;

    private final String key;

    SkillItemPlaceholderKeys(@NotNull String key) {
        this.key = key;
    }

    /**
     * Gets the placeholder key to be replaced.
     *
     * @return The placeholder key to be replaced.
     */
    @NotNull
    public String getKey() {
        return key;
    }
}
