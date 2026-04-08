package us.eunoians.mcrpg.listener.ability;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboInput;
import us.eunoians.mcrpg.ability.combo.ComboTracker;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Translates {@link PlayerInteractEvent}s (both air and block clicks) into combo
 * inputs fed to {@link ComboTracker}.
 * <p>
 * Both air and block interactions are accepted because players are almost always
 * looking at blocks in normal gameplay. The held-item filter in
 * {@link ComboTracker#isAllowedHeldItem} prevents combos while holding non-weapon items,
 * and the tracker itself discards standalone left-clicks that have no in-progress combo.
 * <p>
 * Uses {@code ignoreCancelled = false} because Bukkit marks {@code _AIR} interact
 * events as cancelled (no interacted block), which would silently drop air clicks.
 */
public class OnComboInputListener implements Listener {

    private final ComboTracker comboTracker;

    public OnComboInputListener(@NotNull ComboTracker comboTracker) {
        this.comboTracker = comboTracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Action action = event.getAction();
        boolean isRightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        boolean isLeftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        if (!isRightClick && !isLeftClick) {
            return;
        }

        // Only process players who are tracked by McRPG
        if (McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(event.getPlayer().getUniqueId()).isEmpty()) {
            return;
        }

        ComboInput input = isRightClick ? ComboInput.RIGHT : ComboInput.LEFT;
        comboTracker.processInput(event.getPlayer(), input);
    }
}
