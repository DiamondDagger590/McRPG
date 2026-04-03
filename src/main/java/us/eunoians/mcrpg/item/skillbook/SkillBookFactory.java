package us.eunoians.mcrpg.item.skillbook;

import com.diamonddagger590.mccore.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.List;
import java.util.Map;

/**
 * Static factory for creating skill book {@link ItemStack}s.
 * <p>
 * Skill books are enchanted books tagged with PDC keys that identify them as
 * McRPG skill books and specify which ability they unlock. All skill book
 * sources (MythicMobs drops, quest rewards, commands) should delegate to
 * this factory to ensure consistent item format.
 * <p>
 * Display text is resolved through the localization system. When a player
 * context is available, the item is localized to that player's locale.
 * When no player is available (e.g., MythicMobs drops), the server default
 * locale is used.
 */
public final class SkillBookFactory {

    /**
     * PDC key applied to skill book items to identify them as McRPG skill books.
     */
    public static final NamespacedKey SKILL_BOOK_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book");

    /**
     * PDC key storing the ability {@link NamespacedKey} this book unlocks.
     */
    public static final NamespacedKey SKILL_BOOK_ABILITY_KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book_ability");

    private SkillBookFactory() {
        // Non-instantiable
    }

    /**
     * Creates a skill book item with explicit display name and lore components.
     * <p>
     * This is the low-level overload. Prefer {@link #createSkillBook(NamespacedKey, String, int)}
     * or {@link #createSkillBook(NamespacedKey, McRPGPlayer, int)} which resolve
     * display text through the localization system.
     *
     * @param abilityKey  the {@link NamespacedKey} of the ability this book unlocks
     * @param displayName the display name for the item
     * @param lore        the lore lines for the item
     * @param amount      the stack size (typically 1)
     * @return a fully tagged skill book {@link ItemStack}
     */
    @NotNull
    public static ItemStack createSkillBook(@NotNull NamespacedKey abilityKey,
                                            @NotNull Component displayName,
                                            @NotNull List<Component> lore,
                                            int amount) {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK, Math.max(1, amount));
        ItemMeta meta = itemStack.getItemMeta();

        meta.displayName(displayName);
        meta.lore(lore);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(SKILL_BOOK_KEY, PersistentDataType.BOOLEAN, true);
        pdc.set(SKILL_BOOK_ABILITY_KEY, PersistentDataType.STRING, abilityKey.toString());

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Creates a skill book with display text resolved from the localization system
     * using the given player's locale chain.
     * <p>
     * The display name and lore are resolved via {@link LocalizationKey#SKILL_BOOK_ITEM_NAME}
     * and {@link LocalizationKey#SKILL_BOOK_ITEM_LORE} with the {@code <ability>} placeholder
     * substituted with the ability's localized display name.
     *
     * @param abilityKey the {@link NamespacedKey} of the ability this book unlocks
     * @param player     the player whose locale chain is used for text resolution
     * @param amount     the stack size (typically 1)
     * @return a fully tagged skill book {@link ItemStack}
     */
    @NotNull
    public static ItemStack createSkillBook(@NotNull NamespacedKey abilityKey,
                                            @NotNull McRPGPlayer player,
                                            int amount) {
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        String abilityDisplayName = resolveAbilityDisplayName(abilityKey, player);
        Map<String, String> placeholders = Map.of("ability", abilityDisplayName);

        Component displayName = localizationManager.getLocalizedMessageAsComponent(
                player, LocalizationKey.SKILL_BOOK_ITEM_NAME, placeholders);

        List<Component> lore = localizationManager.getLocalizedMessageAsComponents(
                player, LocalizationKey.SKILL_BOOK_ITEM_LORE, placeholders);

        return createSkillBook(abilityKey, displayName, lore, amount);
    }

    /**
     * Creates a skill book with display text resolved from the localization system
     * using the server default locale.
     * <p>
     * Used when no player context is available (e.g., MythicMobs drops, which are
     * created before being assigned to a specific player).
     *
     * @param abilityKey         the {@link NamespacedKey} of the ability this book unlocks
     * @param abilityDisplayName a human-readable ability name for the {@code <ability>} placeholder
     * @param amount             the stack size (typically 1)
     * @return a fully tagged skill book {@link ItemStack}
     */
    @NotNull
    public static ItemStack createSkillBook(@NotNull NamespacedKey abilityKey,
                                            @NotNull String abilityDisplayName,
                                            int amount) {
        McRPG plugin = McRPG.getInstance();
        McRPGLocalizationManager localizationManager = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        Map<String, String> placeholders = Map.of("ability", abilityDisplayName);

        Component displayName = localizationManager.getLocalizedMessageAsComponent(
                LocalizationKey.SKILL_BOOK_ITEM_NAME, placeholders);

        List<Component> lore = localizationManager.getLocalizedMessageAsComponents(
                LocalizationKey.SKILL_BOOK_ITEM_LORE, placeholders);

        return createSkillBook(abilityKey, displayName, lore, amount);
    }

    /**
     * Checks whether the given item is a skill book by inspecting its PDC.
     *
     * @param itemStack the item to check
     * @return {@code true} if the item has the {@code mcrpg:skill_book} tag set to true
     */
    public static boolean isSkillBook(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();
        return Boolean.TRUE.equals(pdc.get(SKILL_BOOK_KEY, PersistentDataType.BOOLEAN));
    }

    /**
     * Reads the ability key from a skill book item.
     *
     * @param itemStack the skill book item
     * @return the ability {@link NamespacedKey} string, or {@code null} if not present
     */
    @Nullable
    public static String getAbilityKeyString(@NotNull ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return null;
        }
        return itemStack.getItemMeta().getPersistentDataContainer()
                .get(SKILL_BOOK_ABILITY_KEY, PersistentDataType.STRING);
    }

    /**
     * Converts a {@link NamespacedKey} to a human-readable display name by replacing
     * underscores with spaces and capitalizing the first letter.
     * <p>
     * Public so that callers (e.g., {@link us.eunoians.mcrpg.quest.reward.builtin.SkillBookRewardType})
     * can use the same formatting logic when a player context is unavailable.
     *
     * @param key the key to format
     * @return a human-readable name derived from the key
     */
    @NotNull
    public static String formatKeyAsDisplayName(@NotNull NamespacedKey key) {
        String raw = key.getKey().replace("_", " ");
        return raw.substring(0, 1).toUpperCase() + raw.substring(1);
    }

    /**
     * Resolves the localized display name for an ability, falling back to a
     * formatted version of the key if the ability is not registered.
     */
    @NotNull
    private static String resolveAbilityDisplayName(@NotNull NamespacedKey abilityKey,
                                                    @NotNull McRPGPlayer player) {
        McRPG plugin = McRPG.getInstance();
        AbilityRegistry abilityRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);

        if (abilityRegistry.registered(abilityKey)) {
            return plugin.getMiniMessage().serialize(
                    abilityRegistry.getRegisteredAbility(abilityKey).getDisplayName(player));
        }
        return formatKeyAsDisplayName(abilityKey);
    }
}
