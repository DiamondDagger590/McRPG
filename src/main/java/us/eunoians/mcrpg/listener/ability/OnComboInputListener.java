package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboInput;
import us.eunoians.mcrpg.ability.combo.ComboTracker;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Translates {@link PlayerInteractEvent}s with {@link Action#RIGHT_CLICK_AIR} or
 * {@link Action#LEFT_CLICK_AIR} into combo inputs fed to {@link ComboTracker}.
 * <p>
 * Block and entity interactions are intentionally ignored so that mining, combat,
 * container usage, and block placement do not interfere with combo sequences.
 * <p>
 * A {@link ComboInput#LEFT} is forwarded to the tracker only when the player already
 * has an in-progress sequence; standalone left-click-in-air is discarded.
 */
public class OnComboInputListener implements Listener {

    private final ComboTracker comboTracker;

    public OnComboInputListener(@NotNull ComboTracker comboTracker) {
        this.comboTracker = comboTracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.LEFT_CLICK_AIR) {
            return;
        }

        // Only process players who are tracked by McRPG
        if (McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(event.getPlayer().getUniqueId()).isEmpty()) {
            return;
        }

        ComboInput input = (action == Action.RIGHT_CLICK_AIR) ? ComboInput.RIGHT : ComboInput.LEFT;
        comboTracker.processInput(event.getPlayer(), input);
    }
}
