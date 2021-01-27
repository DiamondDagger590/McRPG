package us.eunoians.mcrpg.api.event.ability.swords;

import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.ability.impl.swords.taintedblade.TaintedBlade;
import us.eunoians.mcrpg.api.AbilityHolder;
import us.eunoians.mcrpg.api.event.ability.AbilityActivateEvent;

import java.util.Set;

/**
 * This event is called whenever {@link TaintedBlade} activates
 *
 * @author DiamondDagger590
 */
public class TaintedBladeActivateEvent extends AbilityActivateEvent {

    @NotNull
    private final Set<PotionEffect> potionEffects;

    /**
     * @param abilityHolder The {@link AbilityHolder} that is activating the event
     * @param taintedBlade  The {@link TaintedBlade} being activated
     */
    public TaintedBladeActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull TaintedBlade taintedBlade, @NotNull Set<PotionEffect> potionEffects) {
        super(abilityHolder, taintedBlade, AbilityEventType.COMBAT);
        this.potionEffects = potionEffects;
    }

    /**
     * The {@link Ability} that is being activated by this event
     *
     * @return The {@link Ability} that is being activated by this event
     */
    @Override
    public @NotNull TaintedBlade getAbility() {
        return (TaintedBlade) super.getAbility();
    }

    /**
     * Gets the {@link Set} of {@link PotionEffect}s that are being given to the {@link AbilityHolder} from this event
     *
     * @return The {@link Set} of {@link PotionEffect}s that are being given to the {@link AbilityHolder} from this event
     */
    @NotNull
    public Set<PotionEffect> getPotionEffects() {
        return potionEffects;
    }
}
