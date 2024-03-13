package us.eunoians.mcrpg.skill.impl.mining;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.skill.Skill;

public class Mining extends Skill {

    public static final NamespacedKey MINING_KEY = new NamespacedKey(McRPG.getInstance(), "mining");

    public Mining() {
        super(MINING_KEY);
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Mining";
    }
}
