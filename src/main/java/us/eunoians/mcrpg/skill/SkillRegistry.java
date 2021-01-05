package us.eunoians.mcrpg.skill;

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
    private final Map<String, AbstractSkill> registeredSkills;

    /**
     * Construct a new {@link SkillRegistry}.
     */
    public SkillRegistry() {
        this.registeredSkills = new HashMap<>();
    }

    /**
     * Register a skill to the {@link SkillRegistry}.
     *
     * @param id          the id of the skill
     * @param constructor the implementation of the skill itself.
     * @return the skill that got registered
     */
    public AbstractSkill registerSkill(@NotNull String id, Function<String, ? extends AbstractSkill> constructor) {
        if (getSkill(id).isPresent())
            throw new IllegalArgumentException("A skill with id: \"" + id.toLowerCase() + "\" is already registered!");
        return registeredSkills.put(id.toLowerCase(), constructor.apply(id));
    }


    /**
     * Get the registered skill using the skill id.
     *
     * @param skillId the skill id
     * @return an {@link Optional} containing the skill
     */
    public Optional<AbstractSkill> getSkill(String skillId) {

        // We use lowercase to register skills, so assert that the skill is lowercase
        skillId = skillId.toLowerCase();

        if (!registeredSkills.containsKey(skillId)) return Optional.empty();
        return Optional.of(registeredSkills.get(skillId));
    }
}
