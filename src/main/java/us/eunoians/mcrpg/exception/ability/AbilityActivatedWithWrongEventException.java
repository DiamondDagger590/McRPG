package us.eunoians.mcrpg.exception.ability;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.BaseAbility;

public class AbilityActivatedWithWrongEventException extends RuntimeException {

    private final Ability ability;

    public AbilityActivatedWithWrongEventException(@NotNull Ability ability) {
        this.ability = ability;
    }

    public Ability getAbility() {
        return ability;
    }
}
