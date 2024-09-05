package us.eunoians.mcrpg.ability.ready;

import org.jetbrains.annotations.NotNull;

/**
 * Ready data used for the {@link us.eunoians.mcrpg.skill.impl.mining.Mining} skill.
 */
public class MiningReadyData extends ReadyData {

    @NotNull
    @Override
    public String getReadyMessage() {
        return "You raise your pickaxe.";
    }

    @NotNull
    @Override
    public String getUnreadyMessage() {
        return "You lower your pickaxe.";
    }
}
