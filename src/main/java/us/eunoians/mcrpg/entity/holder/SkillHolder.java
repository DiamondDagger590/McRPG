package us.eunoians.mcrpg.entity.holder;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.api.event.skill.SkillGainExpEvent;
import us.eunoians.mcrpg.api.event.skill.SkillGainLevelEvent;
import us.eunoians.mcrpg.skill.Skill;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

//TODO javadoc
public class SkillHolder extends LoadoutHolder {

    private final Map<NamespacedKey, SkillHolderData> skillData;

    public SkillHolder(@NotNull UUID uuid) {
        super(uuid);
        this.skillData = new HashMap<>();
    }

    public Set<NamespacedKey> getSkills() {
        return skillData.keySet();
    }

    public void addSkillHolderData(@NotNull Skill skill) {
        addSkillHolderData(skill, 0, 0);
    }

    public void addSkillHolderData(@NotNull Skill skill, int currentLevel) {
        addSkillHolderData(skill, currentLevel, 0);
    }

    public void addSkillHolderData(@NotNull Skill skill, int currentLevel, int currentExperience) {
        addSkillHolderData(new SkillHolderData(this, skill.getSkillKey(), currentLevel, currentExperience));
    }

    public void addSkillHolderData(@NotNull SkillHolderData skillHolderData) {
        skillData.put(skillHolderData.getSkillKey(), skillHolderData);
    }

    public Optional<SkillHolderData> getSkillHolderData(@NotNull Skill skill) {
        return getSkillHolderData(skill.getSkillKey());
    }

    public Optional<SkillHolderData> getSkillHolderData(@NotNull NamespacedKey skillKey) {
        return Optional.ofNullable(skillData.get(skillKey));
    }

    public boolean hasSkillHolderData(@NotNull Skill skill) {
        return hasSkillHolderData(skill.getSkillKey());
    }

    public boolean hasSkillHolderData(@NotNull NamespacedKey skillKey) {
        return skillData.containsKey(skillKey);
    }

    public class SkillHolderData {

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

        public void updateExperienceForNextLevel() {
            experienceForNextLevel = currentLevel * 1000; //TODO make configurable
        }

        @NotNull
        public SkillHolder getSkillHolder() {
            return skillHolder;
        }

        @NotNull
        public NamespacedKey getSkillKey() {
            return skillKey;
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
            SkillGainExpEvent skillGainExpEvent = new SkillGainExpEvent(getSkillHolder(), getSkillKey(), Math.max(0, experience));
            Bukkit.getPluginManager().callEvent(skillGainExpEvent);

            if(skillGainExpEvent.isCancelled()) {
                return;
            }

            currentExperience += skillGainExpEvent.getExperience();
            checkForLevelups();
        }

        public void setCurrentExperience(int experience) {
            currentExperience = Math.max(0, experience);
            checkForLevelups();
        }

        public void addLevel(int level) {
            addLevel(level, false);
        }

        public void addLevel(int level, boolean resetExpOnLevelUp) {
            SkillGainLevelEvent skillGainLevelEvent = new SkillGainLevelEvent(getSkillHolder(), skillKey, level);
            Bukkit.getPluginManager().callEvent(skillGainLevelEvent);
            currentLevel += skillGainLevelEvent.getLevels();
            updateExperienceForNextLevel();

            if (resetExpOnLevelUp) {
                currentExperience = 0;
            }

            checkForLevelups();
        }

        private void checkForLevelups() {
            //do level ups
            while (currentExperience >= experienceForNextLevel) {
                addLevel(1);
                currentExperience -= experienceForNextLevel;
                updateExperienceForNextLevel();
            }
        }
    }
}
