package us.eunoians.mcrpg.quest.objective.type.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.impl.objective.QuestObjectiveInstance;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveProgressContext;
import us.eunoians.mcrpg.quest.objective.type.QuestObjectiveType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Map;
import java.util.Optional;

/**
 * Built-in objective type for tracking skill level-up events.
 * <p>
 * Optionally filters to a specific skill via the {@code skill} config key, and optionally
 * requires a minimum number of levels gained in a single event via the {@code levels} key.
 * Because progress is determined by the event itself, this type does not support auto-complete
 * checks: a player can only satisfy it by actually gaining skill levels during the quest.
 */
public class SkillLevelUpObjectiveType implements QuestObjectiveType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_level_up");

    @Nullable
    private final NamespacedKey skillFilter;
    private final int minLevelsPerEvent;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public SkillLevelUpObjectiveType() {
        this.skillFilter = null;
        this.minLevelsPerEvent = 1;
    }

    private SkillLevelUpObjectiveType(@Nullable NamespacedKey skillFilter, int minLevelsPerEvent) {
        this.skillFilter = skillFilter;
        this.minLevelsPerEvent = minLevelsPerEvent;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public SkillLevelUpObjectiveType parseConfig(@NotNull Section section) {
        NamespacedKey parsedSkill = null;
        if (section.contains("skill")) {
            String rawSkill = section.getString("skill");
            parsedSkill = NamespacedKey.fromString(rawSkill);
            if (parsedSkill == null) {
                McRPG.getInstance().getLogger().warning(
                        "skill_level_up objective has invalid 'skill' value: '"
                                + rawSkill + "' — objective will never match");
                parsedSkill = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "unknown");
            }
        }
        int levels = section.contains("levels") ? Math.max(1, section.getInt("levels")) : 1;
        return new SkillLevelUpObjectiveType(parsedSkill, levels);
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
        if (skillContext.getLevelsGained() < minLevelsPerEvent) {
            return 0;
        }
        return skillContext.getLevelsGained();
    }

    @NotNull
    @Override
    public String describeObjective(@NotNull McRPGPlayer player, long requiredProgress) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String count = String.valueOf(requiredProgress);
        if (skillFilter != null) {
            String skillName = skillFilter.getKey();
            return localization.getLocalizedMessage(player,
                    LocalizationKey.QUEST_OBJECTIVE_SKILL_LEVEL_UP_SPECIFIC,
                    Map.of("count", count, "skill", skillName));
        }
        return localization.getLocalizedMessage(player,
                LocalizationKey.QUEST_OBJECTIVE_SKILL_LEVEL_UP_ANY,
                Map.of("count", count));
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
