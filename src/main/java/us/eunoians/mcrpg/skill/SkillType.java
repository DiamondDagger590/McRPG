package us.eunoians.mcrpg.skill;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.AbilityType;
import us.eunoians.mcrpg.util.Parser;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum represents a Skill which contains basic information such as all the {@link AbilityType}s that belong
 * to it
 *
 * @author DiamondDagger590
 */
public enum SkillType {

    SWORDS,
    TAMING;

    //A list of abilities that belong to this skill
    @NotNull
    private List<AbilityType> abilities;

    SkillType() {
        this.abilities = new ArrayList<>();
    }

    /**
     * Returns an {@link List} containing all {@link AbilityType}s that belong to the Skill represented
     * by this enum. If the {@link List} is empty, then it attempts to call the internal {@link #loadAbilities()}
     * to load the {@link List}.
     * <p>
     * This at worst has a performance of O(n) where n is the size of {@link AbilityType#values()}, however this is ran once
     * and then cached so there is no performance impact long term.
     * <p>
     * This method can still return an empty {@link List} however if there are no {@link AbilityType}s with this {@link SkillType}
     * mapped to it. If that is the case, then {@link #loadAbilities()} will continually be called. If that does occur, it is due
     * to sloppy coding on our behalf and should never make it into production so it's an edge case that can be ignored.
     *
     * @return An ideally populated {@link List} containing all {@link AbilityType}s that belong to this skill
     */
    @NotNull
    public List<AbilityType> getAbilities() {

        if (abilities.isEmpty()) {
            loadAbilities();
        }

        return this.abilities;
    }

    /**
     * This method returns the {@link Parser} that contains the level up equation for the skill
     *
     * @return The {@link Parser} that contains the level up equation for the skill
     */
    @NotNull
    public Parser getLevelUpEquation() {

        //TODO actually load equation
        return new Parser("");
    }

    /**
     * Returns if this skill is currently enabled or not
     *
     * @return True if the skill is enabled
     */
    public boolean isEnabled() {
        //TODO load from config
        return false;
    }

    /**
     * This method loads abilities into the internal array
     */
    private void loadAbilities() {

        //Loop through all ability types
        for (AbilityType abilityType : AbilityType.values()) {

            //Check to see if the skill types match and store if so
            if (abilityType.getSkillType() == this) {
                abilities.add(abilityType);
            }

        }
    }
}
