package us.eunoians.mcrpg.listener.ability;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.ability.combo.ComboInput;
import us.eunoians.mcrpg.ability.combo.ComboTracker;
import us.eunoians.mcrpg.registry.McRPGRegistryKey;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

/**
 * Translates {@link PlayerInteractEvent}s (both air and block clicks) and melee
 * attacks ({@link EntityDamageByEntityEvent}) into combo inputs fed to {@link ComboTracker}.
 * <p>
 * Both air and block interactions are accepted because players are almost always
 * looking at blocks in normal gameplay. The held-item filter in
 * {@link ComboTracker#isAllowedHeldItem} prevents combos while holding non-weapon items,
 * and the tracker itself discards standalone left-clicks that have no in-progress combo.
 * <p>
 * Melee attacks are also treated as {@link ComboInput#LEFT} inputs because Bukkit does
 * not fire a {@link PlayerInteractEvent} when a player swings at an entity — only
 * {@link EntityDamageByEntityEvent} is raised. Without this handler, standing directly
 * on top of a target (where no block or air click registers) makes left-input combos
 * impossible to complete.
 * <p>
 * Both handlers use {@code ignoreCancelled = false}: the interact handler needs it because
 * Bukkit marks {@code _AIR} interact events as cancelled (no interacted block), and the
 * attack handler needs it so that cancelled damage (e.g. PvP disabled in a Land territory)
 * still counts as a swing input — a player should still be able to complete a combo by
 * swinging at a friendly entity even when no damage is dealt.
 */
public class OnComboInputListener implements Listener {

    private final ComboTracker comboTracker;

    public OnComboInputListener(@NotNull ComboTracker comboTracker) {
        this.comboTracker = comboTracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        boolean isMainHand = event.getHand() == EquipmentSlot.HAND;
        boolean isEmptyMainHandFallback = event.getHand() == EquipmentSlot.OFF_HAND
                && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR;
        if (!isMainHand && !isEmptyMainHandFallback) {
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

    /**
     * Treats a melee swing at an entity as a {@link ComboInput#LEFT} input.
     * <p>
     * Bukkit only fires {@link PlayerInteractEvent} when a player clicks on air or a block;
     * swinging at an entity never produces that event. This handler bridges the gap so that
     * players who attack a target standing directly on top of them can still complete
     * left-input combo patterns.
     * <p>
     * Cancelled damage events are intentionally included ({@code ignoreCancelled = false})
     * so that players can progress combos even when PvP is disabled (e.g. inside a Lands
     * territory with friendly fire off) — the swing gesture itself is what matters, not
     * whether damage was applied.
     *
     * @param event The entity damage event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onAttack(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        // Only process players who are tracked by McRPG
        if (McRPG.getInstance().registryAccess().registry(McRPGRegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER).getPlayer(player.getUniqueId()).isEmpty()) {
            return;
        }

        comboTracker.processInput(player, ComboInput.LEFT);
    }
}
