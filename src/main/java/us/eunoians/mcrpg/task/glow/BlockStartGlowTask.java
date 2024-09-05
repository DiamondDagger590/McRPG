package us.eunoians.mcrpg.task.glow;

import com.diamonddagger590.mccore.task.core.CoreTask;
import fr.skytasul.glowingentities.GlowingBlocks;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.impl.mining.orescanner.OreScannerBlockType;

import java.util.Set;

/**
 * This task will make blocks at {@link Location}s having a glowing effect for a given {@link Player}.
 * The color of the glowing effect is the determined by the {@link org.bukkit.ChatColor} from the
 * {@link OreScannerBlockType}.
 */
public class BlockStartGlowTask extends CoreTask {

    private final Player player;
    private final OreScannerBlockType scannerBlockType;
    private final Set<Location> locations;

    public BlockStartGlowTask(@NotNull Player player, @NotNull OreScannerBlockType oreScannerBlockType, @NotNull Set<Location> locations) {
        this(player, oreScannerBlockType, locations, McRPG.getInstance());
    }

    public BlockStartGlowTask(@NotNull Player player, @NotNull OreScannerBlockType oreScannerBlockType, @NotNull Set<Location> locations, @NotNull Plugin plugin) {
        super(plugin);
        this.player = player;
        this.scannerBlockType = oreScannerBlockType;
        this.locations = locations;
    }

    /**
     * Gets the {@link Player} to show glowing blocks to.
     *
     * @return The {@link Player} to show glowing blocks to.
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the {@link OreScannerBlockType} to get the {@link org.bukkit.ChatColor} for
     * coloring the glow from.
     *
     * @return The {@link OreScannerBlockType} to get the {@link org.bukkit.ChatColor} for
     * coloring the glow from.
     */
    @NotNull
    public OreScannerBlockType getScannerBlockType() {
        return scannerBlockType;
    }

    /**
     * A {@link Set} of all {@link Location}s to show as glowing for the player.
     *
     * @return A {@link Set} of all {@link Location}s to show as glowing for the player.
     */
    @NotNull
    public Set<Location> getLocations() {
        return locations;
    }

    @Override
    public void run() {
        GlowingBlocks glowingBlocks = McRPG.getInstance().getGlowingBlocks();
        for (Location location : locations) {
            try {
                glowingBlocks.setGlowing(location, getPlayer(), scannerBlockType.color());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
