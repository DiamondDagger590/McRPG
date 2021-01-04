package us.eunoians.mcrpg.skill.impl;

import us.eunoians.mcrpg.abilities.AbilityType;
import us.eunoians.mcrpg.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillType;

import java.util.Set;

public class Swords extends Skill {

    /**
     * @param mcRPGPlayer The {@link McRPGPlayer} who owns this {@link Skill}
     */
    public Swords(McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
    }

    /**
     * @param mcRPGPlayer  The {@link McRPGPlayer} who owns this {@link Skill}
     * @param currentExp   The amount of exp this {@link Skill} already has
     * @param currentLevel The amount of levels this {@link Skill} already has
     */
    public Swords(McRPGPlayer mcRPGPlayer, int currentExp, int currentLevel) {
        super(mcRPGPlayer, currentExp, currentLevel);
    }

    /**
     * @param mcRPGPlayer              The {@link McRPGPlayer} who owns this {@link Skill}
     * @param currentExp               The amount of exp this {@link Skill} already has
     * @param currentLevel             The amount of levels this {@link Skill} already has
     * @param alreadyUnlockedAbilities The {@link Set} of {@link AbilityType}s the player has already unlocked
     * @param abilityPointsGained      The amount of ability points this skill has already awarded
     */
    public Swords(McRPGPlayer mcRPGPlayer, int currentExp, int currentLevel, Set<AbilityType> alreadyUnlockedAbilities, int abilityPointsGained) {
        super(mcRPGPlayer, currentExp, currentLevel, alreadyUnlockedAbilities, abilityPointsGained);
    }

    /**
     * Gets the {@link SkillType} that represents this {@link Skill}
     *
     * @return The {@link SkillType} that represents this {@link Skill}
     */
    @Override
    public SkillType getSkillType() {
        return SkillType.SWORDS;
    }
}
