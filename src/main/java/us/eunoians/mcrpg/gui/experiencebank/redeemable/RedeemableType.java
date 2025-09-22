package us.eunoians.mcrpg.gui.experiencebank.redeemable;

import com.diamonddagger590.mccore.gui.Gui;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.experience.RedeemableExperienceGui;
import us.eunoians.mcrpg.gui.experiencebank.redeemable.levels.RedeemableLevelsGui;
import us.eunoians.mcrpg.skill.Skill;

/**
 * This enum provides all the different types of resources that a player can redeem into a
 * given {@link Skill}.
 */
public enum RedeemableType {

    LEVELS(RedeemableLevelsGui::new),
    EXPERIENCE(RedeemableExperienceGui::new);

    private final RedeemableTypeGuiCreationFunction guiCreationFunction;

    RedeemableType(@NotNull RedeemableTypeGuiCreationFunction guiCreationFunction) {
        this.guiCreationFunction = guiCreationFunction;
    }

    /**
     * Creates a {@link Gui} to spend the resource type represented by this enum into
     * the provided {@link Skill}.
     *
     * @param mcRPGPlayer The player creating the gui.
     * @param skill       The skill to redeem the resource into.
     * @return A {@link Gui} to spend the resource type represented by this enum into
     * the provided {@link Skill}.
     */
    @NotNull
    public Gui<McRPGPlayer> createGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill) {
        return guiCreationFunction.createGui(mcRPGPlayer, skill);
    }

    /**
     * This interface allows for the different redeemable types to easily create {@link Gui}s.
     */
    @FunctionalInterface
    private interface RedeemableTypeGuiCreationFunction {

        /**
         * Creates a {@link Gui} to spend the resource type represented by this enum into
         * the provided {@link Skill}.
         *
         * @param mcRPGPlayer The player creating the gui.
         * @param skill       The skill to redeem the resource into.
         * @return A {@link Gui} to spend the resource type represented by this enum into
         * the provided {@link Skill}.
         */
        @NotNull
        Gui<McRPGPlayer> createGui(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Skill skill);
    }
}
