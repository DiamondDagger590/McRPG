package us.eunoians.mcrpg.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class McRPGPlayer {

    /**
     * The {@link UUID} of the {@link Player}
     */
    @NotNull
    private final UUID uniqueId;

    /**
     * Construct a new {@link McRPGPlayer}.
     *
     * @param uniqueId the unique id of the player this object is representing
     */
    public McRPGPlayer(@NotNull UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * Get the {@link UUID} of the {@link Player}.
     *
     * @return the {@link UUID} of the {@link Player}
     */
    @NotNull
    public UUID getUniqueId() {
        return uniqueId;
    }
}
