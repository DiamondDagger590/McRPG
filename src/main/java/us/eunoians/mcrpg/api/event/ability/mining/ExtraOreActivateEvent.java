package us.eunoians.mcrpg.api.event.ability.mining;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.mining.ExtraOre;
import us.eunoians.mcrpg.api.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

public class ExtraOreActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability EXTRA_ORE = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(ExtraOre.EXTRA_ORE_KEY);
    private int dropMultiplier;
    private boolean cancelled = false;

    public ExtraOreActivateEvent(@NotNull AbilityHolder abilityHolder, int dropMultiplier) {
        super(abilityHolder, EXTRA_ORE);
        this.dropMultiplier = Math.max(1, dropMultiplier);
    }

    @NotNull
    @Override
    public ExtraOre getAbility() {
        return (ExtraOre) super.getAbility();
    }

    public int getDropMultiplier() {
        return dropMultiplier;
    }

    public void setDropMultiplier(int dropMultiplier) {
        this.dropMultiplier = Math.max(1, dropMultiplier);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
