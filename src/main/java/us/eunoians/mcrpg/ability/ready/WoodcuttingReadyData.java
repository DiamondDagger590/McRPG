package us.eunoians.mcrpg.ability.ready;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;

/**
 * Ready data used for the {@link WoodCutting} skill.
 */
public class WoodcuttingReadyData extends ReadyData {

    @NotNull
    @Override
    public Component getReadyMessage(@NotNull McRPGPlayer player) {
        return Component.text("<gray>You raise your axe.");
    }

    @NotNull
    @Override
    public Component getUnreadyMessage(@NotNull McRPGPlayer player) {
        return Component.text("<gray>You lower your axe.");
    }
}
