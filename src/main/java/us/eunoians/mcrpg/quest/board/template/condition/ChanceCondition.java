package us.eunoians.mcrpg.quest.board.template.condition;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

/**
 * Evaluates to {@code true} with the given probability. Uses the generation
 * context's {@link java.util.Random} for deterministic seeding consistency.
 */
public final class ChanceCondition implements TemplateCondition {

    public static final NamespacedKey KEY = new NamespacedKey(McRPGMethods.getMcRPGNamespace(), "chance");

    private final double chance;

    public ChanceCondition(double chance) {
        if (chance < 0.0 || chance > 1.0) {
            throw new IllegalArgumentException("Chance must be between 0.0 and 1.0, got: " + chance);
        }
        this.chance = chance;
    }

    @Override
    public boolean evaluate(@NotNull ConditionContext context) {
        if (context.random() == null) {
            return true;
        }
        return context.random().nextDouble() < chance;
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
        return new ChanceCondition(section.getDouble("chance"));
    }

    public double getChance() {
        return chance;
    }
}
