package us.eunoians.mcrpg.item.skillbook;

import com.diamonddagger590.mccore.builder.item.BaseItemBuilder;
import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

/**
 * A specialized item builder for skill book {@link ItemStack}s.
 * <p>
 * Skill books are items tagged with PDC keys that identify them as McRPG skill books
 * and specify which ability they unlock. This builder extends {@link BaseItemBuilder}
 * to support config-driven appearance with global defaults and per-ability overrides,
 * while always applying the required PDC tags on {@link #build()}.
 * <p>
 * Static inspection methods ({@link #isSkillBook(ItemStack)}, {@link #getAbilityKeyString(ItemStack)})
 * are provided on this class for third-party discoverability.
 * <p>
 * All skill book sources (MythicMobs drops, quest rewards, commands) should use this builder
 * to ensure consistent item format.
 */
public class SkillBookBuilder extends BaseItemBuilder<SkillBookBuilder> {

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

    private NamespacedKey abilityKey;

    public SkillBookBuilder(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    /**
     * Sets the ability that this skill book unlocks. The ability key is written
     * to the item's PDC when {@link #build()} is called.
     *
     * @param abilityKey the {@link NamespacedKey} of the ability this book unlocks
     * @return this builder
     */
    @NotNull
    public SkillBookBuilder withAbility(@NotNull NamespacedKey abilityKey) {
        this.abilityKey = abilityKey;
        return this;
    }

    @NotNull
    @Override
    public SkillBookBuilder build() {
        if (abilityKey != null) {
            getItemStack().editMeta(meta -> {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(SKILL_BOOK_KEY, PersistentDataType.BOOLEAN, true);
                pdc.set(SKILL_BOOK_ABILITY_KEY, PersistentDataType.STRING, abilityKey.toString());
            });
        }
        return this;
    }

    /**
     * Creates a skill book builder with appearance loaded from the config, using the
     * provided ability display name for the {@code <ability>} placeholder.
     * <p>
     * The config is read from the {@code configuration.skill-books} section of
     * {@link FileType#MAIN_CONFIG}. If an ability-specific override exists under
     * {@code ability-overrides.<namespace>.<key>}, it is used instead of the default.
     *
     * @param abilityKey         the ability this book unlocks
     * @param abilityDisplayName the human-readable ability name for placeholder substitution
     * @return a configured skill book builder ready for {@link #asItemStack()}
     */
    @NotNull
    public static SkillBookBuilder fromConfig(@NotNull NamespacedKey abilityKey,
                                              @NotNull String abilityDisplayName) {
        McRPG plugin = McRPG.getInstance();
        YamlDocument config = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG);

        Section itemSection = resolveItemSection(config, abilityKey);

        ItemStack baseItem = ItemBuilder.from(itemSection)
                .addPlaceholder("ability", abilityDisplayName)
                .asItemStack();

        return new SkillBookBuilder(baseItem).withAbility(abilityKey);
    }

    /**
     * Creates a skill book builder with appearance loaded from the config, resolving
     * the ability display name from the {@link AbilityRegistry} using the player's locale.
     * <p>
     * Falls back to {@link #formatKeyAsDisplayName(NamespacedKey)} if the ability is not
     * registered.
     *
     * @param abilityKey the ability this book unlocks
     * @param player     the player whose locale is used for ability name resolution
     * @return a configured skill book builder ready for {@link #asItemStack()}
     */
    @NotNull
    public static SkillBookBuilder fromConfig(@NotNull NamespacedKey abilityKey,
                                              @NotNull McRPGPlayer player) {
        return fromConfig(abilityKey, resolveAbilityDisplayName(abilityKey, player));
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
     * Resolves the config section to use for the skill book item appearance.
     * Checks for an ability-specific override first, falling back to the default section.
     */
    @NotNull
    private static Section resolveItemSection(@NotNull YamlDocument config,
                                              @NotNull NamespacedKey abilityKey) {
        String overridePath = MainConfigFile.SKILL_BOOK_ABILITY_OVERRIDES_HEADER_STRING
                + "." + abilityKey.getNamespace() + "." + abilityKey.getKey();
        Section overrideSection = config.getSection(overridePath);
        if (overrideSection != null) {
            return overrideSection;
        }
        Section defaultSection = config.getSection(MainConfigFile.SKILL_BOOK_DEFAULT_ITEM);
        if (defaultSection == null) {
            throw new IllegalStateException(
                    "Missing skill book default item config at: " + MainConfigFile.SKILL_BOOK_DEFAULT_ITEM);
        }
        return defaultSection;
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
