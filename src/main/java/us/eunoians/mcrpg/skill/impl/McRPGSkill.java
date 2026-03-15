package us.eunoians.mcrpg.skill.impl;

import com.diamonddagger590.mccore.statistic.Statistic;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.skill.BaseSkill;
import us.eunoians.mcrpg.statistic.McRPGStatistic;

import java.util.Optional;
import java.util.Set;

/**
 * A type of skill that is used by native McRPG skills.
 */
public abstract class McRPGSkill extends BaseSkill {

    public McRPGSkill(@NotNull NamespacedKey skillKey) {
        super(skillKey);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Native McRPG skills provide per-skill experience and max level statistics by default.
     * Concrete skills can override to add additional custom statistics alongside the defaults.
     */
    @Override
    @NotNull
    public Set<Statistic> getDefaultStatistics() {
        return Set.of(
                McRPGStatistic.createSkillExperienceStatistic(getSkillKey(), getName()),
                McRPGStatistic.createSkillMaxLevelStatistic(getSkillKey(), getName())
        );
    }
}
