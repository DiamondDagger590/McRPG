package us.eunoians.mcrpg.listener.world;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.api.event.fake.FakeBlockBreakEvent;

/**
 * This listener handles checking the state of a {@link FakeBlockBreakEvent} before marking it as
 * cancelled for the remainder of listeners down the event chain.
 */
public class FakeBlockBreakListener implements Listener {

    // Has to be high in order to support Jobs Reborn :/
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void handleFakeBlockListener(FakeBlockBreakEvent fakeBlockBreakEvent) {
        /*
         Mark that the event was cancelled due to another plugin since the expected end result will be that this
         event will always be cancelled
         */
        if (fakeBlockBreakEvent.isCancelled()) {
            fakeBlockBreakEvent.setPassedChecks(false);
        }
        else {
            fakeBlockBreakEvent.setCancelled(true);
        }
    }
}
