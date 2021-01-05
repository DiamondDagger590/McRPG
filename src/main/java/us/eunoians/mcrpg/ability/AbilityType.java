package us.eunoians.mcrpg.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.SkillType;

/**
 * This enum holds all existing abilities
 *
 * @author DiamondDagger590
 */
public enum AbilityType {

    //Taming Abilities
    GORE(SkillType.TAMING),
    BLEED(SkillType.SWORDS);

    private SkillType skillType;

    AbilityType(SkillType skillType) {
        this.skillType = skillType;
    }

    /**
     * Gets the {@link SkillType} that this {@link AbilityType} belongs to.
     *
     * @return The {@link SkillType} that this {@link AbilityType} belongs to
     */
    @NotNull
    public SkillType getSkillType() {
        return this.skillType;
    }
}
