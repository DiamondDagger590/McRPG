package us.eunoians.mcrpg.quest.reward.builtin;

import com.diamonddagger590.mccore.builder.item.impl.ItemBuilder;
import com.diamonddagger590.mccore.registry.RegistryAccess;
import com.diamonddagger590.mccore.registry.RegistryKey;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.route.Route;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.file.localization.LocalizationKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.reward.QuestRewardType;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.logging.Level;

/**
 * Built-in reward type that grants an item directly to a player's inventory using
 * McCore's {@link ItemBuilder#from(Section)} for declarative YAML-based item configuration.
 * Supports enchantments, custom names, lore, custom model data, and all standard
 * {@link com.diamonddagger590.mccore.builder.item.ItemBuilderConfigurationKeys} fields.
 * <p>
 * Two amount modes exist for template compatibility:
 * <ul>
 *     <li><b>Scalable</b>: top-level {@code amount} key (template engine applies rarity
 *         {@code reward-multiplier}). The {@code item} section uses its own
 *         amount internally set to 1; at grant time the resolved top-level amount is applied.</li>
 *     <li><b>Non-scalable</b>: {@code amount} inside the {@code item} section. The template
 *         engine only touches top-level keys, so nested amount is untouched.</li>
 * </ul>
 * <p>
 * Config format — scalable:
 * <pre>
 * type: mcrpg:item
 * item:
 *   material: DIAMOND
 * amount: 3   # scaled by rarity multiplier
 * </pre>
 * <p>
 * Config format — non-scalable (exclusive reward):
 * <pre>
 * type: mcrpg:item
 * item:
 *   material: DIAMOND_PICKAXE
 *   amount: 1
 *   name: "&lt;gold&gt;Legendary Pick"
 *   enchantments:
 *     efficiency: 4
 *     fortune: 3
 *   settings:
 *     glowing: true
 * </pre>
 * <p>
 * Serialization uses a raw config map (not base64) for human-readable DB storage.
 * Deserialization rebuilds the {@link ItemStack} via an in-memory {@link YamlDocument}.
 */
public class ItemRewardType implements QuestRewardType {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "item");

    private final Map<String, Object> itemConfig;
    private final int amount;
    @Nullable
    private final Route localizationRoute;
    @NotNull
    private final String displayLabel;

    /**
     * Creates an unconfigured base instance for registry registration.
     */
    public ItemRewardType() {
        this.itemConfig = Map.of();
        this.amount = 0;
        this.localizationRoute = null;
        this.displayLabel = "";
    }

    private ItemRewardType(@NotNull Map<String, Object> itemConfig, int amount,
                           @Nullable Route localizationRoute, @NotNull String displayLabel) {
        this.itemConfig = Map.copyOf(itemConfig);
        this.amount = amount;
        this.localizationRoute = localizationRoute;
        this.displayLabel = displayLabel;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public QuestRewardType parseConfig(@NotNull Section section) {
        Section itemSection = section.getSection("item");
        Map<String, Object> rawConfig = itemSection != null
                ? sectionToMap(itemSection) : Map.of("material", "STONE");

        int resolvedAmount;
        if (section.contains("amount")) {
            resolvedAmount = section.getInt("amount", 1);
        } else if (itemSection != null && itemSection.contains("amount")) {
            resolvedAmount = itemSection.getInt("amount", 1);
        } else {
            resolvedAmount = 1;
        }

        return new ItemRewardType(rawConfig, Math.max(1, resolvedAmount), null, "");
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public QuestRewardType fromSerializedConfig(@NotNull Map<String, Object> config) {
        Object itemObj = config.getOrDefault("item", Map.of());
        Map<String, Object> rawConfig = itemObj instanceof Map<?, ?>
                ? new LinkedHashMap<>((Map<String, Object>) itemObj) : Map.of("material", "STONE");

        int resolvedAmount;
        if (config.containsKey("amount")) {
            resolvedAmount = ((Number) config.get("amount")).intValue();
        } else if (rawConfig.containsKey("amount")) {
            resolvedAmount = ((Number) rawConfig.get("amount")).intValue();
        } else {
            resolvedAmount = 1;
        }

        Route route = config.containsKey("localization-route")
                ? Route.fromString(config.get("localization-route").toString())
                : null;
        String label = config.getOrDefault("display", "").toString();

        return new ItemRewardType(rawConfig, Math.max(1, resolvedAmount), route, label);
    }

    @Override
    public void grant(@NotNull Player player) {
        if (itemConfig.isEmpty() || amount <= 0) {
            return;
        }

        try {
            ItemStack item = buildItemStack();
            item.setAmount(amount);

            Map<Integer, ItemStack> overflow = player.getInventory().addItem(item);
            for (ItemStack leftover : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        } catch (Exception e) {
            McRPG.getInstance().getLogger().log(Level.WARNING, "Failed to grant item reward to " + player.getName(), e);
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serializeConfig() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("item", new LinkedHashMap<>(itemConfig));
        map.put("amount", amount);
        if (!displayLabel.isEmpty()) {
            map.put("display", displayLabel);
        }
        if (localizationRoute != null) {
            map.put("localization-route", localizationRoute.join('.'));
        }
        return map;
    }

    @NotNull
    @Override
    public OptionalLong getNumericAmount() {
        return OptionalLong.of(amount);
    }

    @NotNull
    @Override
    public QuestRewardType withAmountMultiplier(double multiplier) {
        int scaled = Math.max(1, (int) Math.round(amount * multiplier));
        return new ItemRewardType(itemConfig, scaled, localizationRoute, displayLabel);
    }

    @NotNull
    @Override
    public QuestRewardType withLocalizationRoute(@NotNull Route route) {
        return new ItemRewardType(itemConfig, amount, route, displayLabel);
    }

    @NotNull
    @Override
    public QuestRewardType withInlineDisplayLabel(@NotNull String label) {
        return new ItemRewardType(itemConfig, amount, localizationRoute, label);
    }

    @NotNull
    @Override
    public String describeForDisplay() {
        String materialName = itemConfig.getOrDefault("material", "Item").toString();
        String formatted = materialName.toLowerCase().replace('_', ' ');
        if (!formatted.isEmpty()) {
            formatted = Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
        }
        return amount + "x " + formatted;
    }

    @NotNull
    @Override
    public String describeForDisplay(@NotNull McRPGPlayer player) {
        var localization = RegistryAccess.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.LOCALIZATION);
        Map<String, String> vars = rewardVars();
        String label;

        if (localizationRoute != null) {
            try {
                label = localization.getLocalizedMessage(player, localizationRoute);
                return prependDefaultColor(localization, player, label);
            } catch (Exception ignored) {
                // Fall through to inline display label
            }
        }
        if (!displayLabel.isEmpty()) {
            label = localization.getLocalizedMessage(displayLabel, vars);
            return prependDefaultColor(localization, player, label);
        }
        try {
            label = localization.getLocalizedMessage(player, LocalizationKey.QUEST_REWARD_ITEM_FORMAT, vars);
            return prependDefaultColor(localization, player, label);
        } catch (Exception ignored) {
            return describeForDisplay();
        }
    }

    /**
     * Builds the placeholder variable map for MiniMessage resolution.
     * Keys match the placeholders documented in {@code en_quest.yml}:
     * {@code <amount>} and {@code <material>}.
     */
    @NotNull
    private Map<String, String> rewardVars() {
        String materialName = itemConfig.getOrDefault("material", "Item").toString();
        String formatted = materialName.toLowerCase().replace('_', ' ');
        if (!formatted.isEmpty()) {
            formatted = Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
        }
        return Map.of("amount", String.valueOf(amount), "material", formatted);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    /**
     * Builds an {@link ItemStack} from the stored config map by creating a temporary
     * in-memory {@link YamlDocument} section and passing it to {@link ItemBuilder#from(Section)}.
     *
     * @return the built item stack (amount is set to 1; caller adjusts)
     * @throws IOException if the in-memory document cannot be created
     */
    @NotNull
    private ItemStack buildItemStack() throws IOException {
        YamlDocument doc = YamlDocument.create(new ByteArrayInputStream("{}".getBytes()));
        Section itemSection = doc.createSection("item");
        populateSection(itemSection, itemConfig);
        return ItemBuilder.from(itemSection).asItemStack();
    }

    /**
     * Recursively populates a {@link Section} from a flat or nested map.
     */
    @SuppressWarnings("unchecked")
    private void populateSection(@NotNull Section section, @NotNull Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> nested) {
                Section child = section.createSection(entry.getKey());
                populateSection(child, (Map<String, Object>) nested);
            } else {
                section.set(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Converts a {@link Section} to a flat/nested {@link Map} for serialization.
     */
    @NotNull
    private static Map<String, Object> sectionToMap(@NotNull Section section) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : section.getRoutesAsStrings(false)) {
            Object value = section.get(key);
            if (value instanceof Section nested) {
                map.put(key, sectionToMap(nested));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }
}

