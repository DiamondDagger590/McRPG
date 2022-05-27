package us.eunoians.mcrpg.ability.component;

public interface DamageableAbility extends AbilityComponent {

    default boolean shouldCancelDamage() {
        return false;
    }
}
