package us.eunoians.mcrpg.configuration.file.localization;

import dev.dejvokep.boostedyaml.route.Route;
import us.eunoians.mcrpg.configuration.file.ConfigFile;

import static com.diamonddagger590.mccore.util.Methods.toRoutePath;

/**
 * A java representation of a language configuration file.
 */
public final class LocalizationKey extends ConfigFile {

    // TODO consider breaking these apart to make this class easier to navigate?
    private static final String LOGIN_HEADER = "login";
    public static final Route LOGIN_UNABLE_TO_LOAD_DATA = Route.fromString(toRoutePath(LOGIN_HEADER, "unable-to-load-data"));

    private static final String COMMAND_HEADER = "commands";
    public static final Route CONSOLE_NAME = Route.fromString(toRoutePath(COMMAND_HEADER, "console-name"));

    private static final String CONFIRMATION_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "confirmation");
    public static final Route NO_PENDING_CONFIRMATION_COMMANDS = Route.fromString(toRoutePath(CONFIRMATION_COMMAND_HEADER, "no-pending-confirmations"));
    public static final Route CONFIRMATION_COMMAND_REQUIRED = Route.fromString(toRoutePath(CONFIRMATION_COMMAND_HEADER, "confirmation-required"));
    private static final String GIVE_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "give");
    private static final String GIVE_EXPERIENCE_COMMAND_HEADER = toRoutePath(GIVE_COMMAND_HEADER, "experience");
    public static final Route GIVE_EXPERIENCE_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(GIVE_EXPERIENCE_COMMAND_HEADER, "recipient-message"));
    public static final Route GIVE_EXPERIENCE_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(GIVE_EXPERIENCE_COMMAND_HEADER, "sender-success-message"));
    public static final Route GIVE_EXPERIENCE_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(GIVE_EXPERIENCE_COMMAND_HEADER, "sender-error-message"));
    private static final String GIVE_LEVELS_COMMAND_HEADER = toRoutePath(GIVE_COMMAND_HEADER, "level");
    public static final Route GIVE_LEVELS_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(GIVE_LEVELS_COMMAND_HEADER, "recipient-message"));
    public static final Route GIVE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(GIVE_LEVELS_COMMAND_HEADER, "sender-success-message"));
    public static final Route GIVE_LEVELS_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(GIVE_LEVELS_COMMAND_HEADER, "sender-error-message"));

    private static final String LINK_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "link");
    public static final Route LINK_COMMAND_NOT_LOOKING_AT_CHEST_MESSAGE = Route.fromString(toRoutePath(LINK_COMMAND_HEADER, "not-looking-at-chest-message"));
    public static final Route LINK_COMMAND_REMOTE_TRANSFER_NOT_ENABLED_MESSAGE = Route.fromString(toRoutePath(LINK_COMMAND_HEADER, "remote-transfer-not-enabled-message"));
    public static final Route LINK_COMMAND_SUCCESS_MESSAGE = Route.fromString(toRoutePath(LINK_COMMAND_HEADER, "link-success-message"));

    private static final String UNLINK_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "unlink");
    public static final Route UNLINK_COMMAND_SUCCESS_MESSAGE = Route.fromString(toRoutePath(UNLINK_COMMAND_HEADER, "unlink-success-message"));
    public static final Route UNLINK_COMMAND_NO_LINKED_CHEST_MESSAGE = Route.fromString(toRoutePath(UNLINK_COMMAND_HEADER, "no-linked-chest-message"));

    private static final String LOADOUT_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "loadout");
    private static final String LOADOUT_EDIT_COMMAND_HEADER = toRoutePath(LOADOUT_COMMAND_HEADER, "edit");
    public static final Route LOADOUT_EDIT_COMMAND_NO_LOADOUT_MATCHES_MESSAGE = Route.fromString(toRoutePath(LOADOUT_EDIT_COMMAND_HEADER, "no-loadout-matches"));
    private static final String LOADOUT_SET_COMMAND_HEADER = toRoutePath(LOADOUT_COMMAND_HEADER, "set");
    public static final Route LOADOUT_SET_COMMAND_NO_LOADOUT_MATCHES_MESSAGE = Route.fromString(toRoutePath(LOADOUT_SET_COMMAND_HEADER, "no-loadout-matches"));
    public static final Route LOADOUT_SET_COMMAND_LOADOUT_SET_SUCCESS_MESSAGE = Route.fromString(toRoutePath(LOADOUT_SET_COMMAND_HEADER, "loadout-set-success-message"));

    private static final String ADMIN_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "admin");
    private static final String RELOAD_COMMAND_HEADER = toRoutePath(ADMIN_COMMAND_HEADER, "reload");
    public static final Route RELOAD_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(RELOAD_COMMAND_HEADER, "sender-success-message"));

    private static final String RESET_COMMAND_HEADER = toRoutePath(ADMIN_COMMAND_HEADER, "reset");
    private static final String RESET_PLAYER_COMMAND_HEADER = toRoutePath(RESET_COMMAND_HEADER, "player");
    public static final Route RESET_PLAYER_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(RESET_PLAYER_COMMAND_HEADER, "recipient-message"));
    public static final Route RESET_PLAYER_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(RESET_PLAYER_COMMAND_HEADER, "sender-success-message"));
    public static final Route RESET_PLAYER_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(RESET_PLAYER_COMMAND_HEADER, "sender-error-message"));
    public static final Route RESET_PLAYER_COMMAND_SENDER_ERROR_SAVING_MESSAGE = Route.fromString(toRoutePath(RESET_PLAYER_COMMAND_HEADER, "sender-error-saving-message"));

    private static final String EXP_BANK_COMMAND_HEADER = toRoutePath(ADMIN_COMMAND_HEADER, "exp-bank");
    private static final String REDEEMABLE_EXP_BANK_COMMAND_HEADER = toRoutePath(EXP_BANK_COMMAND_HEADER, "redeemable");
    private static final String REDEEMABLE_EXPERIENCE_EXP_BANK_COMMAND_HEADER = toRoutePath(REDEEMABLE_EXP_BANK_COMMAND_HEADER, "experience");
    private static final String GIVE_REDEEMABLE_EXPERIENCE_COMMAND_HEADER = toRoutePath(REDEEMABLE_EXPERIENCE_EXP_BANK_COMMAND_HEADER, "give");
    public static final Route GIVE_REDEEMABLE_EXPERIENCE_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(GIVE_REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "recipient-message"));
    public static final Route GIVE_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(GIVE_REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "sender-success-message"));
    public static final Route GIVE_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(GIVE_REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "sender-error-message"));
    private static final String REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_HEADER = toRoutePath(REDEEMABLE_EXPERIENCE_EXP_BANK_COMMAND_HEADER, "remove");
    public static final Route REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "recipient-message"));
    public static final Route REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "sender-success-message"));
    public static final Route REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(REMOVE_REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "sender-error-message"));
    private static final String RESET_REDEEMABLE_EXPERIENCE_COMMAND_HEADER = toRoutePath(REDEEMABLE_EXPERIENCE_EXP_BANK_COMMAND_HEADER, "reset");
    public static final Route RESET_REDEEMABLE_EXPERIENCE_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(RESET_REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "recipient-message"));
    public static final Route RESET_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(RESET_REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "sender-success-message"));
    public static final Route RESET_REDEEMABLE_EXPERIENCE_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(RESET_REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "sender-error-message"));

    private static final String REDEEMABLE_LEVELS_EXP_BANK_COMMAND_HEADER = toRoutePath(REDEEMABLE_EXP_BANK_COMMAND_HEADER, "levels");
    private static final String GIVE_REDEEMABLE_LEVELS_COMMAND_HEADER = toRoutePath(REDEEMABLE_LEVELS_EXP_BANK_COMMAND_HEADER, "give");
    public static final Route GIVE_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(GIVE_REDEEMABLE_LEVELS_COMMAND_HEADER, "recipient-message"));
    public static final Route GIVE_REDEEMABLE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(GIVE_REDEEMABLE_LEVELS_COMMAND_HEADER, "sender-success-message"));
    public static final Route GIVE_REDEEMABLE_LEVELS_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(GIVE_REDEEMABLE_LEVELS_COMMAND_HEADER, "sender-error-message"));
    private static final String REMOVE_REDEEMABLE_LEVELS_COMMAND_HEADER = toRoutePath(REDEEMABLE_EXP_BANK_COMMAND_HEADER, "remove");
    public static final Route REMOVE_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(REMOVE_REDEEMABLE_LEVELS_COMMAND_HEADER, "recipient-message"));
    public static final Route REMOVE_REDEEMABLE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(REMOVE_REDEEMABLE_LEVELS_COMMAND_HEADER, "sender-success-message"));
    public static final Route REMOVE_REDEEMABLE_LEVELS_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(REMOVE_REDEEMABLE_LEVELS_COMMAND_HEADER, "sender-error-message"));
    private static final String RESET_REDEEMABLE_LEVELS_COMMAND_HEADER = toRoutePath(REDEEMABLE_EXP_BANK_COMMAND_HEADER, "reset");
    public static final Route RESET_REDEEMABLE_LEVELS_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(RESET_REDEEMABLE_LEVELS_COMMAND_HEADER, "recipient-message"));
    public static final Route RESET_REDEEMABLE_LEVELS_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(RESET_REDEEMABLE_LEVELS_COMMAND_HEADER, "sender-success-message"));
    public static final Route RESET_REDEEMABLE_LEVELS_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(RESET_REDEEMABLE_LEVELS_COMMAND_HEADER, "sender-error-message"));

    private static final String RESTED_EXP_BANK_COMMAND_HEADER = toRoutePath(EXP_BANK_COMMAND_HEADER, "rested-experience");
    private static final String GIVE_RESTED_EXP_COMMAND_HEADER = toRoutePath(RESTED_EXP_BANK_COMMAND_HEADER, "give");
    public static final Route GIVE_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(GIVE_RESTED_EXP_COMMAND_HEADER, "recipient-message"));
    public static final Route GIVE_RESTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(GIVE_RESTED_EXP_COMMAND_HEADER, "sender-success-message"));
    public static final Route GIVE_RESTED_EXP_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(GIVE_RESTED_EXP_COMMAND_HEADER, "sender-error-message"));
    private static final String REMOVE_RESTED_EXP_COMMAND_HEADER = toRoutePath(RESTED_EXP_BANK_COMMAND_HEADER, "remove");
    public static final Route REMOVE_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(REMOVE_RESTED_EXP_COMMAND_HEADER, "recipient-message"));
    public static final Route REMOVE_RESTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(REMOVE_RESTED_EXP_COMMAND_HEADER, "sender-success-message"));
    public static final Route REMOVE_RESTED_EXP_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(REMOVE_RESTED_EXP_COMMAND_HEADER, "sender-error-message"));
    private static final String RESET_RESTED_EXP_COMMAND_HEADER = toRoutePath(RESTED_EXP_BANK_COMMAND_HEADER, "reset");
    public static final Route RESET_RESTED_EXP_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(RESET_RESTED_EXP_COMMAND_HEADER, "recipient-message"));
    public static final Route RESET_RESTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(RESET_RESTED_EXP_COMMAND_HEADER, "sender-success-message"));
    public static final Route RESET_RESTED_EXP_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(RESET_RESTED_EXP_COMMAND_HEADER, "sender-error-message"));

    private static final String BOOSTED_EXP_BANK_COMMAND_HEADER = toRoutePath(EXP_BANK_COMMAND_HEADER, "boosted-experience");
    private static final String GIVE_BOOSTED_EXP_COMMAND_HEADER = toRoutePath(BOOSTED_EXP_BANK_COMMAND_HEADER, "give");
    public static final Route GIVE_BOOSTED_EXP_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(GIVE_BOOSTED_EXP_COMMAND_HEADER, "recipient-message"));
    public static final Route GIVE_BOOSTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(GIVE_BOOSTED_EXP_COMMAND_HEADER, "sender-success-message"));
    public static final Route GIVE_BOOSTED_EXP_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(GIVE_BOOSTED_EXP_COMMAND_HEADER, "sender-error-message"));
    private static final String REMOVE_BOOSTED_EXP_COMMAND_HEADER = toRoutePath(BOOSTED_EXP_BANK_COMMAND_HEADER, "remove");
    public static final Route REMOVE_BOOSTED_EXP_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(REMOVE_BOOSTED_EXP_COMMAND_HEADER, "recipient-message"));
    public static final Route REMOVE_BOOSTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(REMOVE_BOOSTED_EXP_COMMAND_HEADER, "sender-success-message"));
    public static final Route REMOVE_BOOSTED_EXP_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(REMOVE_BOOSTED_EXP_COMMAND_HEADER, "sender-error-message"));
    private static final String RESET_BOOSTED_EXP_COMMAND_HEADER = toRoutePath(BOOSTED_EXP_BANK_COMMAND_HEADER, "reset");
    public static final Route RESET_BOOSTED_EXP_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(RESET_BOOSTED_EXP_COMMAND_HEADER, "recipient-message"));
    public static final Route RESET_BOOSTED_EXP_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(RESET_BOOSTED_EXP_COMMAND_HEADER, "sender-success-message"));
    public static final Route RESET_BOOSTED_EXP_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(RESET_BOOSTED_EXP_COMMAND_HEADER, "sender-error-message"));


    private static final String RESET_SKILL_COMMAND_HEADER = toRoutePath(RESET_COMMAND_HEADER, "skill");
    public static final Route RESET_SKILL_COMMAND_RECIPIENT_MESSAGE = Route.fromString(toRoutePath(RESET_SKILL_COMMAND_HEADER, "recipient-message"));
    public static final Route RESET_SKILL_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(RESET_SKILL_COMMAND_HEADER, "sender-success-message"));
    public static final Route RESET_SKILL_COMMAND_SENDER_ERROR_MESSAGE = Route.fromString(toRoutePath(RESET_SKILL_COMMAND_HEADER, "sender-error-message"));
    public static final Route RESET_SKILL_COMMAND_SENDER_ERROR_SAVING_MESSAGE = Route.fromString(toRoutePath(RESET_SKILL_COMMAND_HEADER, "sender-error-saving-message"));

    private static final String REDEEM_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "redeem");
    private static final String REDEEMABLE_LEVELS_COMMAND_HEADER = toRoutePath(REDEEM_COMMAND_HEADER, "levels");
    public static final Route REDEEMABLE_LEVELS_NOT_ENOUGH_LEVELS_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_COMMAND_HEADER, "not-enough-levels"));
    public static final Route REDEEMABLE_LEVELS_SKILL_ALREADY_MAXED_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_COMMAND_HEADER, "skill-already-maxed"));
    public static final Route REDEEMABLE_LEVELS_REDEEMED_LEVELS_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_COMMAND_HEADER, "redeemed-levels"));
    private static final String REDEEMABLE_EXPERIENCE_COMMAND_HEADER = toRoutePath(REDEEM_COMMAND_HEADER, "experience");
    public static final Route REDEEMABLE_EXPERIENCE_NOT_ENOUGH_EXPERIENCE_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "not-enough-experience"));
    public static final Route REDEEMABLE_EXPERIENCE_SKILL_ALREADY_MAXED_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "skill-already-maxed"));
    public static final Route REDEEMABLE_EXPERIENCE_REDEEMED_EXPERIENCE_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_COMMAND_HEADER, "redeemed-experience"));

    private static final String EXPANSION_HEADER = "expansion";
    public static final Route MCRPG_EXPANSION_NAME = Route.fromString(toRoutePath(EXPANSION_HEADER, "mcrpg"));

    private static final String EXPERIENCE_HEADER = "experience";
    private static final String RESTED_EXPERIENCE_HEADER = toRoutePath(EXPERIENCE_HEADER, "rested-experience");
    public static final Route MAXIMUM_RESTED_EXPERIENCE_REACHED_MESSAGE = Route.fromString(toRoutePath(RESTED_EXPERIENCE_HEADER, "maximum-rested-experience-reached"));
    public static final Route ENTERING_SAFE_ZONE_ONLINE_ACCUMULATION_MESSAGE = Route.fromString(toRoutePath(RESTED_EXPERIENCE_HEADER, "entering-safe-zone-online-accumulation"));
    public static final Route LEAVING_SAFE_ZONE_ONLINE_ACCUMULATION_MESSAGE = Route.fromString(toRoutePath(RESTED_EXPERIENCE_HEADER, "leaving-safe-zone-online-accumulation"));
    public static final Route ENTERING_SAFE_ZONE_OFFLINE_ACCUMULATION_MESSAGE = Route.fromString(toRoutePath(RESTED_EXPERIENCE_HEADER, "entering-safe-zone-offline-accumulation"));
    public static final Route LEAVING_SAFE_ZONE_OFFLINE_ACCUMULATION_MESSAGE = Route.fromString(toRoutePath(RESTED_EXPERIENCE_HEADER, "leaving-safe-zone-offline-accumulation"));
    public static final Route OFFLINE_RESTED_EXPERIENCE_AWARDED_MESSAGE = Route.fromString(toRoutePath(RESTED_EXPERIENCE_HEADER, "offline-rested-experience-awarded"));
    private static final String EXPERIENCE_DISPLAY_HEADER = toRoutePath(EXPERIENCE_HEADER, "experience-display");
    public static final Route ACTION_BAR_DISPLAY_MESSAGE = Route.fromString(toRoutePath(EXPERIENCE_DISPLAY_HEADER, "action-bar-display-message"));
    public static final Route BOSS_BAR_DISPLAY_MESSAGE = Route.fromString(toRoutePath(EXPERIENCE_DISPLAY_HEADER, "boss-bar-display-message"));

    private static final String SKILLS_HEADER = "skills";
    private static final String SWORDS_HEADER = toRoutePath(SKILLS_HEADER, "swords");
    public static final Route SWORDS_DISPLAY_ITEM = Route.fromString(toRoutePath(SWORDS_HEADER, "display-item"));
    private static final String MINING_HEADER = toRoutePath(SKILLS_HEADER, "mining");
    public static final Route MINING_DISPLAY_ITEM = Route.fromString(toRoutePath(MINING_HEADER, "display-item"));
    private static final String WOODCUTTING_HEADER = toRoutePath(SKILLS_HEADER, "woodcutting");
    public static final Route WOODCUTTING_DISPLAY_ITEM = Route.fromString(toRoutePath(WOODCUTTING_HEADER, "display-item"));
    private static final String HERBALISM_HEADER = toRoutePath(SKILLS_HEADER, "herbalism");
    public static final Route HERBALISM_DISPLAY_ITEM = Route.fromString(toRoutePath(HERBALISM_HEADER, "display-item"));

    private static final String GUI_HEADER = "gui";
    private static final String COMMON_GUI_HEADER = toRoutePath(GUI_HEADER, "common");

    public static final Route GUI_COMMON_NEXT_PAGE_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(COMMON_GUI_HEADER, "next-page-button", "display-item"));
    public static final Route GUI_COMMON_PREVIOUS_PAGE_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(COMMON_GUI_HEADER, "previous-page-button", "display-item"));
    public static final Route GUI_COMMON_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(COMMON_GUI_HEADER, "previous-gui-button", "display-item"));
    public static final Route GUI_COMMON_PREVIOUS_FILLER_ITEM_DISPLAY_ITEM = Route.fromString(toRoutePath(COMMON_GUI_HEADER, "filler-item", "display-item"));

    private static final String ABILITY_SORT_TYPE_HEADER = toRoutePath(GUI_HEADER, "ability-sort-types");
    public static final Route ABILITY_SORT_ALPHABETICAL_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_SORT_TYPE_HEADER, "alphabetical.display-item"));
    public static final Route ABILITY_SORT_INNATE_ABILITIES_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_SORT_TYPE_HEADER, "innate-abilities.display-item"));
    public static final Route ABILITY_SORT_SKILL_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_SORT_TYPE_HEADER, "skill.display-item"));
    public static final Route ABILITY_SORT_UNLOCKED_ABILITIES_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_SORT_TYPE_HEADER, "unlocked-abilities.display-item"));
    public static final Route ABILITY_SORT_UPGRADABLE_ABILITIES_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_SORT_TYPE_HEADER, "upgradable-abilities.display-item"));
    public static final Route ABILITY_SORT_PASSIVE_ABILITIES_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_SORT_TYPE_HEADER, "passive-abilities.display-item"));
    public static final Route ABILITY_SORT_ACTIVE_ABILITIES_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_SORT_TYPE_HEADER, "active-abilities.display-item"));

    private static final String SKILL_SORT_TYPE_HEADER = toRoutePath(GUI_HEADER, "skill-sort-types");
    public static final Route SKILL_SORT_ALPHABETICAL_DISPLAY_ITEM = Route.fromString(toRoutePath(SKILL_SORT_TYPE_HEADER, "alphabetical.display-item"));
    public static final Route SKILL_SORT_SKILL_LEVEL_DISPLAY_ITEM = Route.fromString(toRoutePath(SKILL_SORT_TYPE_HEADER, "skill-level.display-item"));
    public static final Route SKILL_SORT_EXPERIENCE_TO_LEVEL_DISPLAY_ITEM = Route.fromString(toRoutePath(SKILL_SORT_TYPE_HEADER, "experience-to-level.display-item"));

    private static final String ABILITY_GUI_HEADER = toRoutePath(GUI_HEADER, "ability-gui");
    public static final Route ABILITY_GUI_TITLE = Route.fromString(toRoutePath(ABILITY_GUI_HEADER, "title"));

    private static final String SKILL_GUI_HEADER = toRoutePath(GUI_HEADER, "skill-gui");
    public static final Route SKILL_GUI_TITLE = Route.fromString(toRoutePath(SKILL_GUI_HEADER, "title"));

    private static final String HOME_GUI_HEADER = toRoutePath(GUI_HEADER, "home-gui");
    public static final Route HOME_GUI_TITLE = Route.fromString(toRoutePath(HOME_GUI_HEADER, "title"));
    public static final Route HOME_GUI_SETTINGS_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "settings-slot.display-item"));
    public static final Route HOME_GUI_ABILITIES_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "abilities-slot.display-item"));
    public static final Route HOME_GUI_SKILLS_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "skills-slot.display-item"));
    public static final Route HOME_GUI_LOADOUTS_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "loadouts-slot.display-item"));
    public static final Route HOME_GUI_EXPERIENCE_BANK_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "experience-bank-slot.display-item"));

    // Remote Transfer
    private static final String REMOTE_TRANSFER_GUI_HEADER = toRoutePath(GUI_HEADER, "remote-transfer-gui");
    public static final Route REMOTE_TRANSFER_GUI_TITLE = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "title"));
    public static final String REMOTE_TRANSFER_GUI_CATEGORIES_HEADER = toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "categories");
    public static final Route REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_ENABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "category-item-option.option-enabled.display-item"));
    public static final Route REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_DISABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "category-item-option.option-disabled.display-item"));
    private static final String REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_HEADER = toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "toggle-entire-category-slot");
    public static final Route REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_TOGGLE_ENABLE = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_HEADER, "toggle-to-be-enabled"));
    public static final Route REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_TOGGLE_DISABLE = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_HEADER, "toggle-to-be-disabled"));

    private static final String ABILITY_EDIT_GUI_HEADER = toRoutePath(GUI_HEADER, "ability-edit-gui");
    public static final Route ABILITY_EDIT_GUI_TITLE = Route.fromString(toRoutePath(ABILITY_EDIT_GUI_HEADER, "title"));
    private static final String LOCATION_ATTRIBUTE_HEADER = toRoutePath(ABILITY_EDIT_GUI_HEADER, "location-attribute");
    public static final Route LOCATION_ATTRIBUTE_NO_LOCATION_SAVED_DISPLAY_ITEM = Route.fromString(toRoutePath(LOCATION_ATTRIBUTE_HEADER, "no-location-saved.display-item"));
    public static final Route LOCATION_ATTRIBUTE_LOCATION_SAVED_DISPLAY_ITEM = Route.fromString(toRoutePath(LOCATION_ATTRIBUTE_HEADER, "location-saved.display-item"));
    public static final Route TIER_ATTRIBUTE_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_EDIT_GUI_HEADER, "tier-attribute.display-item"));
    private static final String TOGGLED_OFF_HEADER = toRoutePath(ABILITY_EDIT_GUI_HEADER, "toggled-off-attribute");
    public static final Route ABILITY_TOGGLED_OFF_ATTRIBUTE_DISPLAY_ITEM = Route.fromString(toRoutePath(TOGGLED_OFF_HEADER, "toggled-off.display-item"));
    public static final Route ABILITY_TOGGLED_ON_ATTRIBUTE_DISPLAY_ITEM = Route.fromString(toRoutePath(TOGGLED_OFF_HEADER, "toggled-on.display-item"));
    public static final Route REMOTE_TRANSFER_BLOCK_TOGGLE_ATTRIBUTE_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_EDIT_GUI_HEADER, "remote-transfer-block-toggle-attribute.display-item"));

    private static final String LOADOUT_GUI_HEADER = toRoutePath(GUI_HEADER, "loadout-gui");
    public static final Route LOADOUT_GUI_TITLE =  Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "title"));
    public static final Route LOADOUT_GUI_INVALID_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "invalid-slot.display-item"));
    public static final Route LOADOUT_GUI_ABILITY_SLOT_ADDITIONAL_LORE = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "ability-slot.additional-lore"));
    public static final Route LOADOUT_GUI_FREE_ABILITY_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "free-ability-slot.display-item"));
    public static final Route LOADOUT_GUI_OPEN_LOADOUT_DISPLAY_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "loadout-display-open-slot.display-item"));
    public static final Route LOADOUT_GUI_LOADOUT_HOME_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "loadout-home-slot.display-item"));

    private static final String LOADOUT_SELECTION_GUI_HEADER = toRoutePath(GUI_HEADER, "loadout-selection-gui");
    public static final Route LOADOUT_SELECTION_GUI_TITLE = Route.fromString(toRoutePath(LOADOUT_SELECTION_GUI_HEADER, "title"));
    private final static String LOADOUT_SELECTION_GUI_SELECTION_SLOT_GEYSER_HEADER = toRoutePath(LOADOUT_SELECTION_GUI_HEADER, "loadout-selection-slot-geyser");
    private final static String LOADOUT_SELECTION_GUI_SELECTION_SLOT_HEADER = toRoutePath(LOADOUT_SELECTION_GUI_HEADER, "loadout-selection-slot");
    public static final Route LOADOUT_SELECTION_GUI_ACTIVE_LOADOUT_SELECTION_SLOT_GEYSER_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_SELECTION_GUI_SELECTION_SLOT_GEYSER_HEADER, "active-loadout"));
    public static final Route LOADOUT_SELECTION_GUI_INACTIVE_LOADOUT_SELECTION_SLOT_GEYSER_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_SELECTION_GUI_SELECTION_SLOT_GEYSER_HEADER, "inactive-loadout"));
    public static final Route LOADOUT_SELECTION_GUI_ACTIVE_LOADOUT_SELECTION_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_SELECTION_GUI_SELECTION_SLOT_HEADER, "active-loadout"));
    public static final Route LOADOUT_SELECTION_GUI_INACTIVE_LOADOUT_SELECTION_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_SELECTION_GUI_SELECTION_SLOT_HEADER, "inactive-loadout"));

    private static final String LOADOUT_DISPLAY_HOME_GUI_HEADER = toRoutePath(GUI_HEADER, "loadout-display-home-gui");
    public static final Route LOADOUT_DISPLAY_HOME_GUI_TITLE = Route.fromString(toRoutePath(LOADOUT_DISPLAY_HOME_GUI_HEADER, "title"));
    private static final String LOADOUT_DISPLAY_HOME_GUI_NAME_EDIT_SLOT_HEADER = toRoutePath(LOADOUT_DISPLAY_HOME_GUI_HEADER, "edit-name-slot");
    public static final Route LOADOUT_DISPLAY_HOME_GUI_NAME_EDIT_PROMPT = Route.fromString(toRoutePath(LOADOUT_DISPLAY_HOME_GUI_NAME_EDIT_SLOT_HEADER, "name-input-request-prompt"));
    public static final Route LOADOUT_DISPLAY_HOME_GUI_NAME_EDIT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_DISPLAY_HOME_GUI_NAME_EDIT_SLOT_HEADER, "display-item"));
    private static final String LOADOUT_DISPLAY_HOME_GUI_EDIT_DISPLAY_ITEM_SLOT_HEADER = toRoutePath(LOADOUT_DISPLAY_HOME_GUI_HEADER, "edit-display-item-slot");
    public static final Route LOADOUT_DISPLAY_HOME_GUI_EDIT_DISPLAY_ITEM_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_DISPLAY_HOME_GUI_EDIT_DISPLAY_ITEM_SLOT_HEADER, "display-item"));
    private static final String LOADOUT_DISPLAY_HOME_GUI_TOGGLE_LOADOUT_SLOT_HEADER = toRoutePath(LOADOUT_DISPLAY_HOME_GUI_HEADER, "toggle-loadout-slot");
    public static final Route LOADOUT_DISPLAY_HOME_GUI_TOGGLE_LOADOUT_ACTIVE_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_DISPLAY_HOME_GUI_TOGGLE_LOADOUT_SLOT_HEADER, "active-display-item"));
    public static final Route LOADOUT_DISPLAY_HOME_GUI_TOGGLE_LOADOUT_INACTIVE_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_DISPLAY_HOME_GUI_TOGGLE_LOADOUT_SLOT_HEADER, "inactive-display-item"));

    private static final String LOADOUT_DISPLAY_ITEM_INPUT_GUI_HEADER = toRoutePath(GUI_HEADER, "loadout-display-item-input-gui");
    public static final Route LOADOUT_DISPLAY_ITEM_INPUT_GUI_TITLE = Route.fromString(toRoutePath(LOADOUT_DISPLAY_ITEM_INPUT_GUI_HEADER, "title"));
    public static final Route LOADOUT_DISPLAY_ITEM_INPUT_GUI_INPUT_HIGHLIGHT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_DISPLAY_ITEM_INPUT_GUI_HEADER, "item-input-highlight-slot.display-item"));
    public static final Route LOADOUT_DISPLAY_ITEM_INPUT_GUI_CANCEL_ITEM_EDIT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_DISPLAY_ITEM_INPUT_GUI_HEADER, "cancel-item-edit-slot.display-item"));
    public static final Route LOADOUT_DISPLAY_ITEM_INPUT_GUI_CONFIRM_ITEM_EDIT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_DISPLAY_ITEM_INPUT_GUI_HEADER, "confirm-item-edit-slot.display-item"));

    private static final String LOADOUT_ABILITY_SELECT_GUI_HEADER = toRoutePath(GUI_HEADER, "loadout-ability-select-gui");
    public static final Route LOADOUT_ABILITY_SELECT_GUI_TITLE = Route.fromString(toRoutePath(LOADOUT_ABILITY_SELECT_GUI_HEADER, "title"));
    public static final Route LOADOUT_ABILITY_SELECT_ABILITY_SELECT_LORE_TO_APPEND = Route.fromString(toRoutePath(LOADOUT_ABILITY_SELECT_GUI_HEADER, "ability-select-slot.additional-lore"));

    private static final String PLAYER_SETTINGS_GUI_HEADER = toRoutePath(GUI_HEADER, "player-setting-gui");
    public static final Route PLAYER_SETTINGS_GUI_TITLE = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "title"));
    private static final String PLAYER_SETTINGS_GUI_EXPERIENCE_DISPLAY_SETTING_HEADER = toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "experience-display-setting-slot");
    public static final Route PLAYER_SETTINGS_GUI_EXPERIENCE_DISPLAY_SETTING_BOSS_BAR_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_EXPERIENCE_DISPLAY_SETTING_HEADER, "boss-bar.display-item"));
    public static final Route PLAYER_SETTINGS_GUI_EXPERIENCE_DISPLAY_SETTING_ACTION_BAR_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_EXPERIENCE_DISPLAY_SETTING_HEADER, "action-bar.display-item"));
    private static final String PLAYER_SETTINGS_GUI_KEEP_HAND_EMPTY_SETTING_HEADER = toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "keep-hand-empty-setting-slot");
    public static final Route PLAYER_SETTINGS_GUI_KEEP_HAND_EMPTY_SETTING_ENABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_KEEP_HAND_EMPTY_SETTING_HEADER, "enabled.display-item"));
    public static final Route PLAYER_SETTINGS_GUI_KEEP_HAND_EMPTY_SETTING_DISABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_KEEP_HAND_EMPTY_SETTING_HEADER, "disabled.display-item"));
    private static final String PLAYER_SETTINGS_GUI_KEEP_HOTBAR_SLOT_EMPTY_SETTING_HEADER = toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "keep-hotbar-slot-empty-setting-slot");
    public static final Route PLAYER_SETTINGS_GUI_KEEP_HOTBAR_SLOT_EMPTY_SETTING_ENABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_KEEP_HOTBAR_SLOT_EMPTY_SETTING_HEADER, "enabled.display-item"));
    public static final Route PLAYER_SETTINGS_GUI_KEEP_HOTBAR_SLOT_EMPTY_SETTING_DISABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_KEEP_HOTBAR_SLOT_EMPTY_SETTING_HEADER, "disabled.display-item"));
    private static final String PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_HEADER = toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "locale-setting-slot");
    public static final Route PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_CLIENT_LOCALE_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_HEADER, "client-locale.display-item"));
    public static final Route PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_SERVER_LOCALE_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_HEADER, "server-locale.display-item"));
    public static final Route PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_FALLBACK_LOCALE_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_LOCALE_SETTING_SLOT_HEADER, "fallback-locale.display-item"));
    private static final String PLAYER_SETTINGS_GUI_REQUIRE_EMPTY_OFFHAND_SETTING_SLOT_HEADER = toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "require-empty-offhand-to-active-ability-slot");
    public static final Route PLAYER_SETTINGS_GUI_REQUIRE_EMPTY_OFFHAND_SETTING_SLOT_ENABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_REQUIRE_EMPTY_OFFHAND_SETTING_SLOT_HEADER, "enabled.display-item"));
    public static final Route PLAYER_SETTINGS_GUI_REQUIRE_EMPTY_OFFHAND_SETTING_SLOT_DISABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_REQUIRE_EMPTY_OFFHAND_SETTING_SLOT_HEADER, "disabled.display-item"));

    private static final String EXPERIENCE_BANK_GUI_HEADER = toRoutePath(GUI_HEADER, "experience-bank-gui");
    public static final Route EXPERIENCE_BANK_GUI_TITLE = Route.fromString(toRoutePath(EXPERIENCE_BANK_GUI_HEADER, "title"));
    public static final Route EXPERIENCE_BANK_GUI_REDEEMABLE_EXPERIENCE_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(EXPERIENCE_BANK_GUI_HEADER, "redeemable-experience-slot.display-item"));
    public static final Route EXPERIENCE_BANK_GUI_REDEEMABLE_LEVELS_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(EXPERIENCE_BANK_GUI_HEADER, "redeemable-levels-slot.display-item"));
    private static final String EXPERIENCE_BANK_GUI_BOOSTED_EXPERIENCE_SLOT_HEADER = toRoutePath(EXPERIENCE_BANK_GUI_HEADER, "boosted-experience-slot");
    public static final Route EXPERIENCE_BANK_GUI_BOOSTED_EXPERIENCE_SLOT_EXAMPLE_BASE_AMOUNT = Route.fromString(toRoutePath(EXPERIENCE_BANK_GUI_BOOSTED_EXPERIENCE_SLOT_HEADER, "example-base-amount"));
    public static final Route EXPERIENCE_BANK_GUI_BOOSTED_EXPERIENCE_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(EXPERIENCE_BANK_GUI_BOOSTED_EXPERIENCE_SLOT_HEADER, "display-item"));
    private static final String EXPERIENCE_BANK_GUI_RESTED_EXPERIENCE_SLOT_HEADER = toRoutePath(EXPERIENCE_BANK_GUI_HEADER, "rested-experience-slot");
    public static final Route EXPERIENCE_BANK_GUI_RESTED_EXPERIENCE_SLOT_EXAMPLE_BASE_AMOUNT = Route.fromString(toRoutePath(EXPERIENCE_BANK_GUI_RESTED_EXPERIENCE_SLOT_HEADER, "example-base-amount"));
    public static final Route EXPERIENCE_BANK_GUI_RESTED_EXPERIENCE_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(EXPERIENCE_BANK_GUI_RESTED_EXPERIENCE_SLOT_HEADER, "display-item"));

    private static final String REDEEMABLE_SKILL_SELECT_GUI_HEADER = toRoutePath(GUI_HEADER, "redeemable-skill-select-gui");
    public static final Route REDEEMABLE_SKILL_SELECT_GUI_TITLE = Route.fromString(toRoutePath(REDEEMABLE_SKILL_SELECT_GUI_HEADER, "title"));
    public static final Route REDEEMABLE_SKILL_SELECT_GUI_LORE = Route.fromString(toRoutePath(REDEEMABLE_SKILL_SELECT_GUI_HEADER, "lore-to-append"));

    private static final String REDEEMABLE_EXPERIENCE_GUI_HEADER = toRoutePath(GUI_HEADER, "redeemable-experience-gui");
    public static final Route REDEEMABLE_EXPERIENCE_GUI_TITLE = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "title"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_AMOUNT_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-amount.display-item"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_ALL_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-all.display-item"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_CUSTOM_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-custom.display-item"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_CUSTOM_PROMPT_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-custom.prompt"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_INVALID_INPUT = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-custom.invalid-input"));

    private static final String REDEEMABLE_LEVELS_GUI_HEADER = toRoutePath(GUI_HEADER, "redeemable-levels-gui");
    public static final Route REDEEMABLE_LEVELS_GUI_TITLE = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "title"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_AMOUNT_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-amount.display-item"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_ALL_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-all.display-item"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_CUSTOM_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-custom.display-item"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_CUSTOM_PROMPT_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-custom.prompt"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_INVALID_INPUT = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-custom.invalid-input"));

    private static final String ABILITY_HEADER = "ability";
    public static final Route ABILITY_STILL_ON_COOLDOWN = Route.fromString(toRoutePath(ABILITY_HEADER, "ability-still-on-cooldown"));
    public static final Route ABILITY_NO_LONGER_ON_COOLDOWN = Route.fromString(toRoutePath(ABILITY_HEADER, "ability-no-longer-on-cooldown"));

    private static final String ABILITY_LORE_HEADER = toRoutePath(ABILITY_HEADER, "lore");
    private static final String ABILITY_QUEST_LORE_HEADER = toRoutePath(ABILITY_LORE_HEADER, "quest");
    public static final Route NOT_ENOUGH_ABILITY_POINTS_TO_START_QUEST_LORE = Route.fromString(toRoutePath(ABILITY_QUEST_LORE_HEADER, "not-enough-ability-points"));
    public static final Route CLICK_TO_START_UPGRADE_QUEST_LORE = Route.fromString(toRoutePath(ABILITY_QUEST_LORE_HEADER, "click-to-start"));
    public static final Route QUEST_PROGRESS_LORE = Route.fromString(toRoutePath(ABILITY_QUEST_LORE_HEADER, "quest-progress"));
    public static final Route UPGRADE_LOCKED_BEHIND_LEVELUP_LORE = Route.fromString(toRoutePath(ABILITY_LORE_HEADER, "upgrade-locked-behind-levelup"));
    public static final Route ABILITY_POINT_COUNT_LORE = Route.fromString(toRoutePath(ABILITY_LORE_HEADER, "ability-point-count"));
    public static final Route ABILITY_LOCKED_LORE = Route.fromString(toRoutePath(ABILITY_LORE_HEADER, "ability-locked"));
    public static final Route EXPANSION_PACK_LORE = Route.fromString(toRoutePath(ABILITY_LORE_HEADER, "expansion-pack"));

    private static final String ABILITY_SPECIFIC_LOCALIZATION_HEADER = toRoutePath(ABILITY_HEADER, "ability-specific-localization");
    private static final String BLEED_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "bleed");
    public static final Route BLEED_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(BLEED_HEADER, "display-item"));
    private static final String DEEPER_WOUND_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "deeper-wound");
    public static final Route DEEPER_WOUND_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(DEEPER_WOUND_HEADER, "display-item"));
    private static final String ENHANCED_BLEED_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "enhanced-bleed");
    public static final Route ENHANCED_BLEED_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(ENHANCED_BLEED_HEADER, "display-item"));
    private static final String RAGE_SPIKE_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "rage-spike");
    public static final Route RAGE_SPIKE_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(RAGE_SPIKE_HEADER, "display-item"));
    private static final String SERRATED_STRIKES_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "serrated-strikes");
    public static final Route SERRATED_STRIKES_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(SERRATED_STRIKES_HEADER, "display-item"));
    private static final String VAMPIRE_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "vampire");
    public static final Route VAMPIRE_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(VAMPIRE_HEADER, "display-item"));
    private static final String EXTRA_ORE_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "extra-ore");
    public static final Route EXTRA_ORE_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(EXTRA_ORE_HEADER, "display-item"));
    private static final String ITS_A_TRIPLE_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "its-a-triple");
    public static final Route ITS_A_TRIPLE_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(ITS_A_TRIPLE_HEADER, "display-item"));
    private static final String ORE_SCANNER_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "ore-scanner");
    public static final Route ORE_SCANNER_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(ORE_SCANNER_HEADER, "display-item"));
    private static final String REMOTE_TRANSFER_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "remote-transfer");
    public static final Route REMOTE_TRANSFER_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(REMOTE_TRANSFER_HEADER, "display-item"));
    private static final String DRYADS_GIFT_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "dryads-gift");
    public static final Route DRYADS_GIFT_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(DRYADS_GIFT_HEADER, "display-item"));
    private static final String EXTRA_LUMBER_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "extra-lumber");
    public static final Route EXTRA_LUMBER_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(EXTRA_LUMBER_HEADER, "display-item"));
    private static final String HEAVY_SWING_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "heavy-swing");
    public static final Route HEAVY_SWING_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(HEAVY_SWING_HEADER, "display-item"));
    private static final String NYMPHS_VITALITY_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "nymphs-vitality");
    public static final Route NYMPHS_VITALITY_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(NYMPHS_VITALITY_HEADER, "display-item"));
}
