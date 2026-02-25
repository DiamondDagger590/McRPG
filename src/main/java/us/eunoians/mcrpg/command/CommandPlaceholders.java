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
    UPGRADE_POINTS("upgrade-points"),
    REGISTRY_TYPE("registry_type"),
    COUNT("count"),
    REGISTRY_KEY("registry_key"),
    QUEST_KEY("quest_key"),
    QUEST_UUID("quest_uuid"),
    QUEST_STATE("quest_state"),
    SCOPE_TYPE("scope_type"),
    TIMESTAMP("timestamp"),
    STAGE_KEY("stage_key"),
    STAGE_STATE("stage_state"),
    STATE_COLOR("state_color"),
    PHASE_INDEX("phase_index"),
    OBJECTIVE_KEY("objective_key"),
    CURRENT_PROGRESS("current_progress"),
    REQUIRED_PROGRESS("required_progress"),
    STAGES_TOTAL("stages_total"),
    STAGES_COMPLETED("stages_completed"),
    BEFORE_COUNT("before_count"),
    AFTER_COUNT("after_count"),
    OFFERING_ID("offering_id"),
    ENTITY_ID("entity_id"),
    SCOPE_TYPE_PH("scope_type_id"),
    SCOPE_ID("scope_id"),
    COOLDOWN_TYPE("cooldown_type"),
    CATEGORY("category"),
    QUEST_DEF("quest_def"),
    RARITY("rarity"),
    OFFERING_STATE("offering_state"),
    ACCEPTED_BY("accepted_by"),
    EXPIRES("expires"),
    REWARD_TYPE("reward_type"),
    DELETED_COUNT("deleted_count"),
    CANCELLED_COUNT("cancelled_count"),
    REFRESH_TYPE("refresh_type"),
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
