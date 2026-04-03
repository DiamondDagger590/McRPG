package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.item.skillbook.SkillBookFactory;

/**
 * A custom MythicMobs drop type that generates an McRPG skill book item.
 * <p>
 * Registered as {@code mcrpg_skillbook} in MythicMobs drop tables. The ability key
 * is passed as the argument in the drop table configuration:
 * <pre>
 *   Drops:
 *     - mcrpg_skillbook{ability=mcrpg:phase_shift} 1 0.1
 * </pre>
 * <p>
 * Delegates to {@link SkillBookFactory} for item creation, ensuring all skill books
 * have consistent PDC tags and formatting regardless of source.
 */
public class McRPGSkillBookDrop implements IItemDrop {

    private final NamespacedKey abilityKey;
    private final String abilityDisplayName;

    /**
     * Creates a new skill book drop.
     *
     * @param config   the MythicMobs line config for this drop entry
     * @param argument the argument string from the drop table (used as fallback ability key)
     */
    public McRPGSkillBookDrop(@NotNull MythicLineConfig config, @NotNull String argument) {
        String keyString = config.getString("ability", argument);
        this.abilityKey = NamespacedKey.fromString(keyString);
        String defaultDisplayName = abilityKey.getKey().replace("_", " ");
        defaultDisplayName = defaultDisplayName.substring(0, 1).toUpperCase() + defaultDisplayName.substring(1);
        this.abilityDisplayName = config.getString("display-name", defaultDisplayName);
    }

    @Override
    @NotNull
    public AbstractItemStack getDrop(@NotNull DropMetadata dropMetadata, double amount) {
        ItemStack itemStack = SkillBookFactory.createSkillBook(
                abilityKey, abilityDisplayName, Math.max(1, (int) amount));
        return new BukkitItemStack(itemStack);
    }
}
