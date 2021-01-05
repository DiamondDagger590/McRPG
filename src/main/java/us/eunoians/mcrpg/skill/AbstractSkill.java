package us.eunoians.mcrpg.skill;

/**
 * The abstract implementation for all {@link us.eunoians.mcrpg.McRPG} skills.
 *
 * @author OxKitsune
 */
public class AbstractSkill {

    /**
     * The id of the skill.
     */
    private final String id;

    /**
     * Construct a new {@link AbstractSkill}.
     *
     * @param id the id of the skill
     */
    public AbstractSkill(String id) {
        this.id = id;
    }

    /**
     * Used to load data from a configuration for this specific skill.
     */
    public void load() {
    }

    /**
     * Get the id of the {@link AbstractSkill}.
     *
     * @return the id of the skill
     */
    public String getId() {
        return id;
    }
}
