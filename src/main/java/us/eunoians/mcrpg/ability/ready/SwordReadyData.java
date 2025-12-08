package us.eunoians.mcrpg.ability.ready;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * Ready data that is shared by all abilities that activate from using a sword
 * to ready.
 */
public class SwordReadyData extends ReadyData {

    @NotNull
    @Override
    public Component getReadyMessage(@NotNull McRPGPlayer player) {
        return Component.text("<gray>You raise your sword.");
    }

    @NotNull
    @Override
    public Component getUnreadyMessage(@NotNull McRPGPlayer player) {
        return Component.text("<gray>You lower your sword.");
    }
}
