package us.eunoians.mcrpg.ability.impl.swords;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.component.activatable.EventActivatableComponent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This class contains all the components needed to activate {@link RageSpike}.
 */
public class RageSpikeComponents {

    public static final RageSpikeActivateComponent RAGE_SPIKE_ACTIVATE_COMPONENT = new RageSpikeActivateComponent();

    private static class RageSpikeActivateComponent implements EventActivatableComponent {

        @Override
        public boolean shouldActivate(@NotNull AbilityHolder abilityHolder, @NotNull Event event) {
            // This comes after the check of the player being ready so we can go ahead and assume theyre already ready
            return event instanceof PlayerToggleSneakEvent playerToggleSneakEvent && playerToggleSneakEvent.isSneaking()
                    && !playerToggleSneakEvent.isCancelled() && Bukkit.getEntity(abilityHolder.getUUID()) instanceof Player player;
        }
    }
}
