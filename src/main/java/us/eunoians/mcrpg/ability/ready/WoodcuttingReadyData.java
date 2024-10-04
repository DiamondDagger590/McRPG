package us.eunoians.mcrpg.ability.ready;

import org.jetbrains.annotations.NotNull;

/**
 * Ready data used for the {@link us.eunoians.mcrpg.skill.impl.woodcutting.Woodcutting} skill.
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
