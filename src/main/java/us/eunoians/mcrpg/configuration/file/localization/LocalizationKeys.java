package us.eunoians.mcrpg.configuration.file.localization;

import dev.dejvokep.boostedyaml.route.Route;
import us.eunoians.mcrpg.configuration.file.ConfigFile;

/**
 * A java representation of a language configuration file.
 */
public final class LocalizationKeys extends ConfigFile {

    private static final Route LOGIN_HEADER = Route.fromString("login");
    public static final Route LOGIN_UNABLE_TO_LOAD_DATA = Route.addTo(LOGIN_HEADER, "unable-to-load-data");

    private static final Route EXPANSION_HEADER = Route.fromString("expansion");
    public static final Route MCRPG_EXPANSION_NAME = Route.addTo(EXPANSION_HEADER, "mcrpg");

    private static final Route SKILL_HEADER = Route.fromString("skill");
    private static final Route SWORDS_HEADER = Route.addTo(SKILL_HEADER, "swords");

    private static final Route ABILITY_HEADER = Route.fromString("ability");
    public static final Route ABILITY_STILL_ON_COOLDOWN = Route.addTo(ABILITY_HEADER, "ability-still-on-cooldown");
    public static final Route ABILITY_NO_LONGER_ON_COOLDOWN = Route.addTo(ABILITY_HEADER, "ability-no-longer-on-cooldown");

    private static final Route ABILITY_SORT_TYPE_HEADER = Route.addTo(ABILITY_HEADER, "sort-types");
    public static final Route ABILITY_SORT_ALPHABETICAL_DISPLAY_ITEM = Route.addTo(ABILITY_SORT_TYPE_HEADER, "alphabetical.display-item");
    public static final Route ABILITY_SORT_INNATE_ABILITIES_DISPLAY_ITEM = Route.addTo(ABILITY_SORT_TYPE_HEADER, "innate-abilities.display-item");
    public static final Route ABILITY_SORT_SKILL_DISPLAY_ITEM = Route.addTo(ABILITY_SORT_TYPE_HEADER, "skill.display-item");
    public static final Route ABILITY_SORT_UNLOCKED_ABILITIES_DISPLAY_ITEM = Route.addTo(ABILITY_SORT_TYPE_HEADER, "unlocked-abilities.display-item");
    public static final Route ABILITY_SORT_UPGRADABLE_ABILITIES_DISPLAY_ITEM = Route.addTo(ABILITY_SORT_TYPE_HEADER, "upgradable-abilities.display-item");
    public static final Route ABILITY_SORT_PASSIVE_ABILITIES_DISPLAY_ITEM = Route.addTo(ABILITY_SORT_TYPE_HEADER, "passive-abilities.display-item");
    public static final Route ABILITY_SORT_ACTIVE_ABILITIES_DISPLAY_ITEM = Route.addTo(ABILITY_SORT_TYPE_HEADER, "active-abilities.display-item");

    private static final Route ABILITY_SPECIFIC_LOCALIZATION_HEADER = Route.addTo(ABILITY_HEADER, "ability-specific-localization");
    private static final Route BLEED_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "bleed");
    public static final Route BLEED_DISPLAY_ITEM_HEADER = Route.addTo(BLEED_HEADER, "display-item");
    private static final Route DEEPER_WOUND_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "deeper-wound");
    public static final Route DEEPER_WOUND_DISPLAY_ITEM_HEADER = Route.addTo(DEEPER_WOUND_HEADER, "display-item");
    private static final Route ENHANCED_BLEED_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "enhanced-bleed");
    public static final Route ENHANCED_BLEED_DISPLAY_ITEM_HEADER = Route.addTo(ENHANCED_BLEED_HEADER, "display-item");
    private static final Route RAGE_SPIKE_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "rage-spike");
    public static final Route RAGE_SPIKE_DISPLAY_ITEM_HEADER = Route.addTo(RAGE_SPIKE_HEADER, "display-item");
    private static final Route SERRATED_STRIKES_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "serrated-strikes");
    public static final Route SERRATED_STRIKES_DISPLAY_ITEM_HEADER = Route.addTo(SERRATED_STRIKES_HEADER, "display-item");
    private static final Route VAMPIRE_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "vampire");
    public static final Route VAMPIRE_DISPLAY_ITEM_HEADER = Route.addTo(VAMPIRE_HEADER, "display-item");
    private static final Route EXTRA_ORE_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "extra-ore");
    public static final Route EXTRA_ORE_DISPLAY_ITEM_HEADER = Route.addTo(EXTRA_ORE_HEADER, "display-item");
    private static final Route ITS_A_TRIPLE_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "its-a-triple");
    public static final Route ITS_A_TRIPLE_DISPLAY_ITEM_HEADER = Route.addTo(ITS_A_TRIPLE_HEADER, "display-item");
    private static final Route ORE_SCANNER_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "ore-scanner");
    public static final Route ORE_SCANNER_DISPLAY_ITEM_HEADER = Route.addTo(ORE_SCANNER_HEADER, "display-item");
    private static final Route REMOTE_TRANSFER_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "remote-transfer");
    public static final Route REMOTE_TRANSFER_DISPLAY_ITEM_HEADER = Route.addTo(REMOTE_TRANSFER_HEADER, "display-item");
    private static final Route DRYADS_GIFT_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "dryads-gift");
    public static final Route DRYADS_GIFT_DISPLAY_ITEM_HEADER = Route.addTo(DRYADS_GIFT_HEADER, "display-item");
    private static final Route EXTRA_LUMBER_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "extra-lumber");
    public static final Route EXTRA_LUMBER_DISPLAY_ITEM_HEADER = Route.addTo(EXTRA_LUMBER_HEADER, "display-item");
    private static final Route HEAVY_SWING_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "heavy-swing");
    public static final Route HEAVY_SWING_DISPLAY_ITEM_HEADER = Route.addTo(HEAVY_SWING_HEADER, "display-item");
    private static final Route NYMPHS_VITALITY_HEADER = Route.addTo(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "nymphs-vitality");
    public static final Route NYMPHS_VITALITY_DISPLAY_ITEM_HEADER = Route.addTo(NYMPHS_VITALITY_HEADER, "display-item");
}
