package us.eunoians.mcrpg;

import org.bukkit.plugin.java.JavaPlugin;

public class McRPG extends JavaPlugin {

    private static McRPG instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
    }

    public static McRPG getInstance(){
        return instance;
    }
}
