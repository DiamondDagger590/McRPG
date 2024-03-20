package us.eunoians.mcrpg.api.events.mcrpg;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public abstract class FakeBlockBreakEvent extends BlockBreakEvent {

    private boolean passedChecks = true;
    public FakeBlockBreakEvent(@NotNull Block theBlock, @NotNull Player player) {
        super(theBlock, player);
    }

    public boolean hasPassedChecks() {
        return passedChecks;
    }

    public void setPassedChecks(boolean passedChecks) {
        this.passedChecks = passedChecks;
    }
}
