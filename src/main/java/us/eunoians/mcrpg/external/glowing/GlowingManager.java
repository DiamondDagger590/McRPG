package us.eunoians.mcrpg.external.glowing;

import com.diamonddagger590.mccore.registry.manager.Manager;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

/**
 * This manager handles integration with the Glowing Blocks
 * library.
 */
public final class GlowingManager extends Manager<McRPG> {

    private GlowingBlocks glowingBlocks;
    private GlowingEntities glowingEntities;

    public GlowingManager(@NotNull McRPG plugin) {
        super(plugin);
        glowingBlocks = new GlowingBlocks(plugin);
        glowingEntities = new GlowingEntities(plugin);
    }

    /**
     * Shuts down the Glowing Blocks integration.
     */
    public void shutdown() {
        glowingBlocks.disable();
        glowingEntities.disable();
    }

    /**
     * Gets the {@link GlowingBlocks} instance of this manager.
     *
     * @return The {@link GlowingBlocks} instance of this manager.
     */
    @NotNull
    public GlowingBlocks getGlowingBlocks() {
        return glowingBlocks;
    }

    /**
     * Gets the {@link GlowingEntities} instance of this manager.
     *
     * @return The {@link GlowingEntities} instance of this manager.
     */
    @NotNull
    public GlowingEntities getGlowingEntities() {
        return glowingEntities;
    }
}
