package us.eunoians.mcrpg.api.event.swords;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.api.event.McRPGEvent;

import java.util.UUID;

/**
 * This event is called whenever {@link us.eunoians.mcrpg.ability.impl.swords.Bleed} ends for a
 * {@link org.bukkit.entity.LivingEntity}.
 *
 * @author DiamondDagger590
 */
public class BleedEndEvent extends McRPGEvent {

    /**
     * The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} affected.
     *
     * We use a {@link UUID} here because there the entity may be dead or logged off in the case of
     * a player.
     */
    @NotNull
    private final UUID affected;

    /**
     * The {@link UUID} of the {@link org.bukkit.entity.LivingEntity} that inflicted the bleed
     */
    @NotNull
    private final UUID inflicter;

    public BleedEndEvent(@NotNull UUID affected, @NotNull UUID inflicter){
        this.affected = affected;
        this. inflicter = inflicter;
    }

    @NotNull
    public UUID getAffected() {
        return affected;
    }

    @NotNull
    public UUID getInflicter() {
        return inflicter;
    }
}
