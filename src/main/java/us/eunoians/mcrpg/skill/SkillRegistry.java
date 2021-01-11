package us.eunoians.mcrpg.skill;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * This class acts as central registry for all Skills
 *
 * @author OxKitsune
 */
public class SkillRegistry {

    /**
     * A map that contains all registered skills.
     */
    private final Map<NamespacedKey, AbstractSkill> registeredSkills;

    /**
     * Construct a new {@link SkillRegistry}.
     */
    public SkillRegistry() {
        this.registeredSkills = new HashMap<>();
    }

    /**
     * Register a skill to the {@link SkillRegistry}.
     *
     * @param key          the id of the skill
     * @param constructor the implementation of the skill itself.
     * @return the skill that got registered
     */
    public <T extends AbstractSkill> T registerSkill(@NotNull NamespacedKey key, Function<NamespacedKey, T> constructor) {
        if (getSkill(key).isPresent())
            throw new IllegalArgumentException("A skill with id: \"" + key.toString() + "\" is already registered!");
        T skill =  constructor.apply(key);
        registeredSkills.put(key, skill);
        return skill;
    }


    /**
     * Get the registered skill using the skill id (as namespaced key).
     *
     * @param skillKey the key of the skill
     * @return an {@link Optional} containing the skill
     */
    public Optional<AbstractSkill> getSkill(NamespacedKey skillKey) {
        if (!registeredSkills.containsKey(skillKey)) return Optional.empty();
        return Optional.of(registeredSkills.get(skillKey));
    }
}
