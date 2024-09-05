package us.eunoians.mcrpg.ability.impl.mining.orescanner;

import com.google.common.collect.ImmutableSet;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This record contains a grouping of {@link Material}s that can be scanned by {@link us.eunoians.mcrpg.ability.impl.mining.OreScanner}.
 * <p>
 * If the server is running paper, then the {@link ChatColor} is used by {@link fr.skytasul.glowingentities.GlowingBlocks} in order to make blocks
 * glow on the client.
 * <p>
 * A higher weight means that {@link us.eunoians.mcrpg.ability.impl.mining.OreScanner} will prioritize it over lower weighted scanner
 * types when determining what block to point the player to.
 *
 * @param scannableOres A {@link Set} of {@link Material}s that can be scanned for this type.
 * @param typeName      The name of this scanner type (will be displayed to players).
 * @param color         The {@link ChatColor} to use when highlighting the blocks (only on paper).
 * @param weight        The weight of this scanner type.
 */
public record OreScannerBlockType(@NotNull Set<Material> scannableOres, @NotNull String typeName,
                                  @NotNull ChatColor color, int weight) {

    public OreScannerBlockType(@NotNull Set<Material> scannableOres, @NotNull String typeName, @NotNull ChatColor color, int weight) {
        this.scannableOres = scannableOres;
        this.typeName = typeName;
        this.color = color;
        this.weight = weight;
    }

    /**
     * Gets the {@link Set} of {@link Material}s that can be scanned by {@link us.eunoians.mcrpg.ability.impl.mining.OreScanner}.
     *
     * @return An {@link ImmutableSet} of {@link Material}s that can be scanned by {@link us.eunoians.mcrpg.ability.impl.mining.OreScanner}.
     */
    @Override
    @NotNull
    public Set<Material> scannableOres() {
        return ImmutableSet.copyOf(scannableOres);
    }

    /**
     * Checks to see if the provided {@link Block} can be scanned by this scanner type.
     *
     * @param block The {@link Block} to check.
     * @return {@code true} if the provided {@link Block} can be scanned by this scanner type.
     */
    public boolean isBlockScannable(@NotNull Block block) {
        return scannableOres().contains(block.getType());
    }

    /**
     * Gets the name of this scanner type (will be displayed to players).
     *
     * @return The name of this scanner type (will be displayed to players).
     */
    @Override
    @NotNull
    public String typeName() {
        return typeName;
    }

    /**
     * Gets the {@link ChatColor} to use when highlighting the blocks (only on paper).
     *
     * @return The {@link ChatColor} to use when highlighting the blocks (only on paper).
     */
    @Override
    @NotNull
    public ChatColor color() {
        return color;
    }
}
