package us.eunoians.mcrpg.builder.item;

import org.jetbrains.annotations.NotNull;

/**
 * All the different placeholders supported in configuration files
 * to be consumed by {@link AbilityItemBuilder}s.
 */
public enum AbilityItemPlaceholderKeys {
    TIER("tier"),
    SKILL("skill"),
    ABILITY("ability"),
    RANGE("range"),
    RADIUS("radius"),
    COOLDOWN("cooldown"),
    ACTIVATION_CHANCE("activation-chance"),
    EXPANSION("expansion"),
    ABILITY_POINT_COUNT("ability-point-count"),
    DAMAGE("damage"),
    ABILITY_DURATION("ability-duration"),
    ACTIVATION_CHANCE_INCREASE("activation-chance-increase"),
    ADDITIONAL_BLEED_CYCLES("additional-bleed-cycles"),
    BASE_DAMAGE_BOOST("base-damage-boost"),
    BONUS_DAMAGE_CHANCE("bonus-damage-chance"),
    BONUS_DAMAGE("bonus-damage"),
    HEALING_AMOUNT("healing-amount"),
    EXPERIENCE_DROPPED("experience-dropped"),
    MINIMUM_HUNGER("minimum-hunger"),
    ;

    private final String key;

    AbilityItemPlaceholderKeys(@NotNull String key) {
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
