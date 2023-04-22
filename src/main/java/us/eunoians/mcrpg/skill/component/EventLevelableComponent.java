package us.eunoians.mcrpg.skill.component;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public interface EventLevelableComponent {

    public boolean shouldGiveExperience(@NotNull AbilityHolder abilityHolder, @NotNull Event event);

    public int calculateExperienceToGive(@NotNull AbilityHolder abilityHolder, @NotNull Event event);
}
