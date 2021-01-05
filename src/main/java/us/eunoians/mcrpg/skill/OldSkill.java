package us.eunoians.mcrpg.skill;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.player.McRPGPlayer;
import us.eunoians.mcrpg.util.Parser;

import java.util.HashSet;
import java.util.Set;

/**
 * @deprecated Moved to {@link AbstractSkill} and {@link SkillProgression}, use to scrap code only
 * This class represents the abstract of a {@link OldSkill}
 *
 *
 * @author DiamondDagger590
 */
public abstract class OldSkill {

    /**
     * The {@link McRPGPlayer} that owns this skill
     */
    @NotNull
    private McRPGPlayer mcRPGPlayer;

    /**
     * A {@link Set} of {@link AbilityType}s that the player has already unlocked in case
     * a player loses exp or levels
     */
    @NotNull
    protected Set<AbilityType> alreadyUnlockedAbilities;

    /**
     * The amount of ability points already gained from this {@link OldSkill}
     */
    protected int abilityPointsGained;

    /**
     * The amount of exp this skill currently has
     */
    protected int currentExp;

    /**
     * The amount of exp needed to level up (cached)
     */
    protected int expToLevelUp = -1;

    /**
     * The current level of this skill
     */
    protected int currentLevel;

    /**
     * @param mcRPGPlayer The {@link McRPGPlayer} who owns this {@link OldSkill}
     */
    public OldSkill(McRPGPlayer mcRPGPlayer) {
        this.mcRPGPlayer = mcRPGPlayer;

        this.alreadyUnlockedAbilities = new HashSet<>();

        calculateExpToLevelUp();
    }

    /**
     * @param mcRPGPlayer  The {@link McRPGPlayer} who owns this {@link OldSkill}
     * @param currentExp   The amount of exp this {@link OldSkill} already has
     * @param currentLevel The amount of levels this {@link OldSkill} already has
     */
    public OldSkill(McRPGPlayer mcRPGPlayer, int currentExp, int currentLevel) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.currentExp = currentExp;
        this.currentLevel = currentLevel;

        this.alreadyUnlockedAbilities = new HashSet<>();

        calculateExpToLevelUp();
    }

    /**
     * @param mcRPGPlayer              The {@link McRPGPlayer} who owns this {@link OldSkill}
     * @param currentExp               The amount of exp this {@link OldSkill} already has
     * @param currentLevel             The amount of levels this {@link OldSkill} already has
     * @param alreadyUnlockedAbilities The {@link Set} of {@link AbilityType}s the player has already unlocked
     * @param abilityPointsGained      The amount of ability points this skill has already awarded
     */
    public OldSkill(McRPGPlayer mcRPGPlayer, int currentExp, int currentLevel,
                    Set<AbilityType> alreadyUnlockedAbilities, int abilityPointsGained) {
        this.mcRPGPlayer = mcRPGPlayer;
        this.currentExp = currentExp;
        this.currentLevel = currentLevel;

        this.alreadyUnlockedAbilities = alreadyUnlockedAbilities;
        this.abilityPointsGained = abilityPointsGained;

        calculateExpToLevelUp();
    }

    /**
     * Gets the {@link SkillType} that represents this {@link OldSkill}
     *
     * @return The {@link SkillType} that represents this {@link OldSkill}
     */
    public abstract SkillType getSkillType();

    /**
     * Gets the {@link McRPGPlayer} that owns this skill
     *
     * @return The {@link McRPGPlayer} that owns this skill
     */
    @NotNull
    public McRPGPlayer getMcRPGPlayer() {
        return this.mcRPGPlayer;
    }

    /**
     * Gets the amount of exp this {@link OldSkill} has
     *
     * @return The amount of exp this {@link OldSkill} has
     */
    public int getCurrentExp() {
        return currentExp;
    }

    /**
     * Gets the amount of exp needed to level up this {@link OldSkill}. This amount
     * is cached and if it hasn't been loaded yet, then this method loads and caches it before
     * returning the result.
     *
     * @return The amount of exp needed to level up.
     */
    public int getExpToLevelUp() {

        if (expToLevelUp == -1) {
            calculateExpToLevelUp();
        }

        return expToLevelUp;
    }

    /**
     * Gets the current level that this skill is at
     *
     * @return The current level that this skill is at
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Calculates the amount of experience needed to level up. This runs the equation stored in
     * {@link SkillType#getLevelUpEquation()} to calculate the amount and caches it.
     */
    public void calculateExpToLevelUp() {

        Parser parser = getSkillType().getLevelUpEquation();

        parser.setVariable("current_exp", getCurrentExp());
        parser.setVariable("current_level", getCurrentLevel());

        this.expToLevelUp = (int) Math.ceil(parser.getValue());
    }

    /**
     * Gives the {@link McRPGPlayer} exp to this skill. This amount should be positive as the system
     * doesn't currently support negative values and if it does in the future, it will be its own method.
     * <p>
     * Any negative values passed in will be ignored and turned to 0.
     *
     * @param expToGive The amount of exp to be given
     */
    public void giveExp(int expToGive) {

        expToGive = Math.max(expToGive, 0);
        //TODO
    }

    /**
     * Sets the amount of currentExp a {@link McRPGPlayer} has for this {@link OldSkill}. This amount should be positive
     * and will be turned to 0 if a negative amount is provided.
     *
     * @param currentExp The new amount of current exp
     */
    public void setCurrentExp(int currentExp) {

        currentExp = Math.max(currentExp, 0);
        //TODO
    }

    /**
     * Sets the current level for this {@link OldSkill}. This can not currently bypass the max level for the {@link OldSkill} and
     * can't be negative.
     *
     * @param currentLevel The new current level for this {@link OldSkill}
     */
    public void setCurrentLevel(int currentLevel) {

        currentLevel = Math.max(currentLevel, 0);
        //TODO
    }

    /**
     * Gets the amount of ability points gained from this {@link OldSkill} already to prevent giving double in
     * case of exp or level loss if it is implemented down the road
     *
     * @return The amount of ability points gained from this {@link OldSkill} already
     */
    public int getAbilityPointsGained() {
        return abilityPointsGained;
    }

    /**
     * Gets the {@link Set} of {@link AbilityType}s already unlocked from this {@link OldSkill} to prevent allowing a {@link McRPGPlayer}
     * unlocking the {@link us.eunoians.mcrpg.ability.Ability} twice.
     *
     * @return The {@link Set} of {@link AbilityType}s already unlocked
     */
    @NotNull
    public Set<AbilityType> getAlreadyUnlockedAbilities() {
        return alreadyUnlockedAbilities;
    }
}
