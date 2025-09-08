package us.eunoians.mcrpg.ability.ready;

import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.skill.impl.woodcutting.WoodCutting;

/**
 * Ready data used for the {@link WoodCutting} skill.
 */
public class WoodcuttingReadyData extends ReadyData {

    @NotNull
    @Override
    public String getReadyMessage() {
        return "You raise your axe.";
    }

    @NotNull
    @Override
    public String getUnreadyMessage() {
        return "You lower your axe.";
    }
}
