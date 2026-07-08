package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * Built-in objective type that completes when a player's skill level reaches or exceeds a
 * configured target level.
 * <p>
 * Optionally filters to a specific skill via the {@code skill} config key. The target level is
 * read from {@code target-level} and defaults to {@code 1}. This type supports auto-complete
 * so that players who already meet the level threshold when a quest starts receive instant credit.
 */
public class SkillTargetLevelObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_target_level");

    @Nullable
    private final NamespacedKey skillFilter;
    private final int targetLevel;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public SkillTargetLevelObjectiveType() {
        this.skillFilter = null;
        this.targetLevel = 1;
    }

    private SkillTargetLevelObjectiveType(@Nullable NamespacedKey skillFilter, int targetLevel) {
        this.skillFilter = skillFilter;
        this.targetLevel = targetLevel;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public SkillTargetLevelObjectiveType parseConfig(@NotNull Section section) {
        NamespacedKey parsedSkill = null;
        if (section.contains("skill")) {
            String rawSkill = section.getString("skill");
            parsedSkill = NamespacedKey.fromString(rawSkill);
            if (parsedSkill == null) {
                McRPG.getInstance().getLogger().warning(
                        "skill_target_level objective has invalid 'skill' value: '"
                                + rawSkill + "' — objective will never match");
                parsedSkill = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown");
            }
        }
        int level = section.contains("target-level") ? Math.max(1, section.getInt("target-level")) : 1;
        return new SkillTargetLevelObjectiveType(parsedSkill, level);
    }

    @Override
    public boolean canProcess(@NotNull QuestObjectiveProgressContext context) {
        return context instanceof SkillLevelQuestContext;
    }

    @Override
    public long processProgress(@NotNull QuestObjectiveInstance instance,
                                @NotNull QuestObjectiveProgressContext context) {
        if (!(context instanceof SkillLevelQuestContext skillContext)) {
            return 0;
        }
        if (skillFilter != null && !skillFilter.equals(skillContext.getSkillKey())) {
            return 0;
        }
        return skillContext.getNewLevel() >= targetLevel ? 1 : 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks whether the player already has any matching skill at or above the target level
     * at the moment the quest starts, granting instant credit if so.
     */
    @NotNull
    @Override
    public OptionalLong checkAutoComplete(@NotNull UUID playerUUID) {
        return meetsTargetLevel(playerUUID) ? OptionalLong.of(1) : OptionalLong.empty();
    }

    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String level = String.valueOf(targetLevel);
        if (skillFilter != null) {
            String skillName = skillFilter.getKey();
            return localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_SKILL_TARGET_LEVEL_SPECIFIC,
                    Map.of("level", level, "skill", skillName));
        }
        return localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_SKILL_TARGET_LEVEL_ANY,
                Map.of("level", level));
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    /**
     * Returns {@code true} if the player has any qualifying skill at or above {@link #targetLevel}.
     * When {@link #skillFilter} is present, only that skill is checked; otherwise all skills are checked.
     *
     * @param playerUUID the UUID of the player to check
     * @return {@code true} if the player meets the target level condition
     */
    private boolean meetsTargetLevel(@NotNull UUID playerUUID) {
        var playerManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER);
        Optional<McRPGPlayer> playerOpt = playerManager.getPlayer(playerUUID);
        if (playerOpt.isEmpty()) {
            return false;
        }
        SkillHolder skillHolder = playerOpt.get().asSkillHolder();
        if (skillFilter != null) {
            return skillHolder.getSkillHolderData(skillFilter)
                    .map(SkillHolder.SkillHolderData::getCurrentLevel)
                    .orElse(0) >= targetLevel;
        }
        return skillHolder.getSkills().stream()
                .anyMatch(skillKey -> skillHolder.getSkillHolderData(skillKey)
                        .map(SkillHolder.SkillHolderData::getCurrentLevel)
                        .orElse(0) >= targetLevel);
    }
}
