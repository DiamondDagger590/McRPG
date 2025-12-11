package us.eunoians.mcrpg.ability.ready;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

/**
 * Ready data used for the {@link us.eunoians.mcrpg.skill.impl.herbalism.Herbalism} skill.
 */
public class HerbalismReadyData extends ReadyData{

    @NotNull
    @Override
    public Component getReadyMessage(@NotNull McRPGPlayer player) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        return miniMessage.deserialize("<gray>You raise your hoe.");
    }

    @NotNull
    @Override
    public Component getUnreadyMessage(@NotNull McRPGPlayer player) {
        MiniMessage miniMessage = McRPG.getInstance().getMiniMessage();
        return miniMessage.deserialize("<gray>You lower your hoe.");
    }
}
