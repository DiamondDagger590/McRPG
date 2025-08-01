package us.eunoians.mcrpg.command;

import org.jetbrains.annotations.NotNull;

/**
 * An enumeration of all placeholders used by commands for McRPG.
 */
public enum CommandPlaceholders {

    SENDER("sender"),
    TARGET("target"),
    EXPERIENCE("experience"),
    LEVEL("level"),
    SKILL("skill"),
    ABILITY("ability"),
    LOADOUT_SLOT("loadout-slot"),
    ;

    private final String placeholder;

    CommandPlaceholders(@NotNull String placeholder) {
        this.placeholder = placeholder;
    }

    /**
     * Gets the string version of this placeholder to use in messages.
     *
     * @return The string version of this placeholder to use in messages.
     */
    @NotNull
    public String getPlaceholder() {
        return placeholder;
    }
}
