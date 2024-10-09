package us.eunoians.mcrpg.event.entity.player;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

public abstract class McRPGPlayerEvent extends Event {

    private final McRPGPlayer mcRPGPlayer;

    public McRPGPlayerEvent(McRPGPlayer mcRPGPlayer){
        this.mcRPGPlayer = mcRPGPlayer;
    }

    @NotNull
    public final McRPGPlayer getMcRPGPlayer(){
        return mcRPGPlayer;
    }
}
