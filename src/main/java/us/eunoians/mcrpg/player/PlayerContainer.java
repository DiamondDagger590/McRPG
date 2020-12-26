package us.eunoians.mcrpg.player;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerContainer {

    /**
     * A map containing all {@link McRPGPlayer} objects mapped with their respective uuid
     */
    private Map<UUID, McRPGPlayer> playerMap;

    /**
     * Construct a new {@link PlayerContainer}
     */
    public PlayerContainer() {
        this.playerMap = new HashMap<>();
    }

    /**
     * Get the {@link McRPGPlayer} from the player map.
     *
     * @param uniqueId the unique id of the player
     *
     * @return the {@link McRPGPlayer} wrapped in an {@link Optional}.
     */
    public Optional<McRPGPlayer> getPlayer(UUID uniqueId) {
        if (!playerMap.containsKey(uniqueId)) return Optional.empty();
        return Optional.of(playerMap.get(uniqueId));
    }

    /**
     * Get the {@link McRPGPlayer} from the player map.
     *
     * @param player the player
     *
     * @return the {@link McRPGPlayer} wrapped in an {@link Optional}.
     */
    public Optional<McRPGPlayer> getPlayer (@NotNull Player player){
        Validate.isTrue(player != null, "Player cannot be null!");
        return this.getPlayer(player.getUniqueId());
    }
}
