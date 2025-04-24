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

    private static final Route DISPLAY_HEADER = Route.from("experience-display");
    public static final Route ACTION_BAR_DISPLAY_MESSAGE = Route.addTo(DISPLAY_HEADER, "action-bar-display-message");
    public static final Route BOSS_BAR_DISPLAY_MESSAGE = Route.addTo(DISPLAY_HEADER, "boss-bar-display-message");

    private static final Route SKILL_HEADER = Route.fromString("skill");
    private static final Route SWORDS_HEADER = Route.addTo(SKILL_HEADER, "swords");

    private static final Route GUI_HEADER = Route.fromString("gui");

    private static final Route REMOTE_TRANSFER_GUI_HEADER = Route.addTo(GUI_HEADER, "remote-transfer-gui");
    public static final Route REMOTE_TRANSFER_GUI_TITLE = Route.addTo(REMOTE_TRANSFER_GUI_HEADER, "title");
    public static final Route REMOTE_TRANSFER_GUI_CATEGORIES_HEADER = Route.addTo(REMOTE_TRANSFER_GUI_HEADER, "categories");
    public static final Route REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_ENABLED_DISPLAY_ITEM = Route.addTo(REMOTE_TRANSFER_GUI_HEADER, "category-item-option.option-enabled.display-item");
    public static final Route REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_DISABLED_DISPLAY_ITEM = Route.addTo(REMOTE_TRANSFER_GUI_HEADER, "category-item-option.option-disabled.display-item");
    public static final Route REMOTE_TRANSFER_GUI_NEXT_BUTTON_DISPLAY_ITEM = Route.addTo(REMOTE_TRANSFER_GUI_HEADER, "next-button.display-item");
    public static final Route REMOTE_TRANSFER_GUI_PREVIOUS_BUTTON_DISPLAY_ITEM = Route.addTo(REMOTE_TRANSFER_GUI_HEADER, "previous-button.display-item");
    public static final Route REMOTE_TRANSFER_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.addTo(REMOTE_TRANSFER_GUI_HEADER, "previous-gui-button.display-item");
    public static final Route REMOTE_TRANSFER_GUI_FILLER_ITEM_DISPLAY_ITEM = Route.addTo(REMOTE_TRANSFER_GUI_HEADER, "filler-item.display-item");

    private static final Route ABILITY_EDIT_GUI_HEADER = Route.addTo(GUI_HEADER, "ability-edit-gui");
    public static final Route ABILITY_EDIT_GUI_TITLE = Route.addTo(ABILITY_EDIT_GUI_HEADER, "title");
    private static final Route LOCATION_ATTRIBUTE_HEADER = Route.addTo(ABILITY_EDIT_GUI_HEADER, "location-attribute");
    public static final Route LOCATION_ATTRIBUTE_NO_LOCATION_SAVED_DISPLAY_ITEM = Route.addTo(LOCATION_ATTRIBUTE_HEADER, "no-location-saved.display-item");
    public static final Route LOCATION_ATTRIBUTE_LOCATION_SAVED_DISPLAY_ITEM = Route.addTo(LOCATION_ATTRIBUTE_HEADER, "location-saved.display-item");
    public static final Route TIER_ATTRIBUTE_DISPLAY_ITEM = Route.addTo(ABILITY_EDIT_GUI_HEADER, "tier-attribute.display-item");
    private static final Route TOGGLED_OFF_HEADER = Route.addTo(ABILITY_EDIT_GUI_HEADER, "toggle-off-attribute");
    public static final Route ABILITY_TOGGLED_OFF_ATTRIBUTE_DISPLAY_ITEM = Route.addTo(TOGGLED_OFF_HEADER, "toggled-off.display-item");
    public static final Route ABILITY_TOGGLED_ON_ATTRIBUTE_DISPLAY_ITEM = Route.addTo(TOGGLED_OFF_HEADER, "toggled-on.display-item");

    private static final Route ABILITY_HEADER = Route.fromString("ability");
    public static final Route ABILITY_STILL_ON_COOLDOWN = Route.addTo(ABILITY_HEADER, "ability-still-on-cooldown");
    public static final Route ABILITY_NO_LONGER_ON_COOLDOWN = Route.addTo(ABILITY_HEADER, "ability-no-longer-on-cooldown");

    private static final Route ABILITY_LORE_HEADER = Route.addTo(ABILITY_HEADER, "lore");
    private static final Route ABILITY_QUEST_LORE_HEADER = Route.addTo(ABILITY_LORE_HEADER, "quest");
    public static final Route NOT_ENOUGH_ABILITY_POINTS_TO_START_QUEST_LORE = Route.addTo(ABILITY_QUEST_LORE_HEADER, "not-enough-ability-points");
    public static final Route CLICK_TO_START_UPGRADE_QUEST_LORE = Route.addTo(ABILITY_QUEST_LORE_HEADER, "click-to-start");
    public static final Route QUEST_PROGRESS_LORE = Route.addTo(ABILITY_QUEST_LORE_HEADER, "quest-progress");
    public static final Route UPGRADE_LOCKED_BEHIND_LEVELUP_LORE = Route.addTo(ABILITY_LORE_HEADER, "upgrade-locked-behind-levelup");
    public static final Route ABILITY_POINT_COUNT_LORE = Route.addTo(ABILITY_LORE_HEADER, "ability-point-count");
    public static final Route ABILITY_LOCKED_LORE = Route.addTo(ABILITY_LORE_HEADER, "ability-locked");
    public static final Route EXPANSION_PACK_LORE = Route.addTo(ABILITY_LORE_HEADER, "expansion-pack");

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
