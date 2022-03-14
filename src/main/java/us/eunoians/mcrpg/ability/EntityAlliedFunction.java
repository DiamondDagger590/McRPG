package us.eunoians.mcrpg.ability;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * This functional interface is used to define various relations between two {@link Entity entities}
 * where the state of "teammates" or "allies" should be concerned.
 * <p>
 * Such definitions could be two players in a party together or in a Town together and so forth.
 */
@FunctionalInterface
public interface EntityAlliedFunction {

    /**
     * Checks to see if the two provided {@link Entity entities} are considered "allies" in an anonymous context.
     * <p>
     * McRPG doesn't care what the definition of "allies" means in this context, this is used to prevent abilities from affecting
     * "allies". Order of {@link Entity entities} here should not matter and the same value should be returned regardless of order.
     *
     * @param entity1 The first {@link Entity} to compare
     * @param entity2 The second {@link Entity} to compare
     * @return {@code true} if the two {@link Entity entities} should be considered "allies"
     */
    public boolean areAllies(@NotNull Entity entity1, @NotNull Entity entity2);
}
