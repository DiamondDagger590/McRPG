package us.eunoians.mcrpg.task.glow;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.task.core.DelayableCoreTask;
import fr.skytasul.glowingentities.GlowingBlocks;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Set;

/**
 * This task will automatically remove glowing blocks from the player's client
 * after a period of time.
 */
public class BlockRemoveGlowTask extends DelayableCoreTask {

    private final Player player;
    private final Set<Location> locations;

    public BlockRemoveGlowTask(@NotNull Player player, @NotNull Set<Location> locations) {
        this(player, locations, McRPG.getInstance());
    }

    public BlockRemoveGlowTask(@NotNull Player player, @NotNull Set<Location> locations, @NotNull CorePlugin plugin) {
        super(plugin, 10);
        this.player = player;
        this.locations = locations;
    }

    /**
     * Gets the {@link Player} that is having client side blocks displayed.
     *
     * @return The {@link Player} that is having client side blocks displayed.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the {@link Set} of {@link Location}s that are having their glowing state removed.
     *
     * @return The {@link Set} of {@link Location} that are having their glowing state removed.
     */
    @NotNull
    public Set<Location> getLocations() {
        return locations;
    }

    @Override
    public void run() {
        GlowingBlocks glowingBlocks = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.GLOWING).getGlowingBlocks();
        for (Location location : locations) {
            try {
                glowingBlocks.unsetGlowing(location, player);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
