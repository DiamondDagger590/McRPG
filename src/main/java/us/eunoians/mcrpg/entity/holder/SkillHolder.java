package us.eunoians.mcrpg.entity.holder;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.api.event.skill.PostSkillGainExpEvent;
import us.eunoians.mcrpg.api.event.skill.SkillGainExpEvent;
import us.eunoians.mcrpg.api.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A skill holder is a more specific type of {@link LoadoutHolder} that possesses {@link Skill}s
 * which can be leveled up, unlocking more {@link us.eunoians.mcrpg.ability.Ability Abilities} in the process.
 * <p>
 * Being a skill holder also comes with the requirement of being a {@link LoadoutHolder}.
 * <p>
 * This class provides storage and tracking of holder specific skill information in the form of
 * {@link SkillHolderData}.
 */
public class SkillHolder extends LoadoutHolder {

    private final Map<NamespacedKey, SkillHolderData> skillData;

    public SkillHolder(@NotNull UUID uuid) {
        super(uuid);
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
     * Creates and stores a {@link SkillHolderData} with a default level and experience of 0 for the provided
     * {@link Skill}.
     *
     * @param skill The {@link Skill} to create and store data for.
     */
    public void addSkillHolderData(@NotNull Skill skill) {
        addSkillHolderData(skill, 1, 0);
    }

    /**
     * Creates and stores a {@link SkillHolderData} with a default experience of 0 and the provided level for the provided
     * {@link Skill}.
     *
     * @param skill        The {@link Skill} to create and store data for.
     * @param currentLevel The skill level to store in the data.
     */
    public void addSkillHolderData(@NotNull Skill skill, int currentLevel) {
        addSkillHolderData(skill, currentLevel, 0);
    }

    /**
     * Creates and stores a {@link SkillHolderData} with experience and levels matching those provided for the provided
     * {@link Skill}.
     *
     * @param skill             The {@link Skill} to create and store data for.
     * @param currentLevel      The skill level to store in the data.
     * @param currentExperience The skill experience to store in the data.
     */
    public void addSkillHolderData(@NotNull Skill skill, int currentLevel, int currentExperience) {
        addSkillHolderData(new SkillHolderData(this, skill.getSkillKey(), currentLevel, currentExperience));
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

    public static class SkillHolderData {

        private final SkillHolder skillHolder;
        private final NamespacedKey skillKey;
        private int currentExperience;
        private int experienceForNextLevel;
        private int currentLevel;

        public SkillHolderData(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, int currentLevel) {
            this(skillHolder, skillKey, Math.max(0, currentLevel), 0);
        }

        public SkillHolderData(@NotNull SkillHolder skillHolder, @NotNull NamespacedKey skillKey, int currentLevel, int currentExperience) {
            this.skillHolder = skillHolder;
            this.skillKey = skillKey;
            this.currentLevel = Math.max(0, currentLevel);
            this.currentExperience = Math.max(0, currentExperience);
            updateExperienceForNextLevel();
        }

        /**
         * Calculates and updates the amount of experience required for the next level up.
         */
        public void updateExperienceForNextLevel() {
            experienceForNextLevel = currentLevel * 1000; //TODO make configurable
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
            return skillKey;
        }

        /**
         * Gets the current experience for the {@link Skill} represented by this data.
         *
         * @return The current experience for the {@link Skill} represented by this data.
         */
        public int getCurrentExperience() {
            return currentExperience;
        }

        /**
         * Gets the amount of experience required for the next level up.
         *
         * @return The amount of experience required for the next level up.
         */
        public int getExperienceForNextLevel() {
            return experienceForNextLevel;
        }

        /**
         * Gets the current level for the {@link Skill} represented by this data.
         *
         * @return The current level for the {@link Skill} represented by this data.
         */
        public int getCurrentLevel() {
            return currentLevel;
        }

        // TODO refactor to SkillHolder?? should the data be responsible for updating itself? probs not lol

        /**
         * Adds the provided amount of experience to the {@link Skill}.
         * <p>
         * This method will call a {@link SkillGainExpEvent}, and only award experience if the event is not cancelled. The method will
         * then also call {@link #checkForLevelups()}.
         *
         * @param experience The amount of experience to add.
         */
        public void addExperience(int experience) {
            SkillGainExpEvent skillGainExpEvent = new SkillGainExpEvent(getSkillHolder(), getSkillKey(), Math.max(0, experience));
            Bukkit.getPluginManager().callEvent(skillGainExpEvent);

            if (skillGainExpEvent.isCancelled()) {
                return;
            }
            currentExperience += skillGainExpEvent.getExperience();
            checkForLevelups();
            Bukkit.getPluginManager().callEvent(new PostSkillGainExpEvent(skillHolder, skillKey));
        }

        /**
         * Directly sets the amount of experience for the {@link Skill}. This method will NOT call a
         * {@link SkillGainExpEvent}, but will still call {@link #checkForLevelups()}.
         *
         * @param experience The amount of experience to set.
         */
        public void setCurrentExperience(int experience) {
            currentExperience = Math.max(0, experience);
            checkForLevelups();
            Bukkit.getPluginManager().callEvent(new PostSkillGainExpEvent(skillHolder, skillKey));
        }

        /**
         * Adds the provided amount of levels to the skill.
         * <p>
         * This method will not add levels past the max level for a given skill.
         *
         * @param level The amount of levels to add.
         */
        public int addLevel(int level) {
            return addLevel(level, false);
        }

        /**
         * Adds the provided amount of levels to the skill. This method can also reset the current experience for a skill
         * after a level up. This prevents any roll over of experience for whatever reason.
         * <p>
         * This method will not add levels past the max level for a given skill.
         * <p>
         * This method will also call a {@link SkillGainLevelEvent} and then also call {@link #checkForLevelups()} as well.
         *
         * @param level             The amount of levels to add.
         * @param resetExpOnLevelUp If the current experience should be reset on level up or not.
         * @return The amount of levels that were actually added (Amount may differ based on event results/max level)
         */
        public int addLevel(int level, boolean resetExpOnLevelUp) {
            int amountOfLevelups = 0;
            Skill skill = McRPG.getInstance().getSkillRegistry().getRegisteredSkill(getSkillKey());
            level = Math.min(level, skill.getMaxLevel() - getCurrentLevel());
            SkillGainLevelEvent skillGainLevelEvent = new SkillGainLevelEvent(getSkillHolder(), skillKey, level);
            Bukkit.getPluginManager().callEvent(skillGainLevelEvent);
            level = Math.min(skillGainLevelEvent.getLevels(), skill.getMaxLevel() - getCurrentLevel());
            currentLevel += level;
            amountOfLevelups += level;
            // Update the amount of experience needed for the next level and check if there is enough experience to do a level up
            updateExperienceForNextLevel();
            amountOfLevelups += checkForLevelups();
            // If we are resetting experience, reset it
            if (resetExpOnLevelUp) {
                currentExperience = 0;
            }
            return amountOfLevelups;
        }

        public void resetSkill() {
            currentExperience = 0;
            currentLevel = 0;
            updateExperienceForNextLevel();
        }

        /**
         * Checks to see if there is enough experience to level up. If so, it will continue trying to
         * level up the skill until there isn't enough experience to level up.
         *
         * @return The amount of levels the holder has gone up by
         */
        private int checkForLevelups() {
            int amountOfLevelups = 0;
            //do level ups
            while (currentExperience >= experienceForNextLevel) {
                currentExperience -= experienceForNextLevel;
                addLevel(1);
                amountOfLevelups++;
                updateExperienceForNextLevel();
            }
            return amountOfLevelups;
        }
    }
}
