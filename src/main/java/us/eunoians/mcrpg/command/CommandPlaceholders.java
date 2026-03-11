package us.eunoians.mcrpg.command;

import org.jetbrains.annotations.NotNull;

/**
 * An enumeration of all placeholders used by commands for McRPG.
 */
public enum CommandPlaceholders {

    SENDER("sender"),
    TARGET("target"),
    EXPERIENCE("experience"),
    REDEEMABLE_EXPERIENCE("redeemable-experience"),
    REDEEMABLE_LEVELS("redeemable-levels"),
    RESTED_EXPERIENCE("rested-experience"),
    BOOSTED_EXPERIENCE("boosted-experience"),
    REDEEMED_EXPERIENCE("redeemed-experience"),
    REDEEMED_LEVELS("redeemed-levels"),
    LEVEL("level"),
    SKILL("skill"),
    ABILITY("ability"),
    LOADOUT_SLOT("loadout-slot"),
    LOADOUT_MATCHES("loadout-matches"),
    UPGRADE_POINTS("upgrade-points"),
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
