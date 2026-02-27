package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.quest.board.rarity.QuestRarity;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Evaluates to {@code true} if the rolled rarity is at least as rare as the specified minimum.
 * Lower weight = rarer, so the condition passes when {@code rolledWeight <= minimumWeight}.
 */
public final class RarityCondition implements TemplateCondition {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "rarity_gate");

    private final NamespacedKey minimumRarity;

    public RarityCondition(@NotNull NamespacedKey minimumRarity) {
        this.minimumRarity = minimumRarity;
    }

    @Override
    public boolean evaluate(@NotNull ConditionContext context) {
        if (context.rolledRarity() == null || context.rarityRegistry() == null) {
            return true;
        }
        Optional<QuestRarity> rolled = context.rarityRegistry().get(context.rolledRarity());
        Optional<QuestRarity> minimum = context.rarityRegistry().get(minimumRarity);
        if (rolled.isEmpty() || minimum.isEmpty()) {
            return false;
        }
        return rolled.get().getWeight() <= minimum.get().getWeight();
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }

    @NotNull
    @Override
    public Optional<NamespacedKey> getExpansionKey() {
        return Optional.of(McRPGExpansion.EXPANSION_KEY);
    }

    @NotNull
    @Override
    public TemplateCondition fromConfig(@NotNull Section section) {
        String rarityStr = section.getString("min-rarity");
        if (rarityStr == null || rarityStr.isBlank()) {
            throw new IllegalArgumentException("Missing 'min-rarity' in rarity_gate condition");
        }
        NamespacedKey rarityKey = NamespacedKey.fromString(rarityStr.toLowerCase().contains(":")
                ? rarityStr.toLowerCase()
                : McRPGMethods.getMcRPGNamespace() + ":" + rarityStr.toLowerCase());
        if (rarityKey == null) {
            throw new IllegalArgumentException("Invalid rarity key: " + rarityStr);
        }
        return new RarityCondition(rarityKey);
    }

    @NotNull
    public NamespacedKey getMinimumRarity() {
        return minimumRarity;
    }
}
