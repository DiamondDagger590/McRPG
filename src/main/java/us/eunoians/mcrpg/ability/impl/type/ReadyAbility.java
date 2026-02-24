package us.eunoians.mcrpg.ability.impl.type;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.ready.ReadyData;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This interface represents an {@link Ability} that can be "readied",
 * requiring a follow-up action from the user in order to activate.
 */
public interface ReadyAbility extends Ability {

    /**
     * Gets the {@link ReadyData} that is used whenever this ability enters a "ready" state for
     * an {@link AbilityHolder}.
     *
     * @return The {@link ReadyData} that is used whenever this ability enters a "ready" state for
     * an {@link AbilityHolder}.
     */
    @NotNull
    ReadyData getReadyData();
}
