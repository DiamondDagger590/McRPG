package us.eunoians.mcrpg.entity.holder;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//TODO javadoc
public class SkillHolder extends LoadoutHolder {

    private final Map<NamespacedKey, SkillHolderData> skillData;

    public SkillHolder(@NotNull UUID uuid) {
        super(uuid);
        this.skillData = new HashMap<>();
    }

    public class SkillHolderData {

        private int currentExperience;
        private int experienceForNextLevel;
        private int currentLevel;

        public SkillHolderData(int currentLevel) {
            this(0, Math.max(0, currentLevel));
        }

        public SkillHolderData(int currentExperience, int currentLevel) {
            this.currentExperience = Math.max(0, currentExperience);
            this.currentLevel = Math.max(0, currentLevel);
            updateExperienceForNextLevel();
        }

        public void updateExperienceForNextLevel() {
            experienceForNextLevel = currentLevel * 1000;
        }

        public int getCurrentExperience() {
            return currentExperience;
        }

        public int getExperienceForNextLevel() {
            return experienceForNextLevel;
        }

        public int getCurrentLevel() {
            return currentLevel;
        }

        public void addExperience(int experience) {
            currentExperience += Math.max(0, experience);
            checkForLevelups();
        }

        public void setCurrentExperience(int experience){
            currentExperience = Math.max(0, experience);
            checkForLevelups();
        }

        public void addLevel(int level) {
            addLevel(level, false);
        }

        public void addLevel(int level, boolean resetExpOnLevelUp) {
            currentLevel += Math.max(0, level);
            updateExperienceForNextLevel();

            if(resetExpOnLevelUp) {
                currentExperience = 0;
            }

            checkForLevelups();
        }

        private void checkForLevelups() {
            //do level ups
            while(currentExperience >= experienceForNextLevel) {
                addLevel(1);
                currentExperience -= experienceForNextLevel;
                updateExperienceForNextLevel();
            }
        }
    }
}
