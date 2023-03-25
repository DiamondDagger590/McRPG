package us.eunoians.mcrpg.ability.component;

import org.bukkit.entity.Player;
import us.eunoians.mcrpg.McRPG;

//TODO javadoc
public interface TargetableEntityAbility extends AbilityComponent {

    /**
     * Checks whether the ability item should be able to affect allies
     *
     * @return True if the ability item can affect allies
     */
    boolean affectAllies();

    /**
     * Checks whether the ability item should be able to affect enemies
     *
     * @return True if the ability item can affect enemies
     */
    boolean affectEnemies();

    /**
     * Checks whether all entities should be affected regardless of relation to user
     *
     * @return True if all entities should be affected regardless of relation to user
     */
    default boolean affectAll() {
        return affectAllies() && affectEnemies();
    }

    /**
     * Should activator affect target?
     *
     * @param activator Activator
     * @param target    Target
     * @return True, if activator should affect target.
     */
    default boolean doesAffect(Player activator, Player target) {
        //If we affect all, return true
        if (affectAll()) {
            return true;
        }
        //Check if they're allies
        boolean allies = McRPG.getInstance().getAbilityRegistry().areEntitiesAllied(activator, target).getLeft();

        //Return true if allies we affect allies or not ally and we affect enemies
        return (allies && affectAllies()) || (!allies && affectEnemies());
    }
}
