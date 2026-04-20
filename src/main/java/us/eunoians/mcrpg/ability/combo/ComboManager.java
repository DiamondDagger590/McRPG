package us.eunoians.mcrpg.ability.combo;

import com.diamonddagger590.mccore.configuration.collection.ReloadableSet;
import com.diamonddagger590.mccore.registry.RegistryKey;
import com.diamonddagger590.mccore.registry.manager.Manager;
import com.diamonddagger590.mccore.util.item.CustomItemWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.configuration.FileType;
import us.eunoians.mcrpg.configuration.file.combo.ComboConfigFile;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.display.hud.ActionBarHudDisplay;
import us.eunoians.mcrpg.display.hud.CenterContentPriority;
import us.eunoians.mcrpg.display.hud.content.IndefiniteCenterContent;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.combo.ComboCompleteEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Central manager for the combo ability activation system. Handles combo input
 * validation, timeout scheduling, display updates, and pattern completion.
 * <p>
 * Registered via {@link McRPGManagerKey#COMBO} and accessible through
 * {@code registryAccess().registry(RegistryKey.MANAGER).manager(McRPGManagerKey.COMBO)}.
 * <p>
 * Per-player combo state ({@link PlayerComboState}) lives on {@link McRPGPlayer#getComboState()}.
 * This manager orchestrates transitions but does not own the state itself.
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
public class ComboManager extends Manager<McRPG> {

    private static final long DEFAULT_TIMEOUT_TICKS = 14L;
    private static final String FILLED_CIRCLE = "\u2B24";
    private static final String EMPTY_CIRCLE = "\u25CB";

    private final ReloadableSet<CustomItemWrapper> allowedItems;

    /**
     * @param plugin The McRPG plugin instance.
     */
    public ComboManager(@NotNull McRPG plugin) {
        super(plugin);
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

        Optional<McRPGPlayer> mcRPGPlayerOpt = lookupPlayer(player.getUniqueId());
        if (mcRPGPlayerOpt.isEmpty()) {
            return;
        }
        McRPGPlayer mcRPGPlayer = mcRPGPlayerOpt.get();
        PlayerComboState state = mcRPGPlayer.getComboState();

        if (input == ComboInput.LEFT && state.isEmpty()) {
            return;
        }

        state.addInput(input);
        refreshTimeout(player.getUniqueId(), mcRPGPlayer, state);

        if (!state.hasAnyValidContinuation()) {
            resetState(mcRPGPlayer);
            return;
        }

        updateDisplay(mcRPGPlayer, state.getCurrentSequence());

        OptionalInt completedSlot = state.getCompletedSlot();
        if (completedSlot.isPresent()) {
            int slotIndex = completedSlot.getAsInt();
            resetState(mcRPGPlayer);
            ComboCompleteEvent event = new ComboCompleteEvent(player, slotIndex);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    /**
     * Resets the combo state for a player by UUID, cancelling any pending timeout and
     * clearing the action bar center zone content. No-op if the player is not online.
     *
     * @param uuid The player UUID to reset.
     */
    public void resetState(@NotNull UUID uuid) {
        lookupPlayer(uuid).ifPresent(this::resetState);
    }

    /**
     * Resets the combo state for a player, cancelling any pending timeout and clearing
     * the action bar center zone content.
     *
     * @param mcRPGPlayer The player to reset.
     */
    public void resetState(@NotNull McRPGPlayer mcRPGPlayer) {
        PlayerComboState state = mcRPGPlayer.getComboState();
        cancelTimeout(state);
        state.clearSequence();
        getHudDisplay(mcRPGPlayer).ifPresent(hud -> hud.clearSlot(CenterContentPriority.COMBO_STATE));
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

    /**
     * Builds the combo progress display and writes it as persistent center content
     * on the player. Persistent because this manager handles its own cleanup
     * on timeout or completion.
     *
     * @param mcRPGPlayer The player to update.
     * @param sequence    The inputs entered so far.
     */
    private void updateDisplay(@NotNull McRPGPlayer mcRPGPlayer, @NotNull List<ComboInput> sequence) {
        int totalLength = ComboPattern.SLOT_1.getLength();
        Component display = buildComboDisplay(sequence, totalLength);
        DisplayManager displayManager = plugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DISPLAY);
        ActionBarHudDisplay hud = displayManager.getOrCreateActionBarHud(mcRPGPlayer);
        hud.setSlot(CenterContentPriority.COMBO_STATE, new IndefiniteCenterContent(display));
    }

    /**
     * Looks up the player's {@link ActionBarHudDisplay} <em>without</em>
     * materialising one if none exists, used when clearing combo feedback so
     * we don't create an empty HUD display for offline / unknown players.
     *
     * @param mcRPGPlayer The player whose HUD display should be read.
     * @return An {@link Optional} containing the HUD display, or empty if the
     * player has no HUD display registered.
     */
    @NotNull
    private Optional<ActionBarHudDisplay> getHudDisplay(@NotNull McRPGPlayer mcRPGPlayer) {
        return plugin().registryAccess()
                .registry(RegistryKey.MANAGER).manager(McRPGManagerKey.DISPLAY)
                .getDisplay(mcRPGPlayer, ActionBarHudDisplay.class);
    }

    /**
     * Builds the visual combo progress component: filled circles for entered inputs
     * (gold for right, aqua for left) and empty circles for remaining slots.
     *
     * @param sequence    The inputs entered so far.
     * @param totalLength The total number of slots in a combo pattern.
     * @return The composed display component.
     */
    @NotNull
    private Component buildComboDisplay(@NotNull List<ComboInput> sequence, int totalLength) {
        Component result = Component.empty();
        for (int i = 0; i < totalLength; i++) {
            if (i > 0) {
                result = result.append(Component.text(" "));
            }
            if (i < sequence.size()) {
                ComboInput input = sequence.get(i);
                NamedTextColor color = (input == ComboInput.RIGHT) ? NamedTextColor.GOLD : NamedTextColor.AQUA;
                result = result.append(Component.text(FILLED_CIRCLE, color));
            } else {
                result = result.append(Component.text(EMPTY_CIRCLE, NamedTextColor.DARK_GRAY));
            }
        }
        return result;
    }

    /**
     * Looks up the {@link McRPGPlayer} for the given UUID via the player manager.
     *
     * @param uuid The player's UUID.
     * @return The player, or empty if not online or not tracked.
     */
    @NotNull
    private Optional<McRPGPlayer> lookupPlayer(@NotNull UUID uuid) {
        return plugin().registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.PLAYER)
                .getPlayer(uuid);
    }

    /**
     * Cancels any existing timeout and schedules a new delayed task that resets the
     * player's combo state after {@link #DEFAULT_TIMEOUT_TICKS}.
     *
     * @param uuid        The player's UUID (used for the timeout callback).
     * @param mcRPGPlayer The player whose state will be reset on timeout.
     * @param state       The combo state to store the new task ID on.
     */
    private void refreshTimeout(@NotNull UUID uuid, @NotNull McRPGPlayer mcRPGPlayer, @NotNull PlayerComboState state) {
        cancelTimeout(state);
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin(), () -> resetState(mcRPGPlayer), DEFAULT_TIMEOUT_TICKS);
        state.setTimeoutTaskId(taskId);
    }

    /**
     * Cancels the pending timeout task for the given combo state, if one exists.
     *
     * @param state The combo state whose timeout should be cancelled.
     */
    private void cancelTimeout(@NotNull PlayerComboState state) {
        int taskId = state.getTimeoutTaskId();
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            state.setTimeoutTaskId(-1);
        }
    }
}
