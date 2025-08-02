package us.eunoians.mcrpg.ability.attribute;

import com.diamonddagger590.mccore.gui.slot.Slot;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.ability.AbilityAttributeEditGui;
import us.eunoians.mcrpg.gui.slot.McRPGSlot;

/**
 * Any attribute that can be displayed/has some sort of action in the {@link AbilityAttributeEditGui}
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
    McRPGSlot getSlot(@NotNull McRPGPlayer player, @NotNull Ability ability);
}
