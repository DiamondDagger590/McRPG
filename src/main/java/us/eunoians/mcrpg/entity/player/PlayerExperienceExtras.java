package us.eunoians.mcrpg.entity.player;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * This class contains all the different types of "experience" a player can have.
 * <p>
 * Typically, when experience is mentioned in McRPG, it is tied directly to something like a
 * {@link us.eunoians.mcrpg.skill.Skill}. This class contains all experience that belongs to the player
 * but not some further specific attribute of a player. These types of experience tend to be "consumed"
 *  to speed up the progress of leveling some attribute of a player such as a Skill.
 * <p>
 * The player directly consumes redeemable experience/levels to increase the experience and level
 * of their given skills.
 * <p>
 * Boosted experience represents a "bank" of experience that will be used to speed up experience gain from
 * skills. Given a scenario where boosted experience has a 2x consumption rate, 1000 boosted experience stored and 100 experience
 * gained in a skill, the experience gained will be increased to a total of 200 while the remaining boosted experience is decremented
 * to a total of 900. For more on how boosted experience works, see {@link us.eunoians.mcrpg.skill.experience.modifier.BoostedExperienceModifier}.
 * <p>
 * Rested experience is accumulated while a player is offline or within "safe spaces" (probably will be a feature?). This
 * experience is relative, unlike boosted experience and is more easily gained. Rested experience stores a total percentage
 * of a level that a player can gain rested experience for. So a player might have 1.5 levels of rested experience that applies a
 * 50% boost, and if they need 1000 experience to go from level 1 to 2, and they gain 200 experience, then that experience
 * will get increased by 100. Since 100 is 10% of 1000, then 10% of a level's worth of experience is removed from the player's
 * rested experience, bringing their total down to 1.4 levels worth of rested experience.
 */
public class PlayerExperienceExtras {

    private int redeemableExperience;
    private int redeemableLevels;
    private int boostedExperience;
    private float restedExperience;

    public PlayerExperienceExtras() {
        this.redeemableExperience = 0;
        this.redeemableLevels = 0;
        this.boostedExperience = 0;
        this.restedExperience = 0;
    }

    public PlayerExperienceExtras(int redeemableExperience, int redeemableLevels, int boostedExperience, float restedExperience) {
        this.redeemableExperience = redeemableExperience;
        this.redeemableLevels = redeemableLevels;
        this.boostedExperience = boostedExperience;
        this.restedExperience = restedExperience;
    }

    /**
     * Gets the amount of redeemable experience a player has to spend.
     *
     * @return The amount of redeemable experience a player has to spend.
     */
    public int getRedeemableExperience() {
        return redeemableExperience;
    }

    /**
     * Sets the amount of redeemable experience a player has to spend.
     *
     * @param redeemableExperience The amount of redeemable experience a player will have to spend. This number can't
     *                             be a negative value.
     */
    public void setRedeemableExperience(int redeemableExperience) {
        this.redeemableExperience = Math.max(0, redeemableExperience);
    }

    /**
     * Modifies the amount of redeemable experience a player has to spend.
     *
     * @param redeemableExperience The amount of redeemable experience to modify the total sum by for the player. A negative
     *                             value will be subtracted while a positive value will be added.
     */
    public void modifyRedeemableExperience(int redeemableExperience) {
        this.redeemableExperience = Math.max(0, this.redeemableExperience + redeemableExperience);
    }

    /**
     * Gets the amount of redeemable levels a player has to spend.
     *
     * @return The amount of redeemable levels a player has to spend.
     */
    public int getRedeemableLevels() {
        return redeemableLevels;
    }

    /**
     * Sets the amount of redeemable levels a player has to spend.
     *
     * @param redeemableLevels The amount of redeemable levels to a player will have to spend. This
     *                         number can't be a negative value.
     */
    public void setRedeemableLevels(int redeemableLevels) {
        this.redeemableLevels = Math.max(0, redeemableLevels);
    }

    /**
     * Modifies the amount of redeemable levels the player has to spend by adding the provided
     * count of levels to their current count. The result will be clamped at zero at minimum.
     *
     * @param redeemableLevels The amount of redeemable levels to modify the player's count by.
     *                         This value can be negative.
     */
    public void modifyRedeemableLevels(int redeemableLevels) {
        this.redeemableLevels = Math.max(0, this.redeemableLevels + redeemableLevels);
    }

    /**
     * Gets the amount of boosted experience the player has.
     *
     * @return The amount of boosted experience the player has.
     */
    public int getBoostedExperience() {
        return boostedExperience;
    }

    /**
     * Sets the amount of boosted experience the player has.
     *
     * @param boostedExperience The amount of boosted experience to set for the player.
     */
    public void setBoostedExperience(int boostedExperience) {
        this.boostedExperience = Math.max(0, boostedExperience);
    }

    /**
     * Modifies the amount of boosted experience the player has by adding the provided
     * amount to the player's current amount. The result will be clamped at zero at minimum.
     *
     * @param boostedExperience The amount of boosted experience to modify the player's count by.
     *                          This value can be negative.
     */
    public void modifyBoostedExperience(int boostedExperience) {
        this.boostedExperience = Math.max(0, this.boostedExperience + boostedExperience);
    }

    /**
     * Gets the amount of rested experience the player has.
     *
     * @return The amount of rested experience the player has.
     */
    public float getRestedExperience() {
        return restedExperience;
    }

    /**
     * Sets the amount of rested experience the player has.
     *
     * @param restedExperience The amount of rested experience to set for the player.
     */
    public void setRestedExperience(float restedExperience) {
        this.restedExperience = Math.max(0, restedExperience);
    }

    /**
     * Modifies the amount of rested experience the player has by adding the provided
     * amount to the player's current amount. The result will be clamped at zero at minimum.
     *
     * @param restedExperience The amount of rested experience to modify the player's count by.
     *                         This value can be negative.
     */
    public void modifyRestedExperience(float restedExperience) {
        this.restedExperience = Math.max(0, this.restedExperience + restedExperience);
    }

    /**
     * Resets all of a player's extra experience info including their redeemable, rested and boosted
     * experience.
     */
    public void reset() {
        this.redeemableExperience = 0;
        this.redeemableLevels = 0;
        this.boostedExperience = 0;
        this.restedExperience = 0;
    }

    /**
     * Copies the provided {@link PlayerExperienceExtras} and overwrites this instance to
     * contain the same internal values.
     *
     * @param playerExperienceExtras The extras to copy into this instance.
     */
    public void copyExtras(@NotNull PlayerExperienceExtras playerExperienceExtras) {
        this.redeemableExperience = playerExperienceExtras.getRedeemableExperience();
        this.redeemableLevels = playerExperienceExtras.getRedeemableLevels();
        this.boostedExperience = playerExperienceExtras.getBoostedExperience();
        this.restedExperience = playerExperienceExtras.getRestedExperience();
    }
}
