package us.eunoians.mcrpg.event.ability.mining;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.ability.impl.mining.OreScanner;
import us.eunoians.mcrpg.ability.impl.mining.orescanner.OreScannerBlockType;
import us.eunoians.mcrpg.event.ability.AbilityActivateEvent;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

import java.util.Map;
import java.util.Set;

/**
 * This event is called after OreScanner performs its scan.
 * <p>
 * Cancellation of this event will not notify the player in any way of the results of the scan.
 */
public class OreScannerActivateEvent extends AbilityActivateEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private static final Ability ORE_SCANNER = McRPG.getInstance().getAbilityRegistry().getRegisteredAbility(OreScanner.ORE_SCANNER_KEY);
    private final Map<OreScannerBlockType, Set<Location>> instancesOfBlocks;
    private boolean cancelled = false;

    public OreScannerActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Map<OreScannerBlockType, Set<Location>> instancesOfBlocks) {
        super(abilityHolder, ORE_SCANNER);
        this.instancesOfBlocks = instancesOfBlocks;
    }

    @NotNull
    @Override
    public OreScanner getAbility() {
        return (OreScanner) super.getAbility();
    }

    /**
     * Gets a map of all {@link OreScannerBlockType}s mapped to block locations that were scanned.
     *
     * @return An {@link ImmutableMap} of all {@link OreScannerBlockType}s mapped to block locations that were scanned.
     */
    @NotNull
    public Map<OreScannerBlockType, Set<Location>> getInstancesOfBlocks() {
        return ImmutableMap.copyOf(instancesOfBlocks);
    }

    /**
     * Gets a {@link Set} of all {@link Location}s that were detected for the given {@link OreScannerBlockType}.
     *
     * @param oreScannerBlockType The {@link OreScannerBlockType} to get the locations for.
     * @return AN {@link ImmutableSet} of all {@link Location}s that were detected for the given {@link OreScannerBlockType}.
     */
    @NotNull
    public Set<Location> getLocationsOfBlockType(@NotNull OreScannerBlockType oreScannerBlockType) {
        return instancesOfBlocks.containsKey(oreScannerBlockType) ? ImmutableSet.copyOf(instancesOfBlocks.get(oreScannerBlockType)) : ImmutableSet.of();
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
