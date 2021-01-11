package us.eunoians.mcrpg.player;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.AbstractSkill;
import us.eunoians.mcrpg.skill.SkillProgression;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This class serves as an object that holds data regarding a player
 *
 * @author Kitsune/DiamondDagger590
 */
public class McRPGPlayer {

    /**
     * The {@link Map} containing the {@link Player}'s skill progression for each skill
     */
    private Map<NamespacedKey, SkillProgression> skillProgression;

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
        this.skillProgression = new HashMap<>();

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
     * Gets the players {@link SkillProgression} that is linked to the skill-type provided
     *
     * @param skillType the id of the skill
     *
     * @return an {@link Optional} containing the {@link SkillProgression} object for the specified skill.
     */
    public Optional<SkillProgression> getSkillProgression(@NotNull NamespacedKey skillType) {

        if (!skillProgression.containsKey(skillType)) return Optional.empty();
        return Optional.of(skillProgression.get(skillType));
    }

    /**
     * Gets the players {@link SkillProgression} that is linked to the {@link AbstractSkill} provided
     *
     * @param skill the skill
     *
     * @return an {@link Optional} containing the {@link SkillProgression} object for the specified skill
     */
    public Optional<SkillProgression> getSkillProgression(@NotNull AbstractSkill skill) {
        return getSkillProgression(skill.getId());
    }
}
