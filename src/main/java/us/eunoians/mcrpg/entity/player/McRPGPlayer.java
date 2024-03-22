package us.eunoians.mcrpg.entity.player;

import com.diamonddagger590.mccore.player.CorePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

import java.util.UUID;

/**
 * The main "player" object for any player who will be playing McRPG.
 * <p>
 * This is also the main access point to a player's skill data through
 * {@link #asSkillHolder()}
 */
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

    /**
     * Gets the {@link SkillHolder} representation of this player, allowing access to McRPG
     * skill functionality.
     *
     * @return The {@link SkillHolder} representation of this player.
     */
    public SkillHolder asSkillHolder() {
        return skillHolder;
    }
}
