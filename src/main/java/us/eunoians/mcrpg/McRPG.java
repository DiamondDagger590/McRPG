package us.eunoians.mcrpg;

import org.bukkit.plugin.java.JavaPlugin;
import us.eunoians.mcrpg.player.PlayerContainer;

public class McRPG extends JavaPlugin {

    /**
     * Plugin instance
     */
    private static McRPG instance;

    /**
     * The player container, this object holds all the McRPG playesr
     */
    private PlayerContainer playerContainer;

    @Override
    public void onEnable() {
        instance = this;

        this.playerContainer = new PlayerContainer();
    }

    @Override
    public void onDisable() {
    }

    public static McRPG getInstance(){
        return instance;
    }

    /**
     * Get the {@link PlayerContainer}.
     *
     * @return the player container
     */
    public PlayerContainer getPlayerContainer() {
        return playerContainer;
    }
}
