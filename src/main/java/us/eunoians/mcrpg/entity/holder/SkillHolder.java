package us.eunoians.mcrpg.entity.holder;

import com.diamonddagger590.mccore.parser.Parser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.event.skill.PostSkillGainExpEvent;
import us.eunoians.mcrpg.event.skill.PostSkillGainLevelEvent;
import us.eunoians.mcrpg.event.skill.SkillGainExpEvent;
import us.eunoians.mcrpg.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A skill holder is a more specific type of {@link LoadoutHolder} that possesses {@link Skill}s
 * which can be leveled up, unlocking more {@link Ability Abilities} in the process.
 * <p>
 * Being a skill holder also comes with the requirement of being a {@link LoadoutHolder}.
 * <p>
 * This class provides storage and tracking of holder specific skill information in the form of
 * {@link SkillHolderData}.
 */
public class SkillHolder extends LoadoutHolder {

    private final Map<NamespacedKey, SkillHolderData> skillData;

    public SkillHolder(@NotNull McRPG mcRPG, @NotNull UUID uuid) {
        super(mcRPG, uuid);
        this.skillData = new HashMap<>();
    }

    /**
     * Gets a {@link Set} of {@link NamespacedKey}s that represent all the {@link Skill}s the holder has.
     *
     * @return A {@link Skill} of {@link NamespacedKey}s that represent all the {@link Skill}s the holder has.
     */
    public Set<NamespacedKey> getSkills() {
        return skillData.keySet();
    }

    /**
     * Creates and stores a {@link SkillHolderData} with 0 total experience for the provided {@link Skill}.
     *
     * @param skill The {@link Skill} to create and store data for.
     */
    public void addSkillHolderData(@NotNull Skill skill) {
        addSkillHolderData(skill, 0);
    }

    /**
     * Creates and stores a {@link SkillHolderData} with the provided total experience for the provided {@link Skill}.
     *
     * @param skill           The {@link Skill} to create and store data for.
     * @param totalExperience The total experience to store in the data.
     */
    public void addSkillHolderData(@NotNull Skill skill, int totalExperience) {
        addSkillHolderData(new SkillHolderData(this, skill, totalExperience));
    }

    /**
     * Creates and stores a {@link SkillHolderData} at the specified level for the provided {@link Skill}.
     * This is primarily useful for testing scenarios where a player needs to be at a specific level.
     *
     * @param skill The {@link Skill} to create and store data for.
     * @param level The level to set the skill to.
     */
    public void addSkillHolderDataAtLevel(@NotNull Skill skill, int level) {
        SkillHolderData data = new SkillHolderData(this, skill, 0);
        data.setToLevel(level);
        addSkillHolderData(data);
    }

    /**
     * Stores the provided {@link SkillHolderData}.
     *
     * @param skillHolderData The {@link SkillHolderData} to store.
     */
    public void addSkillHolderData(@NotNull SkillHolderData skillHolderData) {
        skillData.put(skillHolderData.getSkillKey(), skillHolderData);
    }

    /**
     * Gets an {@link Optional} containing {@link SkillHolderData} that belongs to the provided {@link Skill}.
     *
     * @param skill The {@link Skill} to get the {@link SkillHolderData} of.
     * @return An {@link Optional} containing {@link SkillHolderData} that belongs to the provided {@link Skill} or it will
     * be empty if {@link #hasSkillHolderData(Skill)} returns {@code false}.
     */
    public Optional<SkillHolderData> getSkillHolderData(@NotNull Skill skill) {
        return getSkillHolderData(skill.getSkillKey());
    }

    /**
     * Gets an {@link Optional} containing {@link SkillHolderData} that belongs to the provided {@link NamespacedKey}.
     *
     * @param skillKey The {@link NamespacedKey} of the {@link Skill} to get the {@link SkillHolderData} of.
     * @return An {@link Optional} containing {@link SkillHolderData} that belongs to the provided {@link NamespacedKey} or it will
     * be empty if {@link #hasSkillHolderData(NamespacedKey)} returns {@code false}.
     */
    public Optional<SkillHolderData> getSkillHolderData(@NotNull NamespacedKey skillKey) {
        return Optional.ofNullable(skillData.get(skillKey));
    }

    /**
     * Checks to see if the provided {@link Skill} has any {@link SkillHolderData} stored.
     *
     * @param skill The {@link Skill} to check.
     * @return {@code true} if the provided {@link Skill} has any {@link SkillHolderData} stored.
     */
    public boolean hasSkillHolderData(@NotNull Skill skill) {
        return hasSkillHolderData(skill.getSkillKey());
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} has any {@link SkillHolderData} stored.
     *
     * @param skillKey The {@link NamespacedKey} to check.
     * @return {@code true} if the provided {@link NamespacedKey} has any {@link SkillHolderData} stored.
     */
    public boolean hasSkillHolderData(@NotNull NamespacedKey skillKey) {
        return skillData.containsKey(skillKey);
    }

    /**
     * Invalidates the level cache for all skills held by this holder.
     * This should be called when skill leveling equations change (e.g., on plugin reload).
     */
    public void invalidateAllLevelCaches() {
        for (SkillHolderData data : skillData.values()) {
            data.invalidateLevelCache();
        }
    }

    /**
     * Holds skill-specific data for a {@link SkillHolder}.
     * <p>
     * Level is calculated dynamically from total experience using the skill's leveling equation.
     * This allows server owners to change leveling equations and have player levels update automatically.
     */
    public static class SkillHolderData {

        private final SkillHolder skillHolder;
        private final Skill skill;

        // Total experience ever earned - the source of truth
        private int totalExperience;

        // Cached values - calculated from totalExperience
        private int cachedLevel;
        private int cachedExperienceTowardsNextLevel;
        private int cachedExperienceForNextLevel;
        private boolean levelCacheValid;

        /**
         * Creates a new {@link SkillHolderData} with the provided total experience.
         *
         * @param skillHolder     The {@link SkillHolder} that owns this data.
         * @param skill           The {@link Skill} this data is for.
         * @param totalExperience The total experience ever earned for this skill.
         */
        public SkillHolderData(@NotNull SkillHolder skillHolder, @NotNull Skill skill, int totalExperience) {
            this.skillHolder = skillHolder;
            this.skill = skill;
            this.totalExperience = Math.max(0, totalExperience);
            this.levelCacheValid = false;
            recalculateLevelCache();
        }

        /**
         * Recalculates the cached level and experience values from total experience.
         * This is called when experience changes or when the cache is invalidated.
         */
        private void recalculateLevelCache() {
            LevelCalculationResult result = calculateLevelFromTotalExperience();
            this.cachedLevel = result.level();
            this.cachedExperienceTowardsNextLevel = result.experienceTowardsNextLevel();

            // Calculate experience needed for next level
            Parser levelParser = skill.getLevelUpEquation();
            levelParser.setVariable("skill_level", cachedLevel);
            this.cachedExperienceForNextLevel = (int) levelParser.getValue();

            this.levelCacheValid = true;
        }

        /**
         * Calculates the level from total experience using the skill's leveling equation.
         *
         * @return A {@link LevelCalculationResult} containing the calculated level and remaining experience
         */
        @NotNull
        private LevelCalculationResult calculateLevelFromTotalExperience() {
            int remainingExp = this.totalExperience;
            int level = 0;
            int maxLevel = skill.getMaxLevel();
            Parser parser = skill.getLevelUpEquation();

            while (level < maxLevel) {
                parser.setVariable("skill_level", level);
                int expForThisLevel = (int) parser.getValue();

                if (remainingExp < expForThisLevel) {
                    break;
                }
                remainingExp -= expForThisLevel;
                level++;
            }

            return new LevelCalculationResult(level, remainingExp);
        }

        /**
         * Invalidates the level cache, forcing a recalculation on next access.
         * This should be called when the skill's leveling equation changes (e.g., on plugin reload).
         */
        public void invalidateLevelCache() {
            this.levelCacheValid = false;
        }

        /**
         * Ensures the level cache is valid, recalculating if necessary.
         */
        private void ensureCacheValid() {
            if (!levelCacheValid) {
                recalculateLevelCache();
            }
        }

        /**
         * Gets the {@link SkillHolder} of this data
         *
         * @return The {@link SkillHolder} of this data.
         */
        @NotNull
        public SkillHolder getSkillHolder() {
            return skillHolder;
        }

        /**
         * Gets the {@link NamespacedKey} that represents the {@link Skill} that has data stored.
         *
         * @return The {@link NamespacedKey} that represents the {@link Skill} that has data stored.
         */
        @NotNull
        public NamespacedKey getSkillKey() {
            return skill.getSkillKey();
        }

        /**
         * Gets the total experience ever earned for this skill.
         *
         * @return The total experience ever earned for this skill.
         */
        public int getTotalExperience() {
            return totalExperience;
        }

        /**
         * Gets the current experience towards the next level for the {@link Skill} represented by this data.
         * This is the partial progress towards the next level, not the total experience.
         *
         * @return The current experience towards the next level.
         */
        public int getCurrentExperience() {
            ensureCacheValid();
            return cachedExperienceTowardsNextLevel;
        }

        /**
         * Gets the amount of experience required for the next level up.
         *
         * @return The amount of experience required for the next level up.
         */
        public int getExperienceForNextLevel() {
            ensureCacheValid();
            return cachedExperienceForNextLevel;
        }

        /**
         * Gets the amount of experience the player still has to gain for the next level up.
         *
         * @return The amount of experience required for the next level up.
         */
        public int getRemainingExperienceForNextLevel() {
            ensureCacheValid();
            return cachedExperienceForNextLevel - cachedExperienceTowardsNextLevel;
        }

        /**
         * Gets the current level for the {@link Skill} represented by this data.
         * The level is dynamically calculated from total experience using the skill's leveling equation.
         *
         * @return The current level for the {@link Skill} represented by this data.
         */
        public int getCurrentLevel() {
            ensureCacheValid();
            return cachedLevel;
        }

        /**
         * Adds the provided amount of experience to the {@link Skill}.
         * <p>
         * This method will call a {@link SkillGainExpEvent}, and only award experience if the event is not canceled.
         *
         * @param experience The amount of experience to add.
         * @return The amount of experience that was unused (when at max level).
         */
        public int addExperience(int experience) {
            ensureCacheValid();
            if (cachedLevel >= skill.getMaxLevel()) {
                return experience;
            } else if (experience <= 0) {
                return 0;
            }

            SkillGainExpEvent skillGainExpEvent = new SkillGainExpEvent(getSkillHolder(), getSkillKey(), Math.max(0, experience));
            Bukkit.getPluginManager().callEvent(skillGainExpEvent);
            if (skillGainExpEvent.isCancelled()) {
                return experience;
            }

            int previousLevel = cachedLevel;
            experience = skillGainExpEvent.getExperience();

            // Add to total experience
            totalExperience += experience;

            // Recalculate level from new total
            recalculateLevelCache();

            // Calculate leftover experience (experience beyond max level)
            int leftoverExperience = 0;
            if (cachedLevel >= skill.getMaxLevel()) {
                // Calculate total XP needed for max level
                int totalExpForMaxLevel = calculateTotalExperienceForLevel(skill.getMaxLevel());
                leftoverExperience = Math.max(0, totalExperience - totalExpForMaxLevel);
                // Cap total experience at max level
                totalExperience = totalExpForMaxLevel;
                recalculateLevelCache();
            }

            // Fire level up event if level changed
            int levelsGained = cachedLevel - previousLevel;
            if (levelsGained > 0) {
                SkillGainLevelEvent skillGainLevelEvent = new SkillGainLevelEvent(getSkillHolder(), getSkillKey(), levelsGained);
                Bukkit.getPluginManager().callEvent(skillGainLevelEvent);
                Bukkit.getPluginManager().callEvent(new PostSkillGainLevelEvent(skillHolder, getSkillKey(), previousLevel, cachedLevel));
            }

            Bukkit.getPluginManager().callEvent(new PostSkillGainExpEvent(skillHolder, getSkillKey()));
            return leftoverExperience;
        }

        /**
         * Directly sets the total experience for the {@link Skill}. This method will NOT call a
         * {@link SkillGainExpEvent}.
         *
         * @param experience The total experience to set.
         */
        public void setTotalExperience(int experience) {
            int previousLevel = getCurrentLevel();
            totalExperience = Math.max(0, experience);
            recalculateLevelCache();

            int levelsGained = cachedLevel - previousLevel;
            if (levelsGained > 0) {
                Bukkit.getPluginManager().callEvent(new PostSkillGainLevelEvent(skillHolder, getSkillKey(), previousLevel, cachedLevel));
            }
            Bukkit.getPluginManager().callEvent(new PostSkillGainExpEvent(skillHolder, getSkillKey()));
        }

        /**
         * Adds the provided amount of levels to the skill by adding the equivalent experience.
         * <p>
         * This method will not add levels past the max level for a given skill.
         *
         * @param levels The amount of levels to add.
         * @return The amount of levels that were actually added.
         */
        public int addLevels(int levels) {
            return addLevels(levels, false);
        }

        /**
         * Adds the provided amount of levels to the skill by adding the equivalent experience.
         * <p>
         * This method will not add levels past the max level for a given skill.
         *
         * @param levels            The amount of levels to add.
         * @param resetExpOnLevelUp If true, sets experience to exactly the start of the new level (no partial progress).
         * @return The amount of levels that were actually added.
         */
        public int addLevels(int levels, boolean resetExpOnLevelUp) {
            if (levels <= 0) {
                return 0;
            }

            ensureCacheValid();
            int previousLevel = cachedLevel;

            // Calculate how much experience we need to add to gain these levels
            int targetLevel = Math.min(cachedLevel + levels, skill.getMaxLevel());
            int experienceToAdd = calculateTotalExperienceForLevel(targetLevel) - totalExperience;

            if (resetExpOnLevelUp) {
                // Set to exactly the start of the target level
                totalExperience = calculateTotalExperienceForLevel(targetLevel);
            } else {
                // Add just enough to reach the target level, keeping partial progress
                totalExperience += Math.max(0, experienceToAdd);
            }

            recalculateLevelCache();

            int levelsGained = cachedLevel - previousLevel;
            if (levelsGained > 0) {
                SkillGainLevelEvent skillGainLevelEvent = new SkillGainLevelEvent(getSkillHolder(), getSkillKey(), levelsGained);
                Bukkit.getPluginManager().callEvent(skillGainLevelEvent);
                Bukkit.getPluginManager().callEvent(new PostSkillGainLevelEvent(skillHolder, getSkillKey(), previousLevel, cachedLevel));
            }

            return levelsGained;
        }

        /**
         * Resets this skill back to 0 total experience (level 0).
         */
        public void resetSkill() {
            totalExperience = 0;
            recalculateLevelCache();
        }

        /**
         * Calculates the total experience needed to reach a specific level.
         *
         * @param targetLevel The level to calculate total experience for.
         * @return The total experience needed to reach the target level.
         */
        public int calculateTotalExperienceForLevel(int targetLevel) {
            int total = 0;
            Parser parser = skill.getLevelUpEquation();
            for (int level = 0; level < targetLevel && level < skill.getMaxLevel(); level++) {
                parser.setVariable("skill_level", level);
                total += (int) parser.getValue();
            }
            return total;
        }

        /**
         * Calculates how much additional experience is needed to gain a specific number of levels from current level.
         *
         * @param levels The number of levels to calculate experience for.
         * @return The experience needed to gain the specified number of levels.
         */
        public int calculateExperienceNeededToGainLevels(int levels) {
            ensureCacheValid();
            int targetLevel = Math.min(cachedLevel + levels, skill.getMaxLevel());
            return calculateTotalExperienceForLevel(targetLevel) - totalExperience;
        }

        /**
         * Sets the skill to a specific level by calculating and setting the total experience needed.
         * This is primarily useful for testing scenarios.
         *
         * @param level The level to set the skill to.
         */
        public void setToLevel(int level) {
            totalExperience = calculateTotalExperienceForLevel(Math.min(level, skill.getMaxLevel()));
            recalculateLevelCache();
        }

        /**
         * Record for holding level calculation results.
         */
        private record LevelCalculationResult(int level, int experienceTowardsNextLevel) {
        }
    }
}
