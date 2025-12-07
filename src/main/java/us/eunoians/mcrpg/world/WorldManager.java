package us.eunoians.mcrpg.world;

import com.diamonddagger590.mccore.configuration.collection.ReloadableSet;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.registry.manager.ManagerKey;
import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This manager handles various tracking various states regarding
 * {@link World}s.
 * <p>
 * The main usage is to check if a block is "natural" or not. A natural
 * block by definition for McRPG is one that is not placed by a player. Blocks
 * created by world generation, cobble farms etc are all valid.
 */
public class WorldManager extends Manager<McRPG> {

    private static final NamespacedKey PLACED_KEY = new NamespacedKey(McRPG.getInstance(), "placed");

    private final ReloadableSet<String> worlds;

    public WorldManager(@NotNull McRPG plugin) {
        super(plugin);
        this.worlds = new ReloadableSet<>(plugin().registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG), MainConfigFile.DISABLED_WORLDS,
                (strings -> strings.stream()
                        .map(string -> string.toLowerCase(Locale.ROOT))
                        .collect(Collectors.toSet())
                ));
        plugin().registryAccess().registry(RegistryKey.MANAGER).manager(ManagerKey.RELOADABLE_CONTENT).trackReloadableContent(worlds);
    }

    /**
     * Checks to see if the provided {@link Block} is natural or not.
     *
     * @param block The block to check.
     * @return {@code true} if the provided {@link Block} is natural.
     */
    public boolean isBlockNatural(@NotNull Block block) {
        CustomBlockData customBlockData = new CustomBlockData(block, plugin());
        return !customBlockData.has(PLACED_KEY) || !customBlockData.get(PLACED_KEY, PersistentDataType.BOOLEAN);
    }

    /**
     * Updates the state of the provided {@link Block} to mark it as placed or natural.
     *
     * This can be overridden by player activity such as breaking or placing blocks. Ensure
     * the updated value is checked for the same block in the future via {@link #isBlockNatural(Block)} in
     * case it has since been updated.
     * @param block The {@link Block} to update the state for.
     * @param placed {@code true} if the block should be marked as manually placed, or {@code false} to mark
     *                           it as natural.
     */
    public void setBlockPlacedState(@NotNull Block block, boolean placed) {
        CustomBlockData customBlockData = new CustomBlockData(block, plugin());
        customBlockData.set(PLACED_KEY, PersistentDataType.BOOLEAN, placed);
    }

    /**
     * Checks if the provided {@link World} is currently disabled for McRPG usage.
     *
     * @param world The {@link World} to check.
     * @return {@code true} if the provided {@link World} is currently disabled for McRPG usage.
     */
    public boolean isWorldDisabled(@NotNull World world) {
        return worlds.getContent().contains(world.getName().toLowerCase(Locale.ROOT));
    }

    /**
     * Checks various criteria to see if {@link McRPG} is able to be activated at given {@link Location}.
     *
     * @param location The {@link Location} to check.
     * @return {@code true} if the provided {@link Location} is valid for McRPG to be used.
     */
    public boolean isMcRPGEnabledForLocation(@NotNull Location location) {
        return !isWorldDisabled(location.getWorld());
    }

    /**
     * Checks to see if based on the specific context of the {@link AbilityHolder} if McRPG is currently
     * active for them.
     *
     * @param holder The {@link AbilityHolder} to check.
     * @return {@code true} if the specific context of the {@link AbilityHolder} allows for McRPG to be active
     * for them.
     */
    public boolean isMcRPGEnabledForHolder(@NotNull AbilityHolder holder) {
        Entity entity = Bukkit.getEntity(holder.getUUID());
        // Eventually this should allow for holder specific context checks
        if (entity == null || !isMcRPGEnabledForLocation(entity.getLocation())) {
            return false;
        }
        return true;
    }
}
