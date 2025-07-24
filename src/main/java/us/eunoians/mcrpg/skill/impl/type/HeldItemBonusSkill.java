package us.eunoians.mcrpg.skill.impl.type;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.Skill;

/**
 * This is a {@link Skill} that allows for bonus experience gain to occur based on what
 * {@link ItemStack} is being held.
 */
public interface HeldItemBonusSkill extends Skill {

    /**
     * Gets the total bonus to be applied to experience gain based on what items
     * are being held.
     * <p>
     * A value of {@code 0.0} means there is no change, a positive value will be added to the
     * total multiplier while a negative will be subtracted.
     *
     * @param items The {@link ItemStack}s to get the total boost for.
     * @return The total bonus to be applied to experience gain, with a value of {@code 0.0} meaning
     * no change, a positive value will be added while a negative will be subtracted.
     */
    double getHeldItemBonus(@NotNull ItemStack... items);
}
