package us.eunoians.mcrpg.skill;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.player.McRPGPlayer;

import java.util.Optional;
import java.util.UUID;

/**
 * Object that holds all the skill progression for a specific {@link AbstractSkill} for a {@link Player}.
 *
 * @author OxKitsune
 */
public class SkillProgression {

    /**
     * The id of the skill this progression object is associated with.
     */
    private final String skillId;

    /**
     * The {@link UUID} of the {@link Player} this progression object is associated with.
     */
    private final UUID playerUniqueId;

    /**
     * The amount of ability points already gained from this {@link AbstractSkill}
     */
    protected int abilityPointsGained;

    /**
     * The highest level the player reached for this {@link AbstractSkill}
     */
    protected int highestLevelReached;

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
     * Construct a new {@link SkillProgression} object for the specified skill and {@link Player}
     *
     * @param skillId        the id of the skill this progression object is associated with
     * @param playerUniqueId the unique id of the player this progression object is associated with
     */
    public SkillProgression(String skillId, UUID playerUniqueId) {
        this.skillId = skillId;
        this.playerUniqueId = playerUniqueId;
    }

    /**
     * Gives the {@link McRPGPlayer} exp to this skill. This amount should be positive as the system
     * doesn't currently support negative values and if it does in the future, it will be its own method.
     * <p>
     * Any negative values passed in will be ignored and turned to 0.
     *
     * @param expToGive the amount of exp to be given
     */
    public void giveExp(int expToGive) {

        expToGive = Math.max(expToGive, 0);
        //TODO: implement logic to give exp to a player that handles leveling
    }

    /**
     * Gives the {@link McRPGPlayer} the provided amount of levels for this {@link OldSkill}. This can not currently
     * bypass the max level for the {@link OldSkill} and can't be negative.
     *
     * @param levelsToGive the amount of levels to be given
     */
    public void giveLevels(int levelsToGive) {

        levelsToGive = Math.max(levelsToGive, 0);
        //TODO: implement logic to give levels to a player that updates exp as well
    }

    /**
     * Get the id of the {@link AbstractSkill} this progression object is associated with.
     *
     * @return the id of the skill
     */
    public String getSkillId() {
        return skillId;
    }

    /**
     * Get the skill this progression object is associated with.
     * This returns an {@link Optional} because the skill can become unavailable.
     *
     * @return an {@link Optional} that contains the skill
     */
    public Optional<AbstractSkill> getSkill() {
        return McRPG.getInstance().getSkillRegistry().getSkill(skillId);
    }

    /**
     * Get the {@link UUID} of the player this progression object is associated with.
     *
     * @return the uuid of the player
     */
    public UUID getPlayerUniqueId() {
        return playerUniqueId;
    }

    /**
     * Get the {@link Player} this progression object is associated with.
     *
     * @return the player
     */
    public Player getPlayer() {
        // TODO: Might cause issues when the player isn't online, for now this will do
        Validate.isTrue(Bukkit.getPlayer(playerUniqueId) != null);
        return Bukkit.getPlayer(playerUniqueId);
    }


    /**
     * Get the amount of ability points the player gained from leveling up the skill this progression object is
     * associated with.
     *
     * @return the amount of ability points the player gained
     */
    public int getAbilityPointsGained() {
        return abilityPointsGained;
    }

    /**
     * Set the amount of ability points the player gained from leveling up the skill this progression object is
     * associated with.
     *
     * @param abilityPointsGained the amount of ability points the player gained in total
     */
    public void setAbilityPointsGained(int abilityPointsGained) {
        this.abilityPointsGained = abilityPointsGained;
    }

    /**
     * Get the highest level the {@link Player} reached for this skill.
     * Players might lose levels for certain abilities. This will always return the highest level the player's ever
     * reached with this skill.
     *
     * @return the highest level the player ever reached for this skill.
     */
    public int getHighestLevelReached() {
        return highestLevelReached;
    }

    /**
     * Set the highest level the {@link Player} has reached for this skill.
     * Players might lose levels for certain abilities. This will always return the highest level the player's ever
     * reached with this skill.
     *
     * @param highestLevelReached the highest level
     */
    public void setHighestLevelReached(int highestLevelReached) {
        this.highestLevelReached = highestLevelReached;
    }

    /**
     * Get the current experience points the {@link Player} has for this skill.
     *
     * @return the amount of experience points the player has
     */
    public int getCurrentExp() {
        return currentExp;
    }

    /**
     * Set the current amount of experience points the {@link Player} has for this skill.
     *
     * @param currentExp the new amount of experience points
     */
    public void setCurrentExp(int currentExp) {
        this.currentExp = Math.max(currentExp, 0);
    }

    /**
     * Get the amount of experience points the {@link Player} needs to level up this skill.
     * This is a cached value and it will only be updated whenever the {@link Player} levels up the skill.
     * A value of {@code -1} indicates that the skill is at the maximum level.
     *
     * @return the amount of experience points required to level up to the next level
     */
    public int getExpToLevelUp() {
        return expToLevelUp;
    }

    /**
     * Set the amount of experience points the {@link Player} needs to level up this skill.
     * This should only be updated whenever the {@link Player} levels up the skill.
     * A value of {@code -1} indicates that the skill is at the maximum level.
     *
     * @param expToLevelUp the amount of experience points required to level up
     */
    public void setExpToLevelUp(int expToLevelUp) {
        this.expToLevelUp = Math.max(expToLevelUp, -1);
    }

    /**
     * Get the current level of skill.
     *
     * @return the current level
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /**
     * Set the current level of the skill.
     *
     * @param level the new level of the skill
     */
    public void setCurrentLevel(int level) {
        this.currentLevel = Math.max(level, 0);
    }
}
