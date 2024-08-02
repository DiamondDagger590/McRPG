package us.eunoians.mcrpg.world;

import com.jeff_media.customblockdata.CustomBlockData;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;

public class WorldManager {

    private static final NamespacedKey PLACED_KEY = new NamespacedKey(McRPG.getInstance(), "placed");

    public static boolean isBlockNatural(@NotNull Block block) {
        CustomBlockData customBlockData = new CustomBlockData(block, McRPG.getInstance());
        return !customBlockData.has(PLACED_KEY) || !customBlockData.get(PLACED_KEY, PersistentDataType.BOOLEAN);
    }

    public static void markBlockAsPlaced(@NotNull Block block) {
        PersistentDataContainer customBlockData = new CustomBlockData(block, McRPG.getInstance());
        customBlockData.set(PLACED_KEY, PersistentDataType.BOOLEAN, true);
    }
}
