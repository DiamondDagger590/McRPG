package us.eunoians.mcrpg.ability.ready;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * Ready data used for the {@link us.eunoians.mcrpg.skill.impl.herbalism.Herbalism} skill.
 */
public class HerbalismReadyData extends ReadyData{

    @NotNull
    @Override
    public Component getReadyMessage(@NotNull McRPGPlayer player) {
        return Component.text("<gray>You ready your hoe.");
    }

    @NotNull
    @Override
    public Component getUnreadyMessage(@NotNull McRPGPlayer player) {
        return Component.text("<gray>You lower your hoe.");
    }
}
