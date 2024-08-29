package us.eunoians.mcrpg.ability.impl.mining.remotetransfer;

import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.configuration.file.skill.MiningConfigFile;

/**
 * This enum contains all the different categories supported for Remote Transfer
 */
public enum RemoteTransferCategoryType {

    CAVES("Caves", Material.AMETHYST_BLOCK, MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_CAVES),
    CUSTOM("Custom", Material.CRAFTING_TABLE, MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_CUSTOM),
    END("End", Material.ENDER_PEARL, MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_END),
    NETHER("Nether", Material.MAGMA_BLOCK, MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_NETHER),
    OCEAN("Ocean", Material.PRISMARINE, MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_OCEAN),
    ORES("Ores", Material.DIAMOND_ORE, MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_ORES),
    OVERWORLD("Overworld", Material.MOSSY_COBBLESTONE, MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_OVERWORLD),
    TERRACOTTA("Terracotta", Material.TERRACOTTA, MiningConfigFile.REMOTE_TRANSFER_ALLOW_LIST_TERRACOTTA),
    ;

    private final String name;
    private final Material displayMaterial;
    private final Route route;

    RemoteTransferCategoryType(@NotNull String name, @NotNull Material displayMaterial, @NotNull Route route) {
        this.name = name;
        this.displayMaterial = displayMaterial;
        this.route = route;
    }

    /**
     * Gets the display name for the category.
     *
     * @return The display name for the category.
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Gets the display {@link Material} for the category.
     *
     * @return The display {@link Material} for the category.
     */
    @NotNull
    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    /**
     * Gets the {@link Route} containing all the materials for this category.
     *
     * @return The {@link Route} containing all the materials for this category.
     */
    @NotNull
    public Route getConfigurationRoute() {
        return route;
    }
}
