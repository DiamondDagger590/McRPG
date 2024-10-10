package us.eunoians.mcrpg.setting;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.List;

/**
 * This represents a setting that will prevent items from being picked up
 * and going into specific slots of a players inventory.
 */
public interface DenySlotSetting extends PlayerSetting {

    /**
     * Gets a {@link List} of all slots that currently prevent items from going into
     * them for a given {@link McRPGPlayer}.
     *
     * @param player The {@link McRPGPlayer} to get the slots for.
     * @return A {@link List} of all slots that currently prevent items from going into them.
     */
    @NotNull
    List<Integer> getDeniedSlots(@NotNull McRPGPlayer player);
}
