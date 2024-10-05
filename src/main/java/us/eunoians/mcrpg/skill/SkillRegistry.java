package us.eunoians.mcrpg.skill;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.event.event.skill.SkillRegisterEvent;
import us.eunoians.mcrpg.event.event.skill.SkillUnregisterEvent;
import us.eunoians.mcrpg.exception.skill.SkillNotRegisteredException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is the main registry where all skills used by McRPG are
 * registered and kept track of. There are a few things to note:
 * <p>
 * 1) Abilities are registered to skills, but skills are not registered to abilities.
 * - This was an issue in the initial iteration of the plugin and was a struggle to work around
 * - The current paradigm is a skill must have abilities but abilities don't have to have skills
 * - Due to this, it makes sense for the skill/ability relationship to be handled in {@link us.eunoians.mcrpg.ability.AbilityRegistry}
 * which is where you can get that information
 * <p>
 * 2) Skills and abilities can be registered independently of each other due to their reliance
 * on {@link NamespacedKey NamespacedKeys}. While you can register them in any order, an ability
 * registered against a skill that is not registered will cause errors to flag in the code at some point.
 */
public class SkillRegistry {

    private final McRPG mcRPG;
    private final Map<NamespacedKey, Skill> skills;

    public SkillRegistry(@NotNull McRPG mcRPG) {
        this.mcRPG = mcRPG;
        this.skills = new HashMap<>();
    }

    /**
     * Registers the provided {@link Skill} to be tracked
     *
     * @param skill The {@link Skill} to track
     */
    public void registerSkill(@NotNull Skill skill) {
        skills.put(skill.getSkillKey(), skill);
        Bukkit.getPluginManager().callEvent(new SkillRegisterEvent(skill));
    }

    /**
     * Gets the registered {@link Skill} that maps to the provided {@link NamespacedKey}.
     * <p>
     * If there is not a registered {@link Skill}, then a {@link SkillNotRegisteredException} will be thrown.
     *
     * @param skillKey The {@link NamespacedKey} to get the registered {@link Skill} for
     * @return The {@link Skill} that maps to the provided {@link NamespacedKey}.
     */
    public Skill getRegisteredSkill(@NotNull NamespacedKey skillKey) {
        if (!isSkillRegistered(skillKey)) {
            throw new SkillNotRegisteredException(skillKey);
        }

        return skills.get(skillKey);
    }

    /**
     * Gets an immutable copy of all registered skill keys.
     *
     * @return An immutable copy of all registered skill keys.
     */
    public Set<NamespacedKey> getRegisteredSkillKeys() {
        return ImmutableSet.copyOf(skills.keySet());
    }


    /**
     * Gets an immutable copy of all registered skills.
     *
     * @return An immutable copy of all registered skills.
     */
    public Set<Skill> getRegisteredSkills() {
        return ImmutableSet.copyOf(skills.values());
    }

    /**
     * Checks to see if the provided {@link Skill} is registered.
     *
     * @param skill The {@link Skill} to check.
     * @return {@code true} if the provided {@link Skill} is registered.
     */
    public boolean isSkillRegistered(@NotNull Skill skill) {
        return isSkillRegistered(skill.getSkillKey());
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} has a registered {@link Skill}.
     *
     * @param skillKey The {@link NamespacedKey} to check.
     * @return {@code true} if the provided {@link NamespacedKey} has a registered {@link Skill}.
     */
    public boolean isSkillRegistered(@NotNull NamespacedKey skillKey) {
        return skills.containsKey(skillKey);
    }

    /**
     * Unregisters the provided {@link Skill}.
     *
     * @param skill The {@link Skill} to unregister
     */
    public void unregisterSkill(@NotNull Skill skill) {
        unregisterSkill(skill.getSkillKey());
    }

    /**
     * Unregisters the provided {@link NamespacedKey}
     *
     * @param skillKey The {@link NamespacedKey} to unregister
     */
    public void unregisterSkill(@NotNull NamespacedKey skillKey) {
        Skill skill = skills.remove(skillKey);
        if (skill != null) {
            Bukkit.getPluginManager().callEvent(new SkillUnregisterEvent(skill));
        }
    }

}
