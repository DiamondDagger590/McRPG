package us.eunoians.mcrpg.entity.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

import java.util.UUID;

//TODO javadoc
public class McRPGPlayer extends CorePlayer {

    private SkillHolder skillHolder;

    public McRPGPlayer(@NotNull Player player) {
        super(player.getUniqueId());
        skillHolder = new SkillHolder(getUUID());
    }

    public McRPGPlayer(@NotNull UUID uuid) {
        super(uuid);
        skillHolder = new SkillHolder(getUUID());
    }

    @Override
    public boolean useMutex() {
        return false;
    }

    public SkillHolder asSkillHolder(){
        return skillHolder;
    }
}
