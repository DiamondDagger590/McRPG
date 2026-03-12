package us.eunoians.mcrpg.quest.board.rarity;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.eunoians.mcrpg.expansion.content.McRPGContent;

import java.util.Optional;

/**
 * Immutable data class representing a single rarity tier for board quests.
 * <p>
 * Loaded from {@code board.yml} config (auto-namespaced under {@code mcrpg:}) or
 * registered programmatically by third-party plugins via content packs.
 */
public final class QuestRarity implements McRPGContent {

    private final NamespacedKey key;
    private final int weight;
    private final double difficultyMultiplier;
    private final double rewardMultiplier;
    private final NamespacedKey expansionKey;
    private final Integer customModelData;
    private final boolean glint;
    private final Material material;
    private final String nameColor;

    public QuestRarity(@NotNull NamespacedKey key,
                       int weight,
                       double difficultyMultiplier,
                       double rewardMultiplier,
                       @NotNull NamespacedKey expansionKey) {
        this(key, weight, difficultyMultiplier, rewardMultiplier, expansionKey, null, false, null, null);
    }

    public QuestRarity(@NotNull NamespacedKey key,
                       int weight,
                       double difficultyMultiplier,
                       double rewardMultiplier,
                       @NotNull NamespacedKey expansionKey,
                       @Nullable Integer customModelData,
                       boolean glint) {
        this(key, weight, difficultyMultiplier, rewardMultiplier, expansionKey, customModelData, glint, null, null);
    }

    public QuestRarity(@NotNull NamespacedKey key,
                       int weight,
                       double difficultyMultiplier,
                       double rewardMultiplier,
                       @NotNull NamespacedKey expansionKey,
                       @Nullable Integer customModelData,
                       boolean glint,
                       @Nullable Material material,
                       @Nullable String nameColor) {
        this.key = key;
        this.weight = weight;
        this.difficultyMultiplier = difficultyMultiplier;
        this.rewardMultiplier = rewardMultiplier;
        this.expansionKey = expansionKey;
        this.customModelData = customModelData;
        this.glint = glint;
        this.material = material;
        this.nameColor = nameColor;
    }

    @NotNull
    public NamespacedKey getKey() {
        return key;
    }

    public int getWeight() {
        return weight;
    }

    public double getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    public double getRewardMultiplier() {
        return rewardMultiplier;
    }

    /**
     * Returns the optional custom model data value for resource pack integration.
     *
     * @return the custom model data, or empty if not configured
     */
    @NotNull
    public Optional<Integer> getCustomModelData() {
        return Optional.ofNullable(customModelData);
    }

    /**
     * Returns whether this rarity should display an enchantment glint on offering items.
     *
     * @return true if glint is enabled
     */
    public boolean hasGlint() {
        return glint;
    }

    /**
     * Returns the display material for offering items of this rarity.
     * When present, overrides the material from the localization section.
     *
     * @return the material, or empty to use the localization default
     */
    @NotNull
    public Optional<Material> getMaterial() {
        return Optional.ofNullable(material);
    }

    /**
     * Returns the MiniMessage color tag to prepend to offering item names for this rarity.
     * For example, {@code "<gold>"} for legendary quests.
     *
     * @return the name color tag, or empty to use the localization default
     */
    @NotNull
    public Optional<String> getNameColor() {
        return Optional.ofNullable(nameColor);
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(expansionKey);
    }
}
