package us.eunoians.mcrpg.ability.combo;

import com.diamonddagger590.mccore.configuration.collection.ReloadableSet;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.combo.ComboConfigFile;
import us.eunoians.mcrpg.event.ability.combo.ComboCompleteEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Manages per-player combo input state for the combo ability activation system.
 * <p>
 * All public methods must be called from the main server thread (they are invoked
 * from Bukkit event handlers).
 * <p>
 * When a player inputs {@link ComboInput#RIGHT} while holding an allowed item (or bare hand),
 * a combo sequence begins (or continues). Each subsequent input is validated against the known
 * {@link ComboPattern}s. When a 3-click pattern is complete, a {@link ComboCompleteEvent}
 * is fired. If no valid continuation exists, the state is reset with feedback.
 * <p>
 * The allowed item list is loaded from {@code combo.allowed-items} in {@code combo_configuration.yml}
 * and reloads automatically when {@code /mcrpg admin reload} is run. An empty hand (AIR) is always
 * permitted regardless of the config list.
 */
public class ComboTracker {

    /** Timeout in ticks between combo inputs before the sequence resets. 700ms ≈ 14 ticks. */
    private static final long DEFAULT_TIMEOUT_TICKS = 14L;

    private final McRPG plugin;
    private final Map<UUID, PlayerComboState> playerStates = new HashMap<>();
    private final ReloadableSet<CustomItemWrapper> allowedItems;

    public ComboTracker(@NotNull McRPG plugin) {
        this.plugin = plugin;
        var comboConfig = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.COMBO_CONFIG);
        this.allowedItems = new ReloadableSet<>(
                comboConfig,
                ComboConfigFile.COMBO_ALLOWED_ITEMS,
                strings -> strings.stream().map(CustomItemWrapper::new).collect(Collectors.toSet())
        );
        plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.RELOADABLE_CONTENT)
                .trackReloadableContent(Set.of(allowedItems));
    }

    /**
     * Processes a single click input from a player.
     * <p>
     * Rules:
     * <ul>
     *   <li>A {@link ComboInput#LEFT} input is ignored if no combo is currently in progress.</li>
     *   <li>A {@link ComboInput#RIGHT} input starts or continues a combo.</li>
     *   <li>The held item must be in the allowed-item set.</li>
     *   <li>If 3 inputs complete a pattern, a {@link ComboCompleteEvent} is fired and state is reset.</li>
     *   <li>If no continuation is possible, state is reset (dead end).</li>
     * </ul>
     *
     * @param player The player who clicked.
     * @param input  The type of click.
     */
    public void processInput(@NotNull Player player, @NotNull ComboInput input) {
        if (!isAllowedHeldItem(player.getInventory().getItemInMainHand())) {
            return;
        }

        UUID uuid = player.getUniqueId();
        PlayerComboState state = playerStates.computeIfAbsent(uuid, id -> new PlayerComboState());

        // LEFT is ignored unless a combo has already started
        if (input == ComboInput.LEFT && state.isEmpty()) {
            return;
        }

        state.addInput(input);
        refreshTimeout(uuid, state);

        // Check for dead end: no pattern has this sequence as a valid prefix
        if (!state.hasAnyValidContinuation()) {
            resetState(uuid);
            return;
        }

        // Update the subtitle display with current progress
        ComboDisplayManager.updateDisplay(player, state.getCurrentSequence());

        // Check for a complete pattern match
        OptionalInt completedSlot = state.getCompletedSlot();
        if (completedSlot.isPresent()) {
            int slotIndex = completedSlot.getAsInt();
            resetState(uuid);
            ComboCompleteEvent event = new ComboCompleteEvent(player, slotIndex);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    /**
     * Resets the combo state for a player, cancelling any pending timeout and clearing the display.
     *
     * @param uuid The player UUID to reset.
     */
    public void resetState(@NotNull UUID uuid) {
        PlayerComboState state = playerStates.get(uuid);
        if (state == null) {
            return;
        }
        cancelTimeout(state);
        state.clearSequence();

        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            ComboDisplayManager.clearDisplay(player);
        }
    }

    /**
     * Checks whether the given item stack is permitted to initiate or continue a combo.
     * <p>
     * An empty hand ({@link Material#AIR}) is always allowed. All other items are checked
     * against the {@code combo.allowed-items} list in {@code combo_configuration.yml},
     * which reloads automatically on {@code /mcrpg admin reload}.
     *
     * @param item The item currently held in the main hand.
     * @return {@code true} if combo input is allowed with this item.
     */
    public boolean isAllowedHeldItem(@NotNull ItemStack item) {
        if (item.getType() == Material.AIR) {
            return true;
        }
        return allowedItems.getContent().contains(new CustomItemWrapper(item));
    }

    // --- Internal helpers ---

    private void refreshTimeout(@NotNull UUID uuid, @NotNull PlayerComboState state) {
        cancelTimeout(state);
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> resetState(uuid), DEFAULT_TIMEOUT_TICKS);
        state.setTimeoutTaskId(taskId);
    }

    private void cancelTimeout(@NotNull PlayerComboState state) {
        int taskId = state.getTimeoutTaskId();
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            state.setTimeoutTaskId(-1);
        }
    }
}
