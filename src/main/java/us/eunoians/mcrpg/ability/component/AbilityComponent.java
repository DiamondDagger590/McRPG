package us.eunoians.mcrpg.ability.component;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

//TODO javadoc
public interface AbilityComponent {

    public void playActivationNoise();

    public void playActivationParticle();

    public void activate(@NotNull AbilityHolder abilityHolder, Event activatingEvent);
}
