package us.eunoians.mcrpg.skill;

import com.diamonddagger590.mccore.registry.Registry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.event.skill.SkillRegisterEvent;
import us.eunoians.mcrpg.event.skill.SkillUnregisterEvent;
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
public class SkillRegistry implements Registry<Skill> {

    private final Map<NamespacedKey, Skill> skills;
    private final Map<Class<? extends Skill>, Skill> skillsByClass;

    public SkillRegistry() {
        this.skills = new HashMap<>();
        this.skillsByClass = new HashMap<>();
    }

    /**
     * Registers the provided {@link Skill} to be tracked
     *
     * @param skill The {@link Skill} to track
     */
    public void register(@NotNull Skill skill) {
        if (skills.containsKey(skill.getSkillKey())) {
            throw new IllegalArgumentException("Skill " + skill.getSkillKey() + " already registered");
        }
        skills.put(skill.getSkillKey(), skill);
        skillsByClass.put(skill.getClass(), skill);
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
        if (!registered(skillKey)) {
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
        return Set.copyOf(skills.keySet());
    }


    /**
     * Gets an immutable copy of all registered skills.
     *
     * @return An immutable copy of all registered skills.
     */
    public Set<Skill> getRegisteredSkills() {
        return Set.copyOf(skills.values());
    }

    /**
     * Checks to see if the provided {@link Skill} is registered.
     *
     * @param skill The {@link Skill} to check.
     * @return {@code true} if the provided {@link Skill} is registered.
     */
    public boolean registered(@NotNull Skill skill) {
        return registered(skill.getSkillKey());
    }

    /**
     * Checks to see if the provided {@link NamespacedKey} has a registered {@link Skill}.
     *
     * @param skillKey The {@link NamespacedKey} to check.
     * @return {@code true} if the provided {@link NamespacedKey} has a registered {@link Skill}.
     */
    public boolean registered(@NotNull NamespacedKey skillKey) {
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
            skillsByClass.remove(skill.getClass());
            Bukkit.getPluginManager().callEvent(new SkillUnregisterEvent(skill));
        }
    }

    /**
     * Gets the {@link Skill} belonging to the provided {@link SkillKey}.
     * <p>
     * This method provides type-safe access to skills without requiring casting.
     *
     * @param skillKey The key to get the corresponding {@link Skill}.
     * @param <T>      The implementation of {@link Skill} which is being returned.
     * @return The {@link Skill} belonging to the provided {@link SkillKey}.
     * @throws IllegalStateException If the provided {@link SkillKey} doesn't have
     *                               a corresponding {@link Skill} registered.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends Skill> T skill(@NotNull SkillKey<T> skillKey) {
        Skill skill = skillsByClass.get(skillKey.skillClass());
        if (skill == null) {
            throw new IllegalStateException("Skill not registered: " + skillKey.skillClass().getSimpleName());
        }
        return (T) skill;
    }

    /**
     * Checks to see if the provided {@link SkillKey} has a corresponding {@link Skill} registered.
     *
     * @param skillKey The {@link SkillKey} to check.
     * @return {@code true} if the skill is registered, {@code false} otherwise.
     */
    public boolean registered(@NotNull SkillKey<?> skillKey) {
        return skillsByClass.containsKey(skillKey.skillClass());
    }

}
