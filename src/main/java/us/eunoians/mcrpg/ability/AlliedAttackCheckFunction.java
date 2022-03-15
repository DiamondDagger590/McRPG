package us.eunoians.mcrpg.ability;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * This functional interface is used to define various relations between two {@link Entity entities}
 * where the state of "teammates" or "allies" should be concerned.
 * <p>
 * Such definitions could be two players in a party together who should be able to attack each other due to the party's
 * pvp setting
 */
@FunctionalInterface
public interface AlliedAttackCheckFunction {

    AlliedAttackCheckFunction DEFAULT_ALLIED_ATTACK_CHECK_FUNCTION = (@NotNull Entity entity1, @NotNull Entity entity2) -> true;

    /**
     * Checks to see if the two {@link Entity entities} are unable to attack each other if they are "allies".
     *
     * This should return {@code true} if they should be UNABLE to attack each other, whilst returning
     * {@code false} if they should be ABLE to attack each other. (Forgive the back-to-front nature of this,
     * there were edge cases in implementation that demanded it)
     *
     * @param entity1 The first {@link Entity} to check
     * @param entity2 The second {@link Entity} to check
     * @return {@code true} if the two {@link Entity entities} should be unable to attack each other, or {@code false} if
     * they should be able to attack each other
     */
    public boolean shouldBeUnableToDamage(@NotNull Entity entity1, @NotNull Entity entity2);
}
