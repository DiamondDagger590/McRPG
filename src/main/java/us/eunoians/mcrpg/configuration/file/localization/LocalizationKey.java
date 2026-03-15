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
    public static final Route NON_PLAYER_COMMAND_ERROR = Route.fromString(toRoutePath(COMMAND_HEADER, "non-player-error"));

    // Command descriptions for help text
    private static final String COMMAND_DESCRIPTIONS_HEADER = toRoutePath(COMMAND_HEADER, "descriptions");
    public static final Route COMMAND_DESCRIPTION_REDEEM = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "redeem"));
    public static final Route COMMAND_DESCRIPTION_REDEEM_SKILL = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "redeem-skill"));
    public static final Route COMMAND_DESCRIPTION_REDEEM_EXPERIENCE_AMOUNT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "redeem-experience-amount"));
    public static final Route COMMAND_DESCRIPTION_REDEEM_LEVELS_AMOUNT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "redeem-levels-amount"));
    public static final Route COMMAND_DESCRIPTION_GIVE = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "give"));
    public static final Route COMMAND_DESCRIPTION_GIVE_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "give-player"));
    public static final Route COMMAND_DESCRIPTION_GIVE_EXPERIENCE_SKILL = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "give-experience-skill"));
    public static final Route COMMAND_DESCRIPTION_GIVE_EXPERIENCE_AMOUNT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "give-experience-amount"));
    public static final Route COMMAND_DESCRIPTION_GIVE_LEVELS_SKILL = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "give-levels-skill"));
    public static final Route COMMAND_DESCRIPTION_GIVE_LEVELS_AMOUNT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "give-levels-amount"));
    public static final Route COMMAND_DESCRIPTION_GIVE_LEVELS_RESET_EXPERIENCE_FLAG = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "give-levels-reset-experience-flag"));
    public static final Route COMMAND_DESCRIPTION_RESET = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "reset"));
    public static final Route COMMAND_DESCRIPTION_RESET_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "reset-player"));
    public static final Route COMMAND_DESCRIPTION_RESET_SKILL = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "reset-skill"));
    public static final Route COMMAND_DESCRIPTION_LOADOUT_SLOT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "loadout-slot"));
    public static final Route COMMAND_DESCRIPTION_LOADOUT_EDIT_SLOT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "loadout-edit-slot"));
    public static final Route COMMAND_DESCRIPTION_DEBUG_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "debug-player"));
    public static final Route COMMAND_DESCRIPTION_EXP_BANK_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "exp-bank-player"));
    public static final Route COMMAND_DESCRIPTION_EXP_BANK_AMOUNT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "exp-bank-amount"));
    public static final Route COMMAND_DESCRIPTION_EXP_BANK_RESTED_AMOUNT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "exp-bank-rested-amount"));
    public static final Route COMMAND_DESCRIPTION_EXP_BANK_BOOSTED_AMOUNT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "exp-bank-boosted-amount"));
    public static final Route COMMAND_DESCRIPTION_EXP_BANK_REDEEMABLE_EXPERIENCE_AMOUNT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "exp-bank-redeemable-experience-amount"));
    public static final Route COMMAND_DESCRIPTION_EXP_BANK_REDEEMABLE_LEVELS_AMOUNT = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "exp-bank-redeemable-levels-amount"));
    public static final Route COMMAND_DESCRIPTION_EXP_BANK_RESET_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "exp-bank-reset-player"));
    public static final Route COMMAND_DESCRIPTION_EXP_BANK_GIVE_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "exp-bank-give-player"));
    public static final Route COMMAND_DESCRIPTION_EXP_BANK_REMOVE_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "exp-bank-remove-player"));

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
    public static final Route LOADOUT_EDIT_COMMAND_AMBIGUOUS_MATCHES_MESSAGE = Route.fromString(toRoutePath(LOADOUT_EDIT_COMMAND_HEADER, "ambiguous-matches"));
    private static final String LOADOUT_SET_COMMAND_HEADER = toRoutePath(LOADOUT_COMMAND_HEADER, "set");
    public static final Route LOADOUT_SET_COMMAND_NO_LOADOUT_MATCHES_MESSAGE = Route.fromString(toRoutePath(LOADOUT_SET_COMMAND_HEADER, "no-loadout-matches"));
    public static final Route LOADOUT_SET_COMMAND_AMBIGUOUS_MATCHES_MESSAGE = Route.fromString(toRoutePath(LOADOUT_SET_COMMAND_HEADER, "ambiguous-matches"));
    public static final Route LOADOUT_SET_COMMAND_LOADOUT_SET_SUCCESS_MESSAGE = Route.fromString(toRoutePath(LOADOUT_SET_COMMAND_HEADER, "loadout-set-success-message"));

    private static final String ADMIN_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "admin");
    private static final String RELOAD_COMMAND_HEADER = toRoutePath(ADMIN_COMMAND_HEADER, "reload");
    public static final Route RELOAD_COMMAND_SENDER_SUCCESS_MESSAGE = Route.fromString(toRoutePath(RELOAD_COMMAND_HEADER, "sender-success-message"));

    private static final String DEBUG_COMMAND_HEADER = toRoutePath(ADMIN_COMMAND_HEADER, "debug");
    public static final Route DEBUG_COMMAND_HEADER_MESSAGE = Route.fromString(toRoutePath(DEBUG_COMMAND_HEADER, "header-message"));
    public static final Route DEBUG_COMMAND_UPGRADE_POINTS_MESSAGE = Route.fromString(toRoutePath(DEBUG_COMMAND_HEADER, "upgrade-points-message"));
    public static final Route DEBUG_COMMAND_SKILL_INFO_MESSAGE = Route.fromString(toRoutePath(DEBUG_COMMAND_HEADER, "skill-info-message"));

    private static final String QUEST_REGISTRY_COMMAND_HEADER = toRoutePath(ADMIN_COMMAND_HEADER, "quest-registry");
    public static final Route QUEST_REGISTRY_HEADER_MESSAGE = Route.fromString(toRoutePath(QUEST_REGISTRY_COMMAND_HEADER, "header"));
    public static final Route QUEST_REGISTRY_ENTRY_MESSAGE = Route.fromString(toRoutePath(QUEST_REGISTRY_COMMAND_HEADER, "entry"));
    public static final Route QUEST_REGISTRY_EMPTY_MESSAGE = Route.fromString(toRoutePath(QUEST_REGISTRY_COMMAND_HEADER, "empty"));

    private static final String QUEST_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "quest");

    private static final String QUEST_COMMON_HEADER = toRoutePath(QUEST_COMMAND_HEADER, "common");
    public static final Route QUEST_CMD_INVALID_UUID = Route.fromString(toRoutePath(QUEST_COMMON_HEADER, "invalid-uuid"));
    public static final Route QUEST_CMD_SPECIFY_PLAYER = Route.fromString(toRoutePath(QUEST_COMMON_HEADER, "specify-player-from-console"));
    public static final Route QUEST_CMD_QUEST_NOT_FOUND = Route.fromString(toRoutePath(QUEST_COMMON_HEADER, "quest-not-found"));

    private static final String QUEST_LIST_COMMAND_HEADER = toRoutePath(QUEST_COMMAND_HEADER, "list");
    public static final Route QUEST_LIST_NO_PERMISSION_OTHERS = Route.fromString(toRoutePath(QUEST_LIST_COMMAND_HEADER, "no-permission-others"));
    public static final Route QUEST_LIST_NO_ACTIVE_QUESTS = Route.fromString(toRoutePath(QUEST_LIST_COMMAND_HEADER, "no-active-quests"));
    public static final Route QUEST_LIST_HEADER = Route.fromString(toRoutePath(QUEST_LIST_COMMAND_HEADER, "header"));
    public static final Route QUEST_LIST_ENTRY = Route.fromString(toRoutePath(QUEST_LIST_COMMAND_HEADER, "entry"));

    private static final String QUEST_INFO_COMMAND_HEADER = toRoutePath(QUEST_COMMAND_HEADER, "info");
    public static final Route QUEST_INFO_TITLE = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "title"));
    public static final Route QUEST_INFO_DEFINITION = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "definition"));
    public static final Route QUEST_INFO_UUID = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "uuid"));
    public static final Route QUEST_INFO_STATE = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "state"));
    public static final Route QUEST_INFO_SCOPE_TYPE = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "scope-type"));
    public static final Route QUEST_INFO_STARTED = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "started"));
    public static final Route QUEST_INFO_ENDED = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "ended"));
    public static final Route QUEST_INFO_EXPIRES = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "expires"));
    public static final Route QUEST_INFO_STAGES_HEADER = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "stages-header"));
    public static final Route QUEST_INFO_STAGE_ENTRY = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "stage-entry"));
    public static final Route QUEST_INFO_OBJECTIVE_ENTRY = Route.fromString(toRoutePath(QUEST_INFO_COMMAND_HEADER, "objective-entry"));

    private static final String QUEST_START_COMMAND_HEADER = toRoutePath(QUEST_COMMAND_HEADER, "start");
    public static final Route QUEST_START_NO_PERMISSION_OTHERS = Route.fromString(toRoutePath(QUEST_START_COMMAND_HEADER, "no-permission-others"));
    public static final Route QUEST_START_CANNOT_START = Route.fromString(toRoutePath(QUEST_START_COMMAND_HEADER, "cannot-start"));
    public static final Route QUEST_START_SUCCESS = Route.fromString(toRoutePath(QUEST_START_COMMAND_HEADER, "success"));
    public static final Route QUEST_START_NO_SCOPE_PROVIDER = Route.fromString(toRoutePath(QUEST_START_COMMAND_HEADER, "no-scope-provider"));
    public static final Route QUEST_START_ERROR = Route.fromString(toRoutePath(QUEST_START_COMMAND_HEADER, "error"));

    private static final String QUEST_CANCEL_COMMAND_HEADER = toRoutePath(QUEST_COMMAND_HEADER, "cancel");
    public static final Route QUEST_CANCEL_NOT_CANCELLABLE = Route.fromString(toRoutePath(QUEST_CANCEL_COMMAND_HEADER, "not-cancellable"));
    public static final Route QUEST_CANCEL_SUCCESS = Route.fromString(toRoutePath(QUEST_CANCEL_COMMAND_HEADER, "success"));

    private static final String QUEST_ADMIN_COMMAND_HEADER = toRoutePath(QUEST_COMMAND_HEADER, "admin");
    private static final String QUEST_ADMIN_RELOAD_HEADER = toRoutePath(QUEST_ADMIN_COMMAND_HEADER, "reload");
    public static final Route QUEST_ADMIN_RELOAD_SUCCESS = Route.fromString(toRoutePath(QUEST_ADMIN_RELOAD_HEADER, "success"));

    private static final String QUEST_ADMIN_COMPLETE_HEADER = toRoutePath(QUEST_ADMIN_COMMAND_HEADER, "complete");
    public static final Route QUEST_ADMIN_COMPLETE_NOT_IN_PROGRESS = Route.fromString(toRoutePath(QUEST_ADMIN_COMPLETE_HEADER, "not-in-progress"));
    public static final Route QUEST_ADMIN_COMPLETE_SUCCESS = Route.fromString(toRoutePath(QUEST_ADMIN_COMPLETE_HEADER, "success"));
    public static final Route QUEST_ADMIN_COMPLETE_DEFINITION_NOT_FOUND = Route.fromString(toRoutePath(QUEST_ADMIN_COMPLETE_HEADER, "definition-not-found"));

    private static final String QUEST_ADMIN_SETPROGRESS_HEADER = toRoutePath(QUEST_ADMIN_COMMAND_HEADER, "setprogress");
    public static final Route QUEST_ADMIN_SETPROGRESS_INVALID_OBJECTIVE_KEY = Route.fromString(toRoutePath(QUEST_ADMIN_SETPROGRESS_HEADER, "invalid-objective-key"));
    public static final Route QUEST_ADMIN_SETPROGRESS_OBJECTIVE_NOT_FOUND = Route.fromString(toRoutePath(QUEST_ADMIN_SETPROGRESS_HEADER, "objective-not-found"));
    public static final Route QUEST_ADMIN_SETPROGRESS_SUCCESS = Route.fromString(toRoutePath(QUEST_ADMIN_SETPROGRESS_HEADER, "success"));

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
    private static final String SKILL_LEVEL_UP_HEADER = toRoutePath(SKILLS_HEADER, "level-up");
    public static final Route SKILL_LEVEL_UP_MESSAGE = Route.fromString(toRoutePath(SKILL_LEVEL_UP_HEADER, "level-up-message"));
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
    public static final Route ABILITY_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_GUI_HEADER, "previous-gui-button.display-item"));

    private static final String SKILL_GUI_HEADER = toRoutePath(GUI_HEADER, "skill-gui");
    public static final Route SKILL_GUI_TITLE = Route.fromString(toRoutePath(SKILL_GUI_HEADER, "title"));
    public static final Route SKILL_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(SKILL_GUI_HEADER, "previous-gui-button.display-item"));

    private static final String HOME_GUI_HEADER = toRoutePath(GUI_HEADER, "home-gui");
    public static final Route HOME_GUI_TITLE = Route.fromString(toRoutePath(HOME_GUI_HEADER, "title"));
    public static final Route HOME_GUI_SETTINGS_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "settings-slot.display-item"));
    public static final Route HOME_GUI_ABILITIES_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "abilities-slot.display-item"));
    public static final Route HOME_GUI_SKILLS_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "skills-slot.display-item"));
    public static final Route HOME_GUI_LOADOUTS_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "loadouts-slot.display-item"));
    public static final Route HOME_GUI_EXPERIENCE_BANK_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "experience-bank-slot.display-item"));
    public static final Route HOME_GUI_QUESTS_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "quests-slot.display-item"));
    public static final Route HOME_GUI_COMING_SOON_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "coming-soon-slot.display-item"));
    public static final Route HOME_GUI_BOARD_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(HOME_GUI_HEADER, "board-slot.display-item"));

    // Quest Board GUI
    private static final String QUEST_BOARD_GUI_HEADER = toRoutePath(GUI_HEADER, "quest-board");
    public static final Route QUEST_BOARD_GUI_TITLE = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "title"));
    public static final Route QUEST_BOARD_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM =
            Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "previous-gui-button.display-item"));
    public static final Route QUEST_BOARD_OFFERING_LORE = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "offering-lore"));
    public static final Route QUEST_BOARD_ACCEPT_BUTTON = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "accept"));
    public static final Route QUEST_BOARD_SLOT_FULL = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "slots-full"));
    public static final Route QUEST_BOARD_COOLDOWN = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "on-cooldown"));

    // Board Admin Commands
    private static final String BOARD_ADMIN_COMMAND_HEADER = toRoutePath(ADMIN_COMMAND_HEADER, "board-admin");
    private static final String BOARD_ADMIN_ROTATE_HEADER = toRoutePath(BOARD_ADMIN_COMMAND_HEADER, "rotate");
    public static final Route BOARD_ADMIN_ROTATE_SUCCESS = Route.fromString(toRoutePath(BOARD_ADMIN_ROTATE_HEADER, "success"));

    private static final String BOARD_ADMIN_OFFERINGS_HEADER = toRoutePath(BOARD_ADMIN_COMMAND_HEADER, "offerings");
    public static final Route BOARD_ADMIN_OFFERINGS_LIST_EMPTY = Route.fromString(toRoutePath(BOARD_ADMIN_OFFERINGS_HEADER, "list-empty"));
    public static final Route BOARD_ADMIN_OFFERINGS_LIST_HEADER = Route.fromString(toRoutePath(BOARD_ADMIN_OFFERINGS_HEADER, "list-header"));
    public static final Route BOARD_ADMIN_OFFERINGS_LIST_ENTRY = Route.fromString(toRoutePath(BOARD_ADMIN_OFFERINGS_HEADER, "list-entry"));
    public static final Route BOARD_ADMIN_OFFERINGS_EXPIRE_NOT_FOUND = Route.fromString(toRoutePath(BOARD_ADMIN_OFFERINGS_HEADER, "expire-not-found"));
    public static final Route BOARD_ADMIN_OFFERINGS_EXPIRE_INVALID_STATE = Route.fromString(toRoutePath(BOARD_ADMIN_OFFERINGS_HEADER, "expire-invalid-state"));
    public static final Route BOARD_ADMIN_OFFERINGS_EXPIRE_SUCCESS = Route.fromString(toRoutePath(BOARD_ADMIN_OFFERINGS_HEADER, "expire-success"));
    public static final Route BOARD_ADMIN_OFFERINGS_EXPIRE_ERROR = Route.fromString(toRoutePath(BOARD_ADMIN_OFFERINGS_HEADER, "expire-error"));

    private static final String BOARD_ADMIN_SCOPED_HEADER = toRoutePath(BOARD_ADMIN_COMMAND_HEADER, "scoped");
    public static final Route BOARD_ADMIN_SCOPED_LIST_EMPTY = Route.fromString(toRoutePath(BOARD_ADMIN_SCOPED_HEADER, "list-empty"));
    public static final Route BOARD_ADMIN_SCOPED_LIST_HEADER = Route.fromString(toRoutePath(BOARD_ADMIN_SCOPED_HEADER, "list-header"));
    public static final Route BOARD_ADMIN_SCOPED_LIST_ENTRY = Route.fromString(toRoutePath(BOARD_ADMIN_SCOPED_HEADER, "list-entry"));
    public static final Route BOARD_ADMIN_SCOPED_RESET_SUCCESS = Route.fromString(toRoutePath(BOARD_ADMIN_SCOPED_HEADER, "reset-success"));
    public static final Route BOARD_ADMIN_SCOPED_RESET_ERROR = Route.fromString(toRoutePath(BOARD_ADMIN_SCOPED_HEADER, "reset-error"));
    public static final Route BOARD_ADMIN_SCOPED_GENERATE_SUCCESS = Route.fromString(toRoutePath(BOARD_ADMIN_SCOPED_HEADER, "generate-success"));

    private static final String BOARD_ADMIN_PLAYER_HEADER = toRoutePath(BOARD_ADMIN_COMMAND_HEADER, "player");
    public static final Route BOARD_ADMIN_PLAYER_OFFERINGS_HEADER_MSG = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "offerings-header"));
    public static final Route BOARD_ADMIN_PLAYER_OFFERINGS_SCOPED_COUNT = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "offerings-scoped-count"));
    public static final Route BOARD_ADMIN_PLAYER_OFFERINGS_ENTRY = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "offerings-entry"));
    public static final Route BOARD_ADMIN_PLAYER_ACCEPTED_EMPTY = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "accepted-empty"));
    public static final Route BOARD_ADMIN_PLAYER_ACCEPTED_HEADER_MSG = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "accepted-header"));
    public static final Route BOARD_ADMIN_PLAYER_ACCEPTED_ENTRY = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "accepted-entry"));
    public static final Route BOARD_ADMIN_PLAYER_ABANDON_NOT_FOUND = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "abandon-not-found"));
    public static final Route BOARD_ADMIN_PLAYER_ABANDON_INVALID_STATE = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "abandon-invalid-state"));
    public static final Route BOARD_ADMIN_PLAYER_ABANDON_SUCCESS = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "abandon-success"));
    public static final Route BOARD_ADMIN_PLAYER_ABANDON_ERROR = Route.fromString(toRoutePath(BOARD_ADMIN_PLAYER_HEADER, "abandon-error"));

    private static final String BOARD_ADMIN_COOLDOWN_HEADER = toRoutePath(BOARD_ADMIN_COMMAND_HEADER, "cooldown");
    public static final Route BOARD_ADMIN_COOLDOWN_LIST_EMPTY = Route.fromString(toRoutePath(BOARD_ADMIN_COOLDOWN_HEADER, "list-empty"));
    public static final Route BOARD_ADMIN_COOLDOWN_LIST_HEADER_MSG = Route.fromString(toRoutePath(BOARD_ADMIN_COOLDOWN_HEADER, "list-header"));
    public static final Route BOARD_ADMIN_COOLDOWN_LIST_ENTRY = Route.fromString(toRoutePath(BOARD_ADMIN_COOLDOWN_HEADER, "list-entry"));
    public static final Route BOARD_ADMIN_COOLDOWN_RESET_SUCCESS = Route.fromString(toRoutePath(BOARD_ADMIN_COOLDOWN_HEADER, "reset-success"));
    public static final Route BOARD_ADMIN_COOLDOWN_RESET_ERROR = Route.fromString(toRoutePath(BOARD_ADMIN_COOLDOWN_HEADER, "reset-error"));

    private static final String BOARD_ADMIN_REWARDS_HEADER = toRoutePath(BOARD_ADMIN_COMMAND_HEADER, "rewards");
    public static final Route BOARD_ADMIN_REWARDS_PENDING_EMPTY = Route.fromString(toRoutePath(BOARD_ADMIN_REWARDS_HEADER, "pending-empty"));
    public static final Route BOARD_ADMIN_REWARDS_PENDING_HEADER_MSG = Route.fromString(toRoutePath(BOARD_ADMIN_REWARDS_HEADER, "pending-header"));
    public static final Route BOARD_ADMIN_REWARDS_PENDING_ENTRY = Route.fromString(toRoutePath(BOARD_ADMIN_REWARDS_HEADER, "pending-entry"));
    public static final Route BOARD_ADMIN_REWARDS_CLEAR_SUCCESS = Route.fromString(toRoutePath(BOARD_ADMIN_REWARDS_HEADER, "clear-success"));
    public static final Route BOARD_ADMIN_REWARDS_CLEAR_ERROR = Route.fromString(toRoutePath(BOARD_ADMIN_REWARDS_HEADER, "clear-error"));

    private static final String BOARD_ADMIN_PURGE_HEADER = toRoutePath(BOARD_ADMIN_COMMAND_HEADER, "purge");
    public static final Route BOARD_ADMIN_PURGE_SUCCESS = Route.fromString(toRoutePath(BOARD_ADMIN_PURGE_HEADER, "success"));
    public static final Route BOARD_ADMIN_PURGE_ERROR = Route.fromString(toRoutePath(BOARD_ADMIN_PURGE_HEADER, "error"));

    // Quest Board GUI -- Group Quests (scoped)
    public static final Route QUEST_BOARD_GROUP_TAB = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-tab"));
    public static final Route QUEST_BOARD_GROUP_SELECTOR_TITLE = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-selector-title"));
    public static final Route QUEST_BOARD_GROUP_ENTITY_SLOT = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-entity-slot"));
    public static final Route QUEST_BOARD_GROUP_NO_OFFERINGS = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-no-offerings"));
    public static final Route QUEST_BOARD_GROUP_NO_OFFERINGS_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-no-offerings.display-item"));
    public static final Route QUEST_BOARD_GROUP_ACCEPT = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-accept"));
    public static final Route QUEST_BOARD_GROUP_ABANDON = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-abandon"));
    public static final Route QUEST_BOARD_GROUP_NO_PERMISSION = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-no-permission"));
    public static final Route QUEST_BOARD_GROUP_SLOTS_FULL = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-slots-full"));
    public static final Route QUEST_BOARD_GROUP_ENTITY_LORE = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "group-entity-lore"));

    // Active Quest GUI
    private static final String ACTIVE_QUEST_GUI_HEADER = toRoutePath(GUI_HEADER, "active-quest-gui");
    public static final Route ACTIVE_QUEST_GUI_TITLE = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "title"));
    public static final Route ACTIVE_QUEST_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "previous-gui-button.display-item"));
    public static final Route ACTIVE_QUEST_GUI_QUEST_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.display-item"));
    public static final Route ACTIVE_QUEST_GUI_VIEW_HISTORY_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "view-history-button.display-item"));
    public static final Route ACTIVE_QUEST_GUI_PHASE_LINE = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.phase-line"));
    public static final Route ACTIVE_QUEST_GUI_OBJECTIVE_PROGRESS_LINE = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.objective-progress-line"));
    public static final Route ACTIVE_QUEST_GUI_OBJECTIVE_DETAIL_LINE = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.objective-detail-line"));
    public static final Route ACTIVE_QUEST_GUI_EXPIRES_LINE = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.expires-line"));
    public static final Route ACTIVE_QUEST_GUI_EXPIRES_TIME_FORMAT = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.expires-time-format"));
    public static final Route ACTIVE_QUEST_GUI_EXPIRES_EXPIRED = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.expires-expired"));
    public static final Route ACTIVE_QUEST_GUI_EXPIRES_NONE = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.expires-none"));
    public static final Route ACTIVE_QUEST_GUI_CLICK_TO_VIEW_DETAILS = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.click-to-view-details"));
    public static final Route ACTIVE_QUEST_GUI_RIGHT_CLICK_TO_ABANDON = Route.fromString(toRoutePath(ACTIVE_QUEST_GUI_HEADER, "quest-slot.right-click-to-abandon"));

    // Quest History GUI
    private static final String QUEST_HISTORY_GUI_HEADER = toRoutePath(GUI_HEADER, "quest-history-gui");
    public static final Route QUEST_HISTORY_GUI_TITLE = Route.fromString(toRoutePath(QUEST_HISTORY_GUI_HEADER, "title"));
    public static final Route QUEST_HISTORY_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_HISTORY_GUI_HEADER, "previous-gui-button.display-item"));
    public static final Route QUEST_HISTORY_GUI_SORT_DATE_DESC_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_HISTORY_GUI_HEADER, "sort-date-desc.display-item"));
    public static final Route QUEST_HISTORY_GUI_SORT_DATE_ASC_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_HISTORY_GUI_HEADER, "sort-date-asc.display-item"));
    public static final Route QUEST_HISTORY_GUI_COMPLETED_QUEST_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_HISTORY_GUI_HEADER, "completed-quest-slot.display-item"));
    public static final Route QUEST_HISTORY_GUI_UNKNOWN_QUEST_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_HISTORY_GUI_HEADER, "unknown-quest-slot.display-item"));
    // Quest Detail GUI
    private static final String QUEST_DETAIL_GUI_HEADER = toRoutePath(GUI_HEADER, "quest-detail-gui");
    public static final Route QUEST_DETAIL_GUI_TITLE = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "title"));
    public static final Route QUEST_DETAIL_GUI_PREVIOUS_FROM_BOARD_BUTTON_DISPLAY_ITEM =
            Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "previous-gui-button.from-board.display-item"));
    public static final Route QUEST_DETAIL_GUI_PREVIOUS_FROM_HISTORY_BUTTON_DISPLAY_ITEM =
            Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "previous-gui-button.from-history.display-item"));
    public static final Route QUEST_DETAIL_GUI_PREVIOUS_FROM_ACTIVE_BUTTON_DISPLAY_ITEM =
            Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "previous-gui-button.from-active.display-item"));
    public static final Route QUEST_DETAIL_GUI_OVERVIEW_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "overview-slot.display-item"));
    public static final Route QUEST_DETAIL_GUI_PHASE_HEADER_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "phase-header.display-item"));
    public static final Route QUEST_DETAIL_GUI_STAGE_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "stage-slot.display-item"));
    public static final Route QUEST_DETAIL_GUI_OBJECTIVE_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "objective-slot.display-item"));
    public static final Route QUEST_DETAIL_GUI_REWARD_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "reward-slot.display-item"));
    public static final Route QUEST_DETAIL_GUI_DURATION_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "duration-slot.display-item"));
    public static final Route QUEST_DETAIL_GUI_ABANDON_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "abandon-button.display-item"));
    public static final Route QUEST_DETAIL_GUI_STATE_PREVIEW = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "state-preview"));
    public static final Route QUEST_DETAIL_GUI_STATE_COMPLETED = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "state-completed"));
    public static final Route QUEST_DETAIL_GUI_REWARD_SLOT_NO_REWARDS = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "reward-slot.no-rewards"));
    public static final Route QUEST_DETAIL_GUI_REWARD_COMPLETION_HEADER = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "reward-slot.completion-header"));
    public static final Route QUEST_DETAIL_GUI_REWARD_PHASE_HEADER = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "reward-slot.phase-header"));
    public static final Route QUEST_DETAIL_GUI_OBJECTIVE_PROGRESS = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "objective-slot.progress-line"));
    public static final Route QUEST_DETAIL_GUI_OBJECTIVE_STATE = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "objective-slot.state-line"));
    public static final Route QUEST_DETAIL_GUI_OBJECTIVE_DESCRIPTION_CONTINUATION = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "objective-slot.description-continuation-line"));
    // PhaseCompletionMode display labels
    public static final Route QUEST_DETAIL_GUI_COMPLETION_MODE_ALL = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "completion-mode.all"));
    public static final Route QUEST_DETAIL_GUI_COMPLETION_MODE_ANY = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "completion-mode.any"));
    // QuestObjectiveState display labels
    public static final Route QUEST_DETAIL_GUI_OBJECTIVE_STATE_NOT_STARTED = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "objective-state.not-started"));
    public static final Route QUEST_DETAIL_GUI_OBJECTIVE_STATE_IN_PROGRESS = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "objective-state.in-progress"));
    public static final Route QUEST_DETAIL_GUI_OBJECTIVE_STATE_COMPLETED = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "objective-state.completed"));
    public static final Route QUEST_DETAIL_GUI_OBJECTIVE_STATE_CANCELLED = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "objective-state.cancelled"));
    // Reward slot entry line. Placeholder: <reward>
    public static final Route QUEST_DETAIL_GUI_REWARD_ENTRY_LINE = Route.fromString(toRoutePath(QUEST_DETAIL_GUI_HEADER, "reward-slot.reward-entry-line"));

    // Quest Abandon Confirm GUI
    private static final String QUEST_ABANDON_CONFIRM_GUI_HEADER = toRoutePath(GUI_HEADER, "quest-abandon-confirm-gui");
    public static final Route QUEST_ABANDON_CONFIRM_GUI_TITLE = Route.fromString(toRoutePath(QUEST_ABANDON_CONFIRM_GUI_HEADER, "title"));
    public static final Route QUEST_ABANDON_CONFIRM_GUI_CONFIRM_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_ABANDON_CONFIRM_GUI_HEADER, "confirm-button.display-item"));
    public static final Route QUEST_ABANDON_CONFIRM_GUI_CANCEL_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_ABANDON_CONFIRM_GUI_HEADER, "cancel-button.display-item"));
    public static final Route QUEST_ABANDON_CONFIRM_GUI_QUEST_INFO_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_ABANDON_CONFIRM_GUI_HEADER, "quest-info.display-item"));

    // Remote Transfer
    private static final String REMOTE_TRANSFER_GUI_HEADER = toRoutePath(GUI_HEADER, "remote-transfer-gui");
    public static final Route REMOTE_TRANSFER_GUI_TITLE = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "title"));
    public static final Route REMOTE_TRANSFER_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "previous-gui-button.display-item"));
    public static final String REMOTE_TRANSFER_GUI_CATEGORIES_HEADER = toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "categories");
    public static final Route REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_ENABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "category-item-option.option-enabled.display-item"));
    public static final Route REMOTE_TRANSFER_GUI_CATEGORY_ITEM_OPTION_DISABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "category-item-option.option-disabled.display-item"));
    private static final String REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_HEADER = toRoutePath(REMOTE_TRANSFER_GUI_HEADER, "toggle-entire-category-slot");
    public static final Route REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_TOGGLE_ENABLE = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_HEADER, "toggle-to-be-enabled"));
    public static final Route REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_TOGGLE_DISABLE = Route.fromString(toRoutePath(REMOTE_TRANSFER_GUI_TOGGLE_ENTIRE_CATEGORY_SLOT_HEADER, "toggle-to-be-disabled"));

    private static final String ABILITY_EDIT_GUI_HEADER = toRoutePath(GUI_HEADER, "ability-edit-gui");
    public static final Route ABILITY_EDIT_GUI_TITLE = Route.fromString(toRoutePath(ABILITY_EDIT_GUI_HEADER, "title"));
    public static final Route ABILITY_EDIT_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_EDIT_GUI_HEADER, "previous-gui-button.display-item"));
    private static final String LOCATION_ATTRIBUTE_HEADER = toRoutePath(ABILITY_EDIT_GUI_HEADER, "location-attribute");
    public static final Route LOCATION_ATTRIBUTE_NO_LOCATION_SAVED_DISPLAY_ITEM = Route.fromString(toRoutePath(LOCATION_ATTRIBUTE_HEADER, "no-location-saved.display-item"));
    public static final Route LOCATION_ATTRIBUTE_LOCATION_SAVED_DISPLAY_ITEM = Route.fromString(toRoutePath(LOCATION_ATTRIBUTE_HEADER, "location-saved.display-item"));
    public static final Route TIER_ATTRIBUTE_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_EDIT_GUI_HEADER, "tier-attribute.display-item"));
    private static final String TOGGLED_OFF_HEADER = toRoutePath(ABILITY_EDIT_GUI_HEADER, "toggled-off-attribute");
    public static final Route ABILITY_TOGGLED_OFF_ATTRIBUTE_DISPLAY_ITEM = Route.fromString(toRoutePath(TOGGLED_OFF_HEADER, "toggled-off.display-item"));
    public static final Route ABILITY_TOGGLED_ON_ATTRIBUTE_DISPLAY_ITEM = Route.fromString(toRoutePath(TOGGLED_OFF_HEADER, "toggled-on.display-item"));
    public static final Route REMOTE_TRANSFER_BLOCK_TOGGLE_ATTRIBUTE_DISPLAY_ITEM = Route.fromString(toRoutePath(ABILITY_EDIT_GUI_HEADER, "remote-transfer-block-toggle-attribute.display-item"));
    private static final String MASS_HARVEST_PULL_ITEMS_ATTRIBUTE_HEADER = toRoutePath(ABILITY_EDIT_GUI_HEADER, "mass-harvest-pull-items-attribute");
    public static final Route MASS_HARVEST_PULL_ITEMS_ATTRIBUTE_DISABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(MASS_HARVEST_PULL_ITEMS_ATTRIBUTE_HEADER, "toggled-off.display-item"));
    public static final Route MASS_HARVEST_PULL_ITEMS_ATTRIBUTE_ENABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(MASS_HARVEST_PULL_ITEMS_ATTRIBUTE_HEADER, "toggled-on.display-item"));

    private static final String LOADOUT_GUI_HEADER = toRoutePath(GUI_HEADER, "loadout-gui");
    public static final Route LOADOUT_GUI_TITLE =  Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "title"));
    public static final Route LOADOUT_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "previous-gui-button.display-item"));
    public static final Route LOADOUT_GUI_INVALID_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "invalid-slot.display-item"));
    public static final Route LOADOUT_GUI_ABILITY_SLOT_ADDITIONAL_LORE = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "ability-slot.additional-lore"));
    public static final Route LOADOUT_GUI_FREE_ABILITY_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "free-ability-slot.display-item"));
    public static final Route LOADOUT_GUI_OPEN_LOADOUT_DISPLAY_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_GUI_HEADER, "loadout-display-open-slot.display-item"));

    private static final String LOADOUT_SELECTION_GUI_HEADER = toRoutePath(GUI_HEADER, "loadout-selection-gui");
    public static final Route LOADOUT_SELECTION_GUI_TITLE = Route.fromString(toRoutePath(LOADOUT_SELECTION_GUI_HEADER, "title"));
    public static final Route LOADOUT_SELECTION_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_SELECTION_GUI_HEADER, "previous-gui-button.display-item"));
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
    public static final Route LOADOUT_ABILITY_SELECT_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(LOADOUT_ABILITY_SELECT_GUI_HEADER, "previous-gui-button.display-item"));
    public static final Route LOADOUT_ABILITY_SELECT_ABILITY_SELECT_LORE_TO_APPEND = Route.fromString(toRoutePath(LOADOUT_ABILITY_SELECT_GUI_HEADER, "ability-select-slot.additional-lore"));

    private static final String PLAYER_SETTINGS_GUI_HEADER = toRoutePath(GUI_HEADER, "player-setting-gui");
    public static final Route PLAYER_SETTINGS_GUI_TITLE = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "title"));
    public static final Route PLAYER_SETTINGS_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "previous-gui-button.display-item"));
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
    private static final String PLAYER_SETTINGS_GUI_DISABLE_BONUS_EXPERIENCE_CONSUMPTION_SETTING_HEADER = toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "disable-bonus-experience-consumption-setting-slot");
    public static final Route PLAYER_SETTINGS_GUI_DISABLE_BONUS_EXPERIENCE_CONSUMPTION_SETTING_ENABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_DISABLE_BONUS_EXPERIENCE_CONSUMPTION_SETTING_HEADER, "enabled.display-item"));
    public static final Route PLAYER_SETTINGS_GUI_DISABLE_BONUS_EXPERIENCE_CONSUMPTION_SETTING_DISABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(PLAYER_SETTINGS_GUI_DISABLE_BONUS_EXPERIENCE_CONSUMPTION_SETTING_HEADER, "disabled.display-item"));

    private static final String EXPERIENCE_BANK_GUI_HEADER = toRoutePath(GUI_HEADER, "experience-bank-gui");
    public static final Route EXPERIENCE_BANK_GUI_TITLE = Route.fromString(toRoutePath(EXPERIENCE_BANK_GUI_HEADER, "title"));
    public static final Route EXPERIENCE_BANK_GUI_PREVIOUS_GUI_BUTTON = Route.fromString(toRoutePath(EXPERIENCE_BANK_GUI_HEADER, "previous-gui-button.display-item"));
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
    public static final Route REDEEMABLE_SKILL_SELECT_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_SKILL_SELECT_GUI_HEADER, "previous-gui-button.display-item"));

    private static final String REDEEMABLE_EXPERIENCE_GUI_HEADER = toRoutePath(GUI_HEADER, "redeemable-experience-gui");
    public static final Route REDEEMABLE_EXPERIENCE_GUI_TITLE = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "title"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "previous-gui-button.display-item"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_AMOUNT_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-amount.display-item"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_ALL_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-all.display-item"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_CUSTOM_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-custom.display-item"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_CUSTOM_PROMPT_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-custom.prompt"));
    public static final Route REDEEMABLE_EXPERIENCE_GUI_REDEEM_INVALID_INPUT = Route.fromString(toRoutePath(REDEEMABLE_EXPERIENCE_GUI_HEADER, "redeem-custom.invalid-input"));

    private static final String REDEEMABLE_LEVELS_GUI_HEADER = toRoutePath(GUI_HEADER, "redeemable-levels-gui");
    public static final Route REDEEMABLE_LEVELS_GUI_TITLE = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "title"));
    public static final Route REDEEMABLE_LEVELS_GUI_PREVIOUS_GUI_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "previous-gui-button.display-item"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_AMOUNT_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-amount.display-item"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_ALL_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-all.display-item"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_CUSTOM_BUTTON_DISPLAY_ITEM = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-custom.display-item"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_CUSTOM_PROMPT_MESSAGE = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-custom.prompt"));
    public static final Route REDEEMABLE_LEVELS_GUI_REDEEM_INVALID_INPUT = Route.fromString(toRoutePath(REDEEMABLE_LEVELS_GUI_HEADER, "redeem-custom.invalid-input"));

    private static final String ABILITY_HEADER = "ability";
    private static final String ABILITY_COOLDOWN_HEADER = toRoutePath(ABILITY_HEADER, "cooldown");
    public static final Route ABILITY_STILL_ON_COOLDOWN = Route.fromString(toRoutePath(ABILITY_COOLDOWN_HEADER, "ability-still-on-cooldown"));
    public static final Route ABILITY_NO_LONGER_ON_COOLDOWN = Route.fromString(toRoutePath(ABILITY_COOLDOWN_HEADER, "ability-no-longer-on-cooldown"));

    private static final String ABILITY_READY_HEADER = toRoutePath(ABILITY_HEADER, "ready");
    private static final String HERBALISM_READY_HEADER = toRoutePath(ABILITY_READY_HEADER, "herbalism");
    public static final Route HERBALISM_READY_MESSAGE = Route.fromString(toRoutePath(HERBALISM_READY_HEADER, "ready-message"));
    public static final Route HERBALISM_UNREADY_MESSAGE = Route.fromString(toRoutePath(HERBALISM_READY_HEADER, "unready-message"));
    private static final String MINING_READY_HEADER = toRoutePath(ABILITY_READY_HEADER, "mining");
    public static final Route MINING_READY_MESSAGE = Route.fromString(toRoutePath(MINING_READY_HEADER, "ready-message"));
    public static final Route MINING_UNREADY_MESSAGE = Route.fromString(toRoutePath(MINING_READY_HEADER, "unready-message"));
    private static final String SWORDS_READY_HEADER = toRoutePath(ABILITY_READY_HEADER, "swords");
    public static final Route SWORDS_READY_MESSAGE = Route.fromString(toRoutePath(SWORDS_READY_HEADER, "ready-message"));
    public static final Route SWORDS_UNREADY_MESSAGE = Route.fromString(toRoutePath(SWORDS_READY_HEADER, "unready-message"));
    private static final String WOODCUTTING_READY_HEADER = toRoutePath(ABILITY_READY_HEADER, "woodcutting");
    public static final Route WOODCUTTING_READY_MESSAGE = Route.fromString(toRoutePath(WOODCUTTING_READY_HEADER, "ready-message"));
    public static final Route WOODCUTTING_UNREADY_MESSAGE = Route.fromString(toRoutePath(WOODCUTTING_READY_HEADER, "unready-message"));

    private static final String ABILITY_UNLOCK_HEADER = toRoutePath(ABILITY_HEADER, "unlock");
    public static final Route ABILITY_UNLOCKED_MESSAGE = Route.fromString(toRoutePath(ABILITY_UNLOCK_HEADER, "ability-unlocked"));
    public static final Route ABILITY_ADDED_TO_LOADOUT_MESSAGE = Route.fromString(toRoutePath(ABILITY_UNLOCK_HEADER, "ability-added-to-loadout"));
    public static final Route ABILITY_NOT_ADDED_DUPLICATE_SKILL_MESSAGE = Route.fromString(toRoutePath(ABILITY_UNLOCK_HEADER, "ability-not-added-duplicate-skill"));

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
    private static final String VERDANT_SURGE_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "verdant-surge");
    public static final Route VERDANT_SURGE_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(VERDANT_SURGE_HEADER, "display-item"));
    private static final String MASS_HARVEST_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "mass-harvest");
    public static final Route MASS_HARVEST_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(MASS_HARVEST_HEADER, "display-item"));
    private static final String INSTANT_IRRIGATION_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "instant-irrigation");
    public static final Route INSTANT_IRRIGATION_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(INSTANT_IRRIGATION_HEADER, "display-item"));
    public static final Route INSTANT_IRRIGATION_ACTIVATION_NOTIFICATION = Route.fromString(toRoutePath(INSTANT_IRRIGATION_HEADER, "activation-message"));
    private static final String TOO_MANY_PLANTS_HEADER = toRoutePath(ABILITY_SPECIFIC_LOCALIZATION_HEADER, "too-many-plants");
    public static final Route TOO_MANY_PLANTS_DISPLAY_ITEM_HEADER = Route.fromString(toRoutePath(TOO_MANY_PLANTS_HEADER, "display-item"));

    public static final Route QUEST_BOARD_OFFERING_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "offering-slot.display-item"));
    public static final Route QUEST_BOARD_SCOPED_OFFERING_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "scoped-offering-slot.display-item"));
    public static final Route QUEST_BOARD_NO_OFFERINGS_SLOT_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "no-offerings-slot.display-item"));

    // Quest progress notification player setting (settings GUI display items)
    private static final String QUEST_PROGRESS_NOTIFICATION_SETTING_HEADER = toRoutePath(PLAYER_SETTINGS_GUI_HEADER, "quest-progress-notification-setting-slot");
    public static final Route QUEST_PROGRESS_NOTIFICATION_SETTING_ENABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_PROGRESS_NOTIFICATION_SETTING_HEADER, "enabled.display-item"));
    public static final Route QUEST_PROGRESS_NOTIFICATION_SETTING_DISABLED_DISPLAY_ITEM = Route.fromString(toRoutePath(QUEST_PROGRESS_NOTIFICATION_SETTING_HEADER, "disabled.display-item"));

    // Quest Board GUI Polish (Phase 4)
    public static final Route QUEST_BOARD_RARITY_LINE = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "rarity-line"));
    public static final Route QUEST_BOARD_DURATION_LINE = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "duration-line"));
    public static final Route QUEST_BOARD_BOARD_QUESTS_LINE = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "board-quests-line"));
    public static final Route QUEST_BOARD_RIGHT_CLICK_PREVIEW = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "right-click-preview"));
    public static final Route QUEST_BOARD_OBJECTIVES_HEADER = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "objectives-header"));
    public static final Route QUEST_BOARD_OBJECTIVE_LINE = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "objective-line"));
    public static final Route QUEST_BOARD_REWARDS_HEADER = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "rewards-header"));
    public static final Route QUEST_BOARD_REWARD_LINE = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "reward-line"));
    public static final Route QUEST_BOARD_REWARD_LINE_NO_AMOUNT = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "reward-line-no-amount"));
    public static final Route QUEST_BOARD_EXPIRES_IN = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "expires-in"));
    public static final Route QUEST_BOARD_OFFERING_CATEGORY = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "offering-category"));
    public static final Route QUEST_BOARD_CLICK_TO_ACCEPT = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "click-to-accept"));
    public static final Route QUEST_BOARD_DISTRIBUTION_HEADER = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "distribution-header"));
    public static final Route QUEST_BOARD_DISTRIBUTION_TIER = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "distribution-tier"));
    public static final Route QUEST_BOARD_PREVIEW_QUALIFIES = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "preview-qualifies"));
    public static final Route QUEST_BOARD_PREVIEW_NOT_QUALIFIES = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "preview-not-qualifies"));
    public static final Route QUEST_BOARD_PREVIEW_CONTRIBUTION = Route.fromString(toRoutePath(QUEST_BOARD_GUI_HEADER, "preview-contribution"));

    // Quest event notifications (chat messages sent to in-scope players on lifecycle events)
    private static final String QUEST_NOTIFICATION_HEADER = "quest-notifications";
    public static final Route QUEST_STARTED_NOTIFICATION = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "quest-started"));
    public static final Route QUEST_COMPLETED_NOTIFICATION = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "quest-completed"));
    public static final Route QUEST_CANCELLED_NOTIFICATION = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "quest-cancelled"));
    public static final Route QUEST_EXPIRED_NOTIFICATION = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "quest-expired"));
    public static final Route QUEST_PHASE_COMPLETED_NOTIFICATION = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "phase-completed"));
    public static final Route QUEST_OBJECTIVE_THRESHOLD_NOTIFICATION = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "objective-threshold"));
    public static final Route QUEST_BOARD_ROTATED_NOTIFICATION = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "board-rotated"));
    public static final Route QUEST_NEAR_EXPIRY_SINGLE_NOTIFICATION = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "near-expiry-single"));
    public static final Route QUEST_NEAR_EXPIRY_BATCH_HEADER = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "near-expiry-batch-header"));
    public static final Route QUEST_NEAR_EXPIRY_BATCH_ENTRY = Route.fromString(toRoutePath(QUEST_NOTIFICATION_HEADER, "near-expiry-batch-entry"));

    // Quest objective type fallback descriptions (used when no quest-specific locale entry exists)
    private static final String QUEST_OBJECTIVE_TYPE_HEADER = "quest-objective-types";
    private static final String QUEST_OBJECTIVE_BLOCK_BREAK_HEADER = toRoutePath(QUEST_OBJECTIVE_TYPE_HEADER, "block-break");
    public static final Route QUEST_OBJECTIVE_BLOCK_BREAK_ANY = Route.fromString(toRoutePath(QUEST_OBJECTIVE_BLOCK_BREAK_HEADER, "any"));
    public static final Route QUEST_OBJECTIVE_BLOCK_BREAK_SINGLE = Route.fromString(toRoutePath(QUEST_OBJECTIVE_BLOCK_BREAK_HEADER, "single"));
    public static final Route QUEST_OBJECTIVE_BLOCK_BREAK_MULTI_HEADER = Route.fromString(toRoutePath(QUEST_OBJECTIVE_BLOCK_BREAK_HEADER, "multi-header"));
    public static final Route QUEST_OBJECTIVE_BLOCK_BREAK_MULTI_ITEM = Route.fromString(toRoutePath(QUEST_OBJECTIVE_BLOCK_BREAK_HEADER, "multi-item"));
    private static final String QUEST_OBJECTIVE_MOB_KILL_HEADER = toRoutePath(QUEST_OBJECTIVE_TYPE_HEADER, "mob-kill");
    public static final Route QUEST_OBJECTIVE_MOB_KILL_ANY = Route.fromString(toRoutePath(QUEST_OBJECTIVE_MOB_KILL_HEADER, "any"));
    public static final Route QUEST_OBJECTIVE_MOB_KILL_SINGLE = Route.fromString(toRoutePath(QUEST_OBJECTIVE_MOB_KILL_HEADER, "single"));
    public static final Route QUEST_OBJECTIVE_MOB_KILL_MULTI_HEADER = Route.fromString(toRoutePath(QUEST_OBJECTIVE_MOB_KILL_HEADER, "multi-header"));
    public static final Route QUEST_OBJECTIVE_MOB_KILL_MULTI_ITEM = Route.fromString(toRoutePath(QUEST_OBJECTIVE_MOB_KILL_HEADER, "multi-item"));

    // Quest reward type fallback display labels (used when no display-key or display label is configured)
    private static final String QUEST_REWARD_TYPE_HEADER = "quest-reward-types";
    public static final Route QUEST_REWARD_COMMAND_FALLBACK_DISPLAY = Route.fromString(toRoutePath(QUEST_REWARD_TYPE_HEADER, "command.fallback-display"));
    public static final Route QUEST_REWARD_SCALABLE_COMMAND_FALLBACK_DISPLAY = Route.fromString(toRoutePath(QUEST_REWARD_TYPE_HEADER, "scalable-command.fallback-display"));
    public static final Route QUEST_REWARD_SCALABLE_AMOUNT_SUFFIX = Route.fromString(toRoutePath(QUEST_REWARD_TYPE_HEADER, "scalable-command.amount-suffix"));

    // Statistic commands
    private static final String STATISTIC_COMMAND_HEADER = toRoutePath(COMMAND_HEADER, "statistic");
    public static final Route STATISTIC_VIEW_MESSAGE = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "view-message"));
    public static final Route STATISTIC_LIST_HEADER = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "list-header"));
    public static final Route STATISTIC_LIST_ENTRY = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "list-entry"));
    public static final Route STATISTIC_SET_SUCCESS = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "set-success"));
    public static final Route STATISTIC_RESET_SUCCESS = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "reset-success"));
    public static final Route STATISTIC_RESET_ALL_SUCCESS = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "reset-all-success"));
    public static final Route STATISTIC_LOADING_MESSAGE = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "loading-message"));
    public static final Route STATISTIC_LOOKUP_ERROR_MESSAGE = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "lookup-error-message"));
    public static final Route STATISTIC_INVALID_VALUE = Route.fromString(toRoutePath(STATISTIC_COMMAND_HEADER, "invalid-value"));

    // Statistic command descriptions
    public static final Route COMMAND_DESCRIPTION_STATISTIC = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_VIEW = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-view"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_VIEW_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-view-player"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_VIEW_STATISTIC = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-view-statistic"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_LIST = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-list"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_LIST_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-list-player"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_SET = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-set"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_SET_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-set-player"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_SET_STATISTIC = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-set-statistic"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_SET_VALUE = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-set-value"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_RESET = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-reset"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_RESET_PLAYER = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-reset-player"));
    public static final Route COMMAND_DESCRIPTION_STATISTIC_RESET_STATISTIC = Route.fromString(toRoutePath(COMMAND_DESCRIPTIONS_HEADER, "statistic-reset-statistic"));
}
