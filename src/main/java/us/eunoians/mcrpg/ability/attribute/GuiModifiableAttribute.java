package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.gui.slot.Slot;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.AbilityEditGui;

import java.util.List;

/**
 * Any attribute that can be displayed/has some sort of action in the {@link AbilityEditGui}
 * should extend this.
 */
public interface GuiModifiableAttribute {

    /**
     * The {@link Slot} that performs some action on this attribute.
     *
     * @param player  The player that the slot should be created for.
     * @param ability The ability that the slot should be created for.
     * @return A {@link Slot} that performs some action on this attribute.
     */
    @NotNull
    Slot getSlot(@NotNull McRPGPlayer player, @NotNull Ability ability);

    /**
     * Gets a {@link List} of {@link Component}s to use as lore for the item of the given {@link Slot}.
     *
     * @param mcRPGPlayer The player that the lore should be created for.
     * @param ability     The ability that the lore should be created for.
     * @return A {@link List} of {@link Component}s to use as lore for the item of the given {@link Slot}
     */
    @NotNull
    List<Component> getGuiLore(@NotNull McRPGPlayer mcRPGPlayer, @NotNull Ability ability);
}
