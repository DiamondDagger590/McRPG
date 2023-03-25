package us.eunoians.mcrpg.ability.component;

//TODO javadoc
public interface DamageableAbility extends AbilityComponent {

    default boolean shouldCancelDamage() {
        return false;
    }
}
