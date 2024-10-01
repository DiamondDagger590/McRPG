package us.eunoians.mcrpg.events.vanilla;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import us.eunoians.mcrpg.api.events.mcrpg.FakeBlockBreakEvent;

public class FakeBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void handleFakeBlockListener(FakeBlockBreakEvent fakeBlockBreakEvent) {
        if (fakeBlockBreakEvent.isCancelled()) {
            fakeBlockBreakEvent.setPassedChecks(false);
        }
        else {
            fakeBlockBreakEvent.setCancelled(true);
        }
    }
}
