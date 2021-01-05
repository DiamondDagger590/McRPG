package us.eunoians.mcrpg;

import org.bukkit.plugin.java.JavaPlugin;
import us.eunoians.mcrpg.player.PlayerContainer;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.skill.impl.Swords;
import us.eunoians.mcrpg.skill.impl.Taming;

public class McRPG extends JavaPlugin {

    /**
     * Plugin instance
     */
    private static McRPG instance;

    /**
     * The player container, this object holds all the McRPG playesr
     */
    private PlayerContainer playerContainer;

    /**
     * The central skill registry object
     */
    private SkillRegistry skillRegistry;

    @Override
    public void onEnable() {
        instance = this;

        this.playerContainer = new PlayerContainer();
        this.skillRegistry = new SkillRegistry();


        // TODO: Move this to an appropriate place
        // Register skills
        McRPG.getInstance().getSkillRegistry().registerSkill("taming", Taming::new);
        McRPG.getInstance().getSkillRegistry().registerSkill("swords", Swords::new);
    }

    @Override
    public void onDisable() {
    }

    /**
     * Get the {@link McRPG} plugin instance.
     *
     * @return the plugin instance
     */
    public static McRPG getInstance() {
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

    /**
     * Get the {@link SkillRegistry} object that can be used to register skills to McRPG.
     *
     * @return the skill registry object
     */
    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }
}
