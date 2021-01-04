package us.eunoians.mcrpg.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;
import us.eunoians.mcrpg.skill.SkillType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class serves as an object that holds data regarding a player
 *
 * @author Kitsune/DiamondDagger590
 */
public class McRPGPlayer {

    /**
     * The {@link Map} containing all {@link Skill}s of a {@link McRPGPlayer}
     */
    private Map<SkillType, Skill> skills;

    /**
     * The {@link UUID} of the {@link Player}
     */
    @NotNull
    private final UUID uniqueId;

    /**
     * Construct a new {@link McRPGPlayer}.
     *
     * @param uniqueId the unique id of the player this object is representing
     */
    public McRPGPlayer(@NotNull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.skills = new HashMap<>();

        //TODO populate skills
    }

    /**
     * Get the {@link UUID} of the {@link Player}.
     *
     * @return the {@link UUID} of the {@link Player}
     */
    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * Gets the players {@link Skill} that is linked to the {@link SkillType} provided
     *
     * @param skillType The {@link SkillType} to get the corresponding {@link Skill} for
     * @return The {@link Skill} linked to the {@link SkillType} provided
     */
    @NotNull
    public Skill getSkill(SkillType skillType) {
        return this.skills.get(skillType);
    }
}
