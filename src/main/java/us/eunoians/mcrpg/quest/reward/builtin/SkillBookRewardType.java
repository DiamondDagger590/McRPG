package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.item.skillbook.SkillBookBuilder;
import us.eunoians.mcrpg.localization.McRPGLocalizationManager;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import us.eunoians.mcrpg.expansion.McRPGExpansion;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * A quest reward type that grants a physical skill book item to the player.
 * <p>
 * The skill book is created via {@link SkillBookBuilder} to ensure consistent
 * PDC tags and formatting. When granted, the item is added to the player's
 * inventory; overflow drops naturally at the player's location.
 * <p>
 * Config format:
 * <pre>
 * rewards:
 *   phase_shift_book:
 *     type: mcrpg:skill_book
 *     ability: "mcrpg:phase_shift"
 * </pre>
 */
public class SkillBookRewardType implements QuestRewardType {

    /**
     * Registry key for this reward type.
     */
    public static final NamespacedKey KEY =
            new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "skill_book");

    private final NamespacedKey abilityKey;
    private final Route localizationRoute;

    /**
     * Base (unconfigured) constructor for registry registration.
     */
    public SkillBookRewardType() {
        this.abilityKey = null;
        this.localizationRoute = null;
    }

    /**
     * Configured constructor with a specific ability key.
     *
     * @param abilityKey        the ability this skill book unlocks
     * @param localizationRoute the auto-derived localization route, or null
     */
    private SkillBookRewardType(@NotNull NamespacedKey abilityKey,
                                @Nullable Route localizationRoute) {
        this.abilityKey = abilityKey;
        this.localizationRoute = localizationRoute;
    }

    @Override
    @NotNull
    public NamespacedKey getKey() {
        return KEY;
    }

    @Override
    @NotNull
    public QuestRewardType parseConfig(@NotNull Section section) {
        String abilityKeyString = section.getString("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyString);
        if (parsedKey == null) {
            throw new IllegalArgumentException(
                    "Invalid ability key in skill_book reward: " + abilityKeyString);
        }
        return new SkillBookRewardType(parsedKey, null);
    }

    @Override
    @NotNull
    public QuestRewardType withLocalizationRoute(@NotNull Route route) {
        if (abilityKey == null) {
            return this;
        }
        return new SkillBookRewardType(abilityKey, route);
    }

    @Override
    public void grant(@NotNull Player player) {
        if (abilityKey == null) {
            throw new IllegalStateException("Cannot grant unconfigured SkillBookRewardType");
        }

        McRPG plugin = McRPG.getInstance();
        Optional<McRPGPlayer> mcRPGPlayerOptional = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(player.getUniqueId());

        ItemStack skillBook;
        if (mcRPGPlayerOptional.isPresent()) {
            skillBook = SkillBookBuilder.fromConfig(abilityKey, mcRPGPlayerOptional.get()).asItemStack();
        } else {
            String displayName = SkillBookBuilder.formatKeyAsDisplayName(abilityKey);
            skillBook = SkillBookBuilder.fromConfig(abilityKey, displayName).asItemStack();
        }

        // Add to inventory, drop overflow naturally
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(skillBook);
        overflow.values().forEach(item ->
                player.getWorld().dropItemNaturally(player.getLocation(), item));
    }

    @Override
    @NotNull
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new HashMap<>();
        if (abilityKey != null) {
            map.put("ability", abilityKey.toString());
        }
        return map;
    }

    @Override
    @NotNull
    public QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        String abilityKeyString = (String) config.get("ability");
        NamespacedKey parsedKey = NamespacedKey.fromString(abilityKeyString);
        if (parsedKey == null) {
            throw new IllegalArgumentException(
                    "Invalid ability key in serialized skill_book reward: " + abilityKeyString);
        }
        return new SkillBookRewardType(parsedKey, this.localizationRoute);
    }

    @Override
    @NotNull
    public OptionalLong getNumericAmount() {
        return OptionalLong.of(1);
    }

    @Override
    @NotNull
    public String describeForDisplay() {
        if (abilityKey == null) {
            return "Skill Book";
        }
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        String abilityName = SkillBookBuilder.formatKeyAsDisplayName(abilityKey);
        return localizationManager.getLocalizedMessage(
                LocalizationKey.SKILL_BOOK_ITEM_NAME, Map.of("ability", abilityName));
    }

    @Override
    @NotNull
    public String describeForDisplay(@NotNull McRPGPlayer player) {
        if (abilityKey == null) {
            return "Skill Book";
        }
        McRPGLocalizationManager localizationManager = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);

        // Resolve the ability's localized display name for this player
        McRPG plugin = McRPG.getInstance();
        AbilityRegistry abilityRegistry = plugin.registryAccess()
                .registry(McRPGRegistryKey.ABILITY);

        String abilityName;
        if (abilityRegistry.registered(abilityKey)) {
            abilityName = plugin.getMiniMessage().serialize(
                    abilityRegistry.getRegisteredAbility(abilityKey).getDisplayName(player));
        } else {
            abilityName = SkillBookBuilder.formatKeyAsDisplayName(abilityKey);
        }

        Map<String, String> placeholders = Map.of("ability", abilityName);

        // Localization chain (follows CommandRewardType pattern):
        // 1. Auto-derived localizationRoute (set by withLocalizationRoute())
        // 2. Fallback to generic SKILL_BOOK_ITEM_NAME key
        if (localizationRoute != null) {
            try {
                return localizationManager.getLocalizedMessage(player, localizationRoute, placeholders);
            } catch (Exception ignored) {
                // Route doesn't exist in locale — fall through to generic key
            }
        }
        return localizationManager.getLocalizedMessage(player, LocalizationKey.SKILL_BOOK_ITEM_NAME, placeholders);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }
}
