package us.eunoians.mcrpg.mutex;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class CorePlayer extends Mutexable {

    private final UUID uuid;

    public CorePlayer(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    @Nullable
    public Player getAsBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void loadPlayerMutex() {

    }

    public void savePlayerMutex() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + getUUID().hashCode();
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {

        if(obj instanceof CorePlayer){

            CorePlayer corePlayer = (CorePlayer) obj;

            return corePlayer.getUUID().equals(getUUID());
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CorePlayer - [uuid=" + this.uuid + "]";
    }
}
