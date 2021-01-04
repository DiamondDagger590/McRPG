package us.eunoians.mcrpg.skill.impl;

import us.eunoians.mcrpg.abilities.AbilityType;
import us.eunoians.mcrpg.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillType;

import java.util.Set;

/**
 * This skill gains experience through {@link org.bukkit.entity.Wolf}s attacking various
 * {@link org.bukkit.entity.LivingEntity}s or a {@link org.bukkit.entity.Player} taming an
 * {@link org.bukkit.entity.Tameable} entity.
 * <p>
 * This {@link Skill} focuses on improving the abilities of {@link org.bukkit.entity.Wolf}s and is
 * primarily combat oriented.
 *
 * @author DiamondDagger590
 */
public class Taming extends Skill {

    /**
     * @param mcRPGPlayer The {@link McRPGPlayer} who owns this {@link Skill}
     */
    public Taming(McRPGPlayer mcRPGPlayer) {
        super(mcRPGPlayer);
    }

    /**
     * @param mcRPGPlayer  The {@link McRPGPlayer} who owns this {@link Skill}
     * @param currentExp   The amount of exp this {@link Skill} already has
     * @param currentLevel The amount of levels this {@link Skill} already has
     */
    public Taming(McRPGPlayer mcRPGPlayer, int currentExp, int currentLevel) {
        super(mcRPGPlayer, currentExp, currentLevel);
    }

    /**
     * @param mcRPGPlayer              The {@link McRPGPlayer} who owns this {@link Skill}
     * @param currentExp               The amount of exp this {@link Skill} already has
     * @param currentLevel             The amount of levels this {@link Skill} already has
     * @param alreadyUnlockedAbilities The {@link Set} of {@link AbilityType}s the player has already unlocked
     * @param abilityPointsGained      The amount of ability points this skill has already awarded
     */
    public Taming(McRPGPlayer mcRPGPlayer, int currentExp, int currentLevel, Set<AbilityType> alreadyUnlockedAbilities, int abilityPointsGained) {
        super(mcRPGPlayer, currentExp, currentLevel, alreadyUnlockedAbilities, abilityPointsGained);
    }

    /**
     * Gets the {@link SkillType} that represents this {@link Skill}
     *
     * @return The {@link SkillType} that represents this {@link Skill}
     */
    @Override
    public SkillType getSkillType() {
        return SkillType.TAMING;
    }
}
