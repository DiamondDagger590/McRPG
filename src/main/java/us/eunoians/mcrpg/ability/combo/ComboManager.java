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
import us.eunoians.mcrpg.configuration.file.MainConfigFile;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.display.hud.ActionBarHudDisplay;
import us.eunoians.mcrpg.display.hud.CenterContentPriority;
import us.eunoians.mcrpg.display.hud.content.IndefiniteCenterContent;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.event.ability.combo.ComboCompleteEvent;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
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
 *
 * <h2>Allowed-item sources</h2>
 * The allowed-item check ({@link #isAllowedHeldItem}) merges three sources:
 * <ol>
 *   <li><b>Built-in config list</b> — loaded from {@code configuration.gameplay.combo.allowed-items}
 *       in {@code config.yml}; reloads automatically on {@code /mcrpg admin reload}.</li>
 *   <li><b>Contributed reloadable sets</b> — third-party plugins register their own
 *       {@link ReloadableSet} via {@link #registerAllowedItemSet}. The caller owns the
 *       set's lifecycle and reload registration.</li>
 *   <li><b>Static entries</b> — individual {@link CustomItemWrapper} values added via
 *       {@link #addAllowedItem}. Not config-backed; plugins re-register during
 *       {@code onEnable}.</li>
 * </ol>
 * An empty hand (AIR) is always permitted regardless of any list.
 *
 * <h2>Third-party extension example</h2>
 * <pre>{@code
 * // Option A: Config-backed reloadable set (auto-updates on reload)
 * ReloadableSet<CustomItemWrapper> myItems = new ReloadableSet<>(
 *         myConfig, myRoute, strings -> strings.stream()
 *                 .map(CustomItemWrapper::new).collect(Collectors.toSet()));
 * comboManager.registerAllowedItemSet(myItems);
 *
 * // Option B: Static entry (no config needed)
 * comboManager.addAllowedItem(new CustomItemWrapper("my_custom_sword"));
 * }</pre>
 */
public class ComboManager extends Manager<McRPG> {

    private static final long DEFAULT_TIMEOUT_TICKS = 14L;
    private static final String FILLED_CIRCLE = "\u2B24";
    private static final String EMPTY_CIRCLE = "\u25CB";

    private final ReloadableSet<CustomItemWrapper> allowedItems;
    /** Third-party reloadable sets registered via {@link #registerAllowedItemSet}. Eviction: {@link #unregisterAllowedItemSet}. */
    private final List<ReloadableSet<CustomItemWrapper>> contributedItemSets = new CopyOnWriteArrayList<>();
    /** Individual programmatic entries registered via {@link #addAllowedItem}. Eviction: {@link #removeAllowedItem}. */
    private final Set<CustomItemWrapper> staticAllowedItems = new CopyOnWriteArraySet<>();

    /**
     * @param plugin The McRPG plugin instance.
     */
    public ComboManager(@NotNull McRPG plugin) {
        super(plugin);
        var mainConfig = plugin.registryAccess()
                .registry(RegistryKey.MANAGER)
                .manager(McRPGManagerKey.FILE)
                .getFile(FileType.MAIN_CONFIG);
        this.allowedItems = new ReloadableSet<>(
                mainConfig,
                MainConfigFile.COMBO_ALLOWED_ITEMS,
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
     * Registers a third-party {@link ReloadableSet} of allowed items to be checked
     * alongside the built-in config list. The caller owns the set's lifecycle and is
     * responsible for registering it with the {@code ReloadableContentManager} if
     * automatic config-reload behaviour is desired.
     * <p>
     * Call {@link #unregisterAllowedItemSet} during your plugin's {@code onDisable}
     * to clean up.
     *
     * @param itemSet The reloadable set to contribute.
     */
    public void registerAllowedItemSet(@NotNull ReloadableSet<CustomItemWrapper> itemSet) {
        contributedItemSets.add(itemSet);
    }

    /**
     * Removes a previously registered third-party {@link ReloadableSet}.
     * No-op if the set was not registered.
     *
     * @param itemSet The reloadable set to remove.
     */
    public void unregisterAllowedItemSet(@NotNull ReloadableSet<CustomItemWrapper> itemSet) {
        contributedItemSets.remove(itemSet);
    }

    /**
     * Adds a single static {@link CustomItemWrapper} to the allowed-item list.
     * This entry is not config-backed and will not survive a server restart —
     * third-party plugins should re-register during {@code onEnable}.
     *
     * @param item The item wrapper to allow.
     */
    public void addAllowedItem(@NotNull CustomItemWrapper item) {
        staticAllowedItems.add(item);
    }

    /**
     * Removes a single static {@link CustomItemWrapper} from the allowed-item list.
     * No-op if the item was not previously added via {@link #addAllowedItem}.
     *
     * @param item The item wrapper to remove.
     */
    public void removeAllowedItem(@NotNull CustomItemWrapper item) {
        staticAllowedItems.remove(item);
    }

    /**
     * Returns a merged, unmodifiable snapshot of all allowed items across all three
     * sources: the built-in config set, contributed reloadable sets, and static entries.
     * Useful for debug output or admin commands.
     *
     * @return An unmodifiable set of all currently allowed {@link CustomItemWrapper}s.
     */
    @NotNull
    public Set<CustomItemWrapper> getAllowedItems() {
        Set<CustomItemWrapper> merged = new HashSet<>(allowedItems.getContent());
        merged.addAll(staticAllowedItems);
        for (ReloadableSet<CustomItemWrapper> contributed : contributedItemSets) {
            merged.addAll(contributed.getContent());
        }
        return Collections.unmodifiableSet(merged);
    }

    /**
     * Checks whether the given item stack is permitted to initiate or continue a combo.
     * <p>
     * An empty hand ({@link Material#AIR}) is always allowed. All other items are checked
     * against the built-in config list, any contributed {@link ReloadableSet}s registered
     * via {@link #registerAllowedItemSet}, and any static entries added via
     * {@link #addAllowedItem}.
     *
     * @param item The item currently held in the main hand.
     * @return {@code true} if combo input is allowed with this item.
     */
    public boolean isAllowedHeldItem(@NotNull ItemStack item) {
        if (item.getType() == Material.AIR) {
            return true;
        }
        CustomItemWrapper wrapper = new CustomItemWrapper(item);
        if (allowedItems.getContent().contains(wrapper)) {
            return true;
        }
        if (staticAllowedItems.contains(wrapper)) {
            return true;
        }
        for (ReloadableSet<CustomItemWrapper> contributed : contributedItemSets) {
            if (contributed.getContent().contains(wrapper)) {
                return true;
            }
        }
        return false;
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
