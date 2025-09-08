package us.eunoians.mcrpg.skill.component.block;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.world.WorldManager;

import java.util.Arrays;
import java.util.Optional;

/**
 * This enum wraps blocks that can be multiple blocks tall and have
 * the entire stack break if a bottom block is broken.
 * <p>
 * This functionality allows for checking if a set of stacked blocks
 * in this manner should award experience for the entire stack or
 * just part of the stack.
 */
public enum MultiBlockType {

    BAMBOO(Material.BAMBOO, 16),
    SUGAR_CANE(Material.SUGAR_CANE, 3),
    CACTUS(Material.CACTUS, 3),
    ;

    private final Material material;
    private final int maxHeight;

    MultiBlockType(Material material, int maxHeight) {
        this.material = material;
        this.maxHeight = maxHeight;
    }

    /**
     * Gets the {@link Material} represented by this multi-block.
     *
     * @return The {@link Material} represented by this multi-block.
     */
    @NotNull
    public Material getMaterial() {
        return material;
    }

    /**
     * Gets how high this multi-block type can get naturally.
     *
     * @return How high this multi-block type can get naturally.
     */
    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Calculates the amount of {@link Block}s that should award experience out of an entire stack of blocks,
     * starting with the provided block.
     *
     * @param block The {@link Block} that is serving as the base.
     * @return The amount of blocks that should award experience out of an entire stack of blocks.
     */
    public int calculateMultiBlockDrops(@NotNull Block block) {
        int result = 0;
        WorldManager worldManager = RegistryAccess.registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.WORLD);
        World world = block.getWorld();
        for (int i = 0; i < maxHeight; i++) {
            Block targetBlock = world.getBlockAt(block.getX(), block.getY() + i, block.getZ());
            if (targetBlock.getType() != material) {
                return result;
            } else if (worldManager.isBlockNatural(targetBlock)) {
                result += 1;
            }
        }
        return result;
    }

    /**
     * Gets the corresponding multi-block type from the provided {@link Block}.
     *
     * @param block The block to get the corresponding multi-block type from.
     * @return An {@link Optional} containing the corresponding multi-block type from
     * the provided {@link Block}, otherwise it will be empty if there are no matches.
     */
    @NotNull
    public static Optional<MultiBlockType> getMultiBlockType(@NotNull Block block) {
        return Arrays.stream(values()).filter(multiBlockType -> multiBlockType.material.equals(block.getType())).findFirst();
    }
}
