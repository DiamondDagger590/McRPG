package us.eunoians.mcrpg.external.mythicmobs;

import io.lumine.mythic.api.adapters.AbstractItemStack;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
import io.lumine.mythic.api.drops.IItemDrop;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;

/**
 * A custom MythicMobs drop type that generates an McRPG skill book item.
 * <p>
 * Registered as {@code mcrpg_skillbook} in MythicMobs drop tables. The skill name
 * is passed as the argument in the drop table configuration:
 * <pre>
 *   Drops:
 *     - mcrpg_skillbook{skill=Fishing} 1 0.1
 * </pre>
 * <p>
 * The generated item is a written book tagged with a PDC key identifying it as
 * an McRPG skill book. The fishing skill system (or any future consumer) reads
 * this key to determine what action to take when a player picks up or uses the item.
 */
public class McRPGSkillBookDrop implements IItemDrop {

    /**
     * PDC key applied to skill book items to identify them as McRPG skill books.
     */
    public static final NamespacedKey SKILL_BOOK_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book");

    /**
     * PDC key storing the skill name this book is associated with.
     */
    public static final NamespacedKey SKILL_BOOK_SKILL_KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book_skill");

    private final String skillName;

    /**
     * Creates a new skill book drop.
     *
     * @param config   The MythicMobs line config for this drop entry
     * @param argument The argument string from the drop table (used as fallback skill name)
     */
    public McRPGSkillBookDrop(@NotNull MythicLineConfig config, @NotNull String argument) {
        this.skillName = config.getString("skill", argument);
    }

    @Override
    @NotNull
    public AbstractItemStack getDrop(@NotNull DropMetadata dropMetadata, double amount) {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK, Math.max(1, (int) amount));
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(Component.text("Skill Book: " + skillName)
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(List.of(
                Component.text("A mysterious tome of knowledge.")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Use this to gain experience in " + skillName + ".")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));

        meta.getPersistentDataContainer().set(SKILL_BOOK_KEY, PersistentDataType.BOOLEAN, true);
        meta.getPersistentDataContainer().set(SKILL_BOOK_SKILL_KEY, PersistentDataType.STRING, skillName);

        itemStack.setItemMeta(meta);
        return new BukkitItemStack(itemStack);
    }
}
