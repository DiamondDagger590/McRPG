package us.eunoians.mcrpg.quest.reward.builtin;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.quest.reward.QuestRewardTypeRegistry;

/**
 * Central reference for all built-in {@link us.eunoians.mcrpg.quest.reward.QuestRewardType} implementations.
 * <p>
 * Call {@link #registerAll(QuestRewardTypeRegistry)} during bootstrap to register all built-in types.
 */
public final class BuiltinRewardTypes {

    public static final ExperienceRewardType EXPERIENCE = new ExperienceRewardType();
    public static final CommandRewardType COMMAND = new CommandRewardType();
    public static final AbilityUpgradeRewardType ABILITY_UPGRADE = new AbilityUpgradeRewardType();
    public static final AbilityUpgradeNextTierRewardType ABILITY_UPGRADE_NEXT_TIER = new AbilityUpgradeNextTierRewardType();

    private BuiltinRewardTypes() {
    }

    /**
     * Registers all built-in reward types with the provided registry.
     *
     * @param registry the reward type registry to register types with
     */
    public static void registerAll(@NotNull QuestRewardTypeRegistry registry) {
        registry.register(EXPERIENCE);
        registry.register(COMMAND);
        registry.register(ABILITY_UPGRADE);
        registry.register(ABILITY_UPGRADE_NEXT_TIER);
    }
}
