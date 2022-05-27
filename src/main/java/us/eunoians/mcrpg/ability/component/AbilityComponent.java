package us.eunoians.mcrpg.ability.component;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.AbilityHolder;

public interface AbilityComponent {

    public void playActivationNoise();

    public void playActivationParticle();

    public void activate(@NotNull AbilityHolder abilityHolder, Object... data);
}
