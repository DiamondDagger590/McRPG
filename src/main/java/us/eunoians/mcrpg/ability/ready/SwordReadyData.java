package us.eunoians.mcrpg.ability.ready;

import org.jetbrains.annotations.NotNull;

/**
 * Ready data that is shared by all abilities that activate from using a sword
 * to ready.
 */
public class SwordReadyData extends ReadyData {

    @NotNull
    @Override
    public String getReadyMessage() {
        return "You raise your sword.";
    }

    @NotNull
    @Override
    public String getUnreadyMessage() {
        return "You lower your sword.";
    }
}
