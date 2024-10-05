package us.eunoians.mcrpg.event.event.fake;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event is used to represent a fake block break in order to test
 * for block protections.
 * <p>
 * If this event is fired, then the end result will be checked before marking the event
 * as cancelled so plugins down the event chain don't consume it as an actual break event.
 * <p>
 * {@link #hasPassedChecks()} should be checked to see if the player is allowed to break the block as
 * the end result of calling {@link #isCancelled()} for this event will always be {@code true}.
 */
public class FakeBlockBreakEvent extends BlockBreakEvent {

    private boolean passedChecks = true;

    public FakeBlockBreakEvent(@NotNull Block theBlock, @NotNull Player player) {
        super(theBlock, player);
    }

    /**
     * Checks to see if the event passed block protection checks.
     * <p>
     * This should be used over {@link #isCancelled()} as the expected result of that
     * will always be {@code true}.
     *
     * @return If the event passed block protection checks.
     */
    public boolean hasPassedChecks() {
        return passedChecks;
    }

    /**
     * Sets if the event passed block protection checks.
     *
     * @param passedChecks If the event passed block protection checks.
     */
    public void setPassedChecks(boolean passedChecks) {
        this.passedChecks = passedChecks;
    }
}
