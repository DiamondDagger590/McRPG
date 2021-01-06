package us.eunoians.mcrpg.skill;

import org.bukkit.NamespacedKey;

/**
 * The abstract implementation for all {@link us.eunoians.mcrpg.McRPG} skills.
 *
 * @author OxKitsune
 */
public class AbstractSkill {

    /**
     * The id of the skill.
     */
    private final NamespacedKey id;

    /**
     * Construct a new {@link AbstractSkill}.
     *
     * @param id the id of the skill
     */
    public AbstractSkill(NamespacedKey id) {
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
    public NamespacedKey getId() {
        return id;
    }
}
